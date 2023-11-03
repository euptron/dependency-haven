package io.thorenkoder.android.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;

/*
 * Utility to retrive the application context from anywhere
 */

public class SingletonContext {

	private static Context mContext;

	public static void initialize(@NonNull Context context) {
		mContext = context.getApplicationContext();
	}

	public static Context getApplicationContext() {
		if (mContext == null) {
			throw new IllegalStateException("initialize() hasn't been called.");
		}
		return mContext;
	}
}
