package io.thorenkoder.android.logging;

import android.content.Context;
import io.thorenkoder.android.util.SingletonContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import org.apache.commons.io.FileUtils;

public class LogPrinter {

  private Context context = SingletonContext.getApplicationContext();

  public static void start(Logger logger) {
    // Reset the log file
    File logFile =
        new File(SingletonContext.getApplicationContext().getExternalFilesDir(null), "logs.txt");
    try {
      FileUtils.write(logFile, "", "UTF-8", false);
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      PrintStream ps =
          new PrintStream(new FileOutputStream(logFile, true)) {
            @Override
            public void write(byte[] b, int off, int len) {
              super.write(b, off, len);

              // Assuming that each line is terminated with a newline character
              String logLine = new String(b, off, len);
              logger.d("System.out", logLine);
            }
          };

      System.setOut(ps);
      System.setErr(ps);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
