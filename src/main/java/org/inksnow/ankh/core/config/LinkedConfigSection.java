package org.inksnow.ankh.core.config;

import org.inksnow.ankh.core.api.config.ConfigExtension;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigSource;
import org.inksnow.ankh.core.api.util.DcLazy;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LinkedConfigSection implements ConfigSection {
  private final ConfigLoader configLoader;
  private final List<ConfigSection> mergeList;
  private final DcLazy<Map<String, ConfigSection>> entryMap = DcLazy.of(this::provideEntryMap);
  private final DcLazy<List<ConfigSection>> entryList = DcLazy.of(this::provideEntryList);

  public LinkedConfigSection(ConfigLoader configLoader, List<ConfigSection> mergeList) {
    this.configLoader = configLoader;
    this.mergeList = mergeList;
  }

  private boolean isEmpty() {
    return mergeList.isEmpty();
  }

  @Override
  public @Nonnull ConfigSource source() {
    return isEmpty() ? ConfigSource.builder().build() : mergeList.get(0).source();
  }

  @Nonnull
  @Override
  public ConfigExtension extension() {
    return ConfigExtension.empty();
  }

  @Override
  public boolean isArray() {
    return mergeList.stream()
        .anyMatch(ConfigSection::isArray);
  }

  @Override
  public boolean isObject() {
    return mergeList.stream()
        .anyMatch(ConfigSection::isObject);
  }

  @Override
  public boolean isPrimitive() {
    return mergeList.stream()
        .anyMatch(ConfigSection::isPrimitive);
  }

  @Override
  public boolean isNull() {
    return !mergeList.stream()
        .allMatch(ConfigSection::isNull);
  }

  @Override
  public int asInteger() {
    return mergeList.stream()
        .filter(ConfigSection::isPrimitive)
        .findFirst()
        .map(ConfigSection::asInteger)
        .orElseThrow(() -> new IllegalStateException("Failed to get primitive from no primitive type"));
  }

  @Override
  public byte asByte() {
    return mergeList.stream()
        .filter(ConfigSection::isPrimitive)
        .findFirst()
        .map(ConfigSection::asByte)
        .orElseThrow(() -> new IllegalStateException("Failed to get primitive from no primitive type"));
  }

  @Override
  public char asCharacter() {
    return mergeList.stream()
        .filter(ConfigSection::isPrimitive)
        .findFirst()
        .map(ConfigSection::asCharacter)
        .orElseThrow(() -> new IllegalStateException("Failed to get primitive from no primitive type"));
  }

  @Override
  public boolean asBoolean() {
    return mergeList.stream()
        .filter(ConfigSection::isPrimitive)
        .findFirst()
        .map(ConfigSection::asBoolean)
        .orElseThrow(() -> new IllegalStateException("Failed to get primitive from no primitive type"));
  }

  @Override
  public Number asNumber() {
    return mergeList.stream()
        .filter(ConfigSection::isPrimitive)
        .findFirst()
        .map(ConfigSection::asNumber)
        .orElseThrow(() -> new IllegalStateException("Failed to get primitive from no primitive type"));
  }

  @Override
  public String asString() {
    return mergeList.stream()
        .filter(ConfigSection::isPrimitive)
        .findFirst()
        .map(ConfigSection::asString)
        .orElseThrow(() -> new IllegalStateException("Failed to get primitive from no primitive type"));
  }

  @Override
  public double asDouble() {
    return mergeList.stream()
        .filter(ConfigSection::isPrimitive)
        .findFirst()
        .map(ConfigSection::asDouble)
        .orElseThrow(() -> new IllegalStateException("Failed to get primitive from no primitive type"));
  }

  @Override
  public BigDecimal asBigDecimal() {
    return mergeList.stream()
        .filter(ConfigSection::isPrimitive)
        .findFirst()
        .map(ConfigSection::asBigDecimal)
        .orElseThrow(() -> new IllegalStateException("Failed to get primitive from no primitive type"));
  }

  @Override
  public BigInteger asBigInteger() {
    return mergeList.stream()
        .filter(ConfigSection::isPrimitive)
        .findFirst()
        .map(ConfigSection::asBigInteger)
        .orElseThrow(() -> new IllegalStateException("Failed to get primitive from no primitive type"));
  }

  @Override
  public float asFloat() {
    return mergeList.stream()
        .filter(ConfigSection::isPrimitive)
        .findFirst()
        .map(ConfigSection::asFloat)
        .orElseThrow(() -> new IllegalStateException("Failed to get primitive from no primitive type"));
  }

  @Override
  public long asLong() {
    return mergeList.stream()
        .filter(ConfigSection::isPrimitive)
        .findFirst()
        .map(ConfigSection::asLong)
        .orElseThrow(() -> new IllegalStateException("Failed to get primitive from no primitive type"));
  }

  @Override
  public short asShort() {
    return mergeList.stream()
        .filter(ConfigSection::isPrimitive)
        .findFirst()
        .map(ConfigSection::asShort)
        .orElseThrow(() -> new IllegalStateException("Failed to get primitive from no primitive type"));
  }

  private ConfigSection provideSection(ConfigSection coreSection) {
    return configLoader.load(coreSection);
  }

  private Map<String, ConfigSection> provideEntryMap() {
    return mergeList.stream()
        .filter(it -> it.isArray() || it.isObject())
        .flatMap(it -> it.entrySet().stream())
        .collect(Collectors.toMap(Map.Entry::getKey, it -> provideSection(it.getValue())));
  }

  private List<ConfigSection> provideEntryList() {
    return mergeList.stream()
        .filter(ConfigSection::isArray)
        .flatMap(it -> it.asList().stream())
        .map(this::provideSection)
        .collect(Collectors.toList());
  }

  @Override
  public Set<Map.Entry<String, ConfigSection>> entrySet() {
    return entryMap.get().entrySet();
  }

  @Override
  public List<ConfigSection> asList() {
    return Collections.unmodifiableList(entryList.get());
  }

  @Override
  public int size() {
    return asList().size();
  }

  @Override
  public boolean has(String memberName) {
    if (entryMap.get().containsKey(memberName)) {
      return true;
    }
    try {
      return Integer.parseInt(memberName) < size();
    } catch (NumberFormatException e) {
      return false;
    }
  }

  @Override
  public boolean has(int index) {
    return (index < size()) || entryMap.get().containsKey(Integer.toString(index));
  }

  @Override
  public ConfigSection get(String memberName) {
    ConfigSection section = entryMap.get().get(memberName);
    if (section != null) {
      return section;
    }
    try {
      return asList().get(Integer.parseInt(memberName));
    } catch (NumberFormatException | IndexOutOfBoundsException e) {
      return null;
    }
  }

  @Override
  public ConfigSection get(int index) {
    ConfigSection section = null;
    try {
      section = entryList.get().get(index);
    } catch (IndexOutOfBoundsException e) {
      //
    }
    if (section != null) {
      return section;
    }
    return entryMap.get().get(Integer.toString(index));
  }
}
