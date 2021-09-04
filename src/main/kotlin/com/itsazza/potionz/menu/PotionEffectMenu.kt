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
import org.bukkit.potion.PotionEffect

class PotionEffectMenu(private var potionEffect: PotionEffect, private val potions: HashSet<PotionEffect>) {
    lateinit var player: Player

    fun open(player: Player) {
        this.player = player
        create().show(player)
    }

    private fun create(): InventoryGui {
        val gui = InventoryGui(
            PotionZ.instance,
            "Effect Editor",
            arrayOf(
                "         ",
                " abcAdef ",
                " ghiBjkl ",
                "   m n   ",
                "         ",
                "   =@1   "
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
            toggleParticlesButton.also {
                val ambient = potionEffect.isAmbient
                val particles = potionEffect.hasParticles()
                if (particles) {
                    if (ambient) {
                        it.setState("ambient")
                    } else {
                        it.setState("true")
                    }
                } else {
                    it.setState("false")
                }
            },
            toggleIconButton.also {
                it.setState(potionEffect.hasIcon().toString())
            },
            Buttons.close,
            Buttons.backInHistory,
            saveButton
        )
        gui.setFiller(Material.PURPLE_STAINED_GLASS_PANE.item)
        gui.setCloseAction { false }
        return gui
    }

    private val saveButton: StaticGuiElement
        get() = StaticGuiElement(
            '1',
            Material.GREEN_DYE.item,
            {
                potions.removeIf { it.type == potionEffect.type }
                potions.add(potionEffect)
                PotionMainMenu(potions).open(player)
                return@StaticGuiElement true
            },
            "§eAdd Effect",
            "§7Adds this effect to the list",
            "§7of effects and opens the potion",
            "§7selection menu where you can get",
            "§7the created potion",
            "§0 ",
            "§eClick to save!"
        )

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
                    PotionEffect(potionEffect.type, potionEffect.duration, (current + amount).coerceAtMost(127), potionEffect.isAmbient, potionEffect.hasParticles(), potionEffect.hasIcon())
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
                    PotionEffect(potionEffect.type, potionEffect.duration, (current - amount).coerceAtLeast(0), potionEffect.isAmbient, potionEffect.hasParticles(), potionEffect.hasIcon())
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
                    PotionEffect(potionEffect.type, (current + amount).coerceAtMost(30000), potionEffect.amplifier, potionEffect.isAmbient, potionEffect.hasParticles(), potionEffect.hasIcon())
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
                    PotionEffect(potionEffect.type, (current - amount).coerceAtLeast(20), potionEffect.amplifier, potionEffect.isAmbient, potionEffect.hasParticles(), potionEffect.hasIcon())
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
                        PotionEffect(potionEffect.type, potionEffect.duration, potionEffect.amplifier, false, true, potionEffect.hasIcon())
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
                        PotionEffect(potionEffect.type, potionEffect.duration, potionEffect.amplifier, true, true, potionEffect.hasIcon())
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
                        PotionEffect(potionEffect.type, potionEffect.duration, potionEffect.amplifier, false, false, potionEffect.hasIcon())
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

    private val toggleIconButton: GuiStateElement
    get() = GuiStateElement(
        'n',
        GuiStateElement.State(
            {
                potionEffect = PotionEffect(potionEffect.type, potionEffect.duration, potionEffect.amplifier, potionEffect.isAmbient, potionEffect.hasParticles(), false)
            },
            "false",
            Material.DEAD_FIRE_CORAL.item,
            "§6§lEffect Icon",
            "§7Should the applied effect show",
            "§7the player the icon on the top right",
            "§7of their screen",
            "§0 ",
            "§cDoesn't work when applying with a",
            "§ccommand block",
            "§0 ",
            "§8Show",
            "§c▶ Don't Show",
            "§0 ",
            "§eClick to toggle!"
        ),
        GuiStateElement.State(
            {
                potionEffect = PotionEffect(potionEffect.type, potionEffect.duration, potionEffect.amplifier, potionEffect.isAmbient, potionEffect.hasParticles(), true)
            },
            "true",
            Material.FIRE_CORAL.item,
            "§6§lEffect Icon",
            "§7Should the applied effect show",
            "§7the player the icon on the top right",
            "§7of their screen",
            "§0 ",
            "§cDoesn't work when applying with a",
            "§ccommand block",
            "§0 ",
            "§a▶ Show",
            "§8Don't Show",
            "§0 ",
            "§eClick to toggle!"
        )
    )

    private fun getPotionDurationMMSS(time: Int): String {
        return "%02d:%02d".format(time / 60, time % 60)
    }
}