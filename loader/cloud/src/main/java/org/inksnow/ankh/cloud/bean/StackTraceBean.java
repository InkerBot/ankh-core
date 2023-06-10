package org.inksnow.ankh.cloud.bean;

import lombok.*;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class StackTraceBean {
  private final String className;
  private final String methodName;
  private final String fileName;
  private final int lineNumber;
  private final String message;

  public static StackTraceBean fromInstance(StackTraceElement element) {
    return builder()
        .className(element.getClassName())
        .methodName(element.getMethodName())
        .fileName(element.getFileName())
        .lineNumber(element.getLineNumber())
        .message(element.toString())
        .build();
  }

  @Override
  public String toString() {
    return message;
  }
}
