package org.inksnow.ankh.core.api.config;

import com.google.gson.reflect.TypeToken;

public interface ConfigTypeAdapter<T> {
  T read(ConfigSection section);

  interface Factory<T> {
    <V extends T> ConfigTypeAdapter<V> create(ConfigLoader configLoader, TypeToken<? super V> typeToken);
  }
}
