package org.inksnow.ankh.core.common.benchmark;

@FunctionalInterface
public interface InputSeed<T> {
  static <T> InputSeed<T> nop() {
    return it -> null;
  }

  T seed(int round);
}
