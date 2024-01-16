package org.inksnow.ankh.kts.menu.dsl

import org.inksnow.ankh.core.api.inventory.menu.slot.SlotAction
import org.inksnow.ankh.core.api.inventory.menu.slot.SlotIcon
import org.inksnow.ankh.kts.dsl.IDsl
import org.inksnow.ankh.kts.dsl.RegistrableTicker
import org.inksnow.ankh.kts.menu.MenuKotlinInstance
import org.inksnow.ankh.kts.menu.MenuKotlinScript
import java.util.function.LongConsumer

class MenuDsl(
    private val instance: MenuKotlinInstance
) : RegistrableTicker, IDsl {
    val menu = this
    operator fun invoke(action: MenuDsl.() -> Unit) {
        action(this)
    }

    val inventory get() = instance.inventory
    val player get() = instance.player

    var icons: Array<SlotIcon>
        get() = instance.accessorIcons
        set(value) {
            instance.accessorIcons = value
        }
    var actions: Array<SlotAction>
        get() = instance.accessorActions
        set(value) {
            instance.accessorActions = value
        }


    override fun onTick(action: LongConsumer) {
        instance.tickActions.add(action)
    }
}

fun MenuKotlinScript.menu(action: MenuDsl.() -> Unit) {
    menuActions.add(action)
}