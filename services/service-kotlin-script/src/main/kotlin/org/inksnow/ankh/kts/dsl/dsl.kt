package org.inksnow.ankh.kts.dsl

interface IDsl

inline fun <T : IDsl> T.invoke(action: T.() -> Unit) {
    action(this)
}