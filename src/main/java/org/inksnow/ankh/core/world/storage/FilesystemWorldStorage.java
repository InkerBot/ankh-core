package org.inksnow.ankh.core.world.storage;

import com.google.common.primitives.Longs;
import lombok.val;
import org.bukkit.Chunk;
import org.inksnow.ankh.core.api.world.storage.BlockStorageEntry;
import org.inksnow.ankh.core.common.util.FastEmbeddedUtil;
import org.inksnow.ankh.core.common.util.HexUtil;
import org.inksnow.ankh.core.common.util.ThreadUtil;
import org.inksnow.ankh.core.common.util.UUIDUtil;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Singleton
public class FilesystemWorldStorage extends AbstractIoWorldStorage {
  private static final Path basePath = Paths.get("data-storage", "ankh-world-storage");

  private Path getChunkStoragePath(UUID worldId, long chunkId) {
    val chunkIdHex = HexUtil.toHex(Longs.toByteArray(chunkId));
    return basePath.resolve(UUIDUtil.toPlainString(worldId))
        .resolve(chunkIdHex + ".bin");
  }

  @Override
  public CompletableFuture<List<BlockStorageEntry>> provide(@Nonnull Chunk chunk) {
    return CompletableFuture.supplyAsync(() -> {
      val targetPath = getChunkStoragePath(
          chunk.getWorld().getUID(),
          FastEmbeddedUtil.chunk_chunkId(chunk.getX(), chunk.getZ())
      );
      if (!Files.exists(targetPath) || Files.isDirectory(targetPath)) {
        return Collections.emptyList();
      }
      try (val in = new DataInputStream(new GZIPInputStream(Files.newInputStream(targetPath)))) {
        return provideByIo(chunk, in);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }, ThreadUtil.asyncExecutor());
  }

  @Override
  public CompletableFuture<Void> store(@Nonnull Chunk chunk, @Nonnull List<BlockStorageEntry> entries) {
    return CompletableFuture.runAsync(() -> {
      val targetPath = getChunkStoragePath(
          chunk.getWorld().getUID(),
          FastEmbeddedUtil.chunk_chunkId(chunk.getX(), chunk.getZ())
      );
      try {
        Files.createDirectories(targetPath.getParent());
        try (val out = new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(targetPath)))) {
          storeByIo(chunk, entries, out);
        }
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }, ThreadUtil.asyncExecutor());
  }
}
