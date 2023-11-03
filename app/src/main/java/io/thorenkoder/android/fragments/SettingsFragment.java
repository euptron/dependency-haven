package io.thorenkoder.android.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.google.android.material.transition.MaterialSharedAxis;
import io.thorenkoder.android.BaseApplication;
import io.thorenkoder.android.R;
import io.thorenkoder.android.SharedPreferenceKeys;
import io.thorenkoder.android.util.PreferencesUtils;
import java.io.File;

public class SettingsFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  public static final String TAG = SettingsFragment.class.getSimpleName();

  private MainFragment.SVM svm;
  private Preference preferenceDownloadDir;

  public static SettingsFragment newInstance() {
    return new SettingsFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    svm = new ViewModelProvider(requireActivity()).get(MainFragment.SVM.class);
    PreferencesUtils.getDefaultPreferences().registerOnSharedPreferenceChangeListener(this);
    setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, false));
    setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, true));
  }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.settings_preferences, rootKey);
    Preference preferenceTheme = findPreference(SharedPreferenceKeys.KEY_APP_THEME);
    preferenceDownloadDir = findPreference(SharedPreferenceKeys.KEY_LIBRARY_MANAGER);
    BaseApplication app = BaseApplication.getInstance();
    String libPref =
        PreferencesUtils.getDefaultPreferences()
            .getString(SharedPreferenceKeys.KEY_LIBRARY_MANAGER, "");

    if (libPref != null || !libPref.isEmpty() && new File(libPref).exists()) {
      preferenceDownloadDir.setSummary(libPref);
    } else {
      preferenceDownloadDir.setSummary(R.string.pref_download_dir_not_set);
    }

    preferenceTheme.setOnPreferenceChangeListener(
        (preference, newValue) -> {
          if (newValue instanceof String) {
            int newTheme = PreferencesUtils.getCurrentTheme((String) newValue);
            AppCompatDelegate.setDefaultNightMode(newTheme);
            return true;
          }
          return false;
        });

    preferenceDownloadDir.setOnPreferenceClickListener(
        preference -> {
          if (MainFragment.isPermissionGranted(requireActivity())) {
            svm.setCanSelectFolder(true);
          } else {
            svm.setCanRequestStoragePermissionState(true);
          }
          return false;
        });
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    PreferencesUtils.getDefaultPreferences().unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    switch (key) {
      case SharedPreferenceKeys.KEY_LIBRARY_MANAGER:
        String libPref =
            PreferencesUtils.getDefaultPreferences()
                .getString(SharedPreferenceKeys.KEY_LIBRARY_MANAGER, "");
        preferenceDownloadDir.setSummary(libPref);
        break;
    }
  }
}
