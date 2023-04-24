package org.inksnow.ankh.core.api.config;

import javax.annotation.Nonnull;
import java.io.Reader;

public interface ConfigSectionFactory {
  @Nonnull
  ConfigSection load(@Nonnull ConfigSource source, @Nonnull Reader reader);
}
