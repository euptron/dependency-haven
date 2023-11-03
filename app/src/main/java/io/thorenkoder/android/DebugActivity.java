package io.thorenkoder.android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.os.Build;

public class DebugActivity extends Activity {

  public static final String LOG_TAG = "CrashExceptionHandler";

  private final String[] exceptionTypes = {
    "StringIndexOutOfBoundsException",
    "IndexOutOfBoundsException",
    "ArithmeticException",
    "NumberFormatException",
    "ActivityNotFoundException",
    "NullPointerException",
    "IllegalStateException",
    "ClassCastException",
    "FileNotFoundException",
  };

  private final String[] exceptionMessages = {
    "Invalid string operation",
    "Invalid list operation",
    "Invalid arithmetical operation",
    "Invalid toNumber block operation",
    "Invalid intent operation",
    "A required object is null",
    "An illegal state was encountered",
    "Invalid type conversion",
    "File not found",
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    handleErrorMessage();
  }

  private void handleErrorMessage() {
    Intent intent = getIntent();
    String errorMessage = intent.getStringExtra("error");
    String madeErrorMessage = createFormattedErrorMessage(errorMessage);
    showErrorDialog(madeErrorMessage);
  }

  private String createFormattedErrorMessage(String errorMessage) {
    String madeErrorMessage = errorMessage;
    try {
      for (int j = 0; j < exceptionTypes.length; j++) {
        if (errorMessage.contains(exceptionTypes[j])) {
          String exceptionType = exceptionTypes[j];
          int addIndex = errorMessage.indexOf(exceptionType) + exceptionType.length();
          madeErrorMessage =
              exceptionMessages[j]
                  + errorMessage.substring(addIndex)
                  + "\n\n"
                  + getBaseContext().getString(R.string.msg_error_format)
                  + ":\n"
                  + errorMessage;
          break;
        }
      }
      if (madeErrorMessage.isEmpty()) {
        madeErrorMessage = errorMessage;
      }
    } catch (Exception e) {
      madeErrorMessage =
          getString(R.string.msg_error_while_collecting_error) + ":" + Log.getStackTraceString(e);
    }
    return madeErrorMessage;
  }

  private void showErrorDialog(String error) {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
    builder.setTitle(getBaseContext().getString(R.string.msg_app_crashed));
    builder.setMessage(getBaseContext().getString(R.string.msg_app_crashed_error) + "\n\n" + error);
    builder.setPositiveButton(
        getBaseContext().getString(R.string.restart), (dialog, which) -> restartApp());
    builder.setNegativeButton(
        getBaseContext().getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
    builder.setNeutralButton(
        getBaseContext().getString(R.string.share), (dialog, which) -> shareText(error));
    builder.setCancelable(false);
    final AlertDialog dialog = builder.create();
    dialog.show();
    ((TextView) dialog.findViewById(android.R.id.message)).setTextIsSelectable(true);
  }

  // Share Error message
  private void shareText(final String mText) {
    Intent shareTextIntent = new Intent();
    shareTextIntent.setAction(Intent.ACTION_SEND);
    shareTextIntent.putExtra(Intent.EXTRA_TEXT, mText);
    shareTextIntent.setType("text/plain");
    startActivity(
        Intent.createChooser(
            shareTextIntent, getBaseContext().getString(R.string.msg_share_error)));
  }

  private void restartApp() {
    Intent intent =
        getBaseContext()
            .getPackageManager()
            .getLaunchIntentForPackage(getBaseContext().getPackageName());
    if (intent != null) {
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      this.startActivity(intent);
      finish();
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Process.killProcess(Process.myPid());
    } else {
      System.exit(1);
    }
  }
}
