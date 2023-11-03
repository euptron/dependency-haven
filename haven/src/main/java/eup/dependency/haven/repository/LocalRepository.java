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

import java.io.File;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

/**
 * A repository on the local file system used to cache contents of remote repository and to store
 * locally installed artifacts.
 *
 * @author EUP
 */
public class LocalRepository extends DefaultArtifactRepository {

  public LocalRepository(String sourcePath) {
    super(sourcePath);
  }

  /**
   * Gets a local repository from a cache directory
   *
   * @param cacheDir the folder containing local repositories
   */
  public static LocalRepository fromCache(File cacheDir) {
    if (!cacheDir.exists() || cacheDir.isFile()) {
      return null;
    }
    LocalRepository localRepository = new LocalRepository(cacheDir.getAbsolutePath());
    localRepository.setName(cacheDir.getName());
    return localRepository;
  }

  /**
   * Retrieves all folders in cache directory as repositories TODO: validate folder
   *
   * @return all cached repositories
   */
  public static List<LocalRepository> getRepositories(File cacheDir) {
    try {
      File dir = cacheDir;
      File[] listFiles = dir.listFiles();
      if (listFiles == null || listFiles.length <= 0) {
        return Collections.emptyList();
      }
      List<LocalRepository> repositories = new ArrayList<>();
      for (File file : listFiles) {
        LocalRepository repository = fromCache(file);
        if (repository != null) {
          repositories.add(repository);
        }
      }
      return repositories;
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to get local repositories " + e);
    }
  }
}
