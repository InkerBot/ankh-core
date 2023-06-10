package org.inksnow.ankh.core.config.adapter.base;

import com.google.gson.reflect.TypeToken;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigLoader;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigTypeAdapter;
import org.inksnow.ankh.core.api.config.exception.ConfigException;
import org.inksnow.ankh.core.api.config.exception.ConfigValidateException;
import org.inksnow.ankh.core.common.util.BootstrapUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("rawtypes")
public class RecordConfigAdapter<T> implements ConfigTypeAdapter<T> {
  private final Class clazz;
  private final MethodHandle constructor;
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
        Set<ConstraintViolation> validateResult = ConfigVaildatorUtils.validator().validateValue(clazz, typedEntry.beanName, value);
        for (val violation : validateResult) {
          exceptions.add(new ConfigException.Entry(subSection.source(), violation.getMessage()));
        }
        args[i] = value;
      }
    }
    if (exceptions.isEmpty()) {
      return (T) constructor.invokeWithArguments(args);
    } else {
      throw new ConfigValidateException(exceptions);
    }
  }

  public static class Factory implements ConfigTypeAdapter.Factory<Object> {
    private static final ConfigTypeAdapter.Factory<Object> delegate = provideDelegate();

    private static ConfigTypeAdapter.Factory<Object> provideDelegate() {
      try {
        Class.forName("java.lang.Record", false, null);
        return new ImplFactory();
      } catch (ClassNotFoundException e) {
        return null;
      }
    }

    @Override
    public <V> ConfigTypeAdapter<V> create(ConfigLoader configLoader, TypeToken<? super V> typeToken) {
      return delegate != null ? delegate.create(configLoader, typeToken) : null;
    }
  }

  private static class ImplFactory implements ConfigTypeAdapter.Factory<Object> {
    private static final MethodHandle classIsRecord = BootstrapUtil.ofVirtual("Ljava/lang/Class;isRecord()Z");
    private static final MethodHandle classGetRecordComponents = BootstrapUtil.ofVirtual("Ljava/lang/Class;getRecordComponents()[Ljava/lang/reflect/RecordComponent;")
        .asType(MethodType.methodType(Object[].class, Class.class));
    private static final MethodHandle recordComponentGetType = BootstrapUtil.ofVirtual("Ljava/lang/reflect/RecordComponent;getType()Ljava/lang/Class;")
        .asType(MethodType.methodType(Class.class, Object.class));
    private static final MethodHandle recordComponentGetGenericType = BootstrapUtil.ofVirtual("Ljava/lang/reflect/RecordComponent;getGenericType()Ljava/lang/reflect/Type;")
        .asType(MethodType.methodType(Type.class, Object.class));
    private static final MethodHandle recordComponentGetName = BootstrapUtil.ofVirtual("Ljava/lang/reflect/RecordComponent;getName()Ljava/lang/String;")
        .asType(MethodType.methodType(String.class, Object.class));

    @SneakyThrows
    public boolean classIsRecord(Class<?> clazz) {
      return (boolean) classIsRecord.invokeExact(clazz);
    }

    @SneakyThrows
    public Object[] classGetRecordComponents(Class<?> clazz) {
      return (Object[]) classGetRecordComponents.invokeExact(clazz);
    }

    @SneakyThrows
    public Class<?> recordComponentGetType(Object recordComponent) {
      return (Class<?>) recordComponentGetType.invokeExact(recordComponent);
    }

    @SneakyThrows
    public Type recordComponentGetGenericType(Object recordComponent) {
      return (Type) recordComponentGetGenericType.invokeExact(recordComponent);
    }

    @SneakyThrows
    public String recordComponentGetName(Object recordComponent) {
      return (String) recordComponentGetName.invokeExact(recordComponent);
    }

    @Override
    @SneakyThrows
    public <V> ConfigTypeAdapter<V> create(ConfigLoader configLoader, TypeToken<? super V> typeToken) {
      val rawType = ((TypeToken) typeToken).getRawType();
      if (!classIsRecord(rawType)) {
        return null;
      }
      val recordComponents = classGetRecordComponents(rawType);
      val recordComponentTypes = new Class<?>[recordComponents.length];
      val recordEntries = new TypedEntry[recordComponents.length];
      for (int i = 0; i < recordComponents.length; i++) {
        val recordComponent = recordComponents[i];
        val recordComponentType = recordComponentGetType(recordComponent);
        val recordComponentGenericType = recordComponentGetGenericType(recordComponent);
        val recordComponentTypeToken = TypeToken.get(recordComponentGenericType);
        val recordComponentName = recordComponentGetName(recordComponent);
        recordComponentTypes[i] = recordComponentType;
        recordEntries[i] = new TypedEntry(
            recordComponent,
            recordComponentName,
            configLoader.translateName(recordComponentName),
            recordComponentTypeToken,
            configLoader.getAdapter(recordComponentTypeToken)
        );
      }
      val constructor = BootstrapUtil.lookup()
          .findConstructor(rawType, MethodType.methodType(void.class, recordComponentTypes));
      return new RecordConfigAdapter<>(rawType, constructor, recordEntries);
    }
  }

  @RequiredArgsConstructor
  private static class TypedEntry {
    private final Object recordComponent;
    private final String beanName;
    private final String configName;
    private final TypeToken<?> typeToken;
    private final ConfigTypeAdapter<?> adapter;
  }
}
