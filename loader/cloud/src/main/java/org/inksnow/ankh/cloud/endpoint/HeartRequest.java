package org.inksnow.ankh.cloud.endpoint;

import lombok.*;
import org.bukkit.Bukkit;
import org.inksnow.ankh.cloud.AnkhCloudEnvironment;
import org.inksnow.ankh.cloud.AnkhCloudEula;
import org.inksnow.ankh.cloud.bean.MemoryUsageBean;
import org.inksnow.ankh.cloud.bean.PlayerBean;
import org.inksnow.ankh.cloud.bean.WorldBean;

import java.lang.management.ManagementFactory;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public final class HeartRequest {
  public static final URI URL = URI.create(AnkhCloudEnvironment.BASE_URL + "/endpoint/heartbeat");
  private final UUID serverId = AnkhCloudEula.serverId();

  private final MemoryUsageBean heapMemory;
  private final MemoryUsageBean nonHeapMemory;
  private final double systemLoad;

  private final List<PlayerBean> onlinePlayers;
  private final List<WorldBean> loadedWorlds;

  public static HeartRequest create(Executor primaryThreadExecutor) {
    val builder = builder();

    val primaryThreadFuture = CompletableFuture.runAsync(() ->
            builder.onlinePlayers(provideOnlinePlayers())
                .loadedWorlds(provideLoadedWorlds()),
        primaryThreadExecutor);

    val memory = ManagementFactory.getMemoryMXBean();
    val os = ManagementFactory.getOperatingSystemMXBean();
    builder.heapMemory(MemoryUsageBean.fromInstance(memory.getHeapMemoryUsage()))
        .nonHeapMemory(MemoryUsageBean.fromInstance(memory.getNonHeapMemoryUsage()))
        .systemLoad(os.getSystemLoadAverage());

    primaryThreadFuture.join();
    return builder.build();
  }

  private static List<WorldBean> provideLoadedWorlds() {
    return Bukkit.getWorlds()
        .stream()
        .map(WorldBean::fromInstance)
        .collect(Collectors.toList());
  }

  private static List<PlayerBean> provideOnlinePlayers() {
    return Bukkit.getOnlinePlayers()
        .stream()
        .map(PlayerBean::fromInstance)
        .collect(Collectors.toList());
  }
}
