package org.inksnow.ankh.core.loader.scanner;

import com.google.inject.Inject;
import com.google.inject.Module;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.inksnow.ankh.core.loader.AnkhPluginLoader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

@Slf4j
public class PluginModuleProcessor implements ScannerClassProcessor {
  private final ClassLoader ankhClassLoader;

  @Inject
  private PluginModuleProcessor(ClassLoader ankhClassLoader) {
    this.ankhClassLoader = ankhClassLoader;
  }

  @Override
  @SneakyThrows
  public void process(ClassNode classNode, AnnotationNode annotationNode) {
    logger.debug("process PluginModule in {}", classNode.name);
    val moduleClass = Class.forName(classNode.name.replace('/', '.'), false, ankhClassLoader);
    AnkhPluginLoader.instance()
        .pluginModules()
        .add(moduleClass.asSubclass(Module.class));
  }
}
