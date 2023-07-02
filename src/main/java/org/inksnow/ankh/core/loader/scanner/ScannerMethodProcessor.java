package org.inksnow.ankh.core.loader.scanner;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public interface ScannerMethodProcessor {
  void process(ClassNode classNode, MethodNode methodNode, AnnotationNode annotationNode);
}
