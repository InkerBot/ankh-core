package org.inksnow.ankh.core.config.adapter;

import com.google.gson.reflect.TypeToken;
import lombok.experimental.UtilityClass;
import org.inksnow.ankh.core.api.config.ConfigLoader;
import org.inksnow.ankh.core.api.config.ConfigTypeAdapter;

@UtilityClass
public class AdaptersUtils {
    public static <T> ConfigTypeAdapter.Factory<T> createFactory(Class<T> clazz, ConfigTypeAdapter<? super T> adapter) {
        return new ConfigTypeAdapter.Factory<T>() {
            @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
            @Override
            public <V extends T> ConfigTypeAdapter<V> create(ConfigLoader configLoader, TypeToken<? super V> typeToken) {
                return typeToken.getRawType() == clazz ? (ConfigTypeAdapter<V>) adapter : null;
            }
        };
    }

    public static <T> ConfigTypeAdapter.Factory<T> createFactory(Class<T> base, Class<? extends T> sub, ConfigTypeAdapter<? super T> adapter) {
        return new ConfigTypeAdapter.Factory<T>() {
            @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
            @Override
            public <V extends T> ConfigTypeAdapter<V> create(ConfigLoader configLoader, TypeToken<? super V> typeToken) {
                return (typeToken.getRawType() == base || typeToken.getRawType() == sub) ? (ConfigTypeAdapter<V>) adapter : null;
            }
        };
    }
}
