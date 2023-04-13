package org.inksnow.ankh.core.plugin;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public interface ScannerMethodProcessor {
  void process(PluginClassScanner scanner, ClassNode classNode, MethodNode methodNode, AnnotationNode annotationNode);
}
