package org.inksnow.ankh.core.plugin;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

public interface ScannerClassProcessor {
  void process(PluginClassScanner scanner, ClassNode classNode, AnnotationNode annotationNode);
}
