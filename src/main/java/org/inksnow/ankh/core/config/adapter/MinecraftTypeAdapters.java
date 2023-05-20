package org.inksnow.ankh.core.config.adapter;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.inksnow.ankh.core.api.config.ConfigTypeAdapter;

import java.util.List;

import static org.inksnow.ankh.core.config.adapter.AdaptersUtils.createFactory;

public class MinecraftTypeAdapters {
    private MinecraftTypeAdapters() {
        throw new UnsupportedOperationException();
    }

    public static final ConfigTypeAdapter.Factory<Component> COMPONENT = createFactory(Component.class, it-> MiniMessage.miniMessage().deserialize(it.asString()));
    public static final ConfigTypeAdapter.Factory<Key> KEY = createFactory(Key.class, it->Key.key(it.asString()));

    public static final List<ConfigTypeAdapter.Factory> asList = ImmutableList.<ConfigTypeAdapter.Factory>builder()
            .add(COMPONENT, KEY)
            .build();
}
