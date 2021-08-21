package com.itsazza.potionz.util

fun formatBukkitString(string: String) : String {
    return string.split("_").joinToString(" ") { it.toLowerCase().capitalize() }
}