package org.inksnow.ankh.core.config.adapter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.inksnow.ankh.core.api.config.ConfigLoader;
import org.inksnow.ankh.core.api.config.ConfigTypeAdapter;

import static org.inksnow.ankh.core.config.adapter.AdaptersUtils.createFactory;

public class MinecraftTypeAdapters {
    private MinecraftTypeAdapters() {
        throw new UnsupportedOperationException();
    }

    public static final ConfigTypeAdapter.Factory<Component> COMPONENT = createFactory(Component.class, it-> MiniMessage.miniMessage().deserialize(it.asString()));

    public static void install(ConfigLoader.Builder builder){
        builder.registerFactory(COMPONENT);
    }
}
