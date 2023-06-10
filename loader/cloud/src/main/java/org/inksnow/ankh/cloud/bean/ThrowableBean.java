package org.inksnow.ankh.cloud.bean;

import lombok.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public final class ThrowableBean {
  private final String name;
  private final String message;
  private final StackTraceBean[] stackTrace;
  private final ThrowableBean[] causedBy;

  public static ThrowableBean fromInstance(Throwable rootException) {
    if (rootException == null) {
      return null;
    }
    val rootBuilder = builder();

    Set<InstanceReference<Throwable>> loopSet = new HashSet<>();
    List<ThrowableBean> causedByList = new LinkedList<>();
    Throwable exception = rootException;

    while (exception != null) {
      val builder = (exception == rootException) ? rootBuilder : builder();
      builder.name(exception.getClass().getSimpleName())
          .message(exception.getMessage());
      val stackTraceElements = exception.getStackTrace();
      val stackTraceBeans = new StackTraceBean[stackTraceElements.length];
      for (int i = 0; i < stackTraceElements.length; i++) {
        stackTraceBeans[i] = StackTraceBean.fromInstance(stackTraceElements[i]);
      }
      builder.stackTrace(stackTraceBeans);
      if (exception != rootException) {
        causedByList.add(builder.build());
      }
      val cause = exception.getCause();
      if (cause != null && cause != exception && loopSet.add(new InstanceReference<>(cause))) {
        exception = cause;
      } else {
        exception = null;
      }
    }

    return rootBuilder.causedBy(causedByList.toArray(new ThrowableBean[0]))
        .build();
  }

  private static final class InstanceReference<T> {
    private final T instance;

    public InstanceReference(T instance) {
      this.instance = instance;
    }

    public T get() {
      return instance;
    }

    @Override
    public boolean equals(Object o) {
      return this == o
          || o instanceof InstanceReference
          && instance == ((InstanceReference<?>) o).instance;
    }

    @Override
    public int hashCode() {
      return System.identityHashCode(instance);
    }
  }
}
