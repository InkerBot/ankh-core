package org.inksnow.ankh.logger.simple;

import org.inksnow.ankh.logger.delegate.DelegateLogger;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class SimpleNamePrefixLoggerFactory implements ILoggerFactory {
  private final String prefix;
  private final ILoggerFactory delegate;

  public SimpleNamePrefixLoggerFactory(String prefix, ILoggerFactory delegate) {
    this.prefix = prefix;
    this.delegate = delegate;
  }

  @Override
  public Logger getLogger(String name) {
    if (name.contains(".")) {
      name = name.substring(name.lastIndexOf('.') + 1);
    }
    if (name.contains("$")) {
      name = name.substring(0, name.indexOf('$'));
    }
    return new DelegateLogger(name, delegate.getLogger(prefix + name));
  }
}
