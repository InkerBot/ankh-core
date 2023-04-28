package org.inksnow.ankh.core.api.config;

import org.inksnow.ankh.core.api.ioc.IocLazy;
import org.inksnow.ankh.core.api.util.DcLazy;

import javax.annotation.Nonnull;
import java.util.List;

public interface ConfigExtension {
  @Nonnull
  List<String> includeList();

  @Nonnull ConfigExtension include(@Nonnull String fileName);

  @Nonnull
  static Factory factory(){
    return $internal$actions$.factory.get();
  }

  @Nonnull
  static ConfigExtension empty(){
    return factory().empty();
  }

  interface Factory {
    @Nonnull ConfigExtension empty();
  }

  class $internal$actions$ {
    private static final DcLazy<Factory> factory = IocLazy.of(Factory.class);
  }
}
