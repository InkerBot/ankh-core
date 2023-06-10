package org.inksnow.ankh.core.api.config;

import com.google.gson.reflect.TypeToken;

public interface ConfigTypeAdapter<T> {
  T read(ConfigSection section);

  default ConfigTypeAdapter<T> nullable() {
    ConfigTypeAdapter<T> delegate = this;
    return section -> (section == null || section.isNull()) ? null : delegate.read(section);
  }

  interface Factory<T> {
    <V extends T> ConfigTypeAdapter<V> create(ConfigLoader loader, TypeToken<? super V> typeToken);

    default Factory<T> nullable() {
      Factory<T> delegate = this;
      return new Factory<T>() {
        @Override
        public <V extends T> ConfigTypeAdapter<V> create(ConfigLoader loader, TypeToken<? super V> typeToken) {
          ConfigTypeAdapter<V> delegateAdapter = delegate.create(loader, typeToken);
          return delegateAdapter == null ? null : section -> (section == null || section.isNull()) ? null : delegateAdapter.read(section);
        }
      };
    }
  }
}
