package org.inksnow.ankh.logger.cloud;

import lombok.val;
import org.inksnow.ankh.cloud.AnkhCloudEnvironment;
import org.inksnow.ankh.cloud.AnkhCloudLoader;
import org.inksnow.ankh.cloud.bean.LogBean;
import org.inksnow.ankh.cloud.bean.ThrowableBean;
import org.inksnow.ankh.logger.util.LogBeanUtils;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;

public class AnkhCloudLogger extends AbstractLogger {
  private final String name;
  private final int logLevel;

  public AnkhCloudLogger(String name) {
    this.name = name;
    for (val entry : AnkhCloudLoader.levelMap().entrySet()) {
      if (name.startsWith(entry.getKey())) {
        this.logLevel = entry.getValue();
        return;
      }
    }
    this.logLevel = AnkhCloudEnvironment.WARN_INT;
  }

  @Override
  protected String getFullyQualifiedCallerName() {
    return AnkhCloudLogger.class.getName();
  }

  @Override
  protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {
    val bean = LogBean.builder()
        .time(System.currentTimeMillis())
        .level(level.toInt())
        .name(name)
        .marker(LogBeanUtils.dumpAllMarkerAsString(marker))
        .messagePattern(messagePattern)
        .arguments(LogBeanUtils.formatArguments(arguments))
        .throwable(ThrowableBean.fromInstance(throwable))
        .build();
    AnkhCloudLoader.submitLogBean(bean);
  }

  @Override
  public boolean isTraceEnabled() {
    return logLevel <= AnkhCloudEnvironment.TRACE_INT;
  }

  @Override
  public boolean isTraceEnabled(Marker marker) {
    return logLevel <= AnkhCloudEnvironment.TRACE_INT;
  }

  @Override
  public boolean isDebugEnabled() {
    return logLevel <= AnkhCloudEnvironment.DEBUG_INT;
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return logLevel <= AnkhCloudEnvironment.DEBUG_INT;
  }

  @Override
  public boolean isInfoEnabled() {
    return logLevel <= AnkhCloudEnvironment.INFO_INT;
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return logLevel <= AnkhCloudEnvironment.INFO_INT;
  }

  @Override
  public boolean isWarnEnabled() {
    return logLevel <= AnkhCloudEnvironment.WARN_INT;
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return logLevel <= AnkhCloudEnvironment.WARN_INT;
  }

  @Override
  public boolean isErrorEnabled() {
    return logLevel <= AnkhCloudEnvironment.ERROR_INT;
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return logLevel <= AnkhCloudEnvironment.ERROR_INT;
  }
}
