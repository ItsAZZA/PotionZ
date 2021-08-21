package com.itsazza.potionz.menu

import com.itsazza.potionz.PotionZ
import com.itsazza.potionz.util.*
import de.themoep.inventorygui.DynamicGuiElement
import de.themoep.inventorygui.GuiStateElement
import de.themoep.inventorygui.InventoryGui
import de.themoep.inventorygui.StaticGuiElement
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect

class PotionEffectMenu(private var potionEffect: PotionEffect) {
    private val potionTypes = listOf(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION)
    private var type = 0
    private val config = PotionZ.instance.config
    private val romanNumerals = config.getBoolean("romanNumerals")

    fun open(player: Player) {
        create(player).show(player)
    }

    private fun create(player: Player): InventoryGui {
        val gui = InventoryGui(
            PotionZ.instance,
            "Effect Editor",
            arrayOf(
                "         ",
                " abcAdef ",
                " ghiBjkl ",
                "    m    ",
                "   123   ",
                "   =@    "
            )
        )

        gui.addElements(
            amplifierButton,
            durationButton,
            createRemoveAmplifierButton('a', 50),
            createRemoveAmplifierButton('b', 10),
            createRemoveAmplifierButton('c', 1),
            createAddAmplifierButton('d', 1),
            createAddAmplifierButton('e', 10),
            createAddAmplifierButton('f', 50),
            createRemoveDurationButton('g', 6000),
            createRemoveDurationButton('h', 600),
            createRemoveDurationButton('i', 20),
            createAddDurationButton('j', 20),
            createAddDurationButton('k', 600),
            createAddDurationButton('l', 6000),
            givePotionItemButton,
            givePotionEffectButton,
            toggleParticlesButton.also {
                it.setState(potionEffect.hasParticles().toString())
            },
            Buttons.close,
            Buttons.backInHistory
        )
        if (player.hasPermission("potionz.menu.commandblock")) {
            gui.addElement(givePotionCommandBlockButton)
        }
        gui.setFiller(Material.PURPLE_STAINED_GLASS_PANE.item)
        gui.setCloseAction { false }
        return gui
    }

    private val amplifierButton: DynamicGuiElement
        get() = DynamicGuiElement('A') { _: HumanEntity? ->
            StaticGuiElement(
                'A',
                Material.GLOWSTONE_DUST.item,
                "§6§lAmplifier: §7${potionEffect.amplifier + 1}"
            )
        }

    private val durationButton: DynamicGuiElement
        get() = DynamicGuiElement('B') { _: HumanEntity? ->
            StaticGuiElement(
                'B',
                Material.REDSTONE.item,
                "§6§lDuration: §7${getPotionDurationMMSS(potionEffect.duration / 20)}",
            )

        }

    private fun createAddAmplifierButton(char: Char, amount: Int): StaticGuiElement {
        return StaticGuiElement(
            char,
            tippedArrow(Color.LIME),
            {
                val player = it.event.whoClicked as Player
                val current = potionEffect.amplifier
                potionEffect =
                    PotionEffect(potionEffect.type, potionEffect.duration, (current + amount).coerceAtMost(127))
                Sounds.play(player, Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON)
                it.gui.draw()
                return@StaticGuiElement true
            },
            "§a§lADD +$amount"
        )
    }

    private fun createRemoveAmplifierButton(char: Char, amount: Int): StaticGuiElement {
        return StaticGuiElement(
            char,
            tippedArrow(Color.RED),
            {
                val current = potionEffect.amplifier
                val player = it.event.whoClicked as Player
                potionEffect =
                    PotionEffect(potionEffect.type, potionEffect.duration, (current - amount).coerceAtLeast(0))
                Sounds.play(player, Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON)
                it.gui.draw()
                return@StaticGuiElement true
            },
            "§c§lREMOVE -$amount"
        )
    }

    private fun createAddDurationButton(char: Char, amount: Int): StaticGuiElement {
        return StaticGuiElement(
            char,
            tippedArrow(Color.LIME),
            {
                val current = potionEffect.duration
                val player = it.event.whoClicked as Player
                potionEffect =
                    PotionEffect(potionEffect.type, (current + amount).coerceAtMost(30000), potionEffect.amplifier)
                Sounds.play(player, Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON)
                it.gui.draw()
                return@StaticGuiElement true
            },
            "§a§lADD ${getPotionDurationMMSS(amount / 20)}"
        )
    }

    private fun createRemoveDurationButton(char: Char, amount: Int): StaticGuiElement {
        return StaticGuiElement(
            char,
            tippedArrow(Color.RED),
            {
                val current = potionEffect.duration
                val player = it.event.whoClicked as Player
                potionEffect =
                    PotionEffect(potionEffect.type, (current - amount).coerceAtLeast(20), potionEffect.amplifier)
                Sounds.play(player, Sound.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON)
                it.gui.draw()
                return@StaticGuiElement true
            },
            "§c§lREMOVE ${getPotionDurationMMSS(amount / 20)}"
        )
    }

    private val toggleParticlesButton: GuiStateElement
        get() = GuiStateElement(
            'm',
            GuiStateElement.State(
                {
                    potionEffect =
                        PotionEffect(potionEffect.type, potionEffect.duration, potionEffect.amplifier, false, true)
                    it.gui.draw()
                },
                "true",
                Material.LIME_DYE.item,
                "§6§lParticles",
                "§7Should the applied effect give",
                "§7the player a potion particle effect",
                "§0 ",
                "§a▶ Full",
                "§8Ambient",
                "§8None",
                "§0 ",
                "§eClick to toggle!"
            ),
            GuiStateElement.State(
                {
                    potionEffect =
                        PotionEffect(potionEffect.type, potionEffect.duration, potionEffect.amplifier, true, true)
                    it.gui.draw()
                },
                "ambient",
                Material.MAGENTA_DYE.item,
                "§6§lParticles",
                "§7Should the applied effect give",
                "§7the player a potion particle effect",
                "§0 ",
                "§8Full",
                "§d▶ Ambient",
                "§8None",
                "§0 ",
                "§eClick to toggle!"
            ),
            GuiStateElement.State(
                {
                    potionEffect =
                        PotionEffect(potionEffect.type, potionEffect.duration, potionEffect.amplifier, false, false)
                    it.gui.draw()
                },
                "false",
                Material.GRAY_DYE.item,
                "§6§lParticles",
                "§7Should the applied effect give",
                "§7the player a potion particle effect",
                "§0 ",
                "§8Full",
                "§8Ambient",
                "§c▶ None",
                "§0 ",
                "§eClick to toggle!"
            )
        )

    private val givePotionItemButton: DynamicGuiElement
        get() = DynamicGuiElement('1') { viewer: HumanEntity? ->
            StaticGuiElement(
                '1',
                potionTypes[type].item.mutateMeta<PotionMeta> {
                    it.color = potionEffect.type.color
                    it.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
                },
                {
                    val player = viewer as Player
                    val minecraftEffectName =
                        potionEffect.type.minecraftID().replace("minecraft:", "").split("_")
                            .joinToString(" ") { part -> part.toLowerCase().capitalize() }


                    if (it.type.isLeftClick) {
                        if (!player.hasPermission("potionz.menu.item")) {
                            player.sendMessage("§cYou don't have permission to get potion items!")
                            Sounds.play(player, Sound.ENTITY_VILLAGER_NO)
                            return@StaticGuiElement true
                        }

                        player.inventory.addItem(potionTypes[type].item.mutateMeta<PotionMeta> { potion ->
                            potion.color = potionEffect.type.color
                            potion.addCustomEffect(potionEffect, true)
                            potion.setDisplayName("§f${formatBukkitString(potionTypes[type].name)} of $minecraftEffectName")
                            potion.lore = arrayListOf(
                                "§9$minecraftEffectName ${
                                    if (romanNumerals) potionEffect.amplifier.plus(1)
                                        .toRomanNumeral() else potionEffect.amplifier.plus(1)
                                } (${getPotionDurationMMSS(potionEffect.duration / 20)})"
                            )
                            potion.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
                        })
                        return@StaticGuiElement true
                    } else {
                        type = (type + 1) % potionTypes.size
                        Sounds.play(player, Sound.ENTITY_CHICKEN_EGG)
                        it.gui.draw()
                        return@StaticGuiElement true
                    }
                },
                "§6§lGet Potion Item",
                "§7${formatBukkitString(potionEffect.type.name)}",
                "§8${potionEffect.type.minecraftID()}",
                "§0 ",
                "§aDuration: §7${getPotionDurationMMSS(potionEffect.duration / 20)}",
                "§aAmplifier: §7${potionEffect.amplifier + 1}",
                "§aType: §7${formatBukkitString(potionTypes[type].name)}",
                "§0 ",
                "§e§lL-CLICK §7to get",
                "§e§lR-CLICK §7to toggle type"
            )
        }

    private val givePotionCommandBlockButton: DynamicGuiElement
        get() = DynamicGuiElement('2') { viewer: HumanEntity? ->
            StaticGuiElement(
                '2',
                Material.COMMAND_BLOCK.item,
                {
                    val player = viewer as Player
                    val commandString =
                        "minecraft:effect give <selector> ${potionEffect.type.minecraftID()} ${potionEffect.duration / 20} ${potionEffect.amplifier} ${!potionEffect.hasParticles()}"
                    when (it.event.click) {
                        ClickType.LEFT -> {
                            player.inventory.addItem(commandBlockWithCommand(commandString.replace("<selector>", "@p")))
                            return@StaticGuiElement true
                        }
                        ClickType.RIGHT -> {
                            player.inventory.addItem(commandBlockWithCommand(commandString.replace("<selector>", "@a")))
                            return@StaticGuiElement true
                        }
                        ClickType.SHIFT_LEFT,
                        ClickType.SHIFT_RIGHT -> {
                            player.inventory.addItem(commandBlockWithCommand(commandString.replace("<selector>", "@e")))
                            return@StaticGuiElement true
                        }
                        else -> return@StaticGuiElement true
                    }
                },
                "§6§lGet Potion Command Block",
                "§7${formatBukkitString(potionEffect.type.name)}",
                "§8${potionEffect.type.minecraftID()}",
                "§0 ",
                "§aDuration: §7${getPotionDurationMMSS(potionEffect.duration / 20)}",
                "§aAmplifier: §7${potionEffect.amplifier + 1}",
                "§0 ",
                "§e§lL-CLICK §7to get with selector @p",
                "§e§lR-CLICK §7to get with selector @a",
                "§e§lSHIFT-CLICK §7to get with selector @e"
            )
        }

    private val givePotionEffectButton: DynamicGuiElement
        get() = DynamicGuiElement(
            '3'
        ) { viewer: HumanEntity? ->
            StaticGuiElement(
                '3',
                Material.BEACON.item,
                {
                    val player = viewer as Player
                    if (!player.hasPermission("potionz.menu.effect")) {
                        player.sendMessage("§cYou don't have permission to get potion items!")
                        Sounds.play(player, Sound.ENTITY_VILLAGER_NO)
                        return@StaticGuiElement true
                    }
                    player.addPotionEffect(potionEffect)
                    Sounds.play(player, Sound.ENTITY_VILLAGER_YES)
                    player.sendMessage(
                        "§eApplied ${formatBukkitString(potionEffect.type.name)} ${
                            if (romanNumerals) potionEffect.amplifier.plus(
                                1
                            ).toRomanNumeral() else potionEffect.amplifier.plus(1)
                        } for ${getPotionDurationMMSS(potionEffect.duration / 20)}"
                    )
                    it.gui.close()
                    return@StaticGuiElement true
                },
                "§6§lApply Potion Effect",
                "§7${formatBukkitString(potionEffect.type.name)}",
                "§8${potionEffect.type.minecraftID()}",
                "§0 ",
                "§aDuration: §7${getPotionDurationMMSS(potionEffect.duration / 20)}",
                "§aAmplifier: §7${potionEffect.amplifier + 1}",
                "§0 ",
                "§e§lL-CLICK §7to apply",
            )
        }

    private fun getPotionDurationMMSS(time: Int): String {
        return "%02d:%02d".format(time / 60, time % 60)
    }
}