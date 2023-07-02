package org.inksnow.ankh.core.common;

import com.google.inject.Binding;
import com.google.inject.name.Names;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang.StringUtils;
import org.inksnow.ankh.core.api.AnkhServiceLoader;
import org.inksnow.ankh.core.api.util.DcLazy;
import org.inksnow.ankh.core.common.config.AnkhConfig;
import org.inksnow.ankh.core.common.util.CheckUtil;
import org.inksnow.ankh.core.common.util.LazyProxyUtil;
import org.inksnow.ankh.core.loader.AnkhPluginLoader;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Singleton
@Slf4j
public class AnkhServiceLoaderImpl implements AnkhServiceLoader {
  private static final AnkhConfig config = AnkhConfig.instance();
  private static final AtomicReference<Map<KeyCacheKey, Object>> keyInstanceMap = new AtomicReference<>(new HashMap<>());
  private static final AtomicReference<Map<StringCacheKey, Object>> stringInstanceMap = new AtomicReference<>(new HashMap<>());
  private static final AtomicReference<Map<KeyCacheKey, Object>> keyCacheMap = new AtomicReference<>(new ConcurrentHashMap<>());
  private static final AtomicReference<Map<StringCacheKey, Object>> stringCacheMap = new AtomicReference<>(new ConcurrentHashMap<>());
  private static final Map<Class<?>, Object> configLoadService = new ConcurrentHashMap<>();
  private static final Map<Class<?>, List<Object>> configListService = new ConcurrentHashMap<>();
  private static final Function<StringCacheKey, Object> stringLoadFunction = key -> {
    if (!key.clazz.isInterface()) {
      throw new IllegalArgumentException("service class must be interface");
    }
    val byInstanceMap = stringInstanceMap.get().get(key);
    if(byInstanceMap != null){
      return byInstanceMap;
    }
    val injector = AnkhPluginLoader.instance().injector();
    for (val entry : injector.getBindings().entrySet()) {
      if (key.clazz != entry.getKey().getTypeLiteral().getRawType()) {
        continue;
      }
      val named = getNamedAnnotation(entry.getKey().getAnnotation());
      if(named == null){
        continue;
      }
      if(named[1].equals(key.key)){
        return entry.getValue().getProvider().get();
      }
    }
    return null;
  };
  private static final Function<KeyCacheKey, Object> keyLoadFunction = key -> {
    if (!key.clazz.isInterface()) {
      throw new IllegalArgumentException("service class must be interface");
    }
    val byInstanceMap = keyInstanceMap.get().get(key);
    if (byInstanceMap != null) {
      return byInstanceMap;
    }
    val injector = AnkhPluginLoader.instance().injector();
    for (val entry : injector.getBindings().entrySet()) {
      if (key.clazz != entry.getKey().getTypeLiteral().getRawType()) {
        continue;
      }
      val named = getNamedAnnotation(entry.getKey().getAnnotation());
      if(named == null){
        continue;
      }
      if(named[0].equals(key.namespace) && named[1].equals(key.value)){
        return entry.getValue().getProvider().get();
      }
    }
    return null;
  };
  private static final Function<Class<?>, Object> configLoadFunction = clazz -> {
    if (!clazz.isInterface()) {
      throw new IllegalArgumentException("service class must be interface");
    }
    val serviceName = getServiceName(clazz);
    val loadConfigValue = config.service().get(serviceName);
    if (loadConfigValue == null) {
      throw new IllegalStateException(
          "Failed to load service " + serviceName + ", no config found."
      );
    }
    return LazyProxyUtil.generate(clazz, (DcLazy) DcLazy.of(() -> staticLoadService(loadConfigValue, clazz)));
  };
  private static final Function<Class<?>, List<Object>> configListLoadFunction = clazz -> {
    if (!clazz.isInterface()) {
      throw new IllegalArgumentException("service class must be interface");
    }
    val serviceName = getServiceName(clazz);
    return LazyProxyUtil.generate(List.class, DcLazy.of(() -> {
      val resultList = new ArrayList<>();
      val injector = AnkhPluginLoader.instance().injector();
      for (val entry : injector.getBindings().entrySet()) {
        if (clazz != entry.getKey().getTypeLiteral().getRawType()) {
          continue;
        }
        val named = getNamedAnnotation(entry.getKey().getAnnotation());
        if(named == null){
          continue;
        }
        if(nullOrTrue(config.service().get(serviceName + "@" + named[1]))
            && nullOrTrue(config.service().get(serviceName + "@" + named[0] + ":" + named[1]))){
          resultList.add(entry.getValue().getProvider().get());
        }
      }
      return resultList;
    }));
  };

  public static <T> void staticRegisterService(@Nonnull Key key, @Nonnull Class<T> serviceClass, T instance) {
    CheckUtil.ensureMainThread();
    { // register key instance map
      val cacheKey = new KeyCacheKey(key, serviceClass);
      val rawMap = keyInstanceMap.get();
      if (rawMap.containsKey(cacheKey)) {
        throw new IllegalStateException("Register service '" + serviceClass + "' key '" + key + "' multi times");
      }
      val newMap = new HashMap<KeyCacheKey, Object>(rawMap.size() + 1);
      newMap.putAll(rawMap);
      newMap.put(new KeyCacheKey(key, serviceClass), instance);
      keyInstanceMap.set(newMap);
    }
    { // register string instance map
      val cacheKey = new StringCacheKey(key.value(), serviceClass);
      val rawMap = stringInstanceMap.get();
      if (!rawMap.containsKey(cacheKey)) {
        val newMap = new HashMap<StringCacheKey, Object>(rawMap.size() + 1);
        newMap.putAll(rawMap);
        newMap.put(new StringCacheKey(key.value(), serviceClass), instance);
        stringInstanceMap.set(newMap);
      }
    }
    keyCacheMap.set(new ConcurrentHashMap<>());
    stringCacheMap.set(new ConcurrentHashMap<>());
  }

  @SuppressWarnings("unchecked")
  public static <T> @Nonnull T staticLoadService(@Nonnull String key, @Nonnull Class<T> clazz) {
    if (key.contains(":")) {
      return staticLoadService(Key.key(key), clazz);
    }
    val result = (T) stringCacheMap.get().computeIfAbsent(new StringCacheKey(key, clazz), stringLoadFunction);
    if (result == null) {
      throw new IllegalStateException("No named service " + key + " " + clazz + " found");
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public static <T> @Nonnull T staticLoadService(@Nonnull Key key, @Nonnull Class<T> clazz) {
    val result = (T) keyCacheMap.get().computeIfAbsent(new KeyCacheKey(key, clazz), keyLoadFunction);
    if (result == null) {
      throw new IllegalStateException("No named service " + key + " " + clazz + " found");
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public static <T> @Nonnull T staticService(@Nonnull Class<T> clazz) {
    val result = (T) configLoadService.computeIfAbsent(clazz, configLoadFunction);
    if (result == null) {
      throw new IllegalStateException("No config service " + clazz + " found");
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public static <T> @Nonnull List<T> staticSerivceList(@Nonnull Class<T> clazz) {
    return (List<T>) configListService.computeIfAbsent(clazz, configListLoadFunction);
  }

  private static boolean nullOrTrue(String value){
    return value == null || Boolean.parseBoolean(value);
  }

  private static String[] getNamedAnnotation(Annotation annotation){
    String name;
    if (annotation instanceof javax.inject.Named) {
      name = ((javax.inject.Named) annotation).value();
    } else if (annotation instanceof com.google.inject.name.Named) {
      name = ((com.google.inject.name.Named) annotation).value();
    } else {
      return null;
    }
    val key = StringUtils.split(name, ':');
    if(key.length == 2){
      return key;
    }else{
      logger.warn("named annotation key failed to match");
      return null;
    }
  }

  private static String getServiceName(Class<?> clazz) {
    for (val named : clazz.getAnnotationsByType(javax.inject.Named.class)) {
      return named.value();
    }
    for (val named : clazz.getAnnotationsByType(com.google.inject.name.Named.class)) {
      return named.value();
    }
    return translateName(clazz.getSimpleName(), '-').toLowerCase(Locale.ENGLISH);
  }

  private static String translateName(String name, char separator) {
    StringBuilder translation = new StringBuilder();
    for (int i = 0; i < name.length(); i++) {
      char character = name.charAt(i);
      if (Character.isUpperCase(character) && translation.length() != 0) {
        translation.append(separator);
      }
      translation.append(character);
    }
    return translation.toString();
  }

  @Override
  public <T> void registerServiceImpl(@Nonnull Key key, @Nonnull Class<T> serviceClass, T instance) {
    staticRegisterService(key, serviceClass, instance);
  }

  @Override
  public <T> T loadServiceImpl(@Nonnull String key, @Nonnull Class<T> clazz) {
    return staticLoadService(key, clazz);
  }

  @Override
  public <T> T loadServiceImpl(@Nonnull Key key, @Nonnull Class<T> clazz) {
    return staticLoadService(key, clazz);
  }

  @Override
  public <T> @Nonnull T serviceImpl(@Nonnull Class<T> clazz) {
    return staticService(clazz);
  }

  @Override
  public @Nonnull <T> List<T> serviceListImpl(@Nonnull Class<T> clazz) {
    return staticSerivceList(clazz);
  }

  private static class StringCacheKey {
    private final @Nonnull String key;
    private final @Nonnull Class<?> clazz;
    private final int hashCode;

    private StringCacheKey(@Nonnull String key, @Nonnull Class<?> clazz) {
      this.key = key;
      this.clazz = clazz;
      this.hashCode = 31 * key.hashCode() + clazz.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      StringCacheKey that = (StringCacheKey) o;

      if (!key.equals(that.key)) return false;
      return clazz.equals(that.clazz);
    }

    @Override
    public int hashCode() {
      return hashCode;
    }
  }

  private static class KeyCacheKey {
    private final @Nonnull String namespace;
    private final @Nonnull String value;
    private final @Nonnull Class<?> clazz;
    private final int hashCode;

    private KeyCacheKey(@Nonnull Key key, @Nonnull Class<?> clazz) {
      this.namespace = key.namespace();
      this.value = key.value();
      this.clazz = clazz;
      this.hashCode = 31 * (31 * this.namespace.hashCode() + this.value.hashCode()) + clazz.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      KeyCacheKey keyCacheKey = (KeyCacheKey) o;

      if (!this.namespace.equals(keyCacheKey.namespace)) return false;
      if (!this.value.equals(keyCacheKey.value)) return false;
      return clazz.equals(keyCacheKey.clazz);
    }

    @Override
    public int hashCode() {
      return hashCode;
    }
  }
}
