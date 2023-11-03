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

package eup.dependency.haven.resolver.internal;

import eup.dependency.haven.callback.DependencyResolutionCallback;
import eup.dependency.haven.model.Dependency;
import eup.dependency.haven.model.Exclusion;
import eup.dependency.haven.model.Pom;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A skipper that determines whether to skip resolving a dependency
 *
 * @author EUP
 */
public final class DependencyResolutionSkipper {

  private boolean skipOptional = false;
  private DependencyResolutionCallback resolutionCallback;
  private static final Set<String> IGNORED_SCOPES =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList("test", "provided")));
  private static final String WILD_CARD = "*";

  public DependencyResolutionSkipper(DependencyResolutionCallback resolutionCallback) {
    this.resolutionCallback = resolutionCallback;
  }

  /**
   * Check whether the resolution of current dependency can be skipped before resolving.
   *
   * @param visited all visited dependencies when transversing the tree
   * @param dependency the current dependency
   * @param pom the POM containing exclusion(s)
   * @return {@code true} if the node can be skipped for resolution, {@code false} if resolution
   *     required.
   */
  public boolean skipResolution(Set<Dependency> visited, Dependency dependency, Pom pom) {

    if (dependency.getScope() != null & IGNORED_SCOPES.contains(dependency.getScope())) {
      resolutionCallback.info(
          "Skipped resolving dependency "
              + dependency.toString()
              + " with scope "
              + dependency.getScope());
      return true;
    }
    if (isExcluded(dependency, pom.getExclusions())) {
      resolutionCallback.info("Skipped resolving excluded dependency " + dependency.toString());
      return true;
    }

    if (dependency.isOptional()) {
      resolutionCallback.info("Skipped resolving optional dependency " + dependency.toString());
      return true;
    }

    if (visited.stream().anyMatch(resolvedDependency -> resolvedDependency.matches(dependency))) {
      resolutionCallback.info(
          "Skipped resolving already resolved dependency " + dependency.toString());
      return true;
    }

    if (skipMalformed(dependency)) {
      resolutionCallback.info("Skipped resolving malformed dependency " + dependency.toString());
      return true;
    }

    return false;
  }

  public boolean hasVersionConflicts(Dependency visited, Dependency justSeen) {
    return visited.getCoordinates().versionConflicts(justSeen.getCoordinates());
  }

  private boolean skipMalformed(Dependency dp) {
    return dp.getCoordinates().getArtifactId() == null
        || dp.getCoordinates().getGroupId() == null
        || dp.getCoordinates().getVersion() == null;
  }

  private boolean isExcluded(Dependency dependency, List<Exclusion> exclusions) {
    boolean exclusion =
        exclusions.stream()
            .filter(Objects::nonNull)
            .anyMatch(
                ex -> {
                  if (ex == null) {
                    return false;
                  }
                  if (ex.getGroupId() == null) {
                    return false;
                  }
                  if (!ex.getGroupId().equals(dependency.getCoordinates().getGroupId())) {
                    return false;
                  }

                  if (ex.getArtifactId() == null) {
                    return false;
                  }

                  if (!ex.getArtifactId().equals(dependency.getCoordinates().getArtifactId())) {
                    return false;
                  }
                  return false;
                });
    return exclusion;
  }
}
