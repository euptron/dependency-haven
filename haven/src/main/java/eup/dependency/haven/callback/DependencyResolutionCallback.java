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

package eup.dependency.haven.callback;

import eup.dependency.haven.model.Coordinates;
import eup.dependency.haven.model.Dependency;
import java.util.List;

/**
 * An interface to listen for dependency resolution callbacks.
 *
 * @author EUP
 */
public interface DependencyResolutionCallback {

  /**
   * Called when the dependency resolution is complete.
   *
   * @param message The resolution message
   * @param resolvedDependencies the list of resolved dependencies.
   * @param totalTime the total time taken to resolve the dependency in milliseconds.
   */
  void onDependenciesResolved(
      String message, List<Dependency> resolvedDependencies, long totalTime);

  /**
   * Called when unresolved dependencies need to be tracked.
   *
   * @param message The resolution message
   * @param unresolvedDependencies the list of unresolved dependencies.
   */
  void onDependencyNotResolved(String message, List<Dependency> unresolvedDependencies);

  /**
   * Called when an info message is logged
   *
   * @param message the message logged
   */
  void info(String message);

  /**
   * Called when a verbose message is logged
   *
   * @param message the message logged
   */
  void verbose(String message);

  /**
   * Called when an error message is logged
   *
   * @param message the message logged
   */
  void error(String message);

  /**
   * Called when a warning message is logged
   *
   * @param message the message logged
   */
  void warning(String message);
}
