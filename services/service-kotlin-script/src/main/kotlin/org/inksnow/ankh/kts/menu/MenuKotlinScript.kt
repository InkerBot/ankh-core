package org.inksnow.ankh.kts.menu

import org.inksnow.ankh.kts.menu.dsl.MenuDsl
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    fileExtension = "ankhmenu.kts",
    compilationConfiguration = MenuKotlinConfiguration::class
)
abstract class MenuKotlinScript {
    internal val menuActions = ArrayList<MenuDsl.() -> Unit>()
}