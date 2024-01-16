package org.inksnow.ankh.kts.menu.dsl

import it.unimi.dsi.fastutil.ints.IntList
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.inksnow.ankh.core.api.inventory.menu.slot.action.ConfigurableSlotAction
import org.inksnow.ankh.core.api.inventory.menu.slot.icon.ConfigurableSlotIcon
import org.inksnow.ankh.kts.dsl.IDsl
import org.inksnow.ankh.kts.dsl.createVar
import org.inksnow.ankh.kts.dsl.createVarCollection

class SlotDsl internal constructor(
    private val layout: LayoutDsl,
    private val slots: IntList
) : IDsl {
    val slot get() = this

    val mutable by lazy {
        createVarCollection(slots, { slotId ->
            (layout.menu.actions[slotId] as ConfigurableSlotAction).mutable()
        }, { slotId, value ->
            (layout.menu.actions[slotId] as ConfigurableSlotAction).mutable(value)
        })
    }

    val item by lazy {
        createVarCollection(slots, { slotId ->
            layout.menu.inventory.getItem(slotId)
        }, { slotId, value ->
            (layout.menu.icons[slotId] as ConfigurableSlotIcon).item(value)
            layout.menu.inventory.setItem(slotId, value)
        })
    }

    fun onUpdate(action: () -> Unit) {
        layout.menu.actions.forEach {
            (it as ConfigurableSlotAction).onUpdate(action)
        }
    }
}

val ItemStack.displayName
    get() = createVar({
        itemMeta?.displayName() ?: Component.empty()
    }, {
        editMeta { itemMeta ->
            itemMeta.displayName(it)
        }
    })

val SlotDsl.freeSlot: Unit
    get() {
        mutable(true)
        item(ItemStack(Material.AIR))
    }

val SlotDsl.button: Unit
    get() {
        mutable(false)
    }