package org.inksnow.ankh.core.config.adapter;

import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigLoader;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigTypeAdapter;
import org.inksnow.ankh.core.common.util.BootstrapUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
public class ObjectConfigAdapter<T> implements ConfigTypeAdapter<T> {
  private final Class<?> clazz;
  private final TypedEntry[] typedEntries;

  @SneakyThrows
  @Override
  public T read(ConfigSection section) {
    val instance = (T) BootstrapUtil.unsafe().allocateInstance(clazz);
    boolean success = true;
    for (val typedEntry : typedEntries) {
      if (typedEntry.adapter != null) {
        val subSection = section.get(typedEntry.name);
        val value = typedEntry.adapter.read(subSection);
        val validateResult = ConfigVaildatorUtils.validator().validateValue(clazz, typedEntry.name, value);
        for (val violation : validateResult) {
          logger.error("Failed to check config: {}\n\tat {}", violation.getMessage(), subSection.source());
          success = false;
        }
        typedEntry.setter.invoke(instance, value);
      }
    }
    if(success) {
      return instance;
    }else{
      throw new IllegalArgumentException("Failed to check config");
    }
  }

  public static class Factory implements ConfigTypeAdapter.Factory<Object> {
    @Override
    @SneakyThrows
    public <V> ConfigTypeAdapter<V> create(ConfigLoader configLoader, TypeToken<? super V> typeToken) {
      val rawType = typeToken.getRawType();
      if((rawType.getModifiers() & Modifier.ABSTRACT) != 0 || rawType.isPrimitive() || rawType.isInterface()){
        return null;
      }
      val typedEntries = Stream.concat(Stream.of(rawType), Arrays.stream(rawType.getInterfaces()))
          .filter(it -> it != Object.class)
          .flatMap(it -> Arrays.stream(it.getDeclaredFields()))
          .map(new Function<Field, TypedEntry>() {
            @Override
            @SneakyThrows
            public TypedEntry apply(Field field) {
              val name = field.getName();
              val fieldType = field.getType();
              val fieldTypeToken = TypeToken.get(field.getGenericType());
              val adapter = configLoader.getAdapter(fieldTypeToken);
              val setter = BootstrapUtil.lookup()
                  .findSetter(field.getDeclaringClass(), name, fieldType)
                  .asType(MethodType.methodType(void.class, rawType, field.getType()));
              return new TypedEntry(field, name, setter, fieldTypeToken, adapter);
            }
          })
          .toArray(TypedEntry[]::new);
      return new ObjectConfigAdapter<>(rawType, typedEntries);
    }
  }

  @RequiredArgsConstructor
  private static class TypedEntry {
    private final Field field;
    private final String name;
    private final MethodHandle setter;
    private final TypeToken<?> typeToken;
    private final ConfigTypeAdapter<?> adapter;
  }
}
