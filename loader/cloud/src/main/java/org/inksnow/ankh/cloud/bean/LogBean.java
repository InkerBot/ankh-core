package org.inksnow.ankh.cloud.bean;

import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public final class LogBean {
  private static final int LEVEL_ERROR = 40;
  private static final int LEVEL_WARN = 30;
  private static final int LEVEL_INFO = 20;
  private static final int LEVEL_DEBUG = 10;
  private static final int LEVEL_TRACE = 0;

  private final long time;
  private final int level;
  private final String name;
  @lombok.Builder.Default
  private final String[] marker = new String[0];
  private final String messagePattern;
  @lombok.Builder.Default
  private final String[] arguments = new String[0];
  @lombok.Builder.Default
  private final ThrowableBean throwable = null;
}
