package org.inksnow.ankh.core.config.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.inksnow.ankh.core.api.config.ConfigExtension;
import org.inksnow.ankh.core.api.config.ConfigSection;
import org.inksnow.ankh.core.api.config.ConfigSource;
import org.inksnow.ankh.core.api.util.DcLazy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GsonConfigSection implements ConfigSection {
  @Getter
  private final ConfigSource source;
  @Getter
  private final ConfigExtension extension;
  private final JsonElement element;

  private final DcLazy<List<ConfigSection>> sectionList = DcLazy.of(this::provideSectionList);

  private final DcLazy<Map<String, ConfigSection>> sectionMap = DcLazy.of(this::provideSectionMap);

  @Override
  public boolean isNull() {
    return element.isJsonNull();
  }

  @Override
  public boolean isPrimitive() {
    return element.isJsonPrimitive();
  }

  private JsonPrimitive ensurePrimitive() {
    if (element instanceof JsonPrimitive) {
      return (JsonPrimitive) element;
    } else {
      throw new IllegalStateException(source + " isn't a primitive value");
    }
  }

  @Override
  public int asInteger() {
    return ensurePrimitive().getAsInt();
  }

  @Override
  public byte asByte() {
    return ensurePrimitive().getAsByte();
  }

  @Override
  public char asCharacter() {
    return ensurePrimitive().getAsCharacter();
  }

  @Override
  public boolean asBoolean() {
    return ensurePrimitive().getAsBoolean();
  }

  @Override
  public Number asNumber() {
    return ensurePrimitive().getAsNumber();
  }

  @Override
  public String asString() {
    return ensurePrimitive().getAsString();
  }

  @Override
  public double asDouble() {
    return ensurePrimitive().getAsDouble();
  }

  @Override
  public BigDecimal asBigDecimal() {
    return ensurePrimitive().getAsBigDecimal();
  }

  @Override
  public BigInteger asBigInteger() {
    return ensurePrimitive().getAsBigInteger();
  }

  @Override
  public float asFloat() {
    return ensurePrimitive().getAsFloat();
  }

  @Override
  public long asLong() {
    return ensurePrimitive().getAsLong();
  }

  @Override
  public short asShort() {
    return ensurePrimitive().getAsShort();
  }

  @Override
  public boolean isObject() {
    return element.isJsonObject();
  }

  @Override
  public boolean isArray() {
    return element.isJsonArray();
  }

  private JsonArray ensureArray() {
    if (element instanceof JsonArray) {
      return (JsonArray) element;
    } else {
      throw new IllegalStateException(source + " isn't a array value");
    }
  }

  private JsonObject ensureObject() {
    if (element instanceof JsonObject) {
      return (JsonObject) element;
    } else {
      throw new IllegalStateException(source + " isn't a object value");
    }
  }

  private ConfigSection provideSubSection(String memberName, JsonElement element) {
    return new GsonConfigSection(
        source.toBuilder()
            .path(source.path() + (isArray() ? ("[" + memberName + "]") : ("." + memberName)))
            .build(),
        ConfigExtension.empty(),
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
      return ensureObject().entrySet()
          .stream()
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
    val jsonArray = ensureArray();
    val list = new ArrayList<ConfigSection>(jsonArray.size());
    for (int i = 0; i < jsonArray.size(); i++) {
      list.add(provideSubSection(Integer.toString(i), jsonArray.get(i)));
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
