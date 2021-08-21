package com.itsazza.potionz.util

import de.themoep.inventorygui.GuiBackElement
import de.themoep.inventorygui.StaticGuiElement
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object Buttons {
    val close: StaticGuiElement
        get() = StaticGuiElement('@',
            ItemStack(Material.BARRIER),
            {
                it.gui.destroy()
                return@StaticGuiElement true
            },
            "§c§lClose Menu"
        )

    val backInHistory : GuiBackElement
        get() = GuiBackElement('=',
            tippedArrow(Color.RED),
            "§c§lBack"
        )
}