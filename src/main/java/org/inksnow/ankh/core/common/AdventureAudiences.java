package org.inksnow.ankh.core.common;

import lombok.val;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.inksnow.ankh.core.api.AnkhCoreLoader;
import org.inksnow.ankh.core.api.plugin.PluginLifeCycle;
import org.inksnow.ankh.core.api.plugin.annotations.SubscriptLifecycle;
import org.inksnow.ankh.core.api.util.DcLazy;
import org.inksnow.ankh.core.common.util.LazyProxyUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@SuppressWarnings({"resource", "unused"})
@Singleton
public class AdventureAudiences {
  private static LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
  private static DcLazy<BukkitAudiences> proxyBukkitAudiences;
  private final DcLazy<BukkitAudiences> lazyBukkitAudiences;
  private final AnkhCoreLoader coreLoader;

  @Inject
  private AdventureAudiences(AnkhCoreLoader coreLoader) {
    this.coreLoader = coreLoader;
    this.lazyBukkitAudiences = DcLazy.of(this::createBukkitAudiences);
    proxyBukkitAudiences = DcLazy.of(() -> LazyProxyUtil.generate(BukkitAudiences.class, lazyBukkitAudiences));
  }

  public static BukkitAudiences instance() {
    return proxyBukkitAudiences.get();
  }

  public static @Nonnull Sound.Emitter asEmitter(@Nonnull Entity entity) {
    return BukkitAudiences.asEmitter(entity);
  }

  public static @Nonnull Audience sender(@Nonnull CommandSender sender) {
    return instance().sender(sender);
  }

  public static @Nonnull Audience player(@Nonnull Player player) {
    return instance().player(player);
  }

  public static @Nonnull Audience filter(@Nonnull Predicate<CommandSender> filter) {
    return instance().filter(filter);
  }

  public static @Nonnull Audience all() {
    return instance().all();
  }

  public static @Nonnull Audience console() {
    return instance().console();
  }

  public static @Nonnull Audience players() {
    return instance().players();
  }

  public static @Nonnull Audience player(@Nonnull UUID playerId) {
    return instance().player(playerId);
  }

  public static @Nonnull Audience permission(@Nonnull Key permission) {
    return instance().permission(permission);
  }

  public static @Nonnull Audience permission(@Nonnull String permission) {
    return instance().permission(permission);
  }

  public static @Nonnull Audience world(@Nonnull Key world) {
    return instance().world(world);
  }

  public static @Nonnull Audience server(@Nonnull String serverName) {
    return instance().server(serverName);
  }

  public static @Nonnull ComponentFlattener flattener() {
    return instance().flattener();
  }

  public static @Nonnull TextComponent deserialize(@Nonnull String input) {
    return serializer.deserialize(input);
  }

  public static @Nonnull String serialize(@Nonnull Component component) {
    return serializer.serialize(component);
  }

  public static @Nonnull List<TextComponent> deserialize(@Nonnull List<String> inputList) {
    val result = new ArrayList<TextComponent>(inputList.size());
    for (String input : inputList) {
      result.add(serializer.deserialize(input));
    }
    return result;
  }

  public static @Nonnull List<String> serialize(@Nonnull List<Component> componentList) {
    val result = new ArrayList<String>(componentList.size());
    for (Component component : componentList) {
      result.add(serializer.serialize(component));
    }
    return result;
  }

  public static @Nullable TextComponent deserializeOrNull(@Nullable String input) {
    return serializer.deserializeOrNull(input);
  }

  public static @Nullable TextComponent deserializeOr(@Nullable String input, @Nullable TextComponent fallback) {
    return serializer.deserializeOr(input, fallback);
  }

  public static @Nullable String serializeOrNull(@Nullable Component component) {
    return serializer.serializeOrNull(component);
  }

  public static @Nullable String serializeOr(@Nullable Component component, @Nullable String fallback) {
    return serializer.serializeOr(component, fallback);
  }

  private BukkitAudiences createBukkitAudiences() {
    return BukkitAudiences.create(coreLoader);
  }

  @SubscriptLifecycle(PluginLifeCycle.ENABLE)
  private void onEnable() {
    lazyBukkitAudiences.get();
    proxyBukkitAudiences = lazyBukkitAudiences;
  }

  @SubscriptLifecycle(PluginLifeCycle.DISABLE)
  private void onDisable() {
    lazyBukkitAudiences.get().close();
  }
}
