package org.inksnow.ankh.core.loader.scanner;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

public interface ScannerClassProcessor {
  void process(ClassNode classNode, AnnotationNode annotationNode);
}
