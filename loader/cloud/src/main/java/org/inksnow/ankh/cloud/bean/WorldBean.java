package org.inksnow.ankh.cloud.bean;

import lombok.*;
import org.bukkit.GameRule;
import org.bukkit.World;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public final class WorldBean {
  private final UUID uid;
  private final String name;
  private final int entityCount;
  private final int tileEntityCount;
  private final int tickableEntityCount;
  private final int chunkCount;
  private final int loadedChunkCount;
  private final Map<String, String> gameRule;

  public static WorldBean fromInstance(World world) {
    return builder()
        .uid(world.getUID())
        .name(world.getName())
        .entityCount(world.getEntityCount())
        .tileEntityCount(world.getTileEntityCount())
        .tickableEntityCount(world.getTickableTileEntityCount())
        .chunkCount(world.getChunkCount())
        .loadedChunkCount(world.getLoadedChunks().length)
        .gameRule(provideGameRule(world))
        .build();
  }

  private static Map<String, String> provideGameRule(World world) {
    val result = new LinkedHashMap<String, String>(GameRule.values().length);
    for (val rule : GameRule.values()) {
      Object value = world.getGameRuleValue(rule);
      if (value != null) {
        result.put(rule.getName(), value.toString());
      }
    }
    return result;
  }
}
