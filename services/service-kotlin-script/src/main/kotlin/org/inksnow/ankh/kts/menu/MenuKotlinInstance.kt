package org.inksnow.ankh.kts.menu

import org.bukkit.entity.Player
import org.inksnow.ankh.core.api.inventory.menu.slot.SlotAction
import org.inksnow.ankh.core.api.inventory.menu.slot.SlotIcon
import org.inksnow.ankh.core.inventory.menu.AnkhMenuImpl
import java.util.function.LongConsumer

class MenuKotlinInstance(
    val player: Player,
) : AnkhMenuImpl() {
    var accessorIcons
        get() = icons
        set(value) {
            icons = value
        }
    var accessorActions
        get() = actions
        set(value) {
            actions = value
        }

    var tick = 0L
    val tickActions = ArrayList<LongConsumer>()

    override fun onTick(player: Player) {
        val currentTick = tick++
        tickActions.forEach {
            it.accept(currentTick)
        }
    }

    init {
        icons = Array(54) { SlotIcon.create(null) }
        actions = Array(54) { SlotAction.create() }
    }
}