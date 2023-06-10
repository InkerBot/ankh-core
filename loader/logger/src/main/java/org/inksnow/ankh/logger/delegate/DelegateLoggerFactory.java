package org.inksnow.ankh.logger.delegate;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DelegateLoggerFactory implements ILoggerFactory {
  private final ILoggerFactory[] delegate;
  private final ConcurrentMap<String, Logger> loggerMap = new ConcurrentHashMap<>();

  public DelegateLoggerFactory(ILoggerFactory... delegate) {
    this.delegate = delegate;
  }

  @Override
  public Logger getLogger(String name) {
    return loggerMap.computeIfAbsent(name, this::createLogger);
  }

  private Logger createLogger(String name) {
    Logger[] delegateLogger = new Logger[delegate.length];
    for (int i = 0; i < delegate.length; i++) {
      delegateLogger[i] = delegate[i].getLogger(name);
    }
    return new DelegateLogger(name, delegateLogger);
  }
}
