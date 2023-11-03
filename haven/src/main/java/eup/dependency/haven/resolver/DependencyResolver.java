/*
 *  MIT License
 *  Copyright (c) 2023 EUP
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package eup.dependency.haven.resolver;

import eup.dependency.haven.async.AsyncTaskExecutor;
import eup.dependency.haven.callback.DependencyResolutionCallback;
import eup.dependency.haven.model.Coordinates;
import eup.dependency.haven.model.Dependency;
import eup.dependency.haven.model.Pom;
import eup.dependency.haven.parser.PomParser;
import eup.dependency.haven.repository.LocalRepository;
import eup.dependency.haven.repository.RemoteRepository;
import eup.dependency.haven.repository.Repository;
import eup.dependency.haven.repository.StorageFactory;
import eup.dependency.haven.resolver.internal.DependencyResolutionSkipper;
import eup.dependency.haven.versioning.ComparableVersion;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.xml.sax.SAXException;

/**
 * A {@code DependencyResolver} resolves a {@link Dependency} coordinates {@link Coordinates} from a
 * {@link ArtifactRepository} to provide the direct dependencies including all it's transitive
 * depdendencies for each {@link Coordinates} of the dependency of the dependencies declared in
 * their respective {@link Pom}
 *
 * @author EUP
 */
@SuppressWarnings("unused")
public class DependencyResolver implements Repository {

  private Coordinates coordinates;

  // data structure for bfs transversal
  private Queue<Dependency> queue = new LinkedList<>();
  private Set<Dependency> seen = new HashSet<>();
  // keep track of all resolved dependencies
  private List<Dependency> resolvedDependencies = new LinkedList<>();
  // keep track of all unresolved dependencies
  private List<Dependency> unresolvedDependencies = new ArrayList<>();
  private DependencyResolutionCallback callback;
  // storage factory for caching resolved artifacts
  private StorageFactory storageFactory;
  private DependencyResolutionSkipper skipper;
  public final List<RemoteRepository> repositories;
  private boolean skipInnerDependencies = false;
  // TODO: REWORK THIS CLASS TO USE POM INSTEAD OF DEPENDEBCY WHILE ITERATING
  // SO THAT I CLOUD PRIORITIZE HIGHER VERSIONS OF POMS USING MAP WITH OREFERENCE TO HIGHER POM
  // VERSIONS

  /**
   * Creates a DependencyResolver
   *
   * @param coordinates The dependency coordinates to resolve for
   */
  public DependencyResolver(Coordinates coordinates) {
    this(null, coordinates);
  }

  /**
   * Creates a DependencyResolver with a factory
   *
   * @param storageFactory the factory for managing cached dependencies and POMs
   * @param coordinates The dependency coordinates to resolve for
   */
  public DependencyResolver(StorageFactory storageFactory, Coordinates coordinates) {
    this.storageFactory = storageFactory;
    this.coordinates = coordinates;
    this.repositories = new ArrayList<>();
  }

  /**
   * Initlize resoution for the given dependency by reading its POM file
   *
   * @param callback the dependency to resolution listener
   */
  public void resolve(DependencyResolutionCallback callback) {
    this.callback = callback;
    this.skipper = new DependencyResolutionSkipper(callback);
    if (callback == null) {
      throw new IllegalArgumentException("Dependency Resolution Callback must be set.");
    }
    if (coordinates == null || coordinates.toString().isEmpty()) {
      callback.warning(
          "Enter a dependency declaration!"
              + "\n"
              + "Haven supports any of the following declaration formats"
              + "\n"
              + " Gradle Groovy implementation"
              + "\n"
              + " Gradle Kotlin implementation"
              + "\n"
              + " GroupID:ArtifactID:Version");
      return;
    }
    try {
      // Record the start time
      long startTime = System.currentTimeMillis();
      callback.info("Starting Resolution for " + coordinates);
      AsyncTaskExecutor.loadTaskAsync(
          () -> {
            //  run on a background thread
            List<Dependency> dependencies = resolveDependencies(new Dependency(coordinates));
            return dependencies;
          },
          dependencies -> {
            // runs on the main thread
            long endTime = System.currentTimeMillis();
            if (dependencies.isEmpty()) {
              callback.warning("No dependencies found for " + coordinates);
            } else {
              callback.onDependenciesResolved(
                  "Successfully resolved " + coordinates, dependencies, (endTime - startTime));
            }
          });
    } catch (Exception e) {
      callback.error("Failed to resolve " + coordinates + " " + e.getMessage());
    }
  }

  /**
   * Resolves a dependency and adds its direct and transitive to list
   *
   * @param coordinates the dependency to resolve for
   */
  private List<Dependency> resolveDependencies(Dependency dependency) {
    if (skipInnerDependencies) {
      List<Dependency> directDependencies = new ArrayList<>();
      InputStream is = null;
      Dependency directDependency = dependency;
      try {
        is = searchRepositories(dependency);
        Pom parsedPom = new Pom();
        // parse the pom
        parsedPom = resolvePom(is);
        // retrieve the packaging of the direct dependency
        directDependency.setType(parsedPom.getCoordinates().getPackaging());
        if (is == null) {
          callback.error(
              "Failed to resolve " + dependency + ",Cause: search repositories was null");
          return Collections.emptyList();
        }
      } catch (IOException e) {
        callback.error("Failed to retrieve info for " + dependency + " " + e.getMessage());
      } finally {
        try {
          is.close();
        } catch (IOException e) {
          callback.error("Failed to close search " + e.getMessage());
        }
      }
      directDependencies.add(directDependency);
      return directDependencies;
    } else {
      resolve(new Pom(dependency));
      return resolvedDependencies;
    }
  }

  /**
   * Sets the dependency type from it's packaging
   *
   * <p>If the type is not declared in a POM the packaging is also aliased as type
   *
   * @param dependency the dependency with an ambiguous type
   */
  private String fallbackType(Dependency dependency) {
    String defaultType = "jar";

    try {
      InputStream is = searchRepositories(dependency);
      if (is == null) {
        return defaultType;
      }
      Pom parsedPom = new Pom();
      // parse the pom
      parsedPom = resolvePom(is);
      is.close();
      return parsedPom.getCoordinates().getPackaging();
    } catch (Exception e) {
      callback.error("Failed to get extension for " + dependency + " with ambiguous type " + e);
      // ignore and default type to jar
      return defaultType;
    }
  }

  /**
   * Resolves a dependency from POM
   *
   * @param pom the POM of a dependency
   */
  private void resolve(Pom pom) {
    Dependency parent = pom.getDependency();

    InputStream inputStream = searchRepositories(parent);
    if (inputStream == null) {
      return;
    }
    Pom parsePom = new Pom();
    try {
      // parse the pom
      parsePom = resolvePom(inputStream);

      parsePom
          .getDependencies()
          .forEach(
              directDependency -> {
                // Check if type is not explicitly declared
                if (directDependency.getType() == null || directDependency.getType().isEmpty()) {
                  directDependency.setType(fallbackType(directDependency));
                }
                // add each dependency to the search
                queue.add(directDependency);
              });

    } catch (Exception e) {
      callback.error("Failed to resolve " + parent + " " + e.getMessage());
    }

    while (!queue.isEmpty()) {
      // pull a dependency to resolve
      Dependency currentDependency = queue.poll();

      Pom currPom = new Pom();
      // add just pulled dependency as coordinates to pom
      currPom.setCoordinates(currentDependency.getCoordinates());
      currPom.setExclusions(parsePom.getExclusions());

      if (skipper.skipResolution(seen, currentDependency, currPom)) {
        unresolvedDependencies.add(currentDependency);
        continue;
      }

      seen.forEach(
          visitedDependency -> {
            if (skipper.hasVersionConflicts(visitedDependency, currentDependency)) {
              String msg =
                  "Version conflict detected for "
                      + visitedDependency.toString()
                      + " against "
                      + currentDependency.toString()
                      + " conflicting version would be resolved as configured";
              callback.warning(msg);
              // TODO: Handle version conflicts
            }
          });

      if (!seen.contains(parent)) {
        callback.info("Resolving inner dependency: " + currentDependency);
      }

      if (!seen.contains(currentDependency)) {
        seen.add(currentDependency);
        // add the unseen dependency to pom
        currPom.addDependency(currentDependency);
        // also add unseen dependency to pom
        currPom.setCoordinates(currentDependency.getCoordinates());
        resolvedDependencies.add(currentDependency);
        callback.info("Successfully resolved " + currentDependency);
      }

      try {
        // Add the adjacent dependencies to the BFS queue.
        for (Dependency adjacent : currPom.getDependencies()) {
          if (!seen.contains(adjacent)) {
            queue.add(adjacent);
          }
          // TODO: Only resolve transitive dependencies of POMS with higher version
          // if that exist..data struct Map
          // resolve the transitive dependencies for each adjacent dependency coordinate
          resolveTransitiveDependencies(adjacent, resolvedDependencies);
        }
      } catch (Exception e) {
        callback.warning("Failed to resolve " + currentDependency + " " + e.getMessage());
      }
    }
    try {
      inputStream.close();
    } catch (IOException e) {
      callback.error("Failed to close input stream " + inputStream + " " + e.getMessage());
    }
  }

  public void skipInnerDependencies(boolean enabled) {
    this.skipInnerDependencies = enabled;
  }

  /**
   * Applies DFS (Depth First Search) to recrusively transverse a tree in order to all trace
   * transitive dependencies
   *
   * @param coordinates dependency coordinate to resolve for
   * @param resolvedDependencies the direct dependencies
   */
  private void resolveTransitiveDependencies(
      Dependency indirectDependency, List<Dependency> resolvedDependencies)
      throws IOException, SAXException {
    if (resolvedDependencies.isEmpty()) {
      return;
    }
    for (Dependency transitiveDependency : resolveDependencies(indirectDependency)) {
      // prevent unnecessary recursion
      if (!resolvedDependencies.contains(transitiveDependency)) {
        resolveTransitiveDependencies(transitiveDependency, resolvedDependencies);
      }
    }
  }

  @Override
  public Pom getParentPom(Coordinates coordinates) {
    InputStream is = null;
    try {
      is = searchRepositories(new Dependency(coordinates));
      Pom parsedPom = new Pom();
      callback.info("Parsing parent POM " + coordinates);
      parsedPom = resolvePom(is);
      return parsedPom;
    } catch (IOException e) {
      callback.error("Failed to parse parent POM for " + coordinates + " " + e.getMessage());
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        callback.error("Failed to close search for POM" + e.getMessage());
      }
    }
    return null;
  }

  /**
   * Resolves a POM to get declared pom information
   *
   * @param is the POM input stream
   * @throws IOException in case of I/O error
   * @throws SAXException in case of sax error
   */
  private Pom resolvePom(InputStream is) throws IOException {
    PomParser parser = new PomParser(this);
    return parser.parse(is);
  }

  /**
   * Prioritises the higher version of a dependency coordinate
   *
   * @param firstVersion the first version to compare
   * @param secondVersion the second version to compare
   */
  private int getHigherVersion(String firstVersion, String secondVersion) {
    ComparableVersion firstComparableVersion = new ComparableVersion(firstVersion);
    ComparableVersion secondComparableVersion = new ComparableVersion(secondVersion);
    return firstComparableVersion.compareTo(secondComparableVersion);
  }

  /**
   * Searches for a dependency in either a {@link LocalRepository} or {@link RemoteRepository}
   *
   * @param dependency the dependency to search for
   */
  public InputStream searchRepositories(Dependency dependency) {
    String pomPath = getPomDownloadURL(dependency);

    if (storageFactory != null) {
      // Try to fetch from local repositories first
      List<LocalRepository> repositories =
          LocalRepository.getRepositories(storageFactory.getCacheDirectory());
      for (LocalRepository localRepository : repositories) {
        File localFile = new File(localRepository.getUrl() + "/" + pomPath);
        if (localFile.exists()) {
          try {
            return new FileInputStream(localFile);
          } catch (IOException e) {
            callback.warning(
                "Error fetching artifact "
                    + dependency
                    + " from local repository: "
                    + e.getMessage());
          }
        }
      }
    }

    // If not found in local repositories, try to fetch dependencies from remote repositories
    // concurrently
    int availableCores = Runtime.getRuntime().availableProcessors();

    ExecutorService executorService = Executors.newFixedThreadPool(availableCores);
    // hold Future objects
    List<Future<Map<RemoteRepository, URL>>> futures = new ArrayList<>();

    for (RemoteRepository remoteRepository : repositories) {

      Callable<Map<RemoteRepository, URL>> fetchTask =
          () -> {
            URL downloadUrl = new URL(remoteRepository.getUrl() + pomPath);
            Map<RemoteRepository, URL> i = new HashMap<>();
            i.put(remoteRepository, downloadUrl);
            return i;
          };
      // Submit the fetch task to the executor
      Future<Map<RemoteRepository, URL>> future = executorService.submit(fetchTask);
      futures.add(future);
    }

    // Wait for the first successful result and return it
    for (Future<Map<RemoteRepository, URL>> future : futures) {
      try {
        Map<RemoteRepository, URL> map = future.get();
        for (Entry<RemoteRepository, URL> entry : map.entrySet()) {
          InputStream is = entry.getValue().openStream();
          RemoteRepository remoteRepository = entry.getKey();
          if (is != null) {
            if (storageFactory != null) {
              // download the pom of the respective dependency
              storageFactory.downloadPom(dependency, remoteRepository, pomPath);
            }
            executorService.shutdownNow(); // Terminate other tasks
            return is;
          }
        }
      } catch (InterruptedException | ExecutionException | IOException e) {
        callback.warning(
            "Error fetching artifact"
                + dependency.toString()
                + " from remote repository: "
                + e.getMessage());
      }
    }
    return null; // no result found
  }

  /**
   * Gets a library download url for a dependency
   *
   * <p>The url can also be used with local repositories that follow same remote declaration path
   *
   * @param dependency the dependency to provide library url for
   */
  public static String getLibraryDownloadURL(Dependency dependency) {
    if (dependency == null) {
      return "";
    }
    return getPathFromDeclaration(dependency)
        + ("aar".equalsIgnoreCase(dependency.getType())
            ? ".aar"
            : ".jar" /*+ dependency.getType()*/); // was dependency.getType() but bundle e.t.c could
                                                  // also mean .jar
  }

  /**
   * Gets a POM download url for a dependency
   *
   * <p>The url can also be used with local repositories that follow same remote declaration path
   *
   * @param dependency the dependency to provide POM url for
   */
  public static String getPomDownloadURL(Dependency dependency) {
    if (dependency == null) {
      return "";
    }
    return getPathFromDeclaration(dependency) + ".pom";
  }

  /**
   * Gets a declaration path for a dependency from it's coordinates
   *
   * <p>For example: {@code io.eup:test:1.5} we get a path io/eup/test/1.5/test-1.5
   */
  public static String getPathFromDeclaration(Dependency dependency) {
    if (dependency == null) {
      return "";
    }
    return dependency.getCoordinates().getGroupId().replace(".", "/")
        + "/"
        + dependency.getCoordinates().getArtifactId()
        + "/"
        + dependency.getCoordinates().getVersion()
        + "/"
        + dependency.getCoordinates().getArtifactId()
        + "-"
        + dependency.getCoordinates().getVersion();
  }

  @Override
  public void addRepository(RemoteRepository repository) {
    repositories.add(repository);
  }

  @Override
  public void addRepository(String name, String url) {
    addRepository(new RemoteRepository(name, url));
  }
}
