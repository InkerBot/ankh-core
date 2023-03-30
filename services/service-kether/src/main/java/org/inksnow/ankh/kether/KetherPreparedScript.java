package org.inksnow.ankh.kether;

import kotlin.Unit;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.inksnow.ankh.core.api.script.PreparedScript;
import org.inksnow.ankh.core.api.script.ScriptContext;
import taboolib.library.kether.Quest;
import taboolib.module.kether.ScriptService;

import javax.annotation.Nonnull;

public class KetherPreparedScript implements PreparedScript {
  private final Quest quest;
  private final ScriptCacheStack<KetherContextBinding, Exception> localCache;

  public KetherPreparedScript(Quest quest) throws Exception {
    this.quest = quest;
    this.localCache = new ScriptCacheStack<>(this::create);

    this.localCache.prepare(1);
  }

  private KetherContextBinding create(){
    return new KetherContextBinding(ScriptService.INSTANCE, quest);
  }

  @Override
  public Object execute(@Nonnull ScriptContext context) throws Exception {
    final var player = context.get("player");
    if (player instanceof CommandSender) {
      context.set("@Sender", player);
    } else {
      context.set("@Sender", Bukkit.getConsoleSender());
    }

    final var binding = localCache.borrow();
    try{
      binding.context(context);
      final var rawResult =  binding.contextBinding()
        .runActions()
        .join();
      return rawResult == Unit.INSTANCE ? null : rawResult;
    }finally {
      localCache.sendBack(binding);
    }
  }
}