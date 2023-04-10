package org.inksnow.ankh.core.common.action;

import org.bukkit.event.Cancellable;

@FunctionalInterface
public interface ActionAcceptEventCancellable<T> {
  @SuppressWarnings("unchecked")
  static <E> ActionAcceptEventCancellable<E> nop() {
    return (ActionAcceptEventCancellable<E>) Nop.INSTANCE;
  }

  @SuppressWarnings("unchecked")
  static <E> ActionAcceptEventCancellable<E> cancel() {
    return (ActionAcceptEventCancellable<E>) Cancel.INSTANCE;
  }

  void accept(T event, Cancellable cancelToken);

  @SuppressWarnings("rawtypes")
  class Nop implements ActionAcceptEventCancellable {
    private static final ActionAcceptEventCancellable INSTANCE = new Nop();

    private Nop() {
      //
    }

    @Override
    public void accept(Object event, Cancellable cancelToken) {
      //
    }
  }

  @SuppressWarnings("rawtypes")
  class Cancel implements ActionAcceptEventCancellable {
    private static final ActionAcceptEventCancellable INSTANCE = new Cancel();

    private Cancel() {
      //
    }

    @Override
    public void accept(Object event, Cancellable cancelToken) {
      cancelToken.setCancelled(true);
    }
  }
}
