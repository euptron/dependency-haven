package io.thorenkoder.android.api.library;

import androidx.appcompat.resources.*;
import com.android.tools.r8.CompilationFailedException;
import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.D8;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.OutputMode;
import eup.dependency.haven.api.CachedLibrary;
import eup.dependency.haven.repository.StorageFactory;
import io.thorenkoder.android.api.exception.DexFailedException;
import io.thorenkoder.android.util.BaseUtil;
import io.thorenkoder.android.util.SDKUtil;
import io.thorenkoder.android.util.SDKUtil.API;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import mod.agus.jcoderz.dx.command.dexer.Main;
import mod.hey.studios.lib.JarCheck;
import org.apache.commons.io.FileUtils;

/**
 * A local library manager for Sketchware Pro.
 *
 * @author EUP
 */
public class LocalLibraryManager {

  public interface TaskListener {
    void info(String message);

    void error(String message);
  }

  private TaskListener listener;
  private File newDirectory;
  private StorageFactory storageFactory;
  private File androidJar;
  private File lambdaStubs;

  private Map<String, File> localLibraryJar = new HashMap<>();

  public LocalLibraryManager(StorageFactory storageFactory, File newDirectory) {
    this.storageFactory = storageFactory;
    this.newDirectory = newDirectory;
  }

  public void setCompileResourcesClassPath(File androidJar, File lambdaStubs) {
    this.androidJar = androidJar;
    this.lambdaStubs = lambdaStubs;
  }

  public void setTaskListener(TaskListener listener) {
    this.listener = listener;
  }

  public void copyCachedLibrary(List<CachedLibrary> cachedLibraries) {
    // Recursively copy all cached libraries to the new directory
    for (CachedLibrary library : cachedLibraries) {

      LocalLibrary localLibrary = new LocalLibrary();
      // set local library folder name from coordinates
      localLibrary.setLibraryName(library.getLibraryPom().getCoordinates().toString());
      localLibrary.setLibraryPom(library.getLibraryPom());
      localLibrary.setSourcePath(newDirectory);
      localLibrary.setSourceFile(library.getSourceFile());

      File localLibraryFolder = localLibrary.getSourcePath();

      try {
        // TODO: Handle auto updates
        if (!localLibraryFolder.exists()) {
          FileUtils.forceMkdir(localLibraryFolder);
        }
      } catch (IOException e) {
        listener.error("Failed to create the destination directory: " + e.getMessage());
        continue;
      }

      // Check if dex file exists, and skip if it does
      File dexFile = new File(localLibraryFolder, "classes.dex");
      if (dexFile.exists()) {
        listener.info(
            "Dex file already exists for " + localLibrary.getLibraryName() + ". Skipping.");
        continue;
      }

      try {
        if (localLibrary.isJar()) {
          listener.info("Copying " + localLibrary.getSourceFile() + " to " + localLibraryFolder);
          FileUtils.copyFileToDirectory(localLibrary.getSourceFile(), localLibraryFolder);
          File file = new File(localLibraryFolder, localLibrary.getSourceFile().getName());
          // rename library jar to classes.jar
          file.renameTo(new File(localLibraryFolder, "classes.jar"));
        } else if (localLibrary.isAar()) {
          listener.info("Decompressing " + localLibrary.getSourceFile().getName());
          BaseUtil.unzip(
              localLibrary.getSourceFile().getAbsolutePath(), localLibraryFolder.getAbsolutePath());
          // write configuration file
          File config = new File(localLibraryFolder, "config");
          FileUtils.write(config, localLibrary.findPackageName(), StandardCharsets.UTF_8);
          localLibrary.deleteUnnecessaryFiles();
        } else {
          listener.error("File path " + localLibraryFolder + " does not exist");
        }
        localLibraryJar.put(localLibrary.getLibraryName(), localLibrary.getJarFile());
      } catch (IOException e) {
        listener.error("Failed to copy the file: " + e.getMessage());
        // skip
        continue;
      }
    }
    // Start dexing
    listener.info("Dexing libraries");
    for (Entry<String, File> entry : localLibraryJar.entrySet()) {
      try {
        compileJar(entry.getValue());
      } catch (IOException | CompilationFailedException | DexFailedException e) {
        listener.error("Dexing task failed for " + entry.getKey() + ": " + e.getMessage());
        // skip
        continue;
      }
    }
    listener.info("Download Complete");
  }

  private void compileJar(File jarFile) throws CompilationFailedException, IOException {
    if (SDKUtil.isAtLeast(API.ANDROID_8)) {
      D8Command command =
          D8Command.builder()
              .setIntermediate(true)
              .setMode(CompilationMode.RELEASE)
              .addLibraryFiles(getCompileResources())
              .addProgramFiles(jarFile.toPath())
              .setOutput(jarFile.getParentFile().toPath(), OutputMode.DexIndexed)
              .build();
      listener.info("Dexing jar " + jarFile.getParentFile().getName() + " using D8");
      D8.run(command);
    } else {
      if (!JarCheck.checkJar(jarFile.getAbsolutePath(), 41, 51)) {
        Main.clearInternTables();
        listener.info("Dexing jar " + jarFile.getParentFile().getName() + " using DX");
        Main.main(
            new String[] {
              "--debug",
              "--verbose",
              "--multi-dex",
              "--output=" + jarFile.getParentFile().getAbsolutePath(),
              jarFile.getAbsolutePath()
            });
      } else {
        throw new DexFailedException(
            jarFile.getParentFile().getName()
                + " uses java >= 1.8 features and can not be compiled to jar! upgrade to a device with android version >= 8");
      }
    }
  }

  private List<Path> getCompileResources() {
    List<Path> resources = new ArrayList<>();
    resources.add(lambdaStubs.toPath());
    resources.add(androidJar.toPath());
    return resources;
  }
}
