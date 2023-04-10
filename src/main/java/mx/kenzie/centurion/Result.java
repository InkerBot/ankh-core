package mx.kenzie.centurion;

import lombok.Data;

public interface Result {

  CommandResult type();

  Throwable error();

  default boolean successful() {
    return this.type().successful;
  }

  @Data
  class Error implements Result {
    private final CommandResult type;
    private final Throwable error;
  }

}
