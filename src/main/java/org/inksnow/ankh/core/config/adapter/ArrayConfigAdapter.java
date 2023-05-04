package org.inksnow.ankh.core.config.adapter;

import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigLoader;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigTypeAdapter;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ArrayConfigAdapter<T, E> implements ConfigTypeAdapter<T> {
  private final ConfigLoader loader;
  private final ConfigTypeAdapter<E> componentTypeAdapter;
  private final Class<E> componentType;

  @Override
  @SuppressWarnings("unchecked")
  public T read(ConfigSection section) {
    val list = section.asList()
        .stream()
        .map(componentTypeAdapter::read)
        .collect(Collectors.toList());

    if (componentType.isPrimitive()) {
      val array = Array.newInstance(componentType, list.size());
      for (int i = 0; i < list.size(); i++) {
        Array.set(array, i, list.get(i));
      }
      return (T) array;
    } else {
      val array = (E[]) Array.newInstance(componentType, list.size());
      return (T) list.toArray(array);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static class Factory implements ConfigTypeAdapter.Factory<Object> {
    @Override
    public ConfigTypeAdapter create(ConfigLoader configLoader, TypeToken typeToken) {
      val type = typeToken.getType();
      if (!(type instanceof GenericArrayType || type instanceof Class && ((Class<?>) type).isArray())) {
        return null;
      }

      val componentType = TypeUtils.getArrayComponentType(type);
      val componentTypeAdapter = configLoader.getAdapter(TypeToken.get(componentType));
      return new ArrayConfigAdapter(configLoader, componentTypeAdapter, TypeUtils.getRawType(componentType));
    }
  }
}
