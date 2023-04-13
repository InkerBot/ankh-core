package org.inksnow.ankh.core.common.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class ClassWriterWithClassLoader extends ClassWriter {
  private final ClassLoader classLoader;

  public ClassWriterWithClassLoader(ClassLoader classLoader, int flags) {
    super(flags);
    this.classLoader = classLoader;
  }

  public ClassWriterWithClassLoader(ClassLoader classLoader, ClassReader classReader, int flags) {
    super(classReader, flags);
    this.classLoader = classLoader;
  }

  @Override
  protected ClassLoader getClassLoader() {
    return classLoader;
  }
}
