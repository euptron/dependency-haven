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

import eup.dependency.haven.callback.DownloadCallback;
import eup.dependency.haven.model.Dependency;
import eup.dependency.haven.model.Pom;
import eup.dependency.haven.resolver.DependencyResolver;
import java.io.File;
import java.io.IOException;

/**
 * An interface for caching and managing resolved artifacts.
 *
 * @author EUP
 */
public interface StorageFactory {

  /**
   * Downloads a POM from a remote repository.
   *
   * @param dependency The POM dependency.
   * @param remoteRepository The remote repository.
   * @param relativePath The path relative to the POM download URL.
   * @return The downloaded POM file or null if not found in the repository.
   */
  File downloadPom(Dependency dependency, RemoteRepository remoteRepository, String relativePath);

  /**
   * Returns the library file (e.g., JAR or AAR) associated with a POM.
   *
   * @param pom The POM file.
   * @return The library file or null if not found in the local repository.
   * @throws IOException If an I/O error occurs.
   */
  File getLibrary(Pom pom) throws IOException;

  /**
   * Sets the directory for caching files.
   *
   * @param directory The directory for storing cached files.
   */
  void setCacheDirectory(File directory);

  /**
   * Retrieves the cache directory.
   *
   * @return The cache directory.
   */
  File getCacheDirectory();

  /**
   * Attaches the given dependency resolver to the factory.
   *
   * @param resolver The dependency resolver to attach.
   */
  void attach(DependencyResolver resolver);

  /**
   * Downloads all libraries associated with a given POM.
   *
   * @param pom The POM for which to download libraries.
   */
  void downloadLibraries(Pom pom);

  /**
   * Sets the download callback for the factory.
   *
   * @param callback The DownloadCallback to set.
   */
  void setDownloadCallback(DownloadCallback callback);
}
