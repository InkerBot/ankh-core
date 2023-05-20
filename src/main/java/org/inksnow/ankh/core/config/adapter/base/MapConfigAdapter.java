package org.inksnow.ankh.core.config.adapter.base;

import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigLoader;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigTypeAdapter;
import org.inksnow.ankh.core.config.ObjectConfigSectionImpl;

import java.util.Map;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class MapConfigAdapter<K, V> implements ConfigTypeAdapter<Map<K, V>> {
  private final Supplier<? extends Map<K, V>> constructor;
  private final ConfigTypeAdapter<K> keyAdapter;
  private final ConfigTypeAdapter<V> valueAdapter;

  @Override
  public Map<K, V> read(ConfigSection section) {
    val map = constructor.get();
    for (val entry : section.entrySet()) {
      map.put(
          keyAdapter.read(ObjectConfigSectionImpl.builder()
              .value(entry.getKey())
              .source(entry.getValue().source())
              .build()),
          valueAdapter.read(entry.getValue())
      );
    }
    return map;
  }

  public static class Factory implements ConfigTypeAdapter.Factory<Map<?, ?>> {
    @Override
    public ConfigTypeAdapter create(ConfigLoader configLoader, TypeToken typeToken) {
      val type = typeToken.getType();
      val rawType = typeToken.getRawType();
      if (!Map.class.isAssignableFrom(rawType)) {
        return null;
      }
      val constructor = configLoader.getConstructor(typeToken);
      val keyAndValueTypes = TypeUtils.getMapKeyAndValueTypes(type, rawType);
      val keyAdapter = configLoader.getAdapter(TypeToken.get(keyAndValueTypes[0]));
      val valueAdapter = configLoader.getAdapter(TypeToken.get(keyAndValueTypes[1]));
      return new MapConfigAdapter(constructor, keyAdapter, valueAdapter);
    }
  }
}
