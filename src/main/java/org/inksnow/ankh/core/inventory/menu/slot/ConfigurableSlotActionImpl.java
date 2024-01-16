package org.inksnow.ankh.core.inventory.menu.slot;

import lombok.*;
import org.inksnow.ankh.core.api.inventory.menu.slot.SlotAction;
import org.inksnow.ankh.core.api.inventory.menu.slot.action.ConfigurableSlotAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigurableSlotActionImpl implements ConfigurableSlotAction {
  private @Nullable List<Runnable> updateListeners = new ArrayList<>();

  @Getter
  @Setter
  private boolean mutable = false;

  public void onUpdate(@Nonnull Runnable listener) {
    if (updateListeners == null) {
      updateListeners = new ArrayList<>();
    }
    updateListeners.add(listener);
  }

  @Override
  public final void update() {
    if (updateListeners != null) {
      for (val listener : updateListeners) {
        listener.run();
      }
    }
  }

  @Singleton
  public static class Factory implements SlotAction.Factory {
    @Override
    public @Nonnull ConfigurableSlotActionImpl create() {
      return new ConfigurableSlotActionImpl();
    }
  }
}
