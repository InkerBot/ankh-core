package org.inksnow.ankh.core.config.adapter;

import com.google.gson.reflect.TypeToken;
import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigLoader;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigTypeAdapter;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CollectionConfigAdapter<E> implements ConfigTypeAdapter<Collection<E>> {
  private final Supplier<? extends Collection<E>> constructor;
  private final ConfigTypeAdapter<E> elementTypeAdapter;

  public CollectionConfigAdapter(Supplier<? extends Collection<E>> constructor, ConfigTypeAdapter<E> elementTypeAdapter) {
    this.constructor = constructor;
    this.elementTypeAdapter = elementTypeAdapter;
  }

  @Override
  public Collection<E> read(ConfigSection section) {
    return section.asList()
        .stream()
        .map(elementTypeAdapter::read)
        .collect(Collectors.toCollection(constructor));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static class Factory implements ConfigTypeAdapter.Factory<Collection<?>> {
    @Override
    public ConfigTypeAdapter create(ConfigLoader configLoader, TypeToken typeToken) {
      val type = typeToken.getType();
      val rawType = typeToken.getRawType();
      if (!Collection.class.isAssignableFrom(rawType)) {
        return null;
      }
      val constructor = configLoader.getConstructor(typeToken);
      val elementType = TypeUtils.getCollectionElementType(type, rawType);
      val elementTypeAdapter = configLoader.getAdapter(TypeToken.get(elementType));
      return new CollectionConfigAdapter(constructor, elementTypeAdapter);
    }
  }
}
