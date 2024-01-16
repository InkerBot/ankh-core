package org.inksnow.ankh.kts.dsl

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage

inline operator fun DslVar<Component>.invoke(action: TextComponent.Builder.() -> Unit) {
    set(Component.text().apply(action).build())
}

fun miniMessage(input: String): Component {
    return MiniMessage.miniMessage().deserialize(input)
}

infix fun DslVar<in Component>.miniMessage(value: String) {
    set(MiniMessage.miniMessage().deserialize(value))
}