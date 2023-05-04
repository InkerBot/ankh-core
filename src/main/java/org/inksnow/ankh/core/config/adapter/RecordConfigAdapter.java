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
import java.lang.reflect.Type;

@RequiredArgsConstructor
@Slf4j
public class RecordConfigAdapter<T> implements ConfigTypeAdapter<T> {
  private final Class<?> clazz;
  private final MethodHandle constructor;
  private final TypedEntry[] typedEntries;

  @Override
  @SneakyThrows
  @SuppressWarnings("unchecked")
  public T read(ConfigSection section) {
    val args = new Object[typedEntries.length];
    boolean success = true;
    for (int i = 0; i < typedEntries.length; i++) {
      val typedEntry = typedEntries[i];
      if (typedEntry.adapter != null) {
        val subSection = section.get(typedEntry.name);
        val value = typedEntry.adapter.read(subSection);
        val validateResult = ConfigVaildatorUtils.validator().validateValue(clazz, typedEntry.name, value);
        for (val violation : validateResult) {
          logger.error("Failed to check config: {}\n\tat {}", violation.getMessage(), subSection.source());
          success = false;
        }
        args[i] = value;
      }
    }
    if(success) {
      return (T) constructor.invokeWithArguments(args);
    }else{
      throw new IllegalArgumentException("Failed to check config");
    }
  }

  public static class Factory implements ConfigTypeAdapter.Factory<Object> {
    private static final ConfigTypeAdapter.Factory<Object> delegate = provideDelegate();

    private static ConfigTypeAdapter.Factory<Object> provideDelegate() {
      try{
        Class.forName("java.lang.Record", false, null);
        return new ImplFactory();
      }catch (ClassNotFoundException e){
        return null;
      }
    }

    @Override
    public <V> ConfigTypeAdapter<V> create(ConfigLoader configLoader, TypeToken<? super V> typeToken) {
      return delegate != null ? delegate.create(configLoader, typeToken) : null;
    }
  }

  private static class ImplFactory implements ConfigTypeAdapter.Factory<Object> {
    private final MethodHandle classIsRecord = BootstrapUtil.ofVirtual("Ljava/lang/Class;isRecord()Z");
    private final MethodHandle classGetRecordComponents = BootstrapUtil.ofVirtual("Ljava/lang/Class;getRecordComponents()[Ljava/lang/reflect/RecordComponent;")
        .asType(MethodType.methodType(Object[].class, Class.class));
    private final MethodHandle recordComponentGetType = BootstrapUtil.ofVirtual("Ljava/lang/reflect/RecordComponent;getType()Ljava/lang/Class;")
        .asType(MethodType.methodType(Class.class, Object.class));
    private final MethodHandle recordComponentGetGenericType = BootstrapUtil.ofVirtual("Ljava/lang/reflect/RecordComponent;getGenericType()Ljava/lang/reflect/Type;")
        .asType(MethodType.methodType(Type.class, Object.class));
    private final MethodHandle recordComponentGetName = BootstrapUtil.ofVirtual("Ljava/lang/reflect/RecordComponent;getName()Ljava/lang/String;")
        .asType(MethodType.methodType(String.class, Object.class));

    @SneakyThrows
    public boolean classIsRecord(Class<?> clazz){
      return (boolean) classIsRecord.invokeExact(clazz);
    }

    @SneakyThrows
    public Object[] classGetRecordComponents(Class<?> clazz){
      return (Object[]) classGetRecordComponents.invokeExact(clazz);
    }

    @SneakyThrows
    public Class<?> recordComponentGetType(Object recordComponent){
      return (Class<?>) recordComponentGetType.invokeExact(recordComponent);
    }

    @SneakyThrows
    public Type recordComponentGetGenericType(Object recordComponent){
      return (Type) recordComponentGetGenericType.invokeExact(recordComponent);
    }

    @SneakyThrows
    public String recordComponentGetName(Object recordComponent){
      return (String) recordComponentGetName.invokeExact(recordComponent);
    }

    @Override
    @SneakyThrows
    public <V> ConfigTypeAdapter<V> create(ConfigLoader configLoader, TypeToken<? super V> typeToken) {
      val rawType = typeToken.getRawType();
      if(!classIsRecord(rawType)) {
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
        recordComponentTypes[i] = recordComponentType;
        recordEntries[i] = new TypedEntry(
            recordComponent,
            recordComponentGetName(recordComponent),
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
    private final String name;
    private final TypeToken<?> typeToken;
    private final ConfigTypeAdapter<?> adapter;
  }
}
