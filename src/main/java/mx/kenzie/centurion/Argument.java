package mx.kenzie.centurion;

import lombok.Data;

public interface Argument<Type> extends ArgumentFace, Described {

  boolean matches(String input);

  Type parse(String input);

  default int weight() {
    return this.plural() ? 100 : this.literal() ? 3 : 10;
  }

  default Type lapse() {
    return null;
  }

  default ParseResult read(String input) {
    final int space = input.indexOf(' ');
    if (this.plural() || space < 0) return new ParseResult(input.trim(), "");
    else return new ParseResult(input.substring(0, space).trim(), input.substring(space + 1).stripLeading());
  }

  @Override
  default String description() {
    return null;
  }

  @Data
  class ParseResult {
    private final String part;
    private final String remainder;
  }
}

