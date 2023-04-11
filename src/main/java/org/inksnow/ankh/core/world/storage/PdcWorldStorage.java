package org.inksnow.ankh.core.world.storage;

import lombok.val;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.inksnow.ankh.core.api.AnkhCore;
import org.inksnow.ankh.core.api.world.storage.BlockStorageEntry;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.io.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Singleton
public class PdcWorldStorage extends AbstractIoWorldStorage {
  private static final NamespacedKey CHUNK_STORAGE_KEY = new NamespacedKey(AnkhCore.PLUGIN_ID, "chunk-storage");

  @Override
  public CompletableFuture<List<BlockStorageEntry>> provide(@Nonnull Chunk chunk) {
    val fullBytes = chunk.getPersistentDataContainer().get(CHUNK_STORAGE_KEY, PersistentDataType.BYTE_ARRAY);
    if (fullBytes == null) {
      return EMPTY_RESULT;
    }
    try (val in = new DataInputStream(new ByteArrayInputStream(fullBytes))) {
      return CompletableFuture.completedFuture(provideByIo(chunk, in));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public CompletableFuture<Void> store(@Nonnull Chunk chunk, @Nonnull List<BlockStorageEntry> entries) {
    val bout = new ByteArrayOutputStream();
    try (val out = new DataOutputStream(bout)) {
      storeByIo(chunk, entries, out);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    chunk.getPersistentDataContainer().set(CHUNK_STORAGE_KEY, PersistentDataType.BYTE_ARRAY, bout.toByteArray());
    return COMPLETED_RESULT;
  }
}
