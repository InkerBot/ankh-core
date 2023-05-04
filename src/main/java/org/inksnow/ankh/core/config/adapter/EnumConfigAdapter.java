package org.inksnow.ankh.core.config.adapter;

import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigLoader;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigTypeAdapter;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EnumConfigAdapter<T extends Enum<T>> implements ConfigTypeAdapter<T> {
  private final Map<String, T> nameToConstant = new HashMap<>();
  private final Map<String, T> stringToConstant = new HashMap<>();

  @SneakyThrows
  public EnumConfigAdapter(Class<T> classOfT) {
    val fields = classOfT.getDeclaredFields();
    val constantFields = Arrays.stream(fields)
        .filter(Field::isEnumConstant)
        .toArray(Field[]::new);
    AccessibleObject.setAccessible(constantFields, true);
    for (Field constantField : constantFields) {
      @SuppressWarnings("unchecked")
      T constant = (T) (constantField.get(null));
      nameToConstant.put(constant.name(), constant);
      stringToConstant.put(constant.toString(), constant);
    }
  }

  @Override
  public T read(ConfigSection section) {
    val key = section.asString();
    return Optional.ofNullable(nameToConstant.get(key))
        .orElseGet(() -> stringToConstant.get(key));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static class Factory implements ConfigTypeAdapter.Factory<Enum<?>> {
    @Override
    public ConfigTypeAdapter create(ConfigLoader configLoader, TypeToken typeToken) {
      Class rawType = typeToken.getRawType();
      if (!Enum.class.isAssignableFrom(rawType) || rawType == Enum.class) {
        return null;
      }
      if (!rawType.isEnum()) {
        rawType = rawType.getSuperclass();
      }
      return new EnumConfigAdapter(rawType);
    }
  }
}
