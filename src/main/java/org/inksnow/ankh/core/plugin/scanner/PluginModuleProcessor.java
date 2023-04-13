package org.inksnow.ankh.core.plugin.scanner;

import com.google.inject.Inject;
import com.google.inject.Module;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.inksnow.ankh.core.plugin.AnkhPluginContainerImpl;
import org.inksnow.ankh.core.plugin.PluginClassScanner;
import org.inksnow.ankh.core.plugin.ScannerClassProcessor;
import org.inksnow.ankh.loader.AnkhClassLoader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

@Slf4j
public class PluginModuleProcessor implements ScannerClassProcessor {
  private final AnkhClassLoader ankhClassLoader;
  private final AnkhPluginContainerImpl container;

  @Inject
  private PluginModuleProcessor(AnkhClassLoader ankhClassLoader, AnkhPluginContainerImpl container) {
    this.ankhClassLoader = ankhClassLoader;
    this.container = container;
  }

  @Override
  @SneakyThrows
  public void process(PluginClassScanner scanner, ClassNode classNode, AnnotationNode annotationNode) {
    logger.debug("process PluginModule in {}", classNode.name);
    val moduleClass = Class.forName(classNode.name.replace('/', '.'), false, ankhClassLoader);
    container.pluginModules().add(moduleClass.asSubclass(Module.class));
  }
}
