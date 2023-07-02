package org.inksnow.ankh.core.loader;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.inksnow.ankh.core.api.plugin.annotations.AutoRegistered;
import org.inksnow.ankh.core.api.plugin.annotations.PluginModule;
import org.inksnow.ankh.core.api.plugin.annotations.SubscriptEvent;
import org.inksnow.ankh.core.api.plugin.annotations.SubscriptLifecycle;
import org.inksnow.ankh.core.loader.scanner.ScannerClassProcessor;
import org.inksnow.ankh.core.loader.scanner.ScannerMethodProcessor;
import org.inksnow.ankh.core.loader.scanner.AutoRegisteredProcessor;
import org.inksnow.ankh.core.loader.scanner.PluginModuleProcessor;
import org.inksnow.ankh.core.loader.scanner.SubscriptEventProcessor;
import org.inksnow.ankh.core.loader.scanner.SubscriptLifecycleProcessor;
import org.inksnow.asteroid.AkArtifact;
import org.inksnow.asteroid.AkEnvironment;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class AnkhPluginScanner {
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
  private final AkEnvironment environment;

  @Inject
  private AnkhPluginScanner(Injector injector, AkEnvironment environment) {
    this.injector = injector;
    this.environment = environment;
  }

  public void run() {
    environment.installedArtifact()
        .forEach(this::scanArtifact);
  }

  private void scanArtifact(AkArtifact artifact){
    logger.debug("start scan artifact: {}", artifact.key());
    try{
      scanArtifactImpl(artifact);
    }catch (Exception e){
      logger.error("failed to scan artifact: {}", artifact.key());
    }
  }

  private void scanArtifactImpl(AkArtifact artifact) throws IOException {
    logger.trace("scan file {}", artifact.file().getAbsolutePath());
    try(val jarFile = new JarFile(artifact.file())){
      val jarEntryEnumeration = jarFile.entries();
      while (jarEntryEnumeration.hasMoreElements()) {
        val jarEntry = jarEntryEnumeration.nextElement();
        if (jarEntry.isDirectory() || !jarEntry.getName().endsWith(".class")) {
          continue;
        }
        logger.trace("scan class {}", jarEntry.getName());
        val classNode = readClassNode(jarFile, jarEntry);
        if (classNode.visibleAnnotations != null) {
          for (val annotationNode : classNode.visibleAnnotations) {
            val annotationType = Type.getType(annotationNode.desc);
            val processorClass = classProcessors.get(annotationType.getClassName());
            if (processorClass == null) {
              continue;
            }
            val classProcessor = injector.getInstance(processorClass);
            logger.trace("process {} for {}", annotationType, classNode.name);
            classProcessor.process(classNode, annotationNode);
          }
        }
        for (val methodNode : classNode.methods) {
          if (methodNode.visibleAnnotations != null) {
            for (AnnotationNode annotationNode : methodNode.visibleAnnotations) {
              val annotationType = Type.getType(annotationNode.desc);
              val processorClass = methodProcessors.get(annotationType.getClassName());
              if (processorClass == null) {
                continue;
              }
              val methodProcessor = injector.getInstance(processorClass);
              logger.trace("processed {} for {}{}{}", annotationType, classNode.name, methodNode.name, methodNode.desc);
              methodProcessor.process(classNode, methodNode, annotationNode);
            }
          }
        }
      }
    }
  }

  private static ClassNode readClassNode(JarFile jarFile, JarEntry jarEntry) throws IOException {
    try(val input = jarFile.getInputStream(jarEntry)){
      val classReader = new ClassReader(input);
      val classNode = new ClassNode();
      classReader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
      return classNode;
    }
  }
}
