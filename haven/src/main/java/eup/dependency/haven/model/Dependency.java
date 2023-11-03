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

import java.util.ArrayList;
import java.util.List;

/**
 * A dependency in a Maven {@link Pom}.
 *
 * <p>If the type of a dependency is ambiguous Haven would use its packaging instead {@see
 * DependencyResolver}
 *
 * @author EUP
 */
public class Dependency {

  private Coordinates coordinates;
  private String type;
  // ambiguous scope defaults to compile
  private String scope = "compile";
  private boolean optional = false;
  private final List<Exclusion> exclusions = new ArrayList<>();

  /**
   * Creates an empty Dependency object.
   *
   * <p>This constructor initializes an empty Dependency object. It is typically used when you want
   * to create an instance and set the properties later using setter methods.
   */
  public Dependency() {}

  /**
   * Creates a new dependency object.
   *
   * @param coordinates the coordinates in the form of {@code groupId:artifactId:version}
   */
  public Dependency(Coordinates coordinates) {
    this.coordinates = coordinates;
  }

  /** Creates a new dependency object. */
  public Dependency(String declaration) {
    this.coordinates = Coordinates.valueOf(declaration);
  }

  /**
   * Creates a new dependency object.
   *
   * @param dependency the provided dependency
   */
  public Dependency(Dependency copy) {
    this.coordinates = copy.coordinates;
    this.scope = copy.scope;
    this.type = copy.type;
    this.optional = copy.optional;
    this.exclusions.addAll(copy.getExclusions());
  }

  public Coordinates getCoordinates() {
    return this.coordinates;
  }

  public void setCoordinates(Coordinates coordinates) {
    this.coordinates = coordinates;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getScope() {
    return this.scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public boolean isOptional() {
    return this.optional;
  }

  public void setOptional(boolean optional) {
    this.optional = optional;
  }

  public List<Exclusion> getExclusions() {
    return this.exclusions;
  }

  public void addExclusions(Exclusion exclusion) {
    exclusions.add(exclusion);
  }

  @Override
  public String toString() {
    return getCoordinates().toString();
  }

  public String allToString() {
    return "Dependency[coordinates="
        + coordinates.toString()
        + ", type="
        + type
        + ", scope="
        + scope
        + ", optional="
        + optional
        + ", exclusions="
        + exclusions
        + "]";
  }

  public boolean matches(Dependency dependency) {
    String gID = getCoordinates().getGroupId();
    String aID = getCoordinates().getArtifactId();
    // Dependency's G/A to match
    String dAID = dependency.getCoordinates().getArtifactId();
    String dGID = dependency.getCoordinates().getGroupId();
    if ((gID != null || aID != null) || (dGID != null || dAID != null)) {
      return (gID == null || gID.equals(dGID)) && (aID == null || aID.equals(dAID));
    }
    return false;
  }
}
