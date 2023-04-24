package org.inksnow.ankh.core.common.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.inksnow.ankh.core.common.asm.ClassWriterWithClassLoader;
import org.inksnow.ankh.core.common.asm.CodeDefClassLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicLong;

@UtilityClass
public class AnnotationUtil {
  private static final AtomicLong idAllocator = new AtomicLong();

  @SneakyThrows
  public static <T extends Annotation> T create(AnnotationNode annotationNode, ClassLoader classLoader) {
    val cw = new ClassWriterWithClassLoader(classLoader, 0);
    val demoInternalName = "org/inksnow/ankh/core/common/util/AnnotationUtil$generate$annotation$demo$" + idAllocator.getAndIncrement();
    cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, demoInternalName, null, "java/lang/Object", null);
    {
      val aw = cw.visitAnnotation(annotationNode.desc, true);
      annotationNode.accept(aw);
    }
    val bytes = cw.toByteArray();
    val codeDefClassLoader = new CodeDefClassLoader(classLoader);
    val demoClass = codeDefClassLoader.define(demoInternalName.replace('/', '.'), bytes, 0, bytes.length);
    return (T) demoClass.getDeclaredAnnotations()[0];
  }
}
