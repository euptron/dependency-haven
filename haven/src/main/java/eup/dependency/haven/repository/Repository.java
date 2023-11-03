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

import eup.dependency.haven.model.Coordinates;
import eup.dependency.haven.model.Pom;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Manage {@link ArtifactRepository} for the required implementation
 *
 * <p>Note: Only use the default repository flag when reading from a remote repository
 *
 * @see #readRemoterepositoryConfig(String,boolean)
 * @see #readRemoterepositoryConfig(File,boolean)
 * @author EUP
 */
public interface Repository {

  /**
   * Retrieves the parent POM (Project Object Model) of a dependency, either local or remote.
   *
   * @param coordinates The POM coordinates
   * @return The POM object or null if not found in the repository.
   */
  Pom getParentPom(Coordinates coordinates);

  /**
   * Adds a repository to use for subsequent resolution, the search would start in order of which
   * the repositories were added in other words the order in which repositories are added matters.
   * When multiple repositories with the same id are added, only the first repository being added
   * will be used.
   *
   * @param repository the repository to add to the resolution search list, must not be {@code
   *     null}.
   * @throws IllegalArgumentException If the repository could not be added (e.g. due to invalid
   *     URL).
   */
  void addRepository(RemoteRepository repository) throws IllegalArgumentException;

  /**
   * Adds a repository to use for subsequent resolution, the search would start in order of which
   * the repositories were added in other words the order in which repositories are added matters.
   * When multiple repositories with the same id are added, only the first repository being added
   * will be used.
   *
   * @param name the name of the repository to add to the resolution search list, must not be {@code
   *     null}
   * @param url the url of the repository to add to the resolution search list, must not be {@code
   *     null}
   * @throws IllegalArgumentException If the repository could not be added (e.g. due to invalid
   *     URL).
   */
  void addRepository(String name, String url) throws IllegalArgumentException;

  public static class Manager {

    /** A list of the default remote repository to use. */
    public static final List<RemoteRepository> DEFAULT_REMOTE_REPOSITORIES =
        Arrays.asList(
            // Prioritize well-known and common repositories
            new RemoteRepository("maven-central", "https://repo1.maven.org/maven2"),
            new RemoteRepository("google-maven", "https://maven.google.com"),
            new RemoteRepository("jitpack", "https://jitpack.io"),
            new RemoteRepository("jcenter", "https://jcenter.bintray.com"));

    /**
     * Reads an artifact repository configuration from a JSON file
     *
     * @param jsonFile The JSON file to read the repository from.
     * @param useDefaultRepos a flag to indicate that we want to use the default repository
     * @return a list of repository specified in a JSON file
     * @throws possible exceptions that may occur
     */
    public static List<RemoteRepository> readRemoteRepositoryConfig(
        File jsonFile, boolean useDefaultRepos) throws JSONException, IOException {
      if (jsonFile == null || !jsonFile.exists()) {
        return Collections.emptyList();
      }
      // Read repository configuration from the JSON file
      BufferedReader br = new BufferedReader(new FileReader(jsonFile));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line.trim());
      }
      String json = sb.toString();
      return readRemoteRepositoryConfig(json, useDefaultRepos);
    }

    /**
     * Returns a list of repository specified in a string. see {@link #getrepository(file,boolean)}
     */
    public static List<RemoteRepository> readRemoteRepositoryConfig(
        String jsonString, boolean useDefaultRepos) throws JSONException {
      List<RemoteRepository> repositories = new ArrayList<>();
      if (jsonString == null) {
        return Collections.emptyList();
      }
      JSONArray array = new JSONArray(jsonString);
      for (int i = 0; i < array.length(); i++) {
        JSONObject repo = array.getJSONObject(i);
        repositories.add(new RemoteRepository(repo.getString("name"), repo.getString("url")));
      }

      // Add default repository if requested
      if (useDefaultRepos) {
        repositories.addAll(DEFAULT_REMOTE_REPOSITORIES);
      }
      return repositories;
    }

    /**
     * Generates a JSON string from the default list of repositories
     *
     * @return the json string
     */
    public static String generateJSON() {
      return generateJSON(DEFAULT_REMOTE_REPOSITORIES);
    }

    /**
     * POJO method to generates a JSON string from a list of Remote or Local repository.
     *
     * @param repository The list of repository to generate a JSON string from.
     * @param artifactRepositoryList the repositories to generate json from
     * @return A JSON string representing the given repository.
     */
    public static String generateJSON(List<RemoteRepository> remoteRepositoryList) {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      for (int i = 0; i < remoteRepositoryList.size(); i++) {
        RemoteRepository repository = remoteRepositoryList.get(i);
        sb.append("{\"name\": \"");
        sb.append(repository.getName());
        sb.append("\", \"url\": \"");
        if (repository.getUrl().endsWith("/")) {
          sb.append(repository.getUrl().substring(0, repository.getUrl().length() - 1));
        } else {
          sb.append(repository.getUrl());
        }
        sb.append("\"}");
        if (i < remoteRepositoryList.size() - 1) {
          sb.append(",");
        }
      }
      sb.append("]");
      return sb.toString();
    }
  }
}
