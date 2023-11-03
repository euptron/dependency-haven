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

package eup.dependency.haven.api;

import java.io.File;
import eup.dependency.haven.model.Pom;
import eup.dependency.haven.api.Library;
import java.util.List;

/**
 * A cached library of a dependency
 *
 * @author EUP
 */
public class CachedLibrary implements Library {

  private Pom libraryPom;
  private File sourceFile;
  private String libraryName;

  @Override
  public void setSourcePath(String sourcePath) {
    this.sourceFile = new File(sourcePath);
    this.libraryName = sourceFile.getName();
  }

  @Override
  public Pom getLibraryPom() {
    return this.libraryPom;
  }

  @Override
  public void setLibraryPom(Pom libraryPom) {
    this.libraryPom = libraryPom;
  }

  @Override
  public File getSourceFile() {
    return this.sourceFile;
  }

  @Override
  public void setSourceFile(File sourceFile) {
    this.sourceFile = sourceFile;
  }

  @Override
  public String getLibraryName() {
    return this.libraryName;
  }

  @Override
  public void setLibraryName(String libraryName) {
    this.libraryName = libraryName;
  }

  @Override
  public List<CachedLibrary> getFrom(File file) {
    return null;
  }
}
