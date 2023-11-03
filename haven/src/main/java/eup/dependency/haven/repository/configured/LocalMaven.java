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

package eup.dependency.haven.repository.configured;

import eup.dependency.haven.repository.DefaultArtifactRepository;
import java.io.File;

/**
 * {@link DefaultArtifactRepository} for Repository
 *
 * <p>A default local maven repository In the local maven repository to get the root directory use
 * {@link getBaseDir}
 */
public class LocalMaven extends DefaultArtifactRepository {

  private final String sourcePath;

  private File mavenHomeConfiguration = new File(System.getProperty("user.home"), ".m2");

  public LocalMaven(String sourcePath) {
    super(sourcePath);
    this.sourcePath = sourcePath;
  }

  @Override
  public String getId() {
    return "local-x27c";
  }

  @Override
  public String getName() {
    return "Maven-Home";
  }

  @Override
  public File getBaseDir() {
    return new File(mavenHomeConfiguration, sourcePath);
  }
}
