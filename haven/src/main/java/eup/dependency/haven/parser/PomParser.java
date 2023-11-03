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


package eup.dependency.haven.parser;

import eup.dependency.haven.model.Coordinates;
import eup.dependency.haven.model.Dependency;
import eup.dependency.haven.model.Exclusion;
import eup.dependency.haven.model.Pom;
import eup.dependency.haven.repository.Repository;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A parser that retrives dependency information from a POM stream
 *
 * @author EUP
 */
public class PomParser {

  private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([A-Za-z.]*?)\\}");
  private static final Pattern VARIABLE_PATTERNX = Pattern.compile("\\$\\{(.*?)\\}");

  private Pom parent;
  private final Map<String, String> mProperties;
  public static String parsingDuration;
  private Repository repository;

  private static final String NODE_NAME_DEPENDENCY_MANAGEMENT = "dependencyManagement";
  private static final String NODE_NAME_DEPENDENCIES = "dependencies";
  private static final String NODE_NAME_DEPENDENCY = "dependency";
  private static final String NODE_NAME_EXCLUSION = "exclusion";
  private static final String NODE_NAME_PARENT = "parent";
  private static final String NODE_NAME_PROJECT = "project";
  private static final String NODE_NAME_GROUP_ID = "groupId";
  private static final String NODE_NAME_ARTIFACT_ID = "artifactId";
  private static final String NODE_NAME_EXCLUSIONS = "exclusions";
  private static final String NODE_NAME_PROPERTIES = "properties";
  private static final String NODE_NAME_VERSION = "version";
  private static final String NODE_NAME_SCOPE = "scope";
  private static final String NODE_NAME_TYPE = "type";
  private static final String NODE_NAME_PACKAGING = "packaging";

  private DocumentBuilderFactory dbFactory;

  public PomParser() {
    this.mProperties = new HashMap<>();
  }

  public PomParser(Repository repository) {
    this.repository = repository;
    this.mProperties = new HashMap<>();
    this.dbFactory = DocumentBuilderFactory.newInstance();
    this.dbFactory.setIgnoringComments(true);
  }

  public Pom parse(InputStream pomStream) throws IOException {
    if (pomStream == null) {
      return null;
    }
    long startTime = System.currentTimeMillis();
    Document document = parseInputStream(pomStream);
    long endTime = System.currentTimeMillis();
    parsingDuration = (endTime - startTime) + " ms";
    return parseProject(document);
  }

  private Document parseInputStream(InputStream inputStream) throws IOException {
    try {
      return dbFactory.newDocumentBuilder().parse(inputStream);
    } catch (ParserConfigurationException | SAXException e) {
      throw new IOException(e);
    }
  }

  private Pom parseProject(Document document) {
    Element projectElement = document.getDocumentElement();
    if (!NODE_NAME_PROJECT.equals(projectElement.getTagName())) {
      return null;
    }

    NodeList properties = projectElement.getElementsByTagName(NODE_NAME_PROPERTIES);
    if (properties.getLength() > 0) {
      mProperties.putAll(parseProperties((Element) properties.item(0)));
    }

    Pom pom = new Pom();
    Coordinates coordinates = new Coordinates();
    NodeList childNodes = projectElement.getChildNodes();
    Dependency coordinateDependency = new Dependency();

    for (int i = 0; i < childNodes.getLength(); i++) {
      Node child = childNodes.item(i);
      String nodeName = child.getNodeName();
      if (NODE_NAME_DEPENDENCIES.equals(nodeName)) {
        pom.setDependencies(parseDependencies(pom, (Element) child));
      } else if (NODE_NAME_DEPENDENCY_MANAGEMENT.equals(nodeName)) {
        List<Dependency> dependencies = parseDependencies(pom, (Element) child);
        pom.setManagedDependencies(dependencies);
        // added managed dependecies
        pom.setDependencies(parseDependencies(pom, (Element) child));
      } else if (NODE_NAME_PARENT.equals(nodeName)) {
        parent = parseParent((Element) child);
        pom.setParent(parent);
      } else if (NODE_NAME_GROUP_ID.equals(nodeName)) {
        coordinates.setGroupId(getProperty(child));
      } else if (NODE_NAME_ARTIFACT_ID.equals(nodeName)) {
        coordinates.setArtifactId(getProperty(child));
      } else if (NODE_NAME_VERSION.equals(nodeName)) {
        coordinates.setVersion(getProperty(child));
      } else if (NODE_NAME_PACKAGING.equals(nodeName)) {
        coordinates.setPackaging(getProperty(child));
        // add the packaging as type for now
        coordinateDependency.setType(coordinates.getPackaging());
      }
    }

    if (coordinates.getGroupId() == null && pom.getParent() != null) {
      // inherit groupID from parent if null
      coordinates.setGroupId(pom.getParent().getCoordinates().getGroupId());
    }
    if (coordinates.getVersion() == null && pom.getParent() != null) {
      // inherit version from parent if null
      coordinates.setVersion(pom.getParent().getCoordinates().getVersion());
    }

    pom.setCoordinates(coordinates);
    coordinateDependency.setCoordinates(coordinates);
    pom.addDependency(coordinateDependency);

    return pom;
  }

  private String getProperty(Node child) {
    String value = child.getTextContent();
    Matcher matcher = VARIABLE_PATTERN.matcher(value);
    if (matcher.matches()) {
      String name = matcher.group(1);
      String property = mProperties.get(name);
      if (property != null) {
        value = property;
      }
    }
    return value;
  }

  private Pom parseParent(Element element) {
    Coordinates coordinates = new Coordinates();
    NodeList groupIdList = element.getElementsByTagName(NODE_NAME_GROUP_ID);
    if (groupIdList.getLength() < 1) {
      return null;
    }
    coordinates.setGroupId(groupIdList.item(0).getTextContent());

    NodeList artifactIdList = element.getElementsByTagName(NODE_NAME_ARTIFACT_ID);
    if (artifactIdList.getLength() < 1) {
      return null;
    }
    coordinates.setArtifactId(artifactIdList.item(0).getTextContent());

    NodeList versionList = element.getElementsByTagName(NODE_NAME_VERSION);
    if (versionList.getLength() < 1) {
      return null;
    }
    String version = versionList.item(0).getTextContent();
    Matcher matcher = VARIABLE_PATTERN.matcher(version);
    if (matcher.matches()) {
      String name = matcher.group(1);
      String property = mProperties.get(name);
      if (parent != null && property == null) {
        property = parent.getProperty(name);
      }
      if (property != null) {
        version = property;
      }
    }
    coordinates.setVersion(version);
    if (repository != null) {
      return repository.getParentPom(coordinates);
    } else {
      return new Pom(coordinates);
    }
  }

  private Map<String, String> parseProperties(Element propertyElement) {
    Map<String, String> properties = new HashMap<>();
    NodeList propertyTags = propertyElement.getChildNodes();
    for (int i = 0; i < propertyTags.getLength(); i++) {
      if (propertyTags.item(i).getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      Element property = (Element) propertyTags.item(i);
      String key = property.getTagName();
      String value = property.getTextContent();
      properties.put(key, value);
    }
    return properties;
  }

  private List<Dependency> parseDependencies(Pom pom, Element dependenciesNode) {

    List<Dependency> dependencies = new ArrayList<>();
    NodeList dependencyNode = dependenciesNode.getElementsByTagName(NODE_NAME_DEPENDENCY);

    for (int i = 0; i < dependencyNode.getLength(); i++) {
      Element dependencyElement = (Element) dependencyNode.item(i);

      Dependency dependency = new Dependency();
      Coordinates dependencyCoordinates = new Coordinates();

      NodeList scopeList = dependencyElement.getElementsByTagName(NODE_NAME_SCOPE);
      if (scopeList.getLength() >= 1) {
        dependency.setScope(getProperty(scopeList.item(0)));
      }
      NodeList typeList = dependencyElement.getElementsByTagName(NODE_NAME_TYPE);
      if (typeList.getLength() >= 1) {
        dependency.setType(getProperty(typeList.item(0)));
      }
      NodeList groupIdList = dependencyElement.getElementsByTagName(NODE_NAME_GROUP_ID);
      if (groupIdList.getLength() < 1) {
        continue;
      }
      dependencyCoordinates.setGroupId(groupIdList.item(0).getTextContent());

      NodeList artifactIdList = dependencyElement.getElementsByTagName(NODE_NAME_ARTIFACT_ID);
      if (artifactIdList.getLength() < 1) {
        continue;
      }
      dependencyCoordinates.setArtifactId(artifactIdList.item(0).getTextContent());

      NodeList versionList = dependencyElement.getElementsByTagName(NODE_NAME_VERSION);
      if (versionList.getLength() < 1) {
        Pom current = parent;
        boolean found = false;
        outer:
        while (current != null) {
          List<Dependency> managedDependencies = current.getManagedDependencies();
          for (Dependency managedDependency : managedDependencies) {
            if (!managedDependency
                .getCoordinates()
                .getGroupId()
                .equals(dependencyCoordinates.getGroupId())) {
              continue;
            }
            if (!managedDependency
                .getCoordinates()
                .getArtifactId()
                .equals(dependencyCoordinates.getArtifactId())) {
              continue;
            }
            dependencyCoordinates.setVersion(managedDependency.getCoordinates().getVersion());
            dependency.setScope(managedDependency.getScope());
            found = true;
            break outer;
          }
          current = current.getParent();
        }

        if (!found) {
          continue;
        }
      } else {
        dependencyCoordinates.setVersion(getProperty(versionList.item(0)));
      }

      NodeList exclusion = dependencyElement.getElementsByTagName(NODE_NAME_EXCLUSIONS);
      if (exclusion.getLength() > 0) {
        Element exclusionsElement = (Element) exclusion.item(0);
        NodeList exclusionElementList = exclusionsElement.getElementsByTagName(NODE_NAME_EXCLUSION);

        for (int j = 0; j < exclusionElementList.getLength(); j++) {
          Element exclusionElement = (Element) exclusionElementList.item(j);

          Exclusion exclusionDependency = new Exclusion();

          NodeList groupId = exclusionElement.getElementsByTagName(NODE_NAME_GROUP_ID);
          if (groupId.getLength() < 1) {
            continue;
          }
          exclusionDependency.setGroupId(getProperty(groupId.item(0)));

          NodeList artifactId = exclusionElement.getElementsByTagName(NODE_NAME_ARTIFACT_ID);
          if (artifactId.getLength() < 1) {
            continue;
          }
          exclusionDependency.setArtifactId(getProperty(artifactId.item(0)));

          dependency.addExclusions(exclusionDependency);
          // add the exclusions to pom
          pom.addExclusions(exclusionDependency);
        }
      }
      dependency.setCoordinates(dependencyCoordinates);
      dependencies.add(dependency);
    }
    return dependencies;
  }

  public static String getParsingDuration() {
    return parsingDuration;
  }

  public static void main(String[] args) {
    PomParser parser = new PomParser();
    InputStream inputStream = PomParser.class.getResourceAsStream("dummy-pom.xml");
    Pom parsedPom = new Pom();
    try {
      parsedPom = parser.parse(inputStream);
      if (parsedPom == null) {
        System.out.println(
            "Failed to parse POM for "
                + parsedPom.getCoordinates().toString()
                + " because parsedPom is null");
        return;
      }
      inputStream.close();
      System.out.println("Coordinates: " + parsedPom.getCoordinates());
      System.out.println("Dependencies: " + parsedPom.getDependencies());
      System.out.println("Excludes: " + parsedPom.getExclusions());
      System.out.println("Managed Deps: " + parsedPom.getManagedDependencies());
      System.out.println("Pom Parent: " + parsedPom.getParent());
      System.out.println("Parsed POM in " + PomParser.getParsingDuration());
    } catch (Exception e) {
      System.out.println(
          "Failed to parse POM for "
              + parsedPom.getCoordinates().toString()
              + " due to "
              + e.getMessage());
    }
  }
}
