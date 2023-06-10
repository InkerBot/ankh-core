package org.inksnow.ankh.cloud.bean;

import lombok.*;
import org.bukkit.plugin.Plugin;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public final class PluginBean {
  public static final String UNKNOWN_VALUE = "(unknown)";

  private final String name;
  private final String version;

  public static PluginBean fromInstance(Plugin plugin) {
    return builder()
        .name(nullOr(plugin.getDescription().getName()))
        .version(nullOr(plugin.getDescription().getVersion()))
        .build();
  }

  private static String nullOr(String value) {
    return value == null ? UNKNOWN_VALUE : value;
  }
}
