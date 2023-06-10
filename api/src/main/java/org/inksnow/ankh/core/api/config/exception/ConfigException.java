package org.inksnow.ankh.core.api.config.exception;

import org.inksnow.ankh.core.api.config.ConfigSource;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class ConfigException extends RuntimeException {
  private final List<Entry> entries;

  public ConfigException(List<Entry> entries) {
    this.entries = entries;
  }

  public ConfigException(List<Entry> entries, String message) {
    super(message);
    this.entries = entries;
  }

  public ConfigException(List<Entry> entries, String message, Throwable cause) {
    super(message, cause);
    this.entries = entries;
  }

  public ConfigException(List<Entry> entries, Throwable cause) {
    super(cause);
    this.entries = entries;
  }

  public ConfigException(List<Entry> entries, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.entries = entries;
  }

  public ConfigException() {
    this.entries = Collections.emptyList();
  }

  public ConfigException(String message) {
    super(message);
    this.entries = Collections.emptyList();
  }

  public ConfigException(String message, Throwable cause) {
    super(message, cause);
    this.entries = Collections.emptyList();
  }

  public ConfigException(Throwable cause) {
    super(cause);
    this.entries = Collections.emptyList();
  }

  public ConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.entries = Collections.emptyList();
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
      } else {
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

  public static class Entry {
    private final ConfigSource source;
    private final String message;

    public Entry(ConfigSource source, String message) {
      this.source = source;
      this.message = message;
    }

    public ConfigSource source() {
      return source;
    }

    public String message() {
      return message;
    }
  }
}
