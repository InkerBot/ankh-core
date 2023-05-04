package org.inksnow.ankh.core.common.util;

import com.google.common.collect.MapMaker;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.inksnow.ankh.core.common.config.AnkhConfig;

import java.util.concurrent.ConcurrentMap;

@UtilityClass
public class CacheMapUtil {
  private static final MapMaker mapMaker = new MapMaker();

  static {
    val performance = AnkhConfig.instance().performance();
    if (performance.cacheConcurrencyLevel() != -1) {
      mapMaker.concurrencyLevel(performance.cacheConcurrencyLevel());
    }
    if (performance.cacheInitialCapacity() != -1) {
      mapMaker.initialCapacity(performance.cacheInitialCapacity());
    }
    if (performance.cacheKeyWeak()) {
      mapMaker.weakKeys();
    }
    if(performance.cacheValueWeak()){
      mapMaker.weakValues();
    }
  }

  public static <K, V> ConcurrentMap<K, V> make(){
    return mapMaker.makeMap();
  }
}
