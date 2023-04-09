package org.inksnow.ankh.core.common.action;

import org.bukkit.event.Cancellable;

@FunctionalInterface
public interface ActionAcceptEvent<T> {
  void accept(T event);

  @SuppressWarnings("unchecked")
  static <E> ActionAcceptEvent<E> nop(){
    return (ActionAcceptEvent<E>) Nop.INSTANCE;
  }

  @SuppressWarnings("unchecked")
  static <E> ActionAcceptEvent<E> cancel(){
    return (ActionAcceptEvent<E>) Cancel.INSTANCE;
  }

  @SuppressWarnings("rawtypes")
  class Nop implements ActionAcceptEvent {
    private static final ActionAcceptEvent INSTANCE = new Nop();

    private Nop(){
      //
    }

    @Override
    public void accept(Object event) {
      //
    }
  }

  @SuppressWarnings("rawtypes")
  class Cancel implements ActionAcceptEvent {
    private static final ActionAcceptEvent INSTANCE = new Cancel();

    private Cancel(){
      //
    }

    @Override
    public void accept(Object event) {
      if (event instanceof Cancellable) {
        ((Cancellable) event).setCancelled(true);
      }else{
        throw new IllegalArgumentException("try to cancelled event which not support cancel: " + event.getClass().getName());
      }
    }
  }
}
