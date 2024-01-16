package org.inksnow.ankh.core.inventory;

import com.destroystokyo.paper.event.inventory.PrepareGrindstoneEvent;
import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.Value;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.plugin.Plugin;
import org.inksnow.ankh.core.api.inventory.InventoryEventHandler;
import org.inksnow.ankh.core.api.plugin.PluginLifeCycle;
import org.inksnow.ankh.core.api.plugin.annotations.SubscriptLifecycle;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Singleton
public class AnkhMenuService implements Listener {
  private final @Nonnull Plugin plugin;

  private final @Nonnull Map<Class<? extends Event>, InventoryHandlerInvoke<? extends Event>> typedEventMap = new HashMap<>();
  private final @Nonnull Multimap<Class<? extends Event>, InventoryHandlerInvoke<? extends Event>> typedEventHandler = HashMultimap.create();

  private final @Nonnull Function<? extends InventoryEvent, InventoryEventHandler> inventoryEventGetHandler = event -> {
    val holder = event.getInventory().getHolder();
    return (holder instanceof InventoryEventHandler) ? (InventoryEventHandler) holder : null;
  };

  @Inject
  private AnkhMenuService(@Nonnull Plugin plugin) {
    this.plugin = plugin;
    register(InventoryEvent.class, InventoryEventHandler::onInventoryEvent);
    register(InventoryInteractEvent.class, InventoryEventHandler::onInventoryInteractEvent);
    register(InventoryClickEvent.class, InventoryEventHandler::onInventoryClickEvent);
    register(CraftItemEvent.class, InventoryEventHandler::onCraftItemEvent);
    register(InventoryCreativeEvent.class, InventoryEventHandler::onInventoryCreativeEvent);
    register(SmithItemEvent.class, InventoryEventHandler::onSmithItemEvent);
    register(InventoryDragEvent.class, InventoryEventHandler::onInventoryDragEvent);
    register(TradeSelectEvent.class, InventoryEventHandler::onTradeSelectEvent);
    register(InventoryOpenEvent.class, InventoryEventHandler::onInventoryOpenEvent);
    register(PrepareResultEvent.class, InventoryEventHandler::onPrepareResultEvent);
    register(PrepareAnvilEvent.class, InventoryEventHandler::onPrepareAnvilEvent);
    register(PrepareSmithingEvent.class, InventoryEventHandler::onPrepareSmithingEvent);
    register(PrepareGrindstoneEvent.class, InventoryEventHandler::onPrepareGrindstoneEvent);
    register(PrepareItemCraftEvent.class, InventoryEventHandler::onPrepareItemCraftEvent);

    for (Map.Entry<Class<? extends Event>, InventoryHandlerInvoke<? extends Event>> entry : typedEventMap.entrySet()) {
      val eventType = entry.getKey();

      Class<?> currentType = eventType;
      while (typedEventMap.containsKey(currentType)) {
        typedEventHandler.put(eventType, typedEventMap.get(currentType));
        currentType = currentType.getSuperclass();
      }
    }
  }

  private <T extends InventoryEvent> void register(Class<T> eventType, BiFunction<InventoryEventHandler, T, Boolean> handler) {
    typedEventMap.put(eventType, new InventoryHandlerInvoke(eventType, handler, inventoryEventGetHandler));
  }

  @SubscriptLifecycle(PluginLifeCycle.ENABLE)
  private void registerEventListeners() {
    for (val entry : typedEventMap.entrySet()) {
      val eventType = entry.getKey();
      val eventHandler = entry.getValue();
      val handlerSet = typedEventHandler.get(eventType);

      if (!handlerSet.isEmpty()) {
        Bukkit.getPluginManager().registerEvent(eventType, this, EventPriority.NORMAL, (listener, event) -> {
          handleEvent(eventType, eventHandler, (Collection<InventoryHandlerInvoke>) (Object) handlerSet, event);
        }, plugin, true);
      }
    }

    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      for (val onlinePlayer : Bukkit.getOnlinePlayers()) {
        val inventoryView = onlinePlayer.getOpenInventory();
        val topHolder = inventoryView.getTopInventory().getHolder();
        if (topHolder instanceof InventoryEventHandler) {
          ((InventoryEventHandler) topHolder).onTick(onlinePlayer);
        }
        val bottomHolder = inventoryView.getBottomInventory().getHolder();
        if (bottomHolder instanceof InventoryEventHandler) {
          ((InventoryEventHandler) bottomHolder).onTick(onlinePlayer);
        }
      }
    }, 0, 1);
  }

  private void handleEvent(Class<? extends Event> eventType, InventoryHandlerInvoke eventHandler, Collection<InventoryHandlerInvoke> handlerSet, Event event) {
    if (event.getClass() == eventType && (!(event instanceof Cancellable) || !((Cancellable) event).isCancelled())) {
      val holder = (InventoryEventHandler) eventHandler.getHandler.apply(event);
      if (holder == null) {
        return;
      }
      for (val handlerInvoke : handlerSet) {
        if (!(Boolean) handlerInvoke.invoker.apply(holder, event) && event instanceof Cancellable) {
          ((Cancellable) event).setCancelled(true);
        }
      }
    }
  }

  @Value
  private static class InventoryHandlerInvoke<T extends Event> {
    Class<T> eventType;
    BiFunction<InventoryEventHandler, T, Boolean> invoker;
    Function<T, InventoryEventHandler> getHandler;
  }
}
