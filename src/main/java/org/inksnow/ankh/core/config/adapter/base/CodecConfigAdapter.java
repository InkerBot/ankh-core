package org.inksnow.ankh.core.config.adapter.base;

import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigLoader;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigTypeAdapter;
import org.inksnow.ankh.core.common.util.BootstrapUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;

@RequiredArgsConstructor
@SuppressWarnings("rawtypes")
public class CodecConfigAdapter<T> implements ConfigTypeAdapter<T> {
  private final MethodHandle codecMethod;

  @Override
  @SneakyThrows
  @SuppressWarnings("unchecked")
  public T read(ConfigSection section) {
    return (T) codecMethod.invokeExact(section);
  }

  public static class Factory implements ConfigTypeAdapter.Factory<Object> {
    @Override
    @SneakyThrows
    public <V> ConfigTypeAdapter<V> create(ConfigLoader configLoader, TypeToken<? super V> typeToken) {
      val rawType = ((TypeToken) typeToken).getRawType();
      if (rawType.isPrimitive()) {
        return null;
      }
      MethodHandle codecMethod = null;
      if ((rawType.getModifiers() & Modifier.ABSTRACT) != 0 && !rawType.isInterface()) {
        try {
          codecMethod = BootstrapUtil.lookup().findConstructor(rawType, MethodType.methodType(void.class, ConfigSection.class));
        } catch (NoSuchMethodException e) {
          //
        }
      }
      if (codecMethod != null) {
        try {
          codecMethod = BootstrapUtil.lookup().findStatic(rawType, "readFromConfig", MethodType.methodType(rawType, ConfigSection.class));
        } catch (NoSuchMethodException e) {
          //
        }
      }
      if (codecMethod == null) {
        return null;
      }
      return new CodecConfigAdapter<>(
          codecMethod.asType(MethodType.methodType(Object.class, ConfigSection.class))
      );
    }
  }
}
