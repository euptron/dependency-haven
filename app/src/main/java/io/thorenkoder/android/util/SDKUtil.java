package io.thorenkoder.android.util;

import android.os.Build;

public class SDKUtil {

  public enum API {
    ANDROID_5,
    ANDROID_6,
    ANDROID_7,
    ANDROID_8,
    ANDROID_9,
    ANDROID_10,
    ANDROID_11,
    ANDROID_12
  }

  public static boolean isAtLeast(API api) {
    if (api == API.ANDROID_5) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    } else if (api == API.ANDROID_6) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    } else if (api == API.ANDROID_7) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    } else if (api == API.ANDROID_8) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    } else if (api == API.ANDROID_9) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    } else if (api == API.ANDROID_10) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    } else if (api == API.ANDROID_11) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    } else if (api == API.ANDROID_12) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }
    return false; // default
  }
}
