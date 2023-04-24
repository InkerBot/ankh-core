package org.inksnow.ankh.core.config;

import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigSource;
import org.inksnow.ankh.core.api.util.DcLazy;

import javax.annotation.Nonnull;
import java.util.StringJoiner;
import java.util.concurrent.Callable;

@lombok.Builder(toBuilder = true)
public class ConfigSourceImpl implements ConfigSource {
  @lombok.Builder.Default
  private final @Nonnull DcLazy<String> description = DcLazy.ofCompleted("(unknown)");
  @lombok.Builder.Default
  private final @Nonnull DcLazy<String> fileName = DcLazy.ofCompleted("(unknown)");
  @lombok.Builder.Default
  private final @Nonnull DcLazy<Integer> lineNumber = DcLazy.ofCompleted(-1);
  @lombok.Builder.Default
  private final @Nonnull DcLazy<String> path = DcLazy.ofCompleted("(root)");

  @Override
  public @Nonnull String description() {
    return description.get();
  }

  @Override
  public @Nonnull String fileName() {
    return fileName.get();
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
    val joiner = new StringJoiner(", ", "ConfigSource(", ")");
    if (description.get() != null) {
      joiner.add("description=" + description.get());
    }
    if (fileName.get() != null) {
      joiner.add("fileName=" + fileName.get());
    }
    if (lineNumber.get() != null && lineNumber.get() != -1) {
      joiner.add("lineNumber=" + lineNumber.get());
    }
    if (path.get() != null) {
      joiner.add("path=" + path.get());
    }
    return joiner.toString();
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

    public @Nonnull Builder fileName(@Nonnull DcLazy<String> dcLazy) {
      this.fileName$set = true;
      this.fileName$value = dcLazy;
      return getThis();
    }

    @Override
    public @Nonnull Builder fileName(@Nonnull String fileName) {
      return fileName(DcLazy.ofCompleted(fileName));
    }

    @Override
    public @Nonnull Builder fileName(@Nonnull Callable<String> supplier) {
      return fileName(DcLazy.of(supplier));
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
