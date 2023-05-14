package org.inksnow.ankh.core.api.config;

import com.google.gson.reflect.TypeToken;
import org.inksnow.ankh.core.api.ioc.IocLazy;
import org.inksnow.ankh.core.api.util.DcLazy;
import org.inksnow.ankh.core.api.util.IBuilder;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.function.Supplier;

public interface ConfigLoader {
  static @Nonnull Factory factory() {
    return $internal$actions$.factory.get();
  }

  static @Nonnull Builder builder() {
    return factory().builder();
  }

  ConfigSection load(@Nonnull String path);

  <T> T parse(@Nonnull String path, @Nonnull TypeToken<T> typeToken);

  <T> T parse(@Nonnull String path, @Nonnull Class<T> type);

  <T> ConfigTypeAdapter<? extends T> getAdapter(@Nonnull TypeToken<T> typeToken);

  <T> Supplier<T> getConstructor(@Nonnull TypeToken<? super T> type);

  interface Factory {
    @Nonnull
    Builder builder();
  }

  interface Builder extends IBuilder<Builder, ConfigLoader> {
    @Nonnull
    Builder baseDirectory(@Nonnull Path baseDirectory);

    @Nonnull
    Builder registerFactory(@Nonnull ConfigTypeAdapter.Factory<?> factory);

    <T> @Nonnull Builder registerUserImplementation(@Nonnull Class<T> base, @Nonnull Class<? extends T> impl);
  }

  class $internal$actions$ {
    private static DcLazy<Factory> factory = IocLazy.of(Factory.class);
  }
}
