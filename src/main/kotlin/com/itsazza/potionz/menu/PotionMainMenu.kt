package com.itsazza.potionz.menu

import com.itsazza.potionz.PotionZ
import com.itsazza.potionz.util.*
import de.themoep.inventorygui.GuiElementGroup
import de.themoep.inventorygui.InventoryGui
import de.themoep.inventorygui.StaticGuiElement
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object PotionMainMenu  {
    fun open(player: Player) {
        if (!player.hasPermission("potionz.menu")) {
            player.sendMessage("§cNo permission to view the menu!")
            return
        }
        create().show(player)
    }

    private fun create(): InventoryGui {
        val gui = InventoryGui(
            PotionZ.instance,
            "Potion Menu",
            arrayOf (
                " 0000000 ",
                "000000000",
                "000000000",
                " 0000000 ",
                "         ",
                "    @1   ",
            )
        )

        val group = GuiElementGroup('0')
        PotionEffectType.values()
            .sortedBy { it.name }
            .forEach {
                group.addElement(createPotionButton(it))
            }

        gui.addElements(
            Buttons.close,
            group,
            cancelAllEffects
        )

        gui.setFiller(Material.PURPLE_STAINED_GLASS_PANE.item)
        return gui
    }

    private fun createPotionButton(potionEffectType: PotionEffectType) : StaticGuiElement {
        return StaticGuiElement(
            '!',
            Material.POTION.item.mutateMeta<PotionMeta> {
                it.color = potionEffectType.color
                it.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
            },
            {
                val player = it.event.whoClicked as Player
                PotionEffectMenu(PotionEffect(potionEffectType, 20, 0, false, false)).open(player)
                return@StaticGuiElement true
            },
            "§6§l${formatBukkitString(potionEffectType.name)}",
            "§8${potionEffectType.minecraftID()}",
            "§0 ",
            "§7Modify, get and apply",
            "§7this potion effect.",
            "§0 ",
            "§eClick to select!"
        )
    }

    private val cancelAllEffects: StaticGuiElement
        get() = StaticGuiElement(
            '1',
            Material.MILK_BUCKET.item,
            {
                val player = it.event.whoClicked as Player
                player.activePotionEffects.forEach { effect ->
                    player.removePotionEffect(effect.type)
                }
                Sounds.play(player, Sound.ENTITY_VILLAGER_YES)
                player.sendMessage("§6Removed all active potion effects!")
                return@StaticGuiElement true
            },
            "§6§lCancel all effects",
            "§0 ",
            "§7Removes all of your active",
            "§7potion effects",
            "§0 ",
            "§eClick to apply!"
        )
}