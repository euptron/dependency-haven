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

package eup.dependency.haven.repository;

import eup.dependency.haven.api.CachedLibrary;
import eup.dependency.haven.callback.DownloadCallback;
import eup.dependency.haven.model.Dependency;
import eup.dependency.haven.model.Pom;
import eup.dependency.haven.resolver.DependencyResolver;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * Stores and retrieves the POM and library of a dependency
 *
 * @author EUP
 */
public class LocalStorageFactory implements StorageFactory {

  private DependencyResolver resolver;
  private File cacheDirectory;
  private DownloadCallback downloadCallback;

  @Override
  public File downloadPom(
      Dependency dependency, RemoteRepository remoteRepository, String relativePath) {
    try {
      downloadCallback.info("Fetching POM for " + dependency + " in " + remoteRepository.getName());
      File file = getFile(remoteRepository, relativePath);
      if (file != null && file.exists()) {
        downloadCallback.info(
            "Pom for " + dependency + " found in remote repository " + remoteRepository.getName());
        return file;
      }
    } catch (Exception e) {
      downloadCallback.warning(
          "An error occured! Pom for "
              + dependency
              + " was not found in remote repository "
              + remoteRepository.getName()
              + " ERROR:"
              + e);
    }
    return null;
  }

  @Override
  public File getLibrary(Pom pom) {
    String fileName = pom.getDependency().toString();
    String relativePath = DependencyResolver.getLibraryDownloadURL(pom.getDependency());
    // check if file library is cached
    for (LocalRepository repository : LocalRepository.getRepositories(getCacheDirectory())) {
      try {
        File cachedFile = getCachedFile(repository, relativePath);
        if (cachedFile != null && cachedFile.exists()) {
          downloadCallback.info("Library for " + fileName + " found in cache");
          return cachedFile;
        }
      } catch (Exception e) {
        downloadCallback.warning(
            "Cannot find "
                + fileName
                + " in local repository "
                + repository.getName()
                + ", starting search in remote repository");
      }
    }
    // cannot find file library in local repositories , try retrieving from a remote repositories
    for (RemoteRepository repository : resolver.repositories) {
      try {
        File file = getFile(repository, relativePath);
        if (file != null && file.exists()) {
          downloadCallback.info(
              "Library for " + fileName + " found in remote repository " + repository.getName());
          return file;
        }
      } catch (Exception e) {
        downloadCallback.warning(
            "An error occured! Library for "
                + fileName
                + " was not found in remote repository "
                + repository.getName()
                + " ERROR:"
                + e);
      }
    }
    return null;
  }

  private File getCachedFile(ArtifactRepository repository, String relativePath)
      throws IOException {
    File rootDirectory = new File(cacheDirectory, repository.getName());
    if (!rootDirectory.exists()) {
      FileUtils.forceMkdirParent(rootDirectory);
    }

    File file = new File(rootDirectory, relativePath);
    // the file is not found on the disk, return null
    if (!file.exists()) {
      return null;
    }
    return file;
  }

  private File getFile(ArtifactRepository repository, String relativePath) throws IOException {
    File file = getCachedFile(repository, relativePath);
    if (file != null && file.exists()) {
      return file;
    }

    return downloadFile(repository, relativePath);
  }

  /**
   * Downloads a file and saves it
   *
   * @param repository the remote repository the file exist in
   * @param relativePath the relative path to the file we want to download
   */
  private File downloadFile(ArtifactRepository repository, String relativePath) {
    String downloadUrl = repository.getUrl() + relativePath;
    try {
      URL url = new URL(downloadUrl);
      downloadCallback.info("Fetching " + relativePath + " from " + repository.getName());
      InputStream inputStream = url.openStream();
      if (inputStream != null) {
        downloadCallback.info(relativePath + " downloaded");
        // save the file to cache, and return it
        return save(repository, relativePath, inputStream);
      }
    } catch (IOException e) {
      downloadCallback.error(relativePath + " was not found at " + repository.getName());
    }
    return null;
  }

  public File save(ArtifactRepository repository, String path, InputStream inputStream)
      throws IOException {
    File rootDirectory = new File(cacheDirectory, repository.getName());

    if (!rootDirectory.exists()) {
      FileUtils.forceMkdir(rootDirectory);
    }

    File file = new File(rootDirectory, path);
    FileUtils.forceMkdirParent(file);

    if (!file.exists() && !file.createNewFile()) {
      throw new IOException("Failed to create file.");
    }

    FileUtils.copyInputStreamToFile(inputStream, file);
    return file;
  }

  @Override
  public void setCacheDirectory(File directory) {
    if (directory.isFile()) {
      throw new IllegalArgumentException("Cache directory must be a folder");
    }
    if (!directory.canRead() && !directory.canWrite()) {
      throw new IllegalArgumentException("Cache directory must be accessible");
    }
    this.cacheDirectory = directory;
  }

  @Override
  public File getCacheDirectory() {
    return this.cacheDirectory;
  }

  @Override
  public void attach(DependencyResolver resolver) {
    if (resolver == null) {
      throw new IllegalArgumentException("DependencyResolver has not been attached");
    }
    this.resolver = resolver;
  }

  @Override
  public void downloadLibraries(Pom pom) {
    List<Dependency> resolvedDependencies = pom.getDependencies();
    if (resolvedDependencies == null || resolvedDependencies.isEmpty()) {
      return;
    }
    List<CachedLibrary> cachedLibraryList = new ArrayList<>();
    for (Dependency dependency : resolvedDependencies) {
      File library = getLibrary(new Pom(dependency));
      // track all cached libraries library
      if (library != null) {
        CachedLibrary cachedLibrary = new CachedLibrary();
        cachedLibrary.setSourcePath(library.getAbsolutePath());
        // add the dependency coordinates to pom
        Pom cachedPom = new Pom(dependency.getCoordinates());
        cachedLibrary.setLibraryPom(cachedPom);
        cachedLibraryList.add(cachedLibrary);
      }
    }
    downloadCallback.done(cachedLibraryList);
  }

  @Override
  public void setDownloadCallback(DownloadCallback callback) {
    if (callback == null) {
      throw new IllegalArgumentException("DownloadCallback has not been set");
    }
    this.downloadCallback = callback;
  }
}
