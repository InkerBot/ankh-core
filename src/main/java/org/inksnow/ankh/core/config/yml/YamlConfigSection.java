package org.inksnow.ankh.core.config.yml;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigExtension;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigSource;
import org.inksnow.ankh.core.api.util.DcLazy;
import org.inksnow.ankh.core.common.config.LazilyParsedNumber;
import org.inksnow.ankh.core.config.typesafe.TypesafeConfigFactory;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class YamlConfigSection implements ConfigSection {
  @Getter
  private final ConfigSource source;
  private final Object value;

  private final DcLazy<ConfigExtension> extension = DcLazy.of(this::provideExtension);
  private final DcLazy<List<ConfigSection>> sectionList = DcLazy.of(this::provideSectionList);
  private final DcLazy<Map<String, ConfigSection>> sectionMap = DcLazy.of(this::provideSectionMap);

  public @Nonnull ConfigExtension extension() {
    return extension.get();
  }

  private ConfigExtension provideExtension() {
    ConfigExtension extension = ConfigExtension.empty();
    if (value instanceof Map) {
      val configObject = (Map<String, Object>) value;
      val extConfig = configObject.get(SnakeYmlConfigFactory.INTERNAL_EXTENSION_PREFIX);
      if (extConfig != null) {
        if (extConfig instanceof Map) {
          val extConfigObject = (Map<String, Object>) extConfig;
          for (val entry : extConfigObject.entrySet()) {
            extension = extension.include(entry.getKey());
          }
        } else if (extConfig instanceof List) {
          val extConfigList = (List) extConfig;
          for (val entry : extConfigList) {
            if (entry instanceof String || entry instanceof Number) {
              extension = extension.include(entry.toString());
            }
          }
        } else if (extConfig instanceof String || extConfig instanceof Number) {
          extension = extension.include(extConfig.toString());
        }
      }
    }
    return extension;
  }

  @Override
  public boolean isNull() {
    return value == null;
  }

  @Override
  public boolean isPrimitive() {
    return value instanceof String || value instanceof Number || value instanceof Boolean;
  }

  private void ensurePrimitive() {
    if (!isPrimitive()) {
      throw new IllegalStateException(source + " isn't a primitive value");
    }
  }

  @Override
  public int asInteger() {
    return value instanceof Number
        ? ((Number) value).intValue()
        : Integer.parseInt(asString());
  }

  @Override
  public byte asByte() {
    return value instanceof Number
        ? ((Number) value).byteValue()
        : Byte.parseByte(asString());
  }

  @Override
  public char asCharacter() {
    return value instanceof Number
        ? (char) ((Number) value).shortValue()
        : asString().charAt(0);
  }

  @Override
  public boolean asBoolean() {
    return value instanceof Boolean
        ? (Boolean) value
        : Boolean.parseBoolean(asString());
  }

  @Override
  public Number asNumber() {
    return value instanceof Number
        ? (Number) value
        : new LazilyParsedNumber(asString());
  }

  @Override
  public String asString() {
    ensurePrimitive();
    if (value instanceof String) {
      return (String) value;
    } else if (value instanceof Boolean) {
      return ((Boolean) value).toString();
    } else if (value instanceof Number) {
      return value.toString();
    }
    throw new IllegalStateException("Failed to detect config value type " + value.getClass().getSimpleName());
  }

  @Override
  public double asDouble() {
    return value instanceof Number
        ? ((Number) value).doubleValue()
        : Double.parseDouble(asString());
  }

  @Override
  public BigDecimal asBigDecimal() {
    return (value instanceof BigDecimal)
        ? (BigDecimal) value
        : new BigDecimal(asString());
  }

  @Override
  public BigInteger asBigInteger() {
    return (value instanceof BigInteger)
        ? (BigInteger) value
        : new BigInteger(asString());
  }

  @Override
  public float asFloat() {
    return value instanceof Number
        ? ((Number) value).floatValue()
        : Float.parseFloat(asString());
  }

  @Override
  public long asLong() {
    return value instanceof Number
        ? ((Number) value).longValue()
        : Long.parseLong(asString());
  }

  @Override
  public short asShort() {
    return value instanceof Number
        ? ((Number) value).shortValue()
        : Short.parseShort(asString());
  }

  @Override
  public boolean isObject() {
    return value instanceof Map;
  }

  @Override
  public boolean isArray() {
    return value instanceof List;
  }

  private void ensureArray() {
    if (!isArray()) {
      throw new IllegalStateException(source + " isn't a array value");
    }
  }

  private void ensureObject() {
    if (!isObject()) {
      throw new IllegalStateException(source + " isn't a object value");
    }
  }

  private ConfigSection provideSubSection(String memberName, Object element) {
    return new YamlConfigSection(
        source.toBuilder()
            .path(source.path() + (isArray() ? ("[" + memberName + "]") : ("." + memberName)))
            .build(),
        element
    );
  }

  private Map<String, ConfigSection> provideSectionMap() {
    if (isArray()) {
      val map = new HashMap<String, ConfigSection>(sectionList.get().size());
      for (int i = 0; i < sectionList.get().size(); i++) {
        map.put(Integer.toString(i), sectionList.get().get(i));
      }
      return map;
    } else {
      ensureObject();
      val obj = (Map<String, Object>) value;
      return obj.entrySet()
          .stream()
          .filter(it -> !TypesafeConfigFactory.INTERNAL_EXTENSION_PREFIX.equals(it.getKey()))
          .collect(Collectors.collectingAndThen(
              Collectors.toMap(
                  Map.Entry::getKey,
                  it -> provideSubSection(it.getKey(), it.getValue())
              ),
              Collections::unmodifiableMap
          ));
    }
  }

  private List<ConfigSection> provideSectionList() {
    ensureArray();
    val configArray = (List<Object>) value;
    val list = new ArrayList<ConfigSection>(configArray.size());
    for (int i = 0; i < configArray.size(); i++) {
      list.add(provideSubSection(Integer.toString(i), configArray.get(i)));
    }
    return list;
  }

  @Override
  public Set<Map.Entry<String, ConfigSection>> entrySet() {
    return sectionMap.get().entrySet();
  }

  @Override
  public List<ConfigSection> asList() {
    return sectionList.get();
  }

  @Override
  public int size() {
    if (isArray()) {
      return sectionList.get().size();
    } else {
      return sectionMap.get().size();
    }
  }

  @Override
  public boolean has(String memberName) {
    if (isArray()) {
      return Integer.parseInt(memberName) < sectionList.get().size();
    } else {
      return sectionMap.get().containsKey(memberName);
    }
  }

  @Override
  public boolean has(int index) {
    if (isArray()) {
      return index < sectionList.get().size();
    } else {
      return sectionMap.get().containsKey(Integer.toString(index));
    }
  }

  @Override
  public ConfigSection get(String memberName) {
    if (isArray()) {
      return sectionList.get().get(Integer.parseInt(memberName));
    } else {
      return sectionMap.get().get(memberName);
    }
  }

  @Override
  public ConfigSection get(int index) {
    if (isArray()) {
      return sectionList.get().get(index);
    } else {
      return sectionMap.get().get(Integer.toString(index));
    }
  }
}
