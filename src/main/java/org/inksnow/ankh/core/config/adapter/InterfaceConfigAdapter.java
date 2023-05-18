package org.inksnow.ankh.core.config.adapter;

import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigLoader;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigTypeAdapter;
import org.inksnow.ankh.core.api.config.exception.ConfigException;
import org.inksnow.ankh.core.api.config.exception.ConfigValidateException;
import org.inksnow.ankh.core.common.asm.ClassWriterWithClassLoader;
import org.inksnow.ankh.core.common.asm.CodeDefClassLoader;
import org.inksnow.ankh.core.common.util.BootstrapUtil;
import org.inksnow.ankh.core.common.util.CacheMapUtil;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
public class InterfaceConfigAdapter<T> implements ConfigTypeAdapter<T> {
  private final Class<?> generatedClass;
  private final MethodHandle constructorHandle;
  private final TypedEntry[] typedEntries;

  @Override
  @SneakyThrows
  @SuppressWarnings("unchecked")
  public T read(ConfigSection section) {
    val args = new Object[typedEntries.length];
    val exceptions = new LinkedList<ConfigException.Entry>();
    for (int i = 0; i < typedEntries.length; i++) {
      val typedEntry = typedEntries[i];
      if (typedEntry.adapter != null) {
        val subSection = section.get(typedEntry.configName);
        val value = typedEntry.adapter.read(subSection);
        val validateResult = ConfigVaildatorUtils.validator().validateValue(generatedClass, typedEntry.beanName, value);
        for (val violation : validateResult) {
          exceptions.add(new ConfigException.Entry(subSection.source(), violation.getMessage()));
        }
        args[i] = value;
      }
    }
    if (exceptions.isEmpty()) {
      return (T) invokeConstructor(args);
    } else {
      throw new ConfigValidateException(exceptions);
    }
  }

  @SneakyThrows
  private Object invokeConstructor(Object[] args){
    return constructorHandle.invokeExact(args);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static class Factory implements ConfigTypeAdapter.Factory<Object> {
    private static final Map<Class<?>, InterfaceConfigAdapter> classInterfaceCache = CacheMapUtil.make();

    @Override
    @SneakyThrows
    public ConfigTypeAdapter create(ConfigLoader configLoader, TypeToken typeToken) {
      val rawType = typeToken.getRawType();
      if (!rawType.isInterface()) {
        return null;
      }
      val adapters = Stream.concat(Stream.of(rawType), Arrays.stream(rawType.getInterfaces()))
          .filter(it -> it != Object.class)
          .flatMap(it -> Arrays.stream(it.getMethods()))
          .filter(it -> (it.getModifiers() & Modifier.ABSTRACT) != 0)
          .filter(it -> it.getParameterCount() == 0)
          .distinct()
          .map(method -> {
            method.setAccessible(true);
            val beanName = method.getName();
            val configName = configLoader.translateName(beanName);
            val rawReturnType = method.getReturnType();
            val type = TypeToken.get(method.getGenericReturnType());
            val adapter = rawReturnType == void.class ? null : configLoader.getAdapter(type);
            return new TypedEntry(method, beanName, configName, type, adapter);
          })
          .toArray(TypedEntry[]::new);
      val generatedName = "org/inksnow/ankh/core/config/$codedef$/" + rawType.getSimpleName();
      val classLoader = new CodeDefClassLoader(rawType.getClassLoader());
      val classWriter = new ClassWriterWithClassLoader(rawType.getClassLoader(), ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
      writeClass(rawType, generatedName, classWriter, adapters);
      val classBytes = classWriter.toByteArray();
      val generatedClass = classLoader.define(generatedName.replace('/', '.'), classBytes, 0, classBytes.length);
      val constructorHandle = BootstrapUtil.lookup()
          .findConstructor(generatedClass, MethodType.methodType(void.class, Object[].class))
          .asType(MethodType.methodType(Object.class, Object[].class));
      return new InterfaceConfigAdapter(generatedClass, constructorHandle, adapters);
    }

    private void writeClass(Class<?> interfaceClass, String internalName, ClassWriter cw, TypedEntry[] entries) {
      cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, internalName, null, "java/lang/Object", new String[]{ Type.getInternalName(interfaceClass) });
      for (int i = 0; i < entries.length; i++) {
        writeField(cw, i, entries[i]);
        writeMethod(cw, internalName, i, entries[i]);
      }
      writeInit(cw, internalName, entries);
    }

    private void writeField(ClassWriter cw, int index, TypedEntry entry){
      if (entry.method.getReturnType() != void.class) {
        val fw = cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, entry.beanName + "$" + index, Type.getDescriptor(entry.typeToken.getRawType()), null, null);
        fw.visitEnd();
      }
    }

    private void writeMethod(ClassWriter cw, String ownerName, int index, TypedEntry entry){
      val type = Type.getType(entry.method);
      val rType = type.getReturnType();
      val mw = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, entry.beanName, type.getDescriptor(), null, null);
      if(rType == Type.VOID_TYPE){
        mw.visitInsn(Opcodes.RETURN);
      }else {
        mw.visitVarInsn(Opcodes.ALOAD, 0);
        mw.visitFieldInsn(Opcodes.GETFIELD, ownerName, entry.beanName + "$" + index, rType.getDescriptor());
        mw.visitInsn(rType.getOpcode(Opcodes.IRETURN));
      }
      mw.visitMaxs(0, 0);
      mw.visitEnd();
    }

    private void writeInit(ClassWriter cw, String ownerName, TypedEntry[] entries) {
      val mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "([Ljava/lang/Object;)V", null, null);
      mw.visitVarInsn(Opcodes.ALOAD, 0);
      mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
      for (int i = 0; i < entries.length; i++) {
        val entry = entries[i];
        if(entry.method.getReturnType() != void.class) {
          mw.visitVarInsn(Opcodes.ALOAD, 0);
          mw.visitVarInsn(Opcodes.ALOAD, 1);
          mw.visitLdcInsn(i);
          mw.visitInsn(Opcodes.AALOAD);
          mw.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(entry.method.getReturnType()));
          mw.visitFieldInsn(Opcodes.PUTFIELD, ownerName, entry.beanName + "$" + i, Type.getDescriptor(entry.method.getReturnType()));
        }
      }
      mw.visitInsn(Opcodes.RETURN);
      mw.visitMaxs(0, 0);
      mw.visitEnd();
    }
  }

  @RequiredArgsConstructor
  private static class TypedEntry {
    private final Method method;
    private final String beanName;
    private final String configName;
    private final TypeToken<?> typeToken;
    private final ConfigTypeAdapter<?> adapter;
  }
}
