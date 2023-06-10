package org.inksnow.ankh.logger.cloud;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class AnkhCloudLoggerFactory implements ILoggerFactory {
  @Override
  public Logger getLogger(String name) {
    return new AnkhCloudLogger(name);
  }
}
