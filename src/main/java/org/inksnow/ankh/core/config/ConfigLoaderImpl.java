package org.inksnow.ankh.core.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.inksnow.ankh.core.api.config.*;
import org.inksnow.ankh.core.common.util.BootstrapUtil;
import org.inksnow.ankh.core.common.util.CacheMapUtil;
import org.inksnow.ankh.core.config.adapter.BaseTypeAdapters;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class ConfigLoaderImpl implements ConfigLoader {
  private static final Map<Class<?>, MethodHandle> classCacheConstructor = CacheMapUtil.make();

  private static final Function<Class<?>, MethodHandle> findNoArgsConstructor = new Function<Class<?>, MethodHandle>() {
    @Override
    @SneakyThrows
    public MethodHandle apply(Class<?> clazz) {
      return BootstrapUtil.lookup()
          .findConstructor(clazz, MethodType.methodType(void.class));
    }
  };

  private final ConfigService configService;
  private final Path baseDirectoryPath;
  private final ConfigNameStrategy nameStrategy;
  private final Map<Class<?>, Class<?>> implementationMap;
  private final List<ConfigTypeAdapter.Factory<?>> adapterFactories;

  private final Map<String, ConfigSection> sectionByPath = new HashMap<>();
  private final Map<TypeToken<?>, ConfigTypeAdapter<?>> adapterCache = new HashMap<>();
  private final Map<Class<?>, Supplier<?>> cacheConstructor = new HashMap<>();
  private final Function<Class<?>, Supplier<?>> findConstructorSupplier = new Function<Class<?>, Supplier<?>>() {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Supplier apply(Class rawType) {
      val implementationType = implementationMap.get(rawType);
      val usedType = implementationType != null ? implementationType.asSubclass(rawType) : rawType;
      val constructor = classCacheConstructor.computeIfAbsent(usedType, findNoArgsConstructor);
      return new Supplier() {
        @Override
        @SneakyThrows
        public Object get() {
          return constructor.invoke();
        }
      };
    }
  };

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public <T> ConfigTypeAdapter<? extends T> getAdapter(@Nonnull TypeToken<T> typeToken) {
    val cacheValue = adapterCache.get(typeToken);
    if (cacheValue != null) {
      return (ConfigTypeAdapter<? extends T>) cacheValue;
    }

    ConfigTypeAdapter adapter = null;
    for (ConfigTypeAdapter.Factory adapterFactory : adapterFactories) {
      adapter = adapterFactory.create(this, typeToken);
      if (adapter != null) {
        break;
      }
    }
    adapterCache.put(typeToken, adapter);
    return adapter;
  }

  private ConfigSection loadPath(ConfigSource source, String path) {
    Path baseDir = source == null ? null : source.file().getParent();
    if (baseDir == null) {
      baseDir = baseDirectoryPath;
    }
    val targetPath = baseDir.resolve(path);
    val identifyPath = baseDir.relativize(targetPath).toString();
    return sectionByPath.computeIfAbsent(identifyPath, it ->
        configService.readSectionFromPath(targetPath)
    );
  }

  @Override
  public ConfigSection load(@Nonnull String path) {
    return loadPath(null, path);
  }

  public ConfigSection load(@Nonnull ConfigSection coreSection) {
    if (coreSection.extension().includeList().isEmpty()) {
      return new LinkedConfigSectionImpl(this, Collections.singletonList(coreSection));
    } else {
      return new LinkedConfigSectionImpl(this, ImmutableList.<ConfigSection>builder()
          .add(coreSection)
          .addAll(coreSection.extension()
              .includeList()
              .stream()
              .map(it -> loadPath(coreSection.source(), it))
              .filter(Objects::nonNull)
              .iterator())
          .build()
          .reverse()
      );
    }
  }

  @Override
  public <T> T parse(@Nonnull String path, @Nonnull TypeToken<T> typeToken) {
    return getAdapter(typeToken).read(loadPath(null, path));
  }

  @Override
  public <T> T parse(@Nonnull String path, @Nonnull Class<T> type) {
    return getAdapter(TypeToken.get(type)).read(loadPath(null, path));
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Supplier getConstructor(@Nonnull TypeToken type) {
    return cacheConstructor.computeIfAbsent(type.getRawType(), findConstructorSupplier);
  }

  @Override
  public @Nonnull String translateName(@Nonnull String name) {
    return nameStrategy.translateName(name);
  }

  @Singleton
  public static class Factory implements ConfigLoader.Factory {
    private final ConfigService configService;

    @Inject
    private Factory(ConfigService configService) {
      this.configService = configService;
    }

    public @Nonnull Builder builder() {
      return new Builder(configService);
    }
  }

  @RequiredArgsConstructor
  public static class Builder implements ConfigLoader.Builder {
    private final ConfigService configService;
    private final List<ConfigTypeAdapter.Factory<?>> userFactories = new LinkedList<>();
    private final Map<Class<?>, Class<?>> userImplementationMap = new HashMap<>();
    @Setter
    private Path baseDirectory;
    @Setter
    private ConfigNameStrategy nameStrategy = ConfigNameStrategy.identity();

    public @Nonnull Builder registerFactory(@Nonnull ConfigTypeAdapter.Factory<?> factory) {
      userFactories.add(factory);
      return this;
    }

    @Override
    public @Nonnull Builder registerFactory(@Nonnull ConfigTypeAdapter.Factory<?>... factories) {
      userFactories.addAll(Arrays.asList(factories));
      return this;
    }

    @Override
    public @Nonnull Builder registerFactory(@Nonnull Collection<ConfigTypeAdapter.Factory<?>> factories) {
      userFactories.addAll(factories);
      return this;
    }

    public @Nonnull <T> Builder registerUserImplementation(@Nonnull Class<T> base, @Nonnull Class<? extends T> impl) {
      userImplementationMap.put(base, impl);
      return this;
    }

    @Override
    public @Nonnull Builder getThis() {
      return this;
    }

    private <T> void put(ImmutableMap.Builder<Class<?>, Class<?>> builder, Class<T> base, Class<? extends T> impl) {
      if (userImplementationMap.get(base) == null) {
        builder.put(base, impl);
      }
    }

    @Override
    public @Nonnull ConfigLoader build() {
      val factoryListBuilder = ImmutableList.<ConfigTypeAdapter.Factory<?>>builder();
      val implementationMapBuilder = ImmutableMap.<Class<?>, Class<?>>builder();

      factoryListBuilder.add(BaseTypeAdapters.CONFIG_SECTION);

      factoryListBuilder.addAll(userFactories);

      factoryListBuilder.add(
          BaseTypeAdapters.NUMBER, BaseTypeAdapters.INTEGER, BaseTypeAdapters.BYTE, BaseTypeAdapters.CHARACTER,
          BaseTypeAdapters.BOOLEAN, BaseTypeAdapters.DOUBLE, BaseTypeAdapters.FLOAT, BaseTypeAdapters.LONG,
          BaseTypeAdapters.BIG_DECIMAL, BaseTypeAdapters.BIG_INTEGER, BaseTypeAdapters.STRING,
          BaseTypeAdapters.ATOMIC_BOOLEAN, BaseTypeAdapters.ATOMIC_INTEGER, BaseTypeAdapters.ATOMIC_INTEGER_ARRAY,
          BaseTypeAdapters.ATOMIC_LONG, BaseTypeAdapters.ATOMIC_LONG_ARRAY, BaseTypeAdapters.STRING_BUFFER,
          BaseTypeAdapters.STRING_BUILDER, BaseTypeAdapters.URL, BaseTypeAdapters.URI, BaseTypeAdapters.UUID,
          BaseTypeAdapters.CURRENCY, BaseTypeAdapters.LOCALE, BaseTypeAdapters.INET_ADDRESS, BaseTypeAdapters.BIT_SET,
          BaseTypeAdapters.DATE, BaseTypeAdapters.CALENDAR, BaseTypeAdapters.ARRAY, BaseTypeAdapters.COLLECTION,
          BaseTypeAdapters.MAP, BaseTypeAdapters.CODEC, BaseTypeAdapters.ENUM, BaseTypeAdapters.INTERFACE,
          BaseTypeAdapters.RECORD, BaseTypeAdapters.OBJECT, BaseTypeAdapters.NULL
      );

      implementationMapBuilder.putAll(userImplementationMap);

      put(implementationMapBuilder, Map.class, LinkedHashMap.class);
      put(implementationMapBuilder, Collection.class, ArrayList.class);
      put(implementationMapBuilder, List.class, ArrayList.class);
      put(implementationMapBuilder, Set.class, LinkedHashSet.class);

      return new ConfigLoaderImpl(configService, baseDirectory, nameStrategy, implementationMapBuilder.build(), factoryListBuilder.build());
    }
  }
}
