package org.inksnow.ankh.core.plugin;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.SneakyThrows;
import lombok.val;
import org.inksnow.ankh.core.api.plugin.annotations.AutoRegistered;
import org.inksnow.ankh.core.api.plugin.annotations.PluginModule;
import org.inksnow.ankh.core.api.plugin.annotations.SubscriptEvent;
import org.inksnow.ankh.core.api.plugin.annotations.SubscriptLifecycle;
import org.inksnow.ankh.core.plugin.scanner.AutoRegisteredProcessor;
import org.inksnow.ankh.core.plugin.scanner.PluginModuleProcessor;
import org.inksnow.ankh.core.plugin.scanner.SubscriptEventProcessor;
import org.inksnow.ankh.core.plugin.scanner.SubscriptLifecycleProcessor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;

import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Map;

public class PluginClassScanner {
  private static final Map<String, Class<? extends ScannerClassProcessor>> classProcessors =
      ImmutableMap.<String, Class<? extends ScannerClassProcessor>>builder()
          .put(PluginModule.class.getName(), PluginModuleProcessor.class)
          .put(AutoRegistered.class.getName(), AutoRegisteredProcessor.class)
          .build();
  private static final Map<String, Class<? extends ScannerMethodProcessor>> methodProcessors =
      ImmutableMap.<String, Class<? extends ScannerMethodProcessor>>builder()
          .put(SubscriptEvent.class.getName(), SubscriptEventProcessor.class)
          .put(SubscriptLifecycle.class.getName(), SubscriptLifecycleProcessor.class)
          .build();

  private final Injector injector;
  private final Logger logger;
  private final AnkhPluginContainerImpl ankhPluginContainer;

  private int entryCount = 0;
  private int jarCount = 0;

  @Inject
  private PluginClassScanner(Injector injector, Logger logger, AnkhPluginContainerImpl ankhPluginContainer) {
    this.injector = injector;
    this.logger = logger;
    this.ankhPluginContainer = ankhPluginContainer;
  }

  public void scan(){
    val startTime = System.nanoTime();
    val passTime = (System.nanoTime() - startTime) / 1000_000;
    for (val url : ankhPluginContainer.classLoader().getURLs()) {
      doScan(url);
    }
    logger.info("Scanned {} classes in {} jars in {} ms", entryCount, jarCount, passTime);
  }

  @SneakyThrows
  public void doScan(URL url){
    val urlConnection = url.openConnection();
    if(!(urlConnection instanceof JarURLConnection)){
      return;
    }
    val jarFile = ((JarURLConnection) urlConnection).getJarFile();
    jarCount ++;
    val jarEntryEnumeration = jarFile.entries();
    while (jarEntryEnumeration.hasMoreElements()){
      val jarEntry = jarEntryEnumeration.nextElement();
      if(jarEntry.isDirectory()){
        continue;
      }
      if(!jarEntry.getName().endsWith(".class")){
        continue;
      }
      entryCount ++;
      logger.trace("scan class {}", jarEntry.getName());

      val classNode = readClassNode(jarFile.getInputStream(jarEntry));
      if(classNode.visibleAnnotations != null) {
        for (val annotationNode : classNode.visibleAnnotations) {
          val annotationType = Type.getType(annotationNode.desc);
          val processorClass = classProcessors.get(annotationType.getClassName());
          if (processorClass == null) {
            continue;
          }
          val classProcessor = injector.getInstance(processorClass);
          logger.trace("process {} for {}", annotationType, classNode.name);
          classProcessor.process(this, classNode, annotationNode);
        }
      }
      for (val methodNode : classNode.methods) {
        if(methodNode.visibleAnnotations != null){
          for (AnnotationNode annotationNode : methodNode.visibleAnnotations) {
            val annotationType = Type.getType(annotationNode.desc);
            val processorClass = methodProcessors.get(annotationType.getClassName());
            if (processorClass == null) {
              continue;
            }
            val methodProcessor = injector.getInstance(processorClass);
            logger.trace("processed {} for {}{}{}", annotationType, classNode.name, methodNode.name, methodNode.desc);
            methodProcessor.process(this, classNode, methodNode, annotationNode);
          }
        }
      }
    }
  }

  @SneakyThrows
  private ClassNode readClassNode(InputStream input){
    try {
      val classReader = new ClassReader(input);
      val classNode = new ClassNode();
      classReader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
      return classNode;
    }finally {
      input.close();
    }
  }
}
