package org.inksnow.ankh.kts.dsl

import java.util.function.LongConsumer

interface RegistrableTicker {
    fun onTick(action: LongConsumer)
}