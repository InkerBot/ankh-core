package org.inksnow.ankh.core.config.typesafe;

import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigExtension;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigSource;
import org.inksnow.ankh.core.api.util.DcLazy;
import org.inksnow.ankh.core.common.config.LazilyParsedNumber;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TypesafeConfigSection implements ConfigSection {
  @Getter
  private final ConfigSource source;
  private final ConfigValue configValue;

  private final DcLazy<ConfigExtension> extension = DcLazy.of(this::provideExtension);

  private final DcLazy<List<ConfigSection>> sectionList = DcLazy.of(this::provideSectionList);

  private final DcLazy<Map<String, ConfigSection>> sectionMap = DcLazy.of(this::provideSectionMap);

  @Nonnull
  @Override
  public ConfigExtension extension() {
    return extension.get();
  }

  private ConfigExtension provideExtension() {
    ConfigExtension extension = ConfigExtension.empty();
    if (configValue.valueType() == ConfigValueType.OBJECT) {
      val configObject = (ConfigObject) configValue;
      val extConfig = configObject.get(TypesafeConfigFactory.INTERNAL_EXTENSION_PREFIX);
      if (extConfig != null) {
        if (extConfig.valueType() == ConfigValueType.OBJECT) {
          val extConfigObject = (ConfigObject) extConfig;
          for (val entry : extConfigObject.entrySet()) {
            extension = extension.include(entry.getKey());
          }
        } else if (extConfig.valueType() == ConfigValueType.LIST) {
          val extConfigList = (ConfigList) extConfig;
          for (val entry : extConfigList) {
            switch (entry.valueType()) {
              case NUMBER:
              case STRING: {
                extension = extension.include(entry.unwrapped().toString());
              }
            }
          }
        } else if (extConfig.valueType() == ConfigValueType.STRING || extConfig.valueType() == ConfigValueType.NUMBER) {
          extension = extension.include(extConfig.unwrapped().toString());
        }
      }
    }
    return extension;
  }

  @Override
  public boolean isNull() {
    return configValue.valueType() == ConfigValueType.NULL;
  }

  @Override
  public boolean isPrimitive() {
    return configValue.valueType() == ConfigValueType.STRING
        || configValue.valueType() == ConfigValueType.NUMBER
        || configValue.valueType() == ConfigValueType.BOOLEAN;
  }

  private void ensurePrimitive() {
    if (!isPrimitive()) {
      throw new IllegalStateException(source + " isn't a primitive value");
    }
  }

  @Override
  public int asInteger() {
    return configValue.valueType() == ConfigValueType.NUMBER
        ? ((Number) configValue.unwrapped()).intValue()
        : Integer.parseInt(asString());
  }

  @Override
  public byte asByte() {
    return configValue.valueType() == ConfigValueType.NUMBER
        ? ((Number) configValue.unwrapped()).byteValue()
        : Byte.parseByte(asString());
  }

  @Override
  public char asCharacter() {
    return configValue.valueType() == ConfigValueType.NUMBER
        ? (char) ((Number) configValue.unwrapped()).shortValue()
        : asString().charAt(0);
  }

  @Override
  public boolean asBoolean() {
    return configValue.valueType() == ConfigValueType.BOOLEAN
        ? (Boolean) configValue.unwrapped()
        : Boolean.parseBoolean(asString());
  }

  @Override
  public Number asNumber() {
    return configValue.valueType() == ConfigValueType.NUMBER
        ? (Number) configValue.unwrapped()
        : new LazilyParsedNumber(asString());
  }

  @Override
  public String asString() {
    ensurePrimitive();
    if (configValue.valueType() == ConfigValueType.STRING) {
      return (String) configValue.unwrapped();
    } else if (configValue.valueType() == ConfigValueType.BOOLEAN) {
      return ((Boolean) configValue.unwrapped()).toString();
    } else if (configValue.valueType() == ConfigValueType.NUMBER) {
      return ((Number) configValue.unwrapped()).toString();
    }
    throw new IllegalStateException("Failed to detect config value type " + configValue.valueType());
  }

  @Override
  public double asDouble() {
    return configValue.valueType() == ConfigValueType.NUMBER
        ? ((Number) configValue.unwrapped()).doubleValue()
        : Double.parseDouble(asString());
  }

  @Override
  public BigDecimal asBigDecimal() {
    return (configValue.valueType() == ConfigValueType.NUMBER && configValue.unwrapped() instanceof BigDecimal)
        ? (BigDecimal) configValue.unwrapped()
        : new BigDecimal(asString());
  }

  @Override
  public BigInteger asBigInteger() {
    return (configValue.valueType() == ConfigValueType.NUMBER && configValue.unwrapped() instanceof BigInteger)
        ? (BigInteger) configValue.unwrapped()
        : new BigInteger(asString());
  }

  @Override
  public float asFloat() {
    return configValue.valueType() == ConfigValueType.NUMBER
        ? ((Number) configValue.unwrapped()).floatValue()
        : Float.parseFloat(asString());
  }

  @Override
  public long asLong() {
    return configValue.valueType() == ConfigValueType.NUMBER
        ? ((Number) configValue.unwrapped()).longValue()
        : Long.parseLong(asString());
  }

  @Override
  public short asShort() {
    return configValue.valueType() == ConfigValueType.NUMBER
        ? ((Number) configValue.unwrapped()).shortValue()
        : Short.parseShort(asString());
  }

  @Override
  public boolean isObject() {
    return configValue.valueType() == ConfigValueType.OBJECT;
  }

  @Override
  public boolean isArray() {
    return configValue.valueType() == ConfigValueType.LIST;
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

  private ConfigSection provideSubSection(String memberName, ConfigValue element) {
    return new TypesafeConfigSection(
        TypesafeConfigFactory.applyOrigin(source.toBuilder(), element.origin())
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
      val obj = (ConfigObject) configValue;
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
    val configArray = (ConfigList) configValue;
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
