package io.thorenkoder.android;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.color.DynamicColors;
import io.thorenkoder.android.util.PreferencesUtils;
import io.thorenkoder.android.util.SingletonContext;

public class BaseApplication extends Application {

  private static BaseApplication instance;
  public static Context applicationContext;

  private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

  @Override
  public void onCreate() {
    super.onCreate();
    initialize();
    setUncaughtExceptionHandler();
    changeTheme();
    useDynamicColor();
  }

  public static BaseApplication getInstance() {
    return instance;
  }

  private void setUncaughtExceptionHandler() {
    uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    Thread.setDefaultUncaughtExceptionHandler(
        (thread, throwable) -> handleUncaughtException(thread, throwable));
  }

  private void handleUncaughtException(Thread thread, Throwable throwable) {
    Intent intent = new Intent(getApplicationContext(), DebugActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
    intent.putExtra("error", Log.getStackTraceString(throwable));

    PendingIntent pendingIntent =
        PendingIntent.getActivity(
            getApplicationContext(), 11111, intent, PendingIntent.FLAG_IMMUTABLE);

    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, pendingIntent);

    Process.killProcess(Process.myPid());
    System.exit(1);

    uncaughtExceptionHandler.uncaughtException(thread, throwable);
  }

  private void initialize() {
    instance = this;
    applicationContext = this;
    SingletonContext.initialize(applicationContext);
  }

  public void changeTheme() {
    int theme = PreferencesUtils.getCurrentTheme();
    AppCompatDelegate.setDefaultNightMode(theme);
  }

  private void useDynamicColor() {
    if (PreferencesUtils.useDynamicColors()) {
      DynamicColors.applyToActivitiesIfAvailable(this);
    }
  }
}
