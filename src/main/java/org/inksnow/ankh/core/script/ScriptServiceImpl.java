package org.inksnow.ankh.core.script;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.server.ServerCommandEvent;
import org.inksnow.ankh.core.api.AnkhCoreLoader;
import org.inksnow.ankh.core.api.AnkhServiceLoader;
import org.inksnow.ankh.core.api.plugin.annotations.SubscriptEvent;
import org.inksnow.ankh.core.api.script.AnkhScriptEngine;
import org.inksnow.ankh.core.api.script.AnkhScriptService;
import org.inksnow.ankh.core.api.script.PreparedScript;
import org.inksnow.ankh.core.api.script.ScriptContext;
import org.inksnow.ankh.core.api.util.DcLazy;
import org.inksnow.ankh.core.common.config.AnkhConfig;
import org.inksnow.ankh.core.common.util.ExecuteReportUtil;
import org.slf4j.helpers.MessageFormatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Singleton
@Slf4j
public class ScriptServiceImpl implements AnkhScriptService, Provider<AnkhScriptEngine> {
  private final AnkhCoreLoader coreLoader;
  private final AnkhConfig config;
  private final Map<String, AnkhScriptEngine> engineMap = new ConcurrentSkipListMap<>();
  private final Function<String, AnkhScriptEngine> engineLoadFunction = this::loadEngineImpl;

  @Inject
  private ScriptServiceImpl(AnkhCoreLoader coreLoader, AnkhConfig config) {
    this.coreLoader = coreLoader;
    this.config = config;
  }

  @Override
  public @Nonnull AnkhScriptEngine engine(@Nullable String key) {
    if (key == null || key.isEmpty()) {
      return defaultEngine.get();
    }
    return engineMap.computeIfAbsent(key, engineLoadFunction);
  }

  private AnkhScriptEngine defaultEngineImpl() {
    val configDefaultService = config.service().script();
    return engine(configDefaultService == null ? "ankh-core:bsh" : configDefaultService);
  }

  private AnkhScriptEngine loadEngineImpl(String key) {
    return AnkhServiceLoader.loadService(key, AnkhScriptEngine.class);
  }

  @Override
  public @Nonnull AnkhScriptEngine get() {
    return defaultEngine.get();
  }

  @Override
  public void runPlayerShell(@Nonnull Player player, @Nonnull String shell) {
    long ppsCost;
    long executeCost;
    Object result;

    try {
      val context = ScriptContext.builder()
          .player(player)
          .with("isConsole", false)
          .build();
      val startTime = System.nanoTime();
      val script = prepareShell(shell);
      val ppsTime = System.nanoTime();
      result = script.execute(context);
      val finishedTime = System.nanoTime();

      ppsCost = ppsTime - startTime;
      executeCost = finishedTime - ppsTime;
    } catch (Exception e) {
      ExecuteReportUtil.reportForSender(player, e);
      return;
    }

    val resultMessage = MessageFormatter.format("{}", result).getMessage();
    Component layoutComponent;
    if (resultMessage.length() > 45) {
      layoutComponent = Component.text(resultMessage.substring(0, 42), NamedTextColor.WHITE)
          .append(Component.text("...", NamedTextColor.BLUE));
    } else {
      layoutComponent = Component.text(resultMessage, NamedTextColor.WHITE);
    }
    Component hoverComponent;
    if (resultMessage.length() > 1000) {
      hoverComponent = Component.text(resultMessage.substring(0, 800), NamedTextColor.WHITE)
          .append(Component.newline())
          .append(Component.newline())
          .append(Component.text("prepare: ", NamedTextColor.GOLD))
          .append(Component.text(TimeUnit.NANOSECONDS.toNanos(ppsCost), NamedTextColor.WHITE))
          .append(Component.text("ns", NamedTextColor.GOLD))
          .append(Component.newline())
          .append(Component.text("execute: ", NamedTextColor.GOLD))
          .append(Component.text(TimeUnit.NANOSECONDS.toNanos(executeCost), NamedTextColor.WHITE))
          .append(Component.text("ns", NamedTextColor.GOLD))
          .append(Component.newline())
          .append(Component.text("type: ", NamedTextColor.GOLD))
          .append(Component.text(result == null ? "null" : result.getClass().getName()))
          .append(Component.newline())
          .append(Component.text("result length=" + resultMessage.length() + ", cut first 800 chars", NamedTextColor.GOLD));
    } else {
      hoverComponent = Component.text(resultMessage, NamedTextColor.WHITE)
          .append(Component.newline())
          .append(Component.newline())
          .append(Component.text("prepare: ", NamedTextColor.GOLD))
          .append(Component.text(TimeUnit.NANOSECONDS.toNanos(ppsCost), NamedTextColor.WHITE))
          .append(Component.text("ns", NamedTextColor.GOLD))
          .append(Component.newline())
          .append(Component.text("execute: ", NamedTextColor.GOLD))
          .append(Component.text(TimeUnit.NANOSECONDS.toNanos(executeCost), NamedTextColor.WHITE))
          .append(Component.text("ns", NamedTextColor.GOLD))
          .append(Component.newline())
          .append(Component.text("type: ", NamedTextColor.GOLD))
          .append(Component.text(result == null ? "null" : result.getClass().getName()));
    }
    player.sendMessage(Component.text("[result] ", NamedTextColor.GOLD)
        .append(layoutComponent.hoverEvent(hoverComponent)));
  }  private final DcLazy<AnkhScriptEngine> defaultEngine = DcLazy.of(this::defaultEngineImpl);

  @Override
  public void runConsoleShell(@Nonnull String shell) {
    long ppsCost;
    long executeCost;
    Object result;

    try {
      val context = ScriptContext.builder().with("isConsole", true).build();
      val startTime = System.nanoTime();
      val script = prepareShell(shell);
      val ppsTime = System.nanoTime();
      result = script.execute(context);
      val finishedTime = System.nanoTime();

      ppsCost = ppsTime - startTime;
      executeCost = finishedTime - ppsTime;
    } catch (Exception e) {
      logger.error("Failed to run console shell", e);
      return;
    }

    logger.info("[result] class={}, prepare={}ns execute={}ns",
        result == null ? "null" : result.getClass().getName(),
        TimeUnit.NANOSECONDS.toNanos(ppsCost),
        TimeUnit.NANOSECONDS.toNanos(executeCost)
    );
    logger.info("[result] {}", result);
  }

  @Override
  public Object executeShell(@Nonnull ScriptContext context, @Nonnull String shell) throws Exception {
    return prepareShell(shell).execute(context);
  }

  @Override
  public @Nonnull PreparedScript prepareShell(@Nonnull String shell) throws Exception {
    String engineName;
    String command;
    if (shell.startsWith(":")) {
      val firstSplit = shell.indexOf(' ');
      engineName = shell.substring(1, firstSplit == -1 ? shell.length() : firstSplit);
      command = firstSplit == -1 ? "" : shell.substring(firstSplit + 1);
    } else {
      engineName = null;
      command = shell;
    }
    val engine = engineName == null ? get() : engine(engineName);

    return engine.prepare(command);
  }

  @SubscriptEvent(priority = EventPriority.LOW, ignoreCancelled = true)
  private void onServerCommand(ServerCommandEvent event) {
    val rawCommand = event.getCommand();
    if (rawCommand.startsWith(config.playerShell().prefix())) {
      event.setCancelled(true);
      runConsoleShell(rawCommand.substring(config.playerShell().prefix().length()));
    }
  }

  @SubscriptEvent(priority = EventPriority.LOW, ignoreCancelled = true)
  private void onAsyncChat(AsyncChatEvent event) {
    if (!config.playerShell().enable()) {
      return;
    }
    val message = PlainComponentSerializer.plain().serialize(event.originalMessage());
    if (message.startsWith(config.playerShell().prefix())) {
      event.setCancelled(true);
      val player = event.getPlayer();
      val command = message.substring(config.playerShell().prefix().length());
      Bukkit.getScheduler().runTask(coreLoader, () -> {
        if (!player.isOnline()) {
          return;
        }
        if (player.isOp() || player.hasPermission("ankh.script.execute-any")) {
          runPlayerShell(player, command);
        } else {
          player.sendMessage(Component.text("Permission denied", NamedTextColor.RED));
        }
      });
    }
  }




}
