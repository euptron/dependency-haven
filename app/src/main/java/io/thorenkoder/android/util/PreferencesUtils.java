package io.thorenkoder.android.util;

import android.app.Activity;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import io.thorenkoder.android.SharedPreferenceKeys;
import com.google.android.material.color.DynamicColors;
import androidx.appcompat.app.AppCompatDelegate;
import io.thorenkoder.android.util.SDKUtil.API;

public class PreferencesUtils {

	public static int getCurrentTheme() {
		String selectedTheme = getDefaultPreferences().getString(SharedPreferenceKeys.KEY_APP_THEME, "3");
		return getCurrentTheme(selectedTheme);
	}

	public static int getCurrentTheme(String selectedTheme) {
		switch (selectedTheme) {
		case "2":
			return AppCompatDelegate.MODE_NIGHT_YES;
		case "1":
			return AppCompatDelegate.MODE_NIGHT_NO;
		default:
			if (SDKUtil.isAtLeast(API.ANDROID_10)) {
				return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
			} else {
				return AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
			}
		}
	}

	public static SharedPreferences getDefaultPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(SingletonContext.getApplicationContext());
	}

	public static SharedPreferences getPrivatePreferences() {
		return SingletonContext.getApplicationContext().getSharedPreferences(SharedPreferenceKeys.KEY_PRIVATE_PREFERENCES, Activity.MODE_PRIVATE);
	}

	public static boolean useDynamicColors() {
		if (SDKUtil.isAtLeast(API.ANDROID_12) && DynamicColors.isDynamicColorAvailable()) {
			return getDefaultPreferences().getBoolean(SharedPreferenceKeys.KEY_DYNAMIC_COLOURS, true);
		} else {
			return getDefaultPreferences().getBoolean(SharedPreferenceKeys.KEY_DYNAMIC_COLOURS, false);
		}
	}
}