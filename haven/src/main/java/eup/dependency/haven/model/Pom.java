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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A representation of a Maven POM.
 *
 * @author EUP
 */
public class Pom {

  private Pom parent;
  private Coordinates coordinates;
  private Dependency dependency;
  private List<Dependency> managedDependencies;
  private List<Dependency> dependencies;
  private List<Exclusion> exclusions;
  private boolean userDefined;
  private final Map<String, String> properties = new HashMap<>();

  public Pom() {
    managedDependencies = new ArrayList<>();
    dependencies = new ArrayList<>();
    exclusions = new ArrayList<>();
  }

  public Pom(List<Dependency> dependencies) {
    this.dependencies = dependencies;
    managedDependencies = new ArrayList<>();
    exclusions = new ArrayList<>();
  }

  public Pom(Coordinates coordinates) {
    this.coordinates = coordinates;
    managedDependencies = new ArrayList<>();
    dependencies = new ArrayList<>();
    exclusions = new ArrayList<>();
  }

  public Pom(Dependency dependency) {
    this.dependency = dependency;
    managedDependencies = new ArrayList<>();
    dependencies = new ArrayList<>();
    exclusions = new ArrayList<>();
  }

  public Coordinates valueOf(String declaration) {
    return Coordinates.valueOf(declaration);
  }

  public Pom getParent() {
    return this.parent;
  }

  public void setParent(Pom parent) {
    this.parent = parent;
  }

  public Coordinates getCoordinates() {
    return this.coordinates;
  }

  public void setCoordinates(Coordinates coordinates) {
    this.coordinates = coordinates;
  }

  public Dependency getDependency() {
    return this.dependency;
  }

  public void setDependency(Dependency dependency) {
    this.dependency = dependency;
  }

  public List<Dependency> getManagedDependencies() {
    if (managedDependencies == null) {
      managedDependencies = new ArrayList<>();
    }
    return this.managedDependencies;
  }

  public void setManagedDependencies(List<Dependency> managedDependencies) {
    this.managedDependencies = managedDependencies;
  }

  public List<Dependency> getDependencies() {
    if (dependencies == null) {
      return new ArrayList<>();
    }
    return this.dependencies;
  }

  public void addDependency(Dependency dependency) {
    if (dependency == null) {
      this.dependencies = new ArrayList<>();
    }
    this.dependencies.add(dependency);
  }

  public void setDependencies(List<Dependency> dependencies) {
    this.dependencies = dependencies;
  }

  public List<Exclusion> getExclusions() {
    if (exclusions == null) {
      exclusions = new ArrayList<>();
    }
    return this.exclusions;
  }

  public void setExclusions(List<Exclusion> exclusions) {
    this.exclusions = exclusions;
  }

  public Map<String, String> getProperties() {
    return this.properties;
  }

  public void addExclusions(List<Exclusion> exclusions) {
    if (exclusions == null) {
      this.exclusions = new ArrayList<>();
    }
    this.exclusions.addAll(exclusions);
  }

  public void addExclusions(Exclusion exclusions) {
    if (exclusions == null) {
      this.exclusions = new ArrayList<>();
    }
    this.exclusions.add(exclusions);
  }

  public void addProperty(String key, String value) {
    properties.put(key, value);
  }

  public String getProperty(String key) {
    return (properties.get(key) != null) ? properties.get(key) : "";
  }

  public void setUserDefined(boolean enabled) {
    userDefined = enabled;
  }

  public boolean isUserDefined() {
    return userDefined;
  }
}
