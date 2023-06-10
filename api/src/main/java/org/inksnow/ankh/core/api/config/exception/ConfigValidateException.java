package org.inksnow.ankh.core.api.config.exception;

import java.util.List;

public class ConfigValidateException extends ConfigException {

  public ConfigValidateException(List<Entry> entries) {
    super(entries, "Failed to validate config");
  }
}
