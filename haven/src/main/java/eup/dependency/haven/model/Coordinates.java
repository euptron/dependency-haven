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

package eup.dependency.haven.model;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A coordinate, which is an address in canonical form {@code groupId:artifactId:version} for a
 * Maven artifact.
 *
 * @author EUP
 */
public class Coordinates {

  private String groupId;
  private String artifactId;
  private String version;
  // A coordinate may have packaging so we add it, if packaging is null it defaults to jar
  private String packaging = "jar";
  // Pattern to retrieve the declaration of a coordinate
  private static final Pattern GRADLE_GROOVY_IMPLEMENTATION =
      Pattern.compile("(implementation) (['\"])(.*)(['\"])");
  private static final Pattern GRADLE_KOTLIN_IMPLEMENTATION =
      Pattern.compile("(implementation\\()(['\"])(.*)(['\"]\\))");
  // only works when online we check for the availability of coordinate versions
  private boolean versionConsistency = true;

  /**
   * Retrieve the declaration of a Coordinate as kotlin, groovy, or (groupId:artifactId:version).
   *
   * @param declaration The declaration of a coordinate
   * @return A Coordinates object representing the parsed Maven coordinate
   */
  public static Coordinates valueOf(String declaration) {
    // Check for null or empty declaration
    if (declaration == null || declaration.isEmpty()) {
      throw new IllegalArgumentException("Declaration cannot be empty");
    }

    // Attempt to parse as Groovy or Kotlin implementation
    Matcher groovyMatcher = GRADLE_GROOVY_IMPLEMENTATION.matcher(declaration);
    Matcher kotlinMatcher = GRADLE_KOTLIN_IMPLEMENTATION.matcher(declaration);
    String dependencyValue = null;

    if (groovyMatcher.matches()) {
      dependencyValue = groovyMatcher.group(3).trim();
    } else if (kotlinMatcher.matches()) {
      dependencyValue = kotlinMatcher.group(3).trim();
    }

    if (dependencyValue != null) {
      String[] dependenceyPieces = dependencyValue.split(":");
      if (dependenceyPieces.length >= 2) {
        String groupId = dependenceyPieces[0];
        String artifactId = dependenceyPieces[1];
        String version = dependenceyPieces[2];
        // parse a new Coordinate object
        return new Coordinates(groupId, artifactId, version);
      }
    }

    // Attempt to split the declaration into (groupId, artifactId, version)
    String[] names = declaration.split(":");
    if (names.length >= 3) {
      return new Coordinates(names[0], names[1], names[2]);
    }

    throw new IllegalArgumentException(
        "Failed to convert coordinates string to Coordinates: " + declaration);
  }

  /**
   * Creates an empty Coordinates object.
   *
   * <p>This constructor initializes an empty Coordinates object. It is typically used when you want
   * to create an instance and set the properties later using setter methods.
   */
  public Coordinates() {}

  /**
   * Constructs a Coordinates object with the specified groupId, artifactId, and version.
   *
   * @param groupId The Maven group ID
   * @param artifactId The Maven artifact ID
   * @param version The Maven version
   */
  public Coordinates(String groupId, String artifactId, String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
  }

  /**
   * Constructs a Coordinates object with the specified groupId, artifactId, version, and packaging.
   *
   * @param groupId The Maven group ID
   * @param artifactId The Maven artifact ID
   * @param version The Maven version
   * @param packaging The packaging type (defaults to "jar" if null)
   */
  public Coordinates(String groupId, String artifactId, String version, String packaging) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.packaging = packaging;
  }

  /**
   * Gets the group ID of the Maven coordinate.
   *
   * @return The group ID
   */
  public String getGroupId() {
    return this.groupId;
  }

  /**
   * Sets the group ID of the Maven coordinate.
   *
   * @param groupId The group ID to set
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * Gets the artifact ID of the Maven coordinate.
   *
   * @return The artifact ID
   */
  public String getArtifactId() {
    return this.artifactId;
  }

  /**
   * Sets the artifact ID of the Maven coordinate.
   *
   * @param artifactId The artifact ID to set
   */
  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  /**
   * Gets the version of the Maven coordinate, considering version consistency rules.
   *
   * @return The version
   */
  public String getVersion() {
    if (this.version != null) {
      String fromattedVersion =
          version.replace("[", "").replace("]", "").replace("(", "").replace(")", "");
      if (fromattedVersion.contains(",")) {
        String[] versions = fromattedVersion.split(",");
        for (String version : versions) {
          // return the first version for now.
          if (!version.isEmpty()) {
            return version;
          }
        }
      }
      return fromattedVersion;
    } else {
      return this.version;
    }
  }

  /**
   * Sets the version of the Maven coordinate.
   *
   * @param version The version to set
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Gets the packaging type of the Maven coordinate.
   *
   * @return The packaging type (defaults to "jar" if null)
   */
  public String getPackaging() {
    return (packaging == null) ? "jar" : this.packaging;
  }

  /**
   * Sets the packaging type of the Maven coordinate.
   *
   * @param packaging The packaging type to set
   */
  public void setPackaging(String packaging) {
    this.packaging = packaging;
  }

  /**
   * Gets the version consistency state.
   *
   * @return true if version consistency is enabled, false otherwise
   */
  public boolean getVersionConsistencyState() {
    return this.versionConsistency;
  }

  /**
   * Enables or disables version consistency for the Maven coordinate.
   *
   * @param enabled true to enable version consistency, false to disable
   */
  public void enableVersionConsistency(boolean enabled) {
    this.versionConsistency = enabled;
  }

  /**
   * Compare between this coordinate and another to check if they have same {@code groupId} and
   * {@code artifactId} with different versions.
   *
   * @param other the coordinate to compare
   * @return true when version conflicts
   */
  public boolean versionConflicts(Coordinates other) {
    if (other == null) {
      return false;
    }
    return Objects.equals(groupId, other.getGroupId())
        && Objects.equals(artifactId, other.getArtifactId())
        && !Objects.equals(version, other.getVersion());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Coordinates other = (Coordinates) obj;
    return Objects.equals(groupId, other.groupId)
        && Objects.equals(artifactId, other.artifactId)
        && Objects.equals(version, other.version)
        && Objects.equals(packaging, other.packaging);
  }

  @Override
  public int hashCode() {
    int result = 18;
    result = 31 * result + (groupId != null ? groupId.hashCode() : 0);
    result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
    result = 31 * result + (version != null ? version.hashCode() : 0);
    result = 31 * result + (packaging != null ? packaging.hashCode() : 0);
    return result;
  }

  public String getGroupAndArtifactId() {
    return String.format("%s:%s", getGroupId(), getArtifactId());
  }

  @Override
  public String toString() {
    return String.format("%s:%s:%s", getGroupId(), getArtifactId(), getVersion());
  }
}
