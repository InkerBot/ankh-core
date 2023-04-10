package mx.kenzie.centurion;

interface ArgumentFace {
  String label();

  default boolean plural() {
    return false;
  }

  default boolean optional() {
    return false;
  }

  default int weight() {
    return 10;
  }

  default boolean literal() {
    return false;
  }

  default String[] possibilities() {
    return new String[0];
  }
}
