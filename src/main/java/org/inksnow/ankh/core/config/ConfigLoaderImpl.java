package org.inksnow.ankh.core.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.inksnow.ankh.core.api.config.*;
import org.inksnow.ankh.core.common.util.BootstrapUtil;
import org.inksnow.ankh.core.common.util.CacheMapUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class ConfigLoaderImpl implements ConfigLoader {
  private static final List<ConfigTypeAdapter.Factory<?>> DEFAULT_FACTORY_LIST = ImmutableList.of(
      AnkhTypeAdapters.CONFIG_SECTION, AnkhTypeAdapters.NUMBER, AnkhTypeAdapters.INTEGER, AnkhTypeAdapters.BYTE,
      AnkhTypeAdapters.CHARACTER, AnkhTypeAdapters.BOOLEAN, AnkhTypeAdapters.DOUBLE, AnkhTypeAdapters.FLOAT,
      AnkhTypeAdapters.LONG, AnkhTypeAdapters.BIG_DECIMAL, AnkhTypeAdapters.BIG_INTEGER, AnkhTypeAdapters.STRING,
      AnkhTypeAdapters.ATOMIC_BOOLEAN, AnkhTypeAdapters.ATOMIC_INTEGER, AnkhTypeAdapters.ATOMIC_INTEGER_ARRAY,
      AnkhTypeAdapters.ATOMIC_LONG, AnkhTypeAdapters.ATOMIC_LONG_ARRAY, AnkhTypeAdapters.STRING_BUFFER,
      AnkhTypeAdapters.STRING_BUILDER, AnkhTypeAdapters.URL, AnkhTypeAdapters.URI, AnkhTypeAdapters.UUID,
      AnkhTypeAdapters.CURRENCY, AnkhTypeAdapters.LOCALE, AnkhTypeAdapters.INET_ADDRESS, AnkhTypeAdapters.BIT_SET,
      AnkhTypeAdapters.DATE, AnkhTypeAdapters.CALENDAR, AnkhTypeAdapters.ARRAY, AnkhTypeAdapters.COLLECTION,
      AnkhTypeAdapters.MAP, AnkhTypeAdapters.ENUM, AnkhTypeAdapters.INTERFACE, AnkhTypeAdapters.RECORD,
      AnkhTypeAdapters.OBJECT, AnkhTypeAdapters.NULL
  );
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
  private static final Map<Class<?>, Supplier<?>> cacheConstructor = CacheMapUtil.make();
  private final Map<String, ConfigSection> sectionByPath;
  private final Map<Class<?>, Class<?>> implementationMap;
  private final List<ConfigTypeAdapter.Factory<?>> adapterFactories;
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

  public ConfigLoaderImpl(ConfigService configService, Path baseDirectory) {
    this.configService = configService;
    this.baseDirectoryPath = baseDirectory;
    this.implementationMap = new HashMap<>();
    this.sectionByPath = new HashMap<>();
    this.adapterFactories = new LinkedList<>(DEFAULT_FACTORY_LIST);
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public <T> ConfigTypeAdapter<? extends T> getAdapter(TypeToken<T> typeToken) {
    for (ConfigTypeAdapter.Factory adapterFactory : adapterFactories) {
      ConfigTypeAdapter adapter = adapterFactory.create(this, typeToken);
      if (adapter != null) {
        return adapter;
      }
    }
    return null;
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
  public ConfigSection load(String path) {
    return loadPath(null, path);
  }

  public ConfigSection load(ConfigSection coreSection) {
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
  public <T> T parse(String path, TypeToken<T> typeToken) {
    return getAdapter(typeToken).read(loadPath(null, path));
  }

  @Override
  public <T> T parse(String path, Class<T> type) {
    return getAdapter(TypeToken.get(type)).read(loadPath(null, path));
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Supplier getConstructor(TypeToken type) {
    return cacheConstructor.computeIfAbsent(type.getRawType(), findConstructorSupplier);
  }
}
