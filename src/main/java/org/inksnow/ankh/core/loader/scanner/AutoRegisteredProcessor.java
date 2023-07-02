package org.inksnow.ankh.core.loader.scanner;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.event.EventPriority;
import org.inksnow.ankh.core.api.block.AnkhBlock;
import org.inksnow.ankh.core.api.item.AnkhItem;
import org.inksnow.ankh.core.api.plugin.AnkhPluginContainer;
import org.inksnow.ankh.core.block.BlockRegisterService;
import org.inksnow.ankh.core.item.ItemRegisterService;
import org.inksnow.ankh.core.loader.AnkhPluginLoader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import javax.inject.Inject;

@Slf4j
public class AutoRegisteredProcessor implements ScannerClassProcessor {
  private final ClassLoader ankhClassLoader;

  @Inject
  private AutoRegisteredProcessor(ClassLoader ankhClassLoader) {
    this.ankhClassLoader = ankhClassLoader;
  }


  @Override
  public void process(ClassNode classNode, AnnotationNode annotationNode) {
    logger.debug("process AutoRegistered in {}", classNode.name);
    val container = AnkhPluginLoader.instance();
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
