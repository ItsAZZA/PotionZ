package com.itsazza.potionz.util

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

val Material.item
    get() = ItemStack(this, 1)

inline fun <reified T> ItemStack.mutateMeta(mutator: (T) -> Unit): ItemStack {
    this.itemMeta = this.itemMeta.also { mutator(it as T) }
    return this
}