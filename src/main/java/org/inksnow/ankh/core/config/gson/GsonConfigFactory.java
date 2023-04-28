package org.inksnow.ankh.core.config.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.inksnow.ankh.core.api.config.ConfigExtension;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigSectionFactory;
import org.inksnow.ankh.core.api.config.ConfigSource;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.io.Reader;

@Singleton
public class GsonConfigFactory implements ConfigSectionFactory {
  private static final Gson gson = new GsonBuilder()
      .setLenient()
      .create();

  @Override
  public @Nonnull ConfigSection load(@Nonnull ConfigSource source, @Nonnull Reader reader) {
    return new GsonConfigSection(source, ConfigExtension.empty(), gson.fromJson(reader, JsonElement.class));
  }
}
