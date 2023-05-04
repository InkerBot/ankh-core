package org.inksnow.ankh.core.api.config;

import com.google.gson.reflect.TypeToken;

import java.util.function.Supplier;

public interface ConfigLoader {
  ConfigSection load(String path);
  <T> T parse(String path, TypeToken<T> typeToken);

  <T> T parse(String path, Class<T> type);

  <T> ConfigTypeAdapter<? extends T> getAdapter(TypeToken<T> typeToken);

  <T> Supplier<T> getConstructor(TypeToken<? super T> type);
}
