package org.inksnow.ankh.core.plugin.scanner;

import com.google.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.inksnow.ankh.core.api.plugin.annotations.SubscriptLifecycle;
import org.inksnow.ankh.core.common.util.AnnotationUtil;
import org.inksnow.ankh.core.common.util.BootstrapUtil;
import org.inksnow.ankh.core.plugin.AnkhPluginContainerImpl;
import org.inksnow.ankh.core.plugin.PluginClassScanner;
import org.inksnow.ankh.core.plugin.ScannerMethodProcessor;
import org.inksnow.ankh.loader.AnkhClassLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.invoke.MethodType;

@Slf4j
public class SubscriptLifecycleProcessor implements ScannerMethodProcessor {
  private final AnkhClassLoader ankhClassLoader;
  private final AnkhPluginContainerImpl container;

  @Inject
  private SubscriptLifecycleProcessor(AnkhClassLoader ankhClassLoader, AnkhPluginContainerImpl container) {
    this.ankhClassLoader = ankhClassLoader;
    this.container = container;
  }

  @Override
  @SneakyThrows
  public void process(PluginClassScanner scanner, ClassNode classNode, MethodNode methodNode, AnnotationNode annotationNode) {
    logger.debug("process SubscriptLifecycle in {}{}{}", classNode.name, methodNode.name, methodNode.desc);
    val isStaticMethod = (methodNode.access & Opcodes.ACC_STATIC) != 0;
    val methodType = Type.getMethodType(methodNode.desc);
    val argumentTypes = methodType.getArgumentTypes();
    if (argumentTypes.length != 0) {
      throw new IllegalStateException("subscript lifecycle require no-args method");
    }
    val callMethod = new Runnable(){
      @Override
      @SneakyThrows
      public void run() {
        val ownerClass = Class.forName(classNode.name.replace('/', '.'), true, ankhClassLoader);
        if(isStaticMethod){
          BootstrapUtil.lookup().findStatic(
              ownerClass,
              methodNode.name,
              MethodType.fromMethodDescriptorString(methodNode.desc, ownerClass.getClassLoader())
          ).invoke();
        }else{
          BootstrapUtil.lookup().findVirtual(
              ownerClass,
              methodNode.name,
              MethodType.fromMethodDescriptorString(methodNode.desc, ownerClass.getClassLoader())
          ).invoke(container.injector().getInstance(ownerClass));
        }
      }
    };
    val annotation = (SubscriptLifecycle) AnnotationUtil.create(annotationNode, ankhClassLoader);
    switch (annotation.value()) {
      case LOAD: {
        container.onLoad(annotation.priority(), callMethod);
        break;
      }
      case ENABLE: {
        container.onEnable(annotation.priority(), callMethod);
        break;
      }
      case DISABLE: {
        container.onDisable(annotation.priority(), callMethod);
        break;
      }
    }
  }
}
