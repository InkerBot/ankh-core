package org.inksnow.ankh.core.loader;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;
import org.inksnow.ankh.core.api.AnkhCore;
import org.inksnow.ankh.core.api.AnkhCoreLoader;
import org.inksnow.ankh.core.api.plugin.AnkhPluginContainer;
import org.inksnow.ankh.core.common.config.AnkhConfig;
import org.inksnow.ankh.core.ioc.BridgerInjector;
import org.inksnow.asteroid.AkEnvironment;
import org.inksnow.asteroid.AkLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

public class AnkhPluginLoader implements AnkhPluginContainer {
  private static final AnkhPluginLoader instance = new AnkhPluginLoader();
  private final ListenerSet clinitListeners = new ListenerSet();
  private final ListenerSet initListeners = new ListenerSet();
  private final ListenerSet loadListeners = new ListenerSet();
  private final ListenerSet enableListeners = new ListenerSet();
  private final ListenerSet disableListeners = new ListenerSet();
  @Getter
  private final List<Class<? extends Module>> pluginModules = new ArrayList<>();
  @Getter
  private Injector injector;
  @Getter
  private AnkhCoreLoader plugin;

  private AnkhPluginLoader(){
    //
  }

  public static AnkhPluginLoader instance() {
    return instance;
  }

  public static AnkhPluginContainer load(AnkhCoreLoader pluginInstance) {
    val ankhCoreEnvironment = AkLoader.getOrCreateEnvironment("ankh-core-impl");
    val scannerInjector = Guice.createInjector(binder -> {
      binder.bind(Plugin.class).to(AnkhCoreLoader.class);
      binder.bind(AnkhCoreLoader.class).toInstance(pluginInstance);
      binder.bind(AnkhConfig.class).toProvider(AnkhConfig.provider());
      binder.bind(AkEnvironment.class).toInstance(ankhCoreEnvironment);
      binder.bind(ClassLoader.class).toInstance(ankhCoreEnvironment.classLoader());
    });
    scannerInjector.getInstance(AnkhPluginScanner.class).run();
    return AnkhPluginLoader.instance();
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
  public void callInit(AnkhCoreLoader plugin) {
    this.plugin = plugin;
    val modules = new Module[pluginModules.size()];
    for (int i = 0; i < pluginModules.size(); i++) {
      modules[i] = createModule(pluginModules.get(i));
    }
    this.injector = Guice.createInjector(modules);
    AnkhCore.$internal$actions$.setInjector(new BridgerInjector(injector));
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

  private static class ListenerSet {
    private static final AtomicLong loadIdAllocator = new AtomicLong();
    private final Set<SortEntry> listeners = new TreeSet<>();
    private boolean loaded = false;

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
