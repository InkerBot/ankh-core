package org.inksnow.ankh.core.ioc;

import net.kyori.adventure.key.Key;
import org.inksnow.ankh.core.api.AnkhServiceLoader;
import org.inksnow.ankh.core.api.util.DcLazy;

import javax.annotation.Nonnull;
import javax.inject.Provider;
import java.util.List;
import java.util.concurrent.Callable;

public final class SpiProvider<T> implements Provider<T> {
  private final DcLazy<T> instance;

  private SpiProvider(Callable<T> supplier) {
    this.instance = DcLazy.of(supplier);
  }

  public static <T> @Nonnull SpiProvider<T> service(@Nonnull Class<T> serviceClass) {
    return new SpiProvider<>(() -> AnkhServiceLoader.service(serviceClass));
  }

  public static <T> @Nonnull SpiProvider<List<T>> serviceList(@Nonnull Class<T> serviceClass) {
    return new SpiProvider<>(() -> AnkhServiceLoader.serviceList(serviceClass));
  }

  public static <T> @Nonnull SpiProvider<T> loadService(@Nonnull String key, @Nonnull Class<T> clazz) {
    return new SpiProvider<>(() -> AnkhServiceLoader.loadService(key, clazz));
  }

  static <T> @Nonnull SpiProvider<T> loadService(@Nonnull Key key, @Nonnull Class<T> clazz) {
    return new SpiProvider<>(() -> AnkhServiceLoader.loadService(key, clazz));
  }

  @Override
  public T get() {
    return instance.get();
  }
}
