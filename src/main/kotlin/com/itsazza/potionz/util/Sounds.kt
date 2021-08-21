package com.itsazza.potionz.util

import org.bukkit.Sound
import org.bukkit.entity.Player

object Sounds {
    fun play(player: Player, sound: Sound) {
        player.playSound(player.location, sound, 1f, 1f)
    }
}