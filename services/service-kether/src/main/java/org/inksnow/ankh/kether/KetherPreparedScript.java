package org.inksnow.ankh.kether;

import kotlin.Unit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.inksnow.ankh.core.api.script.PreparedScript;
import org.inksnow.ankh.core.api.script.ScriptContext;
import taboolib.library.kether.Quest;

import javax.annotation.Nonnull;

public class KetherPreparedScript implements PreparedScript {
  private final Quest quest;
  // private final ScriptCacheStack<KetherContextBinding, Exception> localCache;

  public KetherPreparedScript(Quest quest) throws Exception {
    this.quest = quest;
  }

  @Override
  public Object execute(@Nonnull ScriptContext context) throws Exception {
    Player player = (Player) context.get("player");
    if (player != null) {
      context.set("@Sender", player);
    } else {
      context.set("@Sender", Bukkit.getConsoleSender());
    }
    KetherContextBinding contextBinding = new KetherContextBinding(context, quest);
    Object rawResult = contextBinding
        .contextBinding()
        .runActions()
        .join();
    return rawResult == Unit.INSTANCE ? null : rawResult;
  }
}
