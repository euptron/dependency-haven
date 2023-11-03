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

/**
 * An exclusion of an artifact by group ID and artifact ID.
 *
 * @author EUP
 */
public class Exclusion {

  private String groupId;
  private String artifactId;
  // TODO: support WILDCARD
  private static final String WILDCARD = "*";

  /**
   * Creates an empty Exclusion object.
   *
   * <p>This constructor initializes an empty Exclusion object. It is typically used when you want
   * to create an instance and set the properties later using setter methods.
   */
  public Exclusion() {}

  /**
   * Creates a new {@code Exclusion} from {@code groupId} and {@code artifactId}
   *
   * @param groupId The group identifier, may be {@code null}.
   * @param artifactId The artifact identifier, may be {@code null}.
   */
  public Exclusion(String groupId, String artifactId) {
    this.groupId = (groupId != null) ? groupId : "";
    this.artifactId = (artifactId != null) ? artifactId : "";
  }

  /**
   * Gets the group identifier to exclude.
   *
   * @return The group identifier.
   */
  public String getGroupId() {
    return this.groupId;
  }

  /**
   * Sets the group identifier to exclude.
   *
   * @return The group identifier.
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * Gets the artifact identifier to exclude.
   *
   * @return The artifact identifier.
   */
  public String getArtifactId() {
    return this.artifactId;
  }

  /**
   * Sets the artifact identifier to exclude.
   *
   * @return The artifact identifier.
   */
  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public boolean matches(Dependency dependency) {
    String gID = getGroupId();
    String aID = getArtifactId();
    // dependency G/A to match
    String dAID = dependency.getCoordinates().getArtifactId();
    String dGID = dependency.getCoordinates().getGroupId();
    if ((gID != null || aID != null) || (dGID != null || dAID != null)) {
      return (gID == null || gID.equals(dGID)) && (aID == null || aID.equals(dAID));
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format("%s:%s", getGroupId(), getArtifactId());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if ((obj == null) || (getClass() != obj.getClass())) {
      return false;
    }
    Exclusion other = (Exclusion) obj;
    return Objects.equals(groupId, other.groupId) && Objects.equals(artifactId, other.artifactId);
  }

  @Override
  public int hashCode() {
    int result = 18;
    result = 31 * result + (groupId != null ? groupId.hashCode() : 0);
    result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
    return result;
  }
}
