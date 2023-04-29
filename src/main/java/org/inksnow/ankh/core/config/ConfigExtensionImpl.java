package org.inksnow.ankh.core.config;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.inksnow.ankh.core.api.config.ConfigExtension;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class ConfigExtensionImpl implements ConfigExtension {
  @Getter
  private final @Nonnull List<String> includeList;

  @Override
  public @Nonnull ConfigExtension include(@Nonnull String fileName) {
    return new ConfigExtensionImpl(
        ImmutableList.<String>builder()
            .addAll(includeList)
            .add(fileName)
            .build()
    );
  }

  @Singleton
  public static class Factory implements ConfigExtension.Factory {
    private static final ConfigExtension EMPTY = new ConfigExtensionImpl(Collections.emptyList());

    @Override
    public @Nonnull ConfigExtension empty() {
      return EMPTY;
    }
  }
}
