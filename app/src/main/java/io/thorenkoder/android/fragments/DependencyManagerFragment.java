package io.thorenkoder.android.fragments;

import static eup.dependency.haven.repository.Repository.Manager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.transition.MaterialSharedAxis;
import eup.dependency.haven.api.CachedLibrary;
import eup.dependency.haven.callback.DependencyResolutionCallback;
import eup.dependency.haven.callback.DownloadCallback;
import eup.dependency.haven.model.Coordinates;
import eup.dependency.haven.model.Dependency;
import eup.dependency.haven.model.Pom;
import eup.dependency.haven.parser.PomParser;
import eup.dependency.haven.repository.LocalStorageFactory;
import eup.dependency.haven.repository.RemoteRepository;
import eup.dependency.haven.resolver.DependencyResolver;
import io.thorenkoder.android.SharedPreferenceKeys;
import io.thorenkoder.android.api.library.LocalLibraryManager;
import io.thorenkoder.android.databinding.FragmentDependencyManagerBinding;
import io.thorenkoder.android.logging.LogAdapter;
import io.thorenkoder.android.logging.LogViewModel;
import io.thorenkoder.android.logging.Logger;
import io.thorenkoder.android.util.BaseUtil.Path;
import io.thorenkoder.android.util.PreferencesUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;

public class DependencyManagerFragment extends Fragment
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  public static final String TAG = DependencyManagerFragment.class.getSimpleName();
  private FragmentDependencyManagerBinding binding;
  private Logger logger;
  private LogAdapter logAdapter;
  private LogViewModel model;
  private Coordinates coordinates = null;
  private static String time = null;
  private LocalStorageFactory storageFactory;
  private LocalLibraryManager libraryManager;
  private DependencyResolver resolver;
  private boolean skipInnerDependencies = false;

  public static DependencyManagerFragment newInstance() {
    return new DependencyManagerFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    PreferencesUtils.getDefaultPreferences().registerOnSharedPreferenceChangeListener(this);
    PreferencesUtils.getPrivatePreferences().registerOnSharedPreferenceChangeListener(this);
    setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, false));
    setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, true));
    model = new ViewModelProvider(this).get(LogViewModel.class);
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup viewgroup, Bundle savedInstanceState) {
    binding = FragmentDependencyManagerBinding.inflate(inflater, viewgroup, false);
    setupRecyclerView();
    updateSwitch();
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    binding.skipDependencySwitch.setOnCheckedChangeListener(
        (button, isChecked) -> updateSkipDependencyPreferences(isChecked));
    binding.downloadButton.setOnClickListener(
        v -> {
          clearLogs();
          resolveDependencies();
        });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    this.binding = null;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    PreferencesUtils.getDefaultPreferences().unregisterOnSharedPreferenceChangeListener(this);
    PreferencesUtils.getPrivatePreferences().unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
    switch (key) {
      case SharedPreferenceKeys.KEY_SKIP_SUB_DEPENDENCIES:
        updateSwitch();
        break;
    }
  }

  private void updateSwitch() {
    boolean isChecked =
        PreferencesUtils.getPrivatePreferences()
            .getBoolean(SharedPreferenceKeys.KEY_SKIP_SUB_DEPENDENCIES, false);
    if (isChecked) {
      skipInnerDependencies = true;
    } else {
      skipInnerDependencies = false;
    }
    binding.skipDependencySwitch.setChecked(isChecked);
  }

  private void updateSkipDependencyPreferences(boolean isChecked) {
    PreferencesUtils.getPrivatePreferences()
        .edit()
        .putBoolean(SharedPreferenceKeys.KEY_SKIP_SUB_DEPENDENCIES, isChecked)
        .apply();
  }

  private void setupRecyclerView() {
    logAdapter = new LogAdapter();
    storageFactory = new LocalStorageFactory();
    File cacheDirectory = requireContext().getExternalFilesDir("cache");
    storageFactory.setCacheDirectory(cacheDirectory);
    String libSaveDir =
        PreferencesUtils.getDefaultPreferences()
            .getString(SharedPreferenceKeys.KEY_LIBRARY_MANAGER, "");
    libraryManager = new LocalLibraryManager(storageFactory, new File(libSaveDir));

    libraryManager.setCompileResourcesClassPath(Path.ANDROID_JAR, Path.CORE_LAMDA_STUBS);
    logger = new Logger();
    logger.attach(this);

    binding.logRecyclerview.setLayoutManager(new LinearLayoutManager(requireActivity()));
    binding.logRecyclerview.setAdapter(logAdapter);

    model
        .getLogs()
        .observe(
            this,
            data -> {
              logAdapter.submitList(data);
              scrollToLastItem();
            });
  }

  private void clearLogs() {
    if (logger != null) {
      logger.clear();
      logAdapter.notifyDataSetChanged();
    }
  }

  private void scrollToLastItem() {
    int itemCount = logAdapter.getItemCount();
    if (itemCount > 0) {
      binding.logRecyclerview.scrollToPosition(itemCount - 1);
    }
  }

  private void resolveDependencies() {
    try {
      coordinates = Coordinates.valueOf(binding.tilDepName.getEditText().getText().toString());
    } catch (Exception e) {
      logger.e("ERROR", e.getMessage());
    }
    resolver = new DependencyResolver(storageFactory, coordinates);
    storageFactory.attach(resolver);

    configureRepositories(resolver, logger);

    resolver.skipInnerDependencies(skipInnerDependencies);

    getActivity()
        .runOnUiThread(
            () -> {
              resolver.resolve(
                  new DependencyResolutionCallback() {
                    @Override
                    public void onDependenciesResolved(
                        String message, List<Dependency> resolvedDependencies, long totalTime) {
                      int resolutionSize = resolvedDependencies.size();
                      // additional info
                      StringBuilder sb = new StringBuilder();
                      sb.append("[");
                      resolvedDependencies.forEach(
                          dependency -> {
                            String artifact = dependency.toString();
                            sb.append("\n");
                            sb.append("> ");
                            sb.append(artifact);
                          });
                      sb.append("]");
                      logger.p("Resolved Dependencies", sb.toString());

                      StringBuilder sbt = new StringBuilder();
                      sbt.append("\n");
                      double totalTimeSeconds = totalTime / 1000.0;
                      sbt.append(
                          "Resolved dependencies in "
                              + String.format("%.3f", totalTimeSeconds)
                              + "s");
                      sbt.append("\n");
                      String pluraled = (resolutionSize == 1) ? " dependency" : " dependencies";
                      sbt.append("Successfully resolved " + resolutionSize + pluraled);
                      logger.p("SUMMARY", sbt.toString());
                      // add resolved dependencies to pom for download initilization
                      Pom resolvedPom = new Pom();
                      resolvedPom.setDependencies(resolvedDependencies);
                      storageFactory.downloadLibraries(resolvedPom);
                    }

                    @Override
                    public void onDependencyNotResolved(
                        String message, List<Dependency> unresolvedDependencies) {
                      // TODO: Handle
                    }

                    @Override
                    public void info(String message) {
                      logger.p("INFO", message);
                    }

                    @Override
                    public void verbose(String message) {
                      logger.d("VERBOSE", message);
                    }

                    @Override
                    public void error(String message) {
                      logger.e("ERROR", message);
                    }

                    @Override
                    public void warning(String message) {
                      logger.w("WARNING", message);
                    }
                  });

              storageFactory.setDownloadCallback(
                  new DownloadCallback() {
                    @Override
                    public void info(String message) {
                      logger.p("INFO", message);
                    }

                    @Override
                    public void error(String message) {
                      logger.e("ERROR", message);
                    }

                    @Override
                    public void warning(String message) {
                      logger.w("WARNING", message);
                    }

                    @Override
                    public void done(List<CachedLibrary> cachedLibraryList) {
                      libraryManager.copyCachedLibrary(cachedLibraryList);
                    }
                  });

              libraryManager.setTaskListener(
                  new LocalLibraryManager.TaskListener() {
                    @Override
                    public void info(String message) {
                      logger.p("INFO", message);
                    }

                    @Override
                    public void error(String message) {
                      logger.e("ERROR", message);
                    }
                  });
            });
  }

  @VisibleForTesting
  private void parsePom() {
    PomParser parser = new PomParser();
    InputStream inputStream = getPomStream(binding.tilDepName.getEditText().getText().toString());
    Pom parsedPom = new Pom();
    try {
      parsedPom = parser.parse(inputStream);
      if (parsedPom == null) {
        String ne =
            "Failed to parse POM for "
                + parsedPom.getCoordinates().toString()
                + " because parsedPom is null";
        logger.e("ERROR", ne);
      } else {
        logParsedPom(parsedPom);
      }
      inputStream.close();
    } catch (Exception e) {
      String pe =
          "Failed to parse POM for "
              + parsedPom.getCoordinates().toString()
              + " due to "
              + e.getMessage();
      logger.e("ERROR", pe);
    }
  }

  public static InputStream getPomStream(String dirPath) {
    try {
      File localFile = new File(dirPath);
      return new FileInputStream(localFile);
    } catch (IOException e) {
      throw new IllegalArgumentException("Error occured :" + e.getMessage());
    }
  }

  private void logParsedPom(Pom parsedPom) {
    String parsedPomText =
        "Coordinates: "
            + parsedPom.getCoordinates()
            + "\n"
            + "Dependencies: "
            + parsedPom.getDependencies()
            + "\n"
            + "Excludes: "
            + parsedPom.getExclusions()
            + "\n"
            + "Managed Deps: "
            + parsedPom.getManagedDependencies()
            + "\n"
            + "Pom Parent: "
            + parsedPom.getParent()
            + "\n"
            + "Parsed POM in "
            + PomParser.getParsingDuration();
    logger.p("INFO", parsedPomText);
  }

  private void configureRepositories(DependencyResolver resolver, Logger logger) {

    boolean useDefault = false;

    if (Path.REPOSITORIES_JSON.exists()) {
      try {
        List<RemoteRepository> repositories =
            Manager.readRemoteRepositoryConfig(Path.REPOSITORIES_JSON, false);
        for (RemoteRepository repository : repositories) {
          resolver.addRepository(repository);
        }
      } catch (IOException | JSONException ignored) {
        useDefault = true;
      }
    } else {
      useDefault = true;
    }

    if (useDefault) {
      for (RemoteRepository repository : Manager.DEFAULT_REMOTE_REPOSITORIES) {
        resolver.addRepository(repository);
      }
      logger.d(
          "INFO",
          "Custom Repositories configuration file couldn't be read from. Using default repositories for now");
      try {
        // write default to file
        FileUtils.write(Path.REPOSITORIES_JSON, Manager.generateJSON(), StandardCharsets.UTF_8);
      } catch (IOException e) {
        logger.e("ERROR", "Failed to create " + Path.REPOSITORIES_JSON.getName() + e.getMessage());
      }
    }
  }
}
