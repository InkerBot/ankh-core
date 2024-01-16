package org.inksnow.ankh.kts.menu.dsl

import it.unimi.dsi.fastutil.ints.IntArrayList
import org.inksnow.ankh.kts.dsl.IDsl

class LayoutDsl internal constructor(
    val menu: MenuDsl
) : IDsl {
    val layout get() = this
    var shape: Array<String> = arrayOf()

    operator fun Char.invoke(action: SlotDsl.() -> Unit) {
        val slotIds = IntArrayList()
        for ((i, line) in shape.withIndex()) {
            for ((j, char) in line.withIndex()) {
                if (this == char) {
                    slotIds.add(i * 9 + j)
                }
            }
        }
        SlotDsl(this@LayoutDsl, slotIds).action()
    }
}

fun MenuDsl.layout(action: LayoutDsl.() -> Unit) {
    val layout = LayoutDsl(this)
    action(layout)
}