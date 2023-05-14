package org.inksnow.ankh.core.common.util;

import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;

@UtilityClass
public class NamedUtil {
  public static boolean isNamedClass(@Nullable Class<?> clazz) {
    return clazz == com.google.inject.name.Named.class || clazz == javax.inject.Named.class;
  }

  public static boolean isNamedInstance(Object instance) {
    return instance instanceof com.google.inject.name.Named || instance instanceof javax.inject.Named;
  }

  public static String getValue(Object instance) {
    if (instance instanceof com.google.inject.name.Named) {
      return getValue((com.google.inject.name.Named) instance);
    } else if (instance instanceof javax.inject.Named) {
      return getValue((javax.inject.Named) instance);
    } else {
      throw new IllegalArgumentException("instance neither com.google.inject.name.Named nor javax.inject.Named.");
    }
  }

  public static String getValue(com.google.inject.name.Named instance) {
    if (instance == null) {
      return null;
    }
    return instance.value();
  }

  public static String getValue(javax.inject.Named instance) {
    if (instance == null) {
      return null;
    }
    return instance.value();
  }
}
