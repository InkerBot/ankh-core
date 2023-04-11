package org.inksnow.ankh.core.api.world.storage;

import org.bukkit.Chunk;

import javax.annotation.Nonnull;
import javax.inject.Named;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * StorageBackend store world data, likes block's pos and meta
 */
@Named("world-storage")
public interface WorldStorage {
  /**
   * Provide a stream include all ankh-blocks in WorldChunkEmbedded
   *
   * @param chunk chunk which contains ankh-blocks
   * @return future of list of entries
   */
  CompletableFuture<List<BlockStorageEntry>> provide(@Nonnull Chunk chunk);


  /**
   * Store ankh-blocks in WorldChunkEmbedded
   *
   * @param chunk   chunk which contains ankh-blocks
   * @param entries ankh-blocks entry
   * @return future of success
   */
  CompletableFuture<Void> store(@Nonnull Chunk chunk, @Nonnull List<BlockStorageEntry> entries);
}