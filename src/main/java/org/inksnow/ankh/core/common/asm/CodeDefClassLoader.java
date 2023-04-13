package org.inksnow.ankh.core.common.asm;

import lombok.val;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

public class CodeDefClassLoader extends ClassLoader {
  static  {
    registerAsParallelCapable();
  }

  private final @Nonnull Map<String, Class<?>> codeDefClass;

  public CodeDefClassLoader() {
    this(null, null);
  }

  public CodeDefClassLoader(@Nullable Map<String, Class<?>> codeDefClass) {
    this(null, codeDefClass);
  }

  public CodeDefClassLoader(@Nullable ClassLoader parent) {
    this(parent, null);
  }

  public CodeDefClassLoader(@Nullable ClassLoader parent, @Nullable Map<String, Class<?>> codeDefClass) {
    super(parent);
    this.codeDefClass = codeDefClass == null ? new HashMap<>() : codeDefClass;
  }


  @Override
  protected @Nonnull Class<?> loadClass(@Nonnull String name, boolean resolve) throws ClassNotFoundException {
    val codeDefResult = codeDefClass.get(name);
    if(codeDefResult != null){
      return codeDefResult;
    }
    return super.loadClass(name, resolve);
  }

  public @Nonnull Class<?> define(@Nonnull String name, @Nonnull byte[] b) {
    return defineClass(name, b, 0, b.length);
  }

  public @Nonnull Class<?> define(@Nonnull String name, @Nonnull byte[] b, @Nullable ProtectionDomain protectionDomain) {
    return defineClass(name, b, 0, b.length, protectionDomain);
  }

  public @Nonnull Class<?> define(@Nonnull String name, @Nonnull byte[] b, int off, int len) {
    return defineClass(name, b, off, len);
  }

  public @Nonnull Class<?> define(@Nonnull String name, @Nonnull byte[] b, int off, int len, @Nullable ProtectionDomain protectionDomain){
    return defineClass(name, b, off, len, protectionDomain);
  }
}
