package ink.bgp.hcloader;

import ink.bgp.hcloader.archive.JarFileArchive;
import org.bukkit.plugin.java.JavaPlugin;
import org.inksnow.ankh.core.api.AnkhCoreLoader;
import org.inksnow.ankh.core.api.plugin.AnkhPluginContainer;
import org.inksnow.ankh.core.nbt.loader.CallSiteNbt;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AnkhCoreLoaderPlugin extends JavaPlugin implements AnkhCoreLoader {
  private static Object container;

  static {
    try {
      final Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
      theUnsafeField.setAccessible(true);
      final Unsafe unsafe = (Unsafe) theUnsafeField.get(null);

      final Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
      final MethodHandles.Lookup lookup = (MethodHandles.Lookup) unsafe.getObject(
          unsafe.staticFieldBase(implLookupField),
          unsafe.staticFieldOffset(implLookupField));

      final MethodHandle urlClassLoaderAddUrlHandle = lookup.findVirtual(URLClassLoader.class, "addURL", MethodType.methodType(void.class, URL.class));

      final URLClassLoader targetClassLoader = (URLClassLoader) AnkhCoreLoaderPlugin.class.getClassLoader();
      final File pluginFile = new File(AnkhCoreLoaderPlugin.class.getProtectionDomain().getCodeSource().getLocation().getFile());
      final JarFileArchive pluginArchive = new JarFileArchive(pluginFile);
      final LaunchedURLClassLoader launchedURLClassLoader = new LaunchedURLClassLoader(pluginArchive, new URL[0], targetClassLoader);

      load(urlClassLoaderAddUrlHandle, targetClassLoader, launchedURLClassLoader, pluginFile);

      final File[] ankhFsFiles = new File("plugins")
          .listFiles(file -> !file.isDirectory() && file.getName().endsWith(".ankhplugin"));

      for (final File ankhFsFile : ankhFsFiles) {
        load(urlClassLoaderAddUrlHandle, targetClassLoader, launchedURLClassLoader, ankhFsFile);
      }

      scanDelegateConfig(targetClassLoader, launchedURLClassLoader);

      CallSiteNbt.install(AnkhCoreLoaderPlugin.class);
      // AnkhCloudLoader.initial();

      final Class<?> containerClass = Class.forName(
          "org.inksnow.ankh.core.loader.AnkhPluginLoader",
          true,
          launchedURLClassLoader);

      final AnkhPluginContainer container = (AnkhPluginContainer) containerClass.getMethod("load")
          .invoke(null);
      AnkhCoreLoaderPlugin.container = container;
      container.callClinit();
    } catch (RuntimeException | Error e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  public AnkhCoreLoaderPlugin() {
    ((AnkhPluginContainer) container).callInit(this);
  }

  private static void scanDelegateConfig(
      final @NotNull URLClassLoader targetClassLoader,
      final @NotNull LaunchedURLClassLoader delegateClassLoader) throws IOException {
    final Enumeration<URL> delegateConfigUrls = targetClassLoader.findResources("META-INF/hcloader/delegateconfig");
    while (delegateConfigUrls.hasMoreElements()) {
      URL delegateConfigUrl = delegateConfigUrls.nextElement();
      try (InputStream in = delegateConfigUrl.openStream()) {
        LoadConfigEntry.load(in, delegateClassLoader);
      }
    }
  }

  private static URL packJarUrl(final File jarFile) throws MalformedURLException {
    return new URL("jar", "", -1, jarFile.toURI() + "!/");
  }

  private static void load(
      final MethodHandle urlClassLoaderAddUrlHandle,
      final URLClassLoader targetClassLoader,
      final LaunchedURLClassLoader launchedURLClassLoader,
      final File rawInjectFile) throws Throwable {
    urlClassLoaderAddUrlHandle.invokeExact(targetClassLoader, packJarUrl(rawInjectFile));
    try (final JarFile jarFile = new JarFile(rawInjectFile)) {
      final Enumeration<JarEntry> jarEntries = jarFile.entries();
      while (jarEntries.hasMoreElements()) {
        final JarEntry entry = jarEntries.nextElement();
        if (entry.getName().endsWith(".jar") && !entry.isDirectory()) {
          if (entry.getName().startsWith("META-INF/hcloader/embedded/")) {
            final String fileName = entry.getName().substring("META-INF/hcloader/embedded/".length());
            final Path embeddedPath = Paths.get("cache", "ankh-core", "embedded", fileName);
            try (final InputStream in = jarFile.getInputStream(entry)) {
              Files.copy(in, embeddedPath, StandardCopyOption.REPLACE_EXISTING);
            }
            urlClassLoaderAddUrlHandle.invokeExact(
                targetClassLoader,
                packJarUrl(embeddedPath.toFile()));
          } else if (entry.getName().startsWith("META-INF/hcloader/delegate/")) {
            final String fileName = entry.getName().substring("META-INF/hcloader/delegate/".length());
            final Path delegatePath = Paths.get("cache", "ankh-core", "delegate", fileName);
            try (final InputStream in = jarFile.getInputStream(entry)) {
              Files.copy(in, delegatePath, StandardCopyOption.REPLACE_EXISTING);
            }
            urlClassLoaderAddUrlHandle.invokeExact(
                (URLClassLoader) launchedURLClassLoader,
                packJarUrl(delegatePath.toFile()));
          }
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> RuntimeException throwImpl(final @NotNull Throwable e) throws T {
    throw (T) e;
  }

  @Override
  public void onLoad() {
    ((AnkhPluginContainer) container).callLoad();
  }

  @Override
  public void onEnable() {
    ((AnkhPluginContainer) container).callEnable();
  }

  @Override
  public void onDisable() {
    ((AnkhPluginContainer) container).callDisable();
  }
}
