package org.inksnow.ankh.core.api.config.exception;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigValidateException extends ConfigException {
  private final List<ConfigException.Entry> entries;

  public ConfigValidateException(List<Entry> entries) {
    super("Failed to validate config");
    this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
  }

  public List<Entry> entries() {
    return entries;
  }

  @Override
  public String getMessage() {
    StringBuilder builder = new StringBuilder();
    builder.append(super.getMessage()).append("\n");
    for (Entry entry : entries) {
      builder.append("\t-> (");
      Path sourceFile = entry.source().file();
      if (sourceFile != null) {
        builder.append(sourceFile.getFileName());
      }else{
        builder.append("unknown");
      }
      builder.append(")")
          .append(entry.source().path())
          .append("\t")
          .append(entry.source())
          .append("\n\t\t")
          .append(entry.message());
    }
    builder.append("\n");
    return builder.toString();
  }
}
