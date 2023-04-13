package org.inksnow.ankh.core.hologram.hds;

import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import org.inksnow.ankh.core.api.AnkhCoreLoader;
import org.inksnow.ankh.core.api.hologram.HologramContent;
import org.inksnow.ankh.core.api.hologram.HologramService;
import org.inksnow.ankh.core.api.hologram.HologramTask;
import org.inksnow.ankh.core.api.util.DcLazy;

import javax.inject.Inject;

public class HdsHologramService implements HologramService {
  private final AnkhCoreLoader ankhCoreLoader;
  private final DcLazy<HolographicDisplaysAPI> hdApi;

  @Inject
  private HdsHologramService(AnkhCoreLoader ankhCoreLoader) {
    this.ankhCoreLoader = ankhCoreLoader;
    this.hdApi = DcLazy.of(this::createHdApi);
  }

  private HolographicDisplaysAPI createHdApi() {
    return HolographicDisplaysAPI.get(ankhCoreLoader);
  }

  @Override
  public HologramContent.Builder content() {
    return new HdsHologramContent.Builder();
  }

  @Override
  public HologramTask.Builder builder() {
    return new HdsHologramTask.Builder(hdApi.get());
  }
}
