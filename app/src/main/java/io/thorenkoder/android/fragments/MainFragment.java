package io.thorenkoder.android.fragments;

import static io.thorenkoder.android.util.BaseUtil.Path;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.annotation.SuppressLint;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigationrail.NavigationRailView;
import io.thorenkoder.android.R;
import io.thorenkoder.android.SharedPreferenceKeys;
import io.thorenkoder.android.adapter.PagerAdapter;
import io.thorenkoder.android.databinding.FragmentMainBinding;
import io.thorenkoder.android.util.BaseUtil;
import io.thorenkoder.android.util.Constants;
import io.thorenkoder.android.util.PreferencesUtils;
import io.thorenkoder.android.util.SDKUtil;
import io.thorenkoder.android.util.SDKUtil.API;
import java.io.File;
import mod.agus.jcoderz.lib.FileUtil;

public class MainFragment extends Fragment {

  public static final String TAG = MainFragment.class.getSimpleName();
  private PagerAdapter adapter;
  private MenuItem trackableItem;
  private FragmentMainBinding binding;
  private SVM sVM;
  private ActivityResultLauncher<Intent> pickFolderResult;

  private ActivityResultLauncher<String[]> mPermissionLauncher;
  private final ActivityResultContracts.RequestMultiplePermissions mPermissionsContract =
      new ActivityResultContracts.RequestMultiplePermissions();

  public static MainFragment newInstance() {
    return new MainFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    sVM = new ViewModelProvider(requireActivity()).get(SVM.class);

    mPermissionLauncher =
        registerForActivityResult(
            mPermissionsContract,
            isGranted -> {
              if (isGranted.containsValue(true)) {
                String libPref =
                    PreferencesUtils.getDefaultPreferences()
                        .getString(SharedPreferenceKeys.KEY_LIBRARY_MANAGER, "");
                if (libPref == null || (libPref != null && !new File(libPref).exists())) {
                  // Permission granted
                  new MaterialAlertDialogBuilder(requireActivity())
                      .setTitle(R.string.select_download_dir_title)
                      .setMessage(R.string.select_download_dir_msg)
                      .setPositiveButton(
                          R.string.pick_folder,
                          (d, which) -> {
                            selectFolder();
                          })
                      .setNegativeButton(R.string.cancel, null)
                      .setCancelable(false)
                      .show();
                  //	checkDexingRes();
                }
              } else {
                // Permission denied
                new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(R.string.storage_permission_denied)
                    .setMessage(R.string.storage_permission_denial_prompt)
                    .setPositiveButton(
                        R.string.storage_permission_request_again,
                        (d, which) -> {
                          requestPermission();
                        })
                    .setNegativeButton(
                        R.string.exit,
                        (d, which) -> {
                          requireActivity().finishAffinity();
                          System.exit(0);
                        })
                    .setCancelable(false)
                    .show();
              }
            });
    // pick download folder
    pickFolderResult =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() == Activity.RESULT_OK) {
                Intent intent = result.getData();
                if (intent != null) {
                  Uri folderUri = intent.getData();
                  if (folderUri != null) {
                    onFolderSelected(folderUri);
                  }
                }
              }
            });

    if (!isPermissionGranted(requireActivity())) {
      requestPermission();
    } else if (isPermissionGranted(requireActivity())) {
      checkDexingRes();
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentMainBinding.inflate(inflater, container, false);
    configurePages();
    configureNavigationRail();
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    sVM.canRequestStoragePermission()
        .observe(
            getViewLifecycleOwner(),
            canRequest -> {
              if (canRequest) {
                requestPermission();
              }
            });

    sVM.canSelectFolder()
        .observe(
            getViewLifecycleOwner(),
            canSelectFolder -> {
              if (canSelectFolder) {
                selectFolder();
              }
            });
  }

  @Override
  public void onResume() {
    super.onResume();
    checkDexingRes();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    this.binding = null;
  }

  private void configurePages() {
    adapter = new PagerAdapter(getChildFragmentManager(), getLifecycle());
    adapter.addFragment(DependencyManagerFragment.newInstance());
    adapter.addFragment(CommunityFragment.newInstance());
    adapter.addFragment(SettingsFragment.newInstance());
    binding.pager.setOffscreenPageLimit(3);
    binding.pager.setUserInputEnabled(false);
    binding.pager.setAdapter(adapter);
  }

  private void configureNavigationRail() {
    // Set up your Navigation Rail here
    NavigationRailView navigationRail = binding.navigationRail;
    addHeaderView(navigationRail);
    navigationRail
        .getHeaderView()
        .setOnClickListener(
            headerView -> {
              displayOpenYtChannelDialog();
            });
    navigationRail.setOnItemSelectedListener(
        item -> {
          trackableItem = item;
          int position = item.getItemId();
          binding.pager.setCurrentItem(position, false);
          return true;
        });

    // Configure the items for your Navigation Rail
    navigationRail.getMenu().clear();
    navigationRail
        .getMenu()
        .add(0, 0, 0, getContext().getString(R.string.dependency_manager))
        .setIcon(R.drawable.ic_progress_download);
    navigationRail
        .getMenu()
        .add(0, 1, 1, getContext().getString(R.string.community))
        .setIcon(R.drawable.ic_account_group_outline);
    navigationRail
        .getMenu()
        .add(0, 2, 2, getContext().getString(R.string.settings))
        .setIcon(R.drawable.ic_cog_outline);
  }

  private void addHeaderView(NavigationRailView navigationRailView) {
    navigationRailView.addHeaderView(R.layout.navigation_rail_header_view);
  }

  private void removeHeaderView(NavigationRailView navigationRailView) {
    navigationRailView.removeHeaderView();
  }

  private void displayOpenYtChannelDialog() {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
    builder.setTitle(getContext().getString(R.string.msg_title_yt));
    builder.setMessage(getContext().getString(R.string.msg_yt_body));
    builder.setPositiveButton(
        getContext().getString(R.string.dialog_yt_positive_botton),
        (dialog, which) -> openYtChannel());
    builder.setNegativeButton(
        getContext().getString(R.string.dialog_yt_negative_botton),
        (dialog, which) -> dialog.dismiss());
    builder.setCancelable(false);
    builder.show();
  }

  private void openYtChannel() {
    String url = Constants.YOUTUBE_CHANNEL_LINK;
    Intent i = new Intent(Intent.ACTION_VIEW);
    i.setData(Uri.parse(url));
    startActivity(i);
  }

  private void onFolderSelected(Uri uri) {
    try {
      DocumentFile pickedDir = DocumentFile.fromTreeUri(requireContext(), uri);
      File directory =
          new File(FileUtil.convertUriToFilePath(requireActivity(), pickedDir.getUri()));
      String folderPath = directory.getAbsolutePath();
      if (folderPath != null) {
        sVM.setCanSelectFolder(false);
        // save currently selected folder for library downloads
        PreferencesUtils.getDefaultPreferences()
            .edit()
            .putString(SharedPreferenceKeys.KEY_LIBRARY_MANAGER, directory.getAbsolutePath())
            .apply();
      }
    } catch (Exception e) {
      e.printStackTrace();
      BaseUtil.showToast(e.getMessage());
    }
  }

  private void selectFolder() {
    pickFolderResult.launch(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE));
  }

  private void checkDownloadDir() {
    String path =
        PreferencesUtils.getDefaultPreferences()
            .getString(SharedPreferenceKeys.KEY_LIBRARY_MANAGER, null);
    if (path == null) {
      if (isPermissionGranted(requireActivity())) {
        selectFolder();
      } else {
        requestPermission();
      }
    }
  }

  private void requestPermission() {
    if (SDKUtil.isAtLeast(API.ANDROID_11)) {
      Intent intent = new Intent();
      intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
      Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
      intent.setData(uri);
      getActivity().startActivity(intent);
      sVM.setCanRequestStoragePermissionState(false);
    } else {
      sVM.setCanRequestStoragePermissionState(false);
      mPermissionLauncher.launch(
          new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
          });
    }
  }


  @SuppressLint("NewApi")
  public static boolean isPermissionGranted(Context context) {
    if (SDKUtil.isAtLeast(API.ANDROID_11)) {
      return Environment.isExternalStorageManager();
    } else {
      return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
              == PackageManager.PERMISSION_GRANTED
          && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
              == PackageManager.PERMISSION_GRANTED;
    }
  }

  private void checkDexingRes() {
    if (!Path.ANDROID_JAR.exists()) {
      BaseUtil.unzipFromAssets("dexing-res/android.zip", Path.RESOURCE_FOLDER.getAbsolutePath());
    }
    if (!Path.CORE_LAMDA_STUBS.exists()) {
      BaseUtil.unzipFromAssets(
          "dexing-res/core-lambda-stubs.zip", Path.RESOURCE_FOLDER.getAbsolutePath());
    }
  }

  public static class SVM extends ViewModel {

    private MutableLiveData<Boolean> canRequestStoragePermission = new MutableLiveData<>(false);

    private MutableLiveData<Boolean> canSelectFolder = new MutableLiveData<>(false);

    public LiveData<Boolean> canRequestStoragePermission() {
      return this.canRequestStoragePermission;
    }

    public void setCanRequestStoragePermissionState(boolean enabled) {
      this.canRequestStoragePermission.setValue(enabled);
    }

    public LiveData<Boolean> canSelectFolder() {
      return this.canSelectFolder;
    }

    public void setCanSelectFolder(boolean enabled) {
      this.canSelectFolder.setValue(enabled);
    }
  }
}
