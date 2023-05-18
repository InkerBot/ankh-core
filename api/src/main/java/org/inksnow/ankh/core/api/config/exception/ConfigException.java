package org.inksnow.ankh.core.api.config.exception;

import org.inksnow.ankh.core.api.config.ConfigSource;

public class ConfigException extends RuntimeException {
  public ConfigException() {
  }

  public ConfigException(String message) {
    super(message);
  }

  public ConfigException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConfigException(Throwable cause) {
    super(cause);
  }

  public ConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
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
