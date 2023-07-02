package org.inksnow.ankh.core.common.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.bukkit.plugin.EventExecutor;
import org.inksnow.ankh.core.common.asm.ClassWriterWithClassLoader;
import org.inksnow.ankh.core.common.asm.CodeDefClassLoader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodType;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicLong;

@UtilityClass
public class EventExecutorUtil {
  private static final AtomicLong idAllocator = new AtomicLong();

  public static EventExecutor generate(Class<?> ownerClass, Object owner, String methodName, Type rawType, boolean withAccess) {
    return generateDynamic(ownerClass, owner, methodName, rawType);
  }

  @SneakyThrows
  private static EventExecutor generateDynamic(Class<?> ownerClass, Object owner, String methodName, Type rawType) {
    val rawArgumentTypes = rawType.getArgumentTypes();
    val classLoader = ownerClass.getClassLoader();
    val internalName = Type.getInternalName(ownerClass) + "$" + methodName + "$ankh-core-asm-event-executor$" + idAllocator.incrementAndGet();
    val cw = new ClassWriterWithClassLoader(ownerClass.getClassLoader(), ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, internalName, null, "java/lang/Object", new String[]{"org/bukkit/plugin/EventExecutor"});
    cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, "callsite", "Ljava/lang/invoke/CallSite;", null, null).visitEnd();
    {
      val mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ljava/lang/invoke/CallSite;)V", null, null);
      mw.visitVarInsn(Opcodes.ALOAD, 0);
      mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
      mw.visitVarInsn(Opcodes.ALOAD, 1);
      mw.visitFieldInsn(Opcodes.PUTSTATIC, internalName, "callsite", "Ljava/lang/invoke/CallSite;");
      mw.visitInsn(Opcodes.RETURN);
      mw.visitMaxs(0, 0);
      mw.visitEnd();
    }
    {
      val mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "execute", "(Lorg/bukkit/event/Listener;Lorg/bukkit/event/Event;)V", null, null);
      mw.visitVarInsn(Opcodes.ALOAD, 2);
      mw.visitTypeInsn(Opcodes.CHECKCAST, rawArgumentTypes[0].getInternalName());
      mw.visitInvokeDynamicInsn(methodName, rawType.getDescriptor(), new Handle(Opcodes.H_INVOKESTATIC, internalName, "callsite", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false));
      mw.visitInsn(Opcodes.RETURN);
      mw.visitMaxs(0, 0);
      mw.visitEnd();
    }
    {
      val mw = cw.visitMethod(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, "callsite", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", null, null);
      mw.visitFieldInsn(Opcodes.GETSTATIC, internalName, "callsite", "Ljava/lang/invoke/CallSite;");
      mw.visitInsn(Opcodes.ARETURN);
      mw.visitMaxs(0, 0);
      mw.visitEnd();
    }
    val methodType = MethodType.fromMethodDescriptorString(rawType.getDescriptor(), classLoader);
    val handle = (owner == null)
        ? BootstrapUtil.lookup().findStatic(ownerClass, methodName, methodType)
        : BootstrapUtil.lookup().findVirtual(ownerClass, methodName, methodType).bindTo(owner);
    return (EventExecutor) new CodeDefClassLoader(ownerClass.getClassLoader())
        .define(internalName.replace('/', '.'), cw.toByteArray())
        .getConstructor(CallSite.class)
        .newInstance(new ConstantCallSite(handle));
  }

  @SneakyThrows
  private static EventExecutor generateDirect(Class<?> ownerClass, Object owner, String methodName, Type rawType) {
    val rawArgumentTypes = rawType.getArgumentTypes();
    val ownerInternalName = Type.getInternalName(ownerClass);
    val ownerDescriptor = Type.getDescriptor(ownerClass);
    val executorId = idAllocator.getAndIncrement();
    val internalName = Type.getInternalName(ownerClass) + "$" + methodName + "$ankh-core-asm-event-executor$" + executorId;
    val cw = new ClassWriterWithClassLoader(ownerClass.getClassLoader(), ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, ownerInternalName + "$" + methodName + "$ankh-core-asm-event-executor$" + idAllocator.incrementAndGet(), null, "java/lang/Object", new String[]{"org/bukkit/plugin/EventExecutor"});
    if (owner != null) {
      cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "owner", ownerDescriptor, null, null)
          .visitEnd();
      $internal$actions$.set(executorId, owner);
      {
        val mw = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        mw.visitLdcInsn(executorId);
        mw.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName($internal$actions$.class), "get", "(J)Ljava/lang/Object;", false);
        mw.visitTypeInsn(Opcodes.CHECKCAST, ownerInternalName);
        mw.visitFieldInsn(Opcodes.PUTSTATIC, internalName, "owner", ownerDescriptor);
        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(0, 0);
        mw.visitEnd();
      }
    }
    {
      val mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
      mw.visitVarInsn(Opcodes.ALOAD, 0);
      mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
      mw.visitInsn(Opcodes.RETURN);
      mw.visitMaxs(0, 0);
      mw.visitEnd();
    }
    {
      val mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "execute", "(Lorg/bukkit/event/Listener;Lorg/bukkit/event/Event;)V", null, null);
      if (owner != null) {
        mw.visitFieldInsn(Opcodes.GETSTATIC, internalName, "owner", ownerDescriptor);
      }
      mw.visitVarInsn(Opcodes.ALOAD, 2);
      mw.visitTypeInsn(Opcodes.CHECKCAST, rawArgumentTypes[0].getInternalName());
      mw.visitMethodInsn(owner == null ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL, ownerInternalName, methodName, rawType.getDescriptor(), false);
      mw.visitInsn(Opcodes.RETURN);
      mw.visitMaxs(0, 0);
      mw.visitEnd();
    }
    return (EventExecutor) new CodeDefClassLoader(ownerClass.getClassLoader())
        .define(internalName.replace('/', '.'), cw.toByteArray())
        .getConstructor()
        .newInstance();
  }

  public static class $internal$actions$ {
    private static final Long2ObjectMap<WeakReference<Object>> objectMap = Long2ObjectMaps.synchronize(new Long2ObjectAVLTreeMap<>());

    private static void set(long id, Object instance) {
      objectMap.put(id, new WeakReference<>(instance));
    }

    public static Object get(long id) {
      val value = objectMap.get(id);
      objectMap.remove(id);
      return value == null ? null : value.get();
    }
  }
}
