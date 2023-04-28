package org.inksnow.ankh.core.config;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigService;
import org.inksnow.ankh.core.api.config.ConfigSource;
import org.inksnow.ankh.core.api.ioc.IocLazy;
import org.inksnow.ankh.core.api.util.DcLazy;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConfigLoader {
  private final DcLazy<ConfigService> configService = IocLazy.of(ConfigService.class);
  @Getter
  private final Path coreConfigPath;
  @Getter
  private final Path baseDirectoryPath;
  private final Map<String, ConfigSection> sectionByPath;

  public ConfigLoader(Path coreConfigPath) {
    this.coreConfigPath = coreConfigPath;
    this.baseDirectoryPath = coreConfigPath.getParent();
    this.sectionByPath = new HashMap<>();
  }

  private ConfigSection loadPath(ConfigSource source, String path){
    Path baseDir = source == null ? null : source.file().getParent();
    if(baseDir == null){
      baseDir = baseDirectoryPath;
    }
    val targetPath = baseDir.resolve(path);
    val identifyPath = baseDir.relativize(targetPath).toString();
    return sectionByPath.computeIfAbsent(identifyPath, it->
      configService.get().readSectionFromPath(targetPath)
    );
  }

  public ConfigSection loadCore(){
    return load(loadPath(null, coreConfigPath.getFileName().toString()));
  }

  public ConfigSection load(ConfigSection coreSection){
    if (coreSection.extension().includeList().isEmpty()) {
      return new LinkedConfigSection(this, Collections.singletonList(coreSection));
    }else{
      return new LinkedConfigSection(this, ImmutableList.<ConfigSection>builder()
          .add(coreSection)
          .addAll(coreSection.extension()
              .includeList()
              .stream()
              .map(it->loadPath(coreSection.source() ,it))
              .filter(Objects::nonNull)
              .iterator())
          .build()
          .reverse()
      );
    }
  }
}
