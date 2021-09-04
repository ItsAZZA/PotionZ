package com.itsazza.potionz.menu

import com.itsazza.potionz.PotionZ
import com.itsazza.potionz.util.*
import de.themoep.inventorygui.DynamicGuiElement
import de.themoep.inventorygui.GuiElementGroup
import de.themoep.inventorygui.InventoryGui
import de.themoep.inventorygui.StaticGuiElement
import de.tr7zw.changeme.nbtapi.NBTContainer
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class PotionMainMenu (private val potions: HashSet<PotionEffect> = hashSetOf())  {
    private val potionTypes = listOf(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION)
    private var type = 0
    private val config = PotionZ.instance.config
    private val romanNumerals = config.getBoolean("romanNumerals")
    private val statusInformation = config.getBoolean("statusInformation")
    lateinit var player: Player

    fun open(player: Player) {
        if (!player.hasPermission("potionz.menu")) {
            player.sendMessage("§cNo permission to view the menu!")
            return
        }
        this.player = player
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
                "  1234@  ",
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
            cancelAllEffects,
            givePotionItemButton,
            givePotionEffectButton,
        )

        if (player.hasPermission("potionz.menu.commandblock")) {
            gui.addElement(givePotionCommandBlockButton)
        }

        gui.setFiller(Material.PURPLE_STAINED_GLASS_PANE.item)
        gui.setCloseAction { false }
        return gui
    }

    private fun createPotionButton(potionEffectType: PotionEffectType) : StaticGuiElement {
        return StaticGuiElement(
            '!',
            Material.POTION.item.mutateMeta<PotionMeta> {
                if (potions.firstOrNull {potion -> potion.type == potionEffectType } != null) {
                    it.addEnchant(Enchantment.LUCK, 1, true)
                }
                it.color = potionEffectType.color
                it.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
            },
            {
                val player = it.event.whoClicked as Player
                val potion = potions.firstOrNull { potion -> potion.type == potionEffectType }
                if (potion != null) {
                    PotionEffectMenu(PotionEffect(potion.type, potion.duration, potion.amplifier, potion.isAmbient, potion.hasParticles(), potion.hasIcon()), potions)
                        .open(player)
                } else {
                    PotionEffectMenu(PotionEffect(potionEffectType, 20, 0, false, false, true), potions).open(player)
                }
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

    private val givePotionItemButton: DynamicGuiElement
        get() = DynamicGuiElement('1') { viewer: HumanEntity? ->
            StaticGuiElement(
                '1',
                potionTypes[type].item.mutateMeta<PotionMeta> {
                    if(potions.isNotEmpty()) {
                        it.color = potions.first().type.color
                    } else {
                        it.color = Color.ORANGE
                    }
                    it.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
                },
                {
                    val player = viewer as Player
                    if (it.type.isLeftClick) {
                        if (!player.hasPermission("potionz.menu.item")) {
                            player.sendMessage("§cYou don't have permission to get potion items!")
                            Sounds.play(player, Sound.ENTITY_VILLAGER_NO)
                            return@StaticGuiElement true
                        }

                        if (potions.isEmpty()) {
                            player.sendMessage("§cThere are no created effects!")
                            Sounds.play(player, Sound.ENTITY_VILLAGER_NO)
                            return@StaticGuiElement true
                        }

                        player.inventory.addItem(potionTypes[type].item.mutateMeta<PotionMeta> { potion ->
                            // Colors the potion based on the first effect
                            potion.color = potions.first().type.color
                            for (effect in potions) {
                                potion.addCustomEffect(effect, true)
                            }
                            potion.setDisplayName(if (potions.size == 1) "§f${formatBukkitString(potionTypes[type].name)} of ${getPotionName(potions.first())}" else "§fCustom Potion")
                            potion.lore = arrayListOf<String>().also {
                                potions.forEach { potionEffect ->
                                    it.add(getPotionDescription(potionEffect))
                                }
                            }
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
                "§8${formatBukkitString(potionTypes[type].name)}",
                "§0 ",
                if (potions.isNotEmpty()) arrayListOf<String>().also {
                    for (effect in potions) {
                        it.add(getPotionDescription(effect))
                    }
                }.joinToString("\n") else "§cNo Effects",
                "§0 ",
                "§e§lL-CLICK §7to get",
                "§e§lR-CLICK §7to toggle type"
            )
        }

    @Suppress("DEPRECATION")
    private val givePotionCommandBlockButton: DynamicGuiElement
        get() = DynamicGuiElement('2') { viewer: HumanEntity? ->
            StaticGuiElement(
                '2',
                Material.COMMAND_BLOCK.item,
                {
                    val player = viewer as Player
                    if (potions.isEmpty()) {
                        player.sendMessage("§cThere are no effects created!")
                        Sounds.play(player, Sound.ENTITY_VILLAGER_NO)
                        return@StaticGuiElement true
                    }

                    when (it.event.click) {
                        ClickType.LEFT -> {
                            for (effect in potions) {
                                player.inventory.addItem(commandBlockWithCommand("minecraft:effect give @p ${effect.type.minecraftID()} ${effect.duration / 20} ${effect.amplifier} ${!effect.hasParticles()}"))
                            }
                            return@StaticGuiElement true
                        }
                        ClickType.RIGHT -> {
                            val potionDataArray = arrayListOf<String>()
                            potions.forEach { potion ->
                                val id = potion.type.id
                                val amplifier = potion.amplifier
                                val duration = potion.duration
                                val ambient = potion.isAmbient
                                val particles = potion.hasParticles()
                                val icon = potion.hasIcon()
                                potionDataArray.add("{Id:$id,Amplifier:$amplifier,Duration:$duration,Ambient:$ambient,ShowParticles:$particles,ShowIcon:$icon}")
                            }
                            val container = NBTContainer("{CustomPotionEffects:[${potionDataArray.joinToString(",")}]}")
                            val color = potions.first().type.color.asRGB()
                            container.setInteger("CustomPotionColor", color)
                            val command = "minecraft:give @p minecraft:potion$container"
                            player.inventory.addItem(commandBlockWithCommand(command))
                            return@StaticGuiElement true
                        }
                        else -> return@StaticGuiElement true
                    }
                },
                "§6§lGet Potion Command Block",
                "§0 ",
                if (potions.isNotEmpty()) arrayListOf<String>().also {
                    for (effect in potions) {
                        it.add(getPotionDescription(effect))
                    }
                }.joinToString("\n") else "§cNo Effects",
                "§0 ",
                "§e§lL-CLICK §7to get /effect",
                "§c^ Gives cmdblock for each effect!",
                "§e§lR-CLICK §7to get /give potion item"
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

                    if (potions.isEmpty()) {
                        player.sendMessage("§cThere are no effects to apply!")
                        Sounds.play(player, Sound.ENTITY_VILLAGER_NO)
                        return@StaticGuiElement true
                    }

                    for (effect in potions) {
                        player.addPotionEffect(effect)
                    }
                    Sounds.play(player, Sound.ENTITY_VILLAGER_YES)

                    val potionStrings = arrayListOf<String>()
                    for (effect in potions) {
                        potionStrings.add(getPotionDescription(effect))
                    }

                    player.sendMessage("§eApplied Effects: ${potionStrings.joinToString("§7, ")}")
                    it.gui.close()
                    return@StaticGuiElement true
                },
                "§6§lApply Potion Effect(s)",
                "§0 ",
                if (potions.isNotEmpty()) arrayListOf<String>().also {
                    for (effect in potions) {
                        it.add(getPotionDescription(effect))
                    }
                }.joinToString("\n") else "§cNo Effects",
                "§0 ",
                "§e§lL-CLICK §7to apply",
            )
        }

    private val cancelAllEffects: StaticGuiElement
        get() = StaticGuiElement(
            '4',
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

    private fun getPotionName(potionEffect: PotionEffect): String {
        return potionEffect.type.minecraftID().replace("minecraft:", "").split("_")
            .joinToString(" ") { part -> part.toLowerCase().capitalize() }
    }

    private fun getPotionDescription(potionEffect: PotionEffect): String {
        val effectName= formatBukkitString(potionEffect.type.name)
        val amplifier = if (romanNumerals) potionEffect.amplifier.plus(1).toRomanNumeral() else potionEffect.amplifier.plus(1)
        val duration = getPotionDurationMMSS(potionEffect.duration / 20)
        val particles = "${getPotionParticleStatusColor(potionEffect)}*${if (potionEffect.hasIcon()) "§a" else "§c"}*"

        return "§9$effectName $amplifier ($duration)${if (statusInformation) " $particles" else ""}"
    }

    private fun getPotionParticleStatusColor(potionEffect: PotionEffect): String {
        return if (potionEffect.hasParticles()) {
            if (potionEffect.isAmbient) {
                "§d"
            } else {
                "§a"
            }
        } else {
            "§c"
        }
    }

    private fun getPotionDurationMMSS(time: Int): String {
        return "%02d:%02d".format(time / 60, time % 60)
    }
}