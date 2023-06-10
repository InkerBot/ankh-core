package org.inksnow.ankh.cloud.endpoint;

import lombok.*;
import org.bukkit.Bukkit;
import org.inksnow.ankh.cloud.AnkhCloudEnvironment;
import org.inksnow.ankh.cloud.AnkhCloudEula;
import org.inksnow.ankh.cloud.bean.PluginBean;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public final class SystemReportRequest {
  public static final URI URL = URI.create(AnkhCloudEnvironment.BASE_URL + "/endpoint/systemreport");

  private final UUID serverId = AnkhCloudEula.serverId();

  private final String bukkitName;
  private final String bukkitVersion;
  private final String javaVendor;
  private final String javaVersion;
  private final String osName;
  private final String osArch;
  private final String osVersion;
  private final int availableProcessors;
  private final List<PluginBean> pluginList;

  public static SystemReportRequest create() {
    return builder()
        .bukkitName(Bukkit.getName())
        .bukkitVersion(Bukkit.getVersion())
        .javaVendor(System.getProperty("java.vendor"))
        .javaVersion(System.getProperty("java.version"))
        .osName(System.getProperty("os.name"))
        .osArch(System.getProperty("os.arch"))
        .osVersion(System.getProperty("os.version"))
        .availableProcessors(Runtime.getRuntime().availableProcessors())
        .pluginList(providePluginList())
        .build();
  }

  private static List<PluginBean> providePluginList() {
    return Arrays.stream(Bukkit.getPluginManager().getPlugins())
        .map(PluginBean::fromInstance)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }
}
