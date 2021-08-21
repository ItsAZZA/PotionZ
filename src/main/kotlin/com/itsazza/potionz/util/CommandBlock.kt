package com.itsazza.potionz.util

import de.tr7zw.changeme.nbtapi.NBTItem
import org.bukkit.inventory.ItemStack

fun commandBlockWithCommand(command: String): ItemStack {
    val commandBlockItem = NBTItem(org.bukkit.Material.COMMAND_BLOCK.item)
    val tag = commandBlockItem.getOrCreateCompound("BlockEntityTag")
    tag.setString("Command", command)
    commandBlockItem.mergeCompound(tag)
    return commandBlockItem.item
}