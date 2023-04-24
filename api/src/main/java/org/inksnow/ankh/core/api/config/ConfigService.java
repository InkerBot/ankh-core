package org.inksnow.ankh.core.api.config;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public interface ConfigService {
  @Nonnull
  ConfigSection readSectionFromPath(@Nonnull Path path);
}
