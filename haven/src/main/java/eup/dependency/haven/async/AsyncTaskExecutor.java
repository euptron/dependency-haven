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

package eup.dependency.haven.async;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Asynchronous task executor
 *
 * @author EUP
 */
public class AsyncTaskExecutor {

  private static final Executor executor =
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  public static <R> void loadTaskAsync(Callable<R> callable, Callback<R> listener) {
    CompletableFuture.supplyAsync(
            () -> {
              try {
                return callable.call();
              } catch (Exception e) {
                return null;
              }
            },
            executor)
        .thenAcceptAsync(
            data -> {
              if (data != null) {
                listener.onLoaded(data);
              }
            });
  }

  public interface Callback<R> {
    void onLoaded(R result);
  }

  private static class MainThreadExecutor implements Executor {

    @Override
    public void execute(Runnable command) {
      command.run();
    }
  }
}
