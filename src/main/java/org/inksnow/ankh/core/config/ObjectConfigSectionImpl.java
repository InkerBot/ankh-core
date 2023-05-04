package org.inksnow.ankh.core.config;

import lombok.Getter;
import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigExtension;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigSource;
import org.inksnow.ankh.core.api.util.DcLazy;
import org.inksnow.ankh.core.common.config.LazilyParsedNumber;
import org.inksnow.ankh.core.config.typesafe.TypesafeConfigFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@lombok.Builder
public class ObjectConfigSectionImpl implements ConfigSection {
  @lombok.Builder.Default
  @Getter
  private final ConfigSource source = ConfigSource.builder()
      .description("(from code)")
      .build();
  @lombok.Builder.Default
  @Getter
  private final ConfigExtension extension = ConfigExtension.empty();
  @lombok.Builder.Default
  private final Object value = null;

  private final DcLazy<List<ConfigSection>> sectionList = DcLazy.of(this::provideSectionList);
  private final DcLazy<Map<String, ConfigSection>> sectionMap = DcLazy.of(this::provideSectionMap);
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
    return ObjectConfigSectionImpl.builder()
        .source(source.toBuilder()
            .path(source.path() + (isArray() ? ("[" + memberName + "]") : ("." + memberName)))
            .build())
        .value(element)
        .build();
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
      try {
        return sectionList.get().get(Integer.parseInt(memberName));
      } catch (IndexOutOfBoundsException e) {
        return null;
      }
    } else {
      return sectionMap.get().get(memberName);
    }
  }

  @Override
  public ConfigSection get(int index) {
    if (isArray()) {
      try {
        return sectionList.get().get(index);
      } catch (IndexOutOfBoundsException e) {
        return null;
      }
    } else {
      return sectionMap.get().get(Integer.toString(index));
    }
  }
}
