package io.thorenkoder.android.util;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Context;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;
import android.content.ClipData;
import android.os.Environment;
import android.content.ClipboardManager;
import android.os.Build;
import androidx.core.content.ContextCompat;
import android.widget.Toast;
import io.thorenkoder.android.R;
import android.util.TypedValue;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;

/** Utility class to manage certain requirements of the app */
public class BaseUtil {

  private static Context context = SingletonContext.getApplicationContext();

  public static int pxToDp(Context context, float dp) {
    return Math.round(
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
  }

  public static boolean isConnected() {
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }

  public static void copyToClipBoard(String text) {
    ClipboardManager clipboardManager =
        (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clipData = ClipData.newPlainText("", text);
    clipboardManager.setPrimaryClip(clipData);
  }

  public static void copyToClipBoard(String text, boolean withToast) {
    copyToClipBoard(text);
    if (withToast) {
      String msg = context.getString(R.string.copied_to_clipboard);
      showToast(msg);
    }
  }

  public static void showToast(String message) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
  }

  public static boolean isPermissionGaranted(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      return Environment.isExternalStorageManager();
    } else {
      return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
          == PackageManager.PERMISSION_GRANTED;
    }
  }

  // dexing-res/$value < assetpath
  public static void unzipFromAssets(String assetZipFile, String destinationFolder) {
    try {
      if (destinationFolder == null || destinationFolder.length() == 0) {
        throw new IllegalArgumentException("Destination directory must be a set");
      }
      File destination = new File(destinationFolder);
      if (!destination.exists()) {
        FileUtils.forceMkdir(destination);
      }
      InputStream is = context.getAssets().open(assetZipFile);
      unzip(is, destinationFolder);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void unzip(String sourceFilePath, String destinationFolder) throws IOException {
    FileInputStream fis = new FileInputStream(sourceFilePath);
    unzip(fis, destinationFolder);
  }

  public static void unzip(InputStream inputStream, String destinationFolder) throws IOException {
    int BYTE_SIZE = 10240; // 10MB Cap
    byte[] buffer = new byte[BYTE_SIZE];

    ZipInputStream zipInputStream = new ZipInputStream(inputStream);
    ZipEntry zipEntry = zipInputStream.getNextEntry();
    while (zipEntry != null) {
      String entryName = zipEntry.getName();
      File file = new File(destinationFolder + File.separator + entryName);

      if (zipEntry.isDirectory()) {
        file.mkdirs();
      } else {
        file.getParentFile().mkdirs();
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
          int len;
          while ((len = zipInputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
          }
        }
      }
      // close zipInputStream to prevent memory leaks
      zipInputStream.closeEntry();
      zipEntry = zipInputStream.getNextEntry();
    }
    // just added below
    zipInputStream.close();
  }

  public static void unzipX(String sourceFilePath, String destinationFolder) throws IOException {
    int BYTE_SIZE = 10240; // 10MB Cap
    byte[] buffer = new byte[BYTE_SIZE];

    ZipInputStream zipInputStream =
        new ZipInputStream(new BufferedInputStream(new FileInputStream(sourceFilePath)));
    ZipEntry zipEntry = zipInputStream.getNextEntry();
    while (zipEntry != null) {
      String entryName = zipEntry.getName();
      File file = new File(destinationFolder + File.separator + entryName);

      if (zipEntry.isDirectory()) {
        file.mkdirs();
      } else {
        file.getParentFile().mkdirs();
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
          int len;
          while ((len = zipInputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
          }
        }
      }
      // close zipInputStream to prevent memory leaks
      zipInputStream.closeEntry();
      zipEntry = zipInputStream.getNextEntry();
    }
    // just added below
    zipInputStream.close();
  }

  public static class Path {
    public static final File RESOURCE_FOLDER = context.getExternalFilesDir("resources");
    public static final File ANDROID_JAR = new File(RESOURCE_FOLDER, "android.jar");
    public static final File CORE_LAMDA_STUBS = new File(RESOURCE_FOLDER, "core-lambda-stubs.jar");
    public static final File REPOSITORIES_JSON = new File(RESOURCE_FOLDER, "repositories.json");
  }
}
