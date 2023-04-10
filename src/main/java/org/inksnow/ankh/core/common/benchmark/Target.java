package org.inksnow.ankh.core.common.benchmark;

@FunctionalInterface
public interface Target<T, R> {
  static <T, R> Target<T, R> runnable(final Runnable target) {
    return it -> {
      target.run();
      return null;
    };
  }

  R run(final T input);
}
