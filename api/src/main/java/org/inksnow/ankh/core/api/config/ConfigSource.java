package org.inksnow.ankh.core.api.config;

import org.inksnow.ankh.core.api.ioc.IocLazy;
import org.inksnow.ankh.core.api.util.DcLazy;
import org.inksnow.ankh.core.api.util.IBuilder;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

public interface ConfigSource {
  static @Nonnull Factory factory() {
    return $internal$actions$.factory.get();
  }

  static @Nonnull Builder builder() {
    return factory().builder();
  }

  @Nonnull
  String path();

  @Nonnull
  String fileName();

  int lineNumber();

  @Nonnull
  String description();

  @Nonnull
  ConfigSource.Builder toBuilder();

  interface Factory {
    @Nonnull
    Builder builder();
  }

  interface Builder extends IBuilder<Builder, ConfigSource> {
    @Nonnull
    Builder description(@Nonnull String description);

    @Nonnull
    Builder description(@Nonnull Callable<String> supplier);

    @Nonnull
    Builder path(@Nonnull String path);

    @Nonnull
    Builder path(@Nonnull Callable<String> supplier);

    @Nonnull
    Builder fileName(@Nonnull String fileName);

    @Nonnull
    Builder fileName(@Nonnull Callable<String> supplier);

    @Nonnull
    Builder lineNumber(int lineNumber);

    @Nonnull
    Builder lineNumber(@Nonnull Callable<Integer> supplier);
  }

  class $internal$actions$ {
    private static final DcLazy<Factory> factory = IocLazy.of(Factory.class);
  }
}
