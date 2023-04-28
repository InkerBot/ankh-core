package org.inksnow.ankh.core.api.config;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ConfigSection {
  @Nonnull
  ConfigSource source();

  @Nonnull
  ConfigExtension extension();

  boolean isArray();

  boolean isObject();

  boolean isPrimitive();

  boolean isNull();

  // if primitive
  int asInteger();

  byte asByte();

  char asCharacter();

  boolean asBoolean();

  Number asNumber();

  String asString();

  double asDouble();

  BigDecimal asBigDecimal();

  BigInteger asBigInteger();

  float asFloat();

  long asLong();

  short asShort();

  // if object or array
  Set<Map.Entry<String, ConfigSection>> entrySet();

  List<ConfigSection> asList();

  int size();

  boolean has(String memberName);

  boolean has(int index);

  ConfigSection get(String memberName);

  ConfigSection get(int index);
}
