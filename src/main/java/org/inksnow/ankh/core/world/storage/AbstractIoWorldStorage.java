package org.inksnow.ankh.core.world.storage;

import lombok.val;
import net.kyori.adventure.key.Key;
import org.bukkit.Chunk;
import org.inksnow.ankh.core.api.world.storage.BlockStorageEntry;
import org.inksnow.ankh.core.api.world.storage.WorldStorage;
import org.inksnow.ankh.core.common.entity.LocationEmbedded;
import org.inksnow.ankh.core.common.entity.WorldChunkEmbedded;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractIoWorldStorage implements WorldStorage {
  public static final CompletableFuture<List<BlockStorageEntry>> EMPTY_RESULT = CompletableFuture.completedFuture(Collections.emptyList());
  public static final CompletableFuture<Void> COMPLETED_RESULT = CompletableFuture.completedFuture(null);

  protected List<BlockStorageEntry> provideByIo(Chunk chunk, DataInputStream in) throws IOException {
    val chunkEmbedded = WorldChunkEmbedded.of(chunk);
    val entryCount = in.readInt();
    val entryList = new ArrayList<BlockStorageEntry>(entryCount);
    for (int i = 0; i < entryCount; i++) {
      val blockId = in.readLong();
      val key = in.readUTF();
      val fullData = new byte[in.readInt()];
      in.readFully(fullData);
      entryList.add(BlockStorageEntry.of(
          LocationEmbedded.of(chunkEmbedded, blockId),
          Key.key(key),
          fullData
      ));
    }
    return entryList;
  }

  protected void storeByIo(Chunk chunk, List<BlockStorageEntry> entries, DataOutputStream out) throws IOException {
    out.writeInt(entries.size());
    for (val entry : entries) {
      out.writeLong(LocationEmbedded.warp(entry.location()).position());
      out.writeUTF(entry.blockId().asString());
      out.writeInt(entry.content().length);
      out.write(entry.content());
    }
  }
}
