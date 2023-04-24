package org.inksnow.ankh.core.plugin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.inksnow.ankh.core.api.AnkhCore;
import org.inksnow.ankh.core.api.plugin.AnkhBukkitPlugin;
import org.inksnow.ankh.core.api.plugin.AnkhPluginContainer;
import org.inksnow.ankh.core.api.plugin.AnkhPluginYml;
import org.inksnow.ankh.core.common.AnkhServiceLoaderImpl;
import org.inksnow.ankh.core.ioc.BridgerInjector;
import org.inksnow.ankh.loader.AnkhClassLoader;
import org.inksnow.ankh.loader.AnkhCoreLoaderPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

public class AnkhPluginContainerImpl implements AnkhPluginContainer {
  private final Logger logger;
  private final Class<? extends AnkhBukkitPlugin> pluginClass;
  private final AnkhClassLoader classLoader;
  private final PluginDescriptionFile descriptionFile;
  @Getter
  private final AnkhPluginYml pluginYml;

  private final ListenerSet clinitListeners = new ListenerSet();
  private final ListenerSet initListeners = new ListenerSet();
  private final ListenerSet loadListeners = new ListenerSet();
  private final ListenerSet enableListeners = new ListenerSet();
  private final ListenerSet disableListeners = new ListenerSet();

  @Getter
  private List<Class<? extends Module>> pluginModules = new ArrayList<>();
  @Getter
  private Injector injector;
  @Getter
  private AnkhBukkitPlugin plugin;

  public AnkhPluginContainerImpl(Class<? extends AnkhBukkitPlugin> pluginClass, AnkhClassLoader classLoader, PluginDescriptionFile descriptionFile, AnkhPluginYml pluginYml) {
    this.logger = LoggerFactory.getLogger(pluginYml.getName());
    this.pluginClass = pluginClass;
    this.classLoader = classLoader;
    this.descriptionFile = descriptionFile;
    this.pluginYml = pluginYml;
  }

  @Override
  public void onClinit(EventPriority priority, Runnable listener) {
    clinitListeners.register(priority, listener);
  }

  @Override
  public void onInit(EventPriority priority, Runnable listener) {
    initListeners.register(priority, listener);
  }

  @Override
  public void onLoad(EventPriority priority, Runnable listener) {
    loadListeners.register(priority, listener);
  }

  @Override
  public void onEnable(EventPriority priority, Runnable listener) {
    enableListeners.register(priority, listener);
  }

  @Override
  public void onDisable(EventPriority priority, Runnable listener) {
    disableListeners.register(priority, listener);
  }

  @Override
  public void callClinit() {
    clinitListeners.call();
  }

  @Override
  @SneakyThrows
  public void callInit(AnkhBukkitPlugin bukkitPlugin) {
    this.plugin = bukkitPlugin;
    val combineModule = Modules.combine(pluginModules.stream().map(this::createModule).toArray(Module[]::new));
    if (pluginClass == AnkhCoreLoaderPlugin.class) {
      this.injector = Guice.createInjector(
          combineModule,
          binder -> {
            binder.bind(AnkhCoreLoaderPlugin.class).toInstance((AnkhCoreLoaderPlugin) bukkitPlugin);
          }
      );
      AnkhCore.$internal$actions$.setInjector(new BridgerInjector(this.injector));
    } else {
      this.injector = ((AnkhPluginContainerImpl) AnkhCoreLoaderPlugin.container).injector.createChildInjector(
          combineModule,
          binder -> {
            binder.bind(AnkhPluginContainer.class).to(AnkhPluginContainerImpl.class);
            binder.bind(AnkhPluginContainerImpl.class).toInstance(this);
            binder.bind(AnkhBukkitPlugin.class).to(bukkitPlugin.getClass());
            binder.bind((Class) bukkitPlugin.getClass()).toInstance(bukkitPlugin);
          }
      );
    }
    AnkhServiceLoaderImpl.staticRegisterPlugin(pluginYml.getName(), this);
    initListeners.call();
  }

  @SneakyThrows
  private Module createModule(Class<? extends Module> moduleClass) {
    return moduleClass.getConstructor().newInstance();
  }

  @Override
  public void callLoad() {
    loadListeners.call();
  }

  @Override
  public void callEnable() {
    enableListeners.call();
  }

  @Override
  public void callDisable() {
    disableListeners.call();
  }

  public AnkhClassLoader classLoader() {
    return classLoader;
  }

  private static class ListenerSet {
    private static final AtomicLong loadIdAllocator = new AtomicLong();
    private boolean loaded = false;
    private Set<SortEntry> listeners = new TreeSet<>();

    public synchronized void register(EventPriority priority, Runnable listener) {
      if (loaded) {
        throw new IllegalStateException("ListenerSet have been called");
      }
      listeners.add(new SortEntry(listener, priority, loadIdAllocator.getAndIncrement()));
    }

    public synchronized void call() {
      if (loaded) {
        throw new IllegalStateException("ListenerSet have been called");
      }
      loaded = true;
      for (val entry : listeners) {
        entry.listener.run();
      }
    }
  }

  @AllArgsConstructor
  private static class SortEntry implements Comparable<SortEntry> {
    private final Runnable listener;
    private final EventPriority priority;
    private final long loadId;

    @Override
    public int compareTo(SortEntry other) {
      val orderCompareResult = priority.compareTo(other.priority);
      if (orderCompareResult != 0) {
        return orderCompareResult;
      }
      return Long.compare(loadId, other.loadId);
    }

    @Override
    public int hashCode() {
      return Long.hashCode(loadId);
    }
  }
}
