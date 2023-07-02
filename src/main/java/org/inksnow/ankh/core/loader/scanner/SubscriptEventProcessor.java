package org.inksnow.ankh.core.loader.scanner;

import com.google.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.EventExecutor;
import org.inksnow.ankh.core.api.plugin.annotations.SubscriptEvent;
import org.inksnow.ankh.core.common.EnsureIgnoreCancelledEventExecutor;
import org.inksnow.ankh.core.common.util.AnnotationUtil;
import org.inksnow.ankh.core.common.util.EventExecutorUtil;
import org.inksnow.ankh.core.loader.AnkhPluginLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

@Slf4j
public class SubscriptEventProcessor implements ScannerMethodProcessor {
  private final ClassLoader ankhClassLoader;

  @Inject
  private SubscriptEventProcessor(ClassLoader ankhClassLoader) {
    this.ankhClassLoader = ankhClassLoader;
  }

  @Override
  public void process(ClassNode classNode, MethodNode methodNode, AnnotationNode annotationNode) {
    logger.debug("process SubscriptEvent in {}{}{}", classNode.name, methodNode.name, methodNode.desc);
    val container = AnkhPluginLoader.instance();
    container.onEnable(EventPriority.LOWEST, new Runnable() {
      @Override
      @SneakyThrows
      public void run() {
        val methodType = Type.getMethodType(methodNode.desc);
        val methodArgumentTypes = methodType.getArgumentTypes();
        val isStaticMethod = (methodNode.access & Opcodes.ACC_STATIC) != 0;
        val listenerClass = Class.forName(classNode.name.replace('/', '.'), false, ankhClassLoader);
        val listenerInstance = isStaticMethod ? null : container.injector().getInstance(listenerClass);
        val annotation = (SubscriptEvent) AnnotationUtil.create(annotationNode, ankhClassLoader);
        val eventClass = Class.forName(methodArgumentTypes[0].getClassName(), false, ankhClassLoader).asSubclass(Event.class);
        val isPublic = (classNode.access & Opcodes.ACC_PUBLIC) != 0 && (methodNode.access & Opcodes.ACC_PUBLIC) != 0;
        EventExecutor executor = EventExecutorUtil.generate(listenerClass, listenerInstance, methodNode.name, methodType, isPublic);
        // https://github.com/PaperMC/Paper/pull/9099
        if (annotation.ignoreCancelled()) {
          executor = new EnsureIgnoreCancelledEventExecutor(executor);
        }
        Bukkit.getPluginManager().registerEvent(eventClass, container.plugin(), annotation.priority(), executor, container.plugin(), annotation.ignoreCancelled());
      }
    });
  }
}
