package org.inksnow.ankh.logger.delegate;

import org.slf4j.Logger;
import org.slf4j.Marker;

public class DelegateLogger implements Logger {
  private final String name;
  private final Logger[] delegate;

  public DelegateLogger(String name, Logger... delegate) {
    this.name = name;
    this.delegate = delegate;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isTraceEnabled() {
    for (Logger delegate : delegate) {
      if (delegate.isTraceEnabled()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isTraceEnabled(Marker marker) {
    for (Logger delegate : delegate) {
      if (delegate.isTraceEnabled(marker)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void trace(String msg) {
    for (Logger delegate : delegate) {
      delegate.trace(msg);
    }
  }

  @Override
  public void trace(String format, Object arg) {
    for (Logger delegate : delegate) {
      delegate.trace(format, arg);
    }
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    for (Logger delegate : delegate) {
      delegate.trace(format, arg1, arg2);
    }
  }

  @Override
  public void trace(String format, Object... arguments) {
    for (Logger delegate : delegate) {
      delegate.trace(format, arguments);
    }
  }

  @Override
  public void trace(String msg, Throwable t) {
    for (Logger delegate : delegate) {
      delegate.trace(msg, t);
    }
  }

  @Override
  public void trace(Marker marker, String msg) {
    for (Logger delegate : delegate) {
      delegate.trace(marker, msg);
    }
  }

  @Override
  public void trace(Marker marker, String format, Object arg) {
    for (Logger delegate : delegate) {
      delegate.trace(marker, format, arg);
    }
  }

  @Override
  public void trace(Marker marker, String format, Object arg1, Object arg2) {
    for (Logger delegate : delegate) {
      delegate.trace(marker, format, arg1, arg2);
    }
  }

  @Override
  public void trace(Marker marker, String format, Object... argArray) {
    for (Logger delegate : delegate) {
      delegate.trace(marker, format, argArray);
    }
  }

  @Override
  public void trace(Marker marker, String msg, Throwable t) {
    for (Logger delegate : delegate) {
      delegate.trace(marker, msg, t);
    }
  }

  @Override
  public boolean isDebugEnabled() {
    for (Logger delegate : delegate) {
      if (delegate.isDebugEnabled()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    for (Logger delegate : delegate) {
      if (delegate.isDebugEnabled(marker)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void debug(String msg) {
    for (Logger delegate : delegate) {
      delegate.debug(msg);
    }
  }

  @Override
  public void debug(String format, Object arg) {
    for (Logger delegate : delegate) {
      delegate.debug(format, arg);
    }
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    for (Logger delegate : delegate) {
      delegate.debug(format, arg1, arg2);
    }
  }

  @Override
  public void debug(String format, Object... arguments) {
    for (Logger delegate : delegate) {
      delegate.debug(format, arguments);
    }
  }

  @Override
  public void debug(String msg, Throwable t) {
    for (Logger delegate : delegate) {
      delegate.debug(msg, t);
    }
  }

  @Override
  public void debug(Marker marker, String msg) {
    for (Logger delegate : delegate) {
      delegate.debug(marker, msg);
    }
  }

  @Override
  public void debug(Marker marker, String format, Object arg) {
    for (Logger delegate : delegate) {
      delegate.debug(marker, format, arg);
    }
  }

  @Override
  public void debug(Marker marker, String format, Object arg1, Object arg2) {
    for (Logger delegate : delegate) {
      delegate.debug(marker, format, arg1, arg2);
    }
  }

  @Override
  public void debug(Marker marker, String format, Object... arguments) {
    for (Logger delegate : delegate) {
      delegate.debug(marker, format, arguments);
    }
  }

  @Override
  public void debug(Marker marker, String msg, Throwable t) {
    for (Logger delegate : delegate) {
      delegate.debug(marker, msg, t);
    }
  }

  @Override
  public boolean isInfoEnabled() {
    for (Logger delegate : delegate) {
      if (delegate.isInfoEnabled()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    for (Logger delegate : delegate) {
      if (delegate.isInfoEnabled(marker)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void info(String msg) {
    for (Logger delegate : delegate) {
      delegate.info(msg);
    }
  }

  @Override
  public void info(String format, Object arg) {
    for (Logger delegate : delegate) {
      delegate.info(format, arg);
    }
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    for (Logger delegate : delegate) {
      delegate.info(format, arg1, arg2);
    }
  }

  @Override
  public void info(String format, Object... arguments) {
    for (Logger delegate : delegate) {
      delegate.info(format, arguments);
    }
  }

  @Override
  public void info(String msg, Throwable t) {
    for (Logger delegate : delegate) {
      delegate.info(msg, t);
    }
  }

  @Override
  public void info(Marker marker, String msg) {
    for (Logger delegate : delegate) {
      delegate.info(marker, msg);
    }
  }

  @Override
  public void info(Marker marker, String format, Object arg) {
    for (Logger delegate : delegate) {
      delegate.info(marker, format, arg);
    }
  }

  @Override
  public void info(Marker marker, String format, Object arg1, Object arg2) {
    for (Logger delegate : delegate) {
      delegate.info(marker, format, arg1, arg2);
    }
  }

  @Override
  public void info(Marker marker, String format, Object... arguments) {
    for (Logger delegate : delegate) {
      delegate.info(marker, format, arguments);
    }
  }

  @Override
  public void info(Marker marker, String msg, Throwable t) {
    for (Logger delegate : delegate) {
      delegate.info(marker, msg, t);
    }
  }

  @Override
  public boolean isWarnEnabled() {
    for (Logger delegate : delegate) {
      if (delegate.isWarnEnabled()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    for (Logger delegate : delegate) {
      if (delegate.isWarnEnabled(marker)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void warn(String msg) {
    for (Logger delegate : delegate) {
      delegate.warn(msg);
    }
  }

  @Override
  public void warn(String format, Object arg) {
    for (Logger delegate : delegate) {
      delegate.warn(format, arg);
    }
  }

  @Override
  public void warn(String format, Object... arguments) {
    for (Logger delegate : delegate) {
      delegate.warn(format, arguments);
    }
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    for (Logger delegate : delegate) {
      delegate.warn(format, arg1, arg2);
    }
  }

  @Override
  public void warn(String msg, Throwable t) {
    for (Logger delegate : delegate) {
      delegate.warn(msg, t);
    }
  }

  @Override
  public void warn(Marker marker, String msg) {
    for (Logger delegate : delegate) {
      delegate.warn(marker, msg);
    }
  }

  @Override
  public void warn(Marker marker, String format, Object arg) {
    for (Logger delegate : delegate) {
      delegate.warn(marker, format, arg);
    }
  }

  @Override
  public void warn(Marker marker, String format, Object arg1, Object arg2) {
    for (Logger delegate : delegate) {
      delegate.warn(marker, format, arg1, arg2);
    }
  }

  @Override
  public void warn(Marker marker, String format, Object... arguments) {
    for (Logger delegate : delegate) {
      delegate.warn(marker, format, arguments);
    }
  }

  @Override
  public void warn(Marker marker, String msg, Throwable t) {
    for (Logger delegate : delegate) {
      delegate.warn(marker, msg, t);
    }
  }

  @Override
  public boolean isErrorEnabled() {
    for (Logger delegate : delegate) {
      if (delegate.isErrorEnabled()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    for (Logger delegate : delegate) {
      if (delegate.isErrorEnabled(marker)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void error(String msg) {
    for (Logger delegate : delegate) {
      delegate.error(msg);
    }
  }

  @Override
  public void error(String format, Object arg) {
    for (Logger delegate : delegate) {
      delegate.error(format, arg);
    }
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    for (Logger delegate : delegate) {
      delegate.error(format, arg1, arg2);
    }
  }

  @Override
  public void error(String format, Object... arguments) {
    for (Logger delegate : delegate) {
      delegate.error(format, arguments);
    }
  }

  @Override
  public void error(String msg, Throwable t) {
    for (Logger delegate : delegate) {
      delegate.error(msg, t);
    }
  }

  @Override
  public void error(Marker marker, String msg) {
    for (Logger delegate : delegate) {
      delegate.error(marker, msg);
    }
  }

  @Override
  public void error(Marker marker, String format, Object arg) {
    for (Logger delegate : delegate) {
      delegate.error(marker, format, arg);
    }
  }

  @Override
  public void error(Marker marker, String format, Object arg1, Object arg2) {
    for (Logger delegate : delegate) {
      delegate.error(marker, format, arg1, arg2);
    }
  }

  @Override
  public void error(Marker marker, String format, Object... arguments) {
    for (Logger delegate : delegate) {
      delegate.error(marker, format, arguments);
    }
  }

  @Override
  public void error(Marker marker, String msg, Throwable t) {
    for (Logger delegate : delegate) {
      delegate.error(marker, msg, t);
    }
  }
}
