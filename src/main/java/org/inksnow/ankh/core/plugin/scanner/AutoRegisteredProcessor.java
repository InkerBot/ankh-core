package org.inksnow.ankh.core.plugin.scanner;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.event.EventPriority;
import org.inksnow.ankh.core.api.block.AnkhBlock;
import org.inksnow.ankh.core.api.item.AnkhItem;
import org.inksnow.ankh.core.block.BlockRegisterService;
import org.inksnow.ankh.core.item.ItemRegisterService;
import org.inksnow.ankh.core.plugin.AnkhPluginContainerImpl;
import org.inksnow.ankh.core.plugin.PluginClassScanner;
import org.inksnow.ankh.core.plugin.ScannerClassProcessor;
import org.inksnow.ankh.loader.AnkhClassLoader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import javax.inject.Inject;

@Slf4j
public class AutoRegisteredProcessor implements ScannerClassProcessor {
  private final AnkhClassLoader ankhClassLoader;
  private final AnkhPluginContainerImpl container;

  @Inject
  private AutoRegisteredProcessor(AnkhClassLoader ankhClassLoader, AnkhPluginContainerImpl container) {
    this.ankhClassLoader = ankhClassLoader;
    this.container = container;
  }


  @Override
  public void process(PluginClassScanner scanner, ClassNode classNode, AnnotationNode annotationNode) {
    logger.debug("process AutoRegistered in {}", classNode.name);
    container.onLoad(EventPriority.LOWEST, new Runnable() {
      @Override
      @SneakyThrows
      public void run() {
        val injector = container.injector();
        val clazz = Class.forName(classNode.name.replace('/', '.'), false, ankhClassLoader);
        if (AnkhItem.class.isAssignableFrom(clazz)) {
          injector.getInstance(ItemRegisterService.class).register((AnkhItem) injector.getInstance(clazz));
        }
        if (AnkhBlock.Factory.class.isAssignableFrom(clazz)) {
          injector.getInstance(BlockRegisterService.class).register((AnkhBlock.Factory) injector.getInstance(clazz));
        }
      }
    });
  }
}
