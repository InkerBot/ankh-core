package org.inksnow.ankh.core.config;

import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigSource;
import org.inksnow.ankh.core.api.util.DcLazy;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.StringJoiner;
import java.util.concurrent.Callable;

@lombok.Builder(toBuilder = true)
public class ConfigSourceImpl implements ConfigSource {
  @lombok.Builder.Default
  private final @Nonnull DcLazy<String> description = DcLazy.ofCompleted(null);
  @lombok.Builder.Default
  private final @Nonnull DcLazy<Path> file = DcLazy.ofCompleted(null);
  @lombok.Builder.Default
  private final @Nonnull DcLazy<Integer> lineNumber = DcLazy.ofCompleted(-1);
  @lombok.Builder.Default
  private final @Nonnull DcLazy<String> path = DcLazy.ofCompleted("");

  @Override
  public @Nonnull String description() {
    return description.get();
  }

  @Override
  public @Nonnull Path file() {
    return file.get();
  }

  @Override
  public int lineNumber() {
    return lineNumber.get();
  }

  @Override
  public @Nonnull String path() {
    return path.get();
  }

  @Override
  public String toString() {
    val joiner = new StringJoiner(", ", "(", ")");
    if (description.get() != null) {
      joiner.add(description.get());
    }
    if (description.get() == null && file.get() != null) {
      joiner.add(file.get().toUri().toString());
    }
    if (lineNumber.get() != null && lineNumber.get() != -1) {
      joiner.add("lineNumber=" + lineNumber.get());
    }
    if (path.get() != null) {
      return joiner + path.get();
    } else {
      return joiner.toString();
    }
  }

  public static class Factory implements ConfigSource.Factory {

    @Override
    public @Nonnull ConfigSource.Builder builder() {
      return new Builder();
    }
  }

  public static class Builder implements ConfigSource.Builder {
    @Override
    public @Nonnull Builder getThis() {
      return this;
    }

    public @Nonnull Builder description(@Nonnull DcLazy<String> dcLazy) {
      this.description$set = true;
      this.description$value = dcLazy;
      return getThis();
    }

    @Override
    public @Nonnull Builder description(@Nonnull String description) {
      return description(DcLazy.ofCompleted(description));
    }

    @Override
    public @Nonnull Builder description(@Nonnull Callable<String> supplier) {
      return description(DcLazy.of(supplier));
    }

    public @Nonnull Builder file(@Nonnull DcLazy<Path> dcLazy) {
      this.file$set = true;
      this.file$value = dcLazy;
      return getThis();
    }

    @Override
    public @Nonnull Builder file(@Nonnull Path file) {
      return file(DcLazy.ofCompleted(file));
    }

    @Override
    public @Nonnull Builder file(@Nonnull Callable<Path> supplier) {
      return file(DcLazy.of(supplier));
    }

    public @Nonnull Builder lineNumber(@Nonnull DcLazy<Integer> dcLazy) {
      this.lineNumber$set = true;
      this.lineNumber$value = dcLazy;
      return getThis();
    }

    @Override
    public @Nonnull Builder lineNumber(int lineNumber) {
      return lineNumber(DcLazy.ofCompleted(lineNumber));
    }

    @Override
    public @Nonnull Builder lineNumber(@Nonnull Callable<Integer> supplier) {
      return lineNumber(DcLazy.of(supplier));
    }

    public @Nonnull Builder path(@Nonnull DcLazy<String> dcLazy) {
      this.path$set = true;
      this.path$value = dcLazy;
      return getThis();
    }

    @Override
    public @Nonnull Builder path(@Nonnull String path) {
      return path(DcLazy.ofCompleted(path));
    }

    @Override
    public @Nonnull Builder path(@Nonnull Callable<String> supplier) {
      return path(DcLazy.of(supplier));
    }
  }
}
