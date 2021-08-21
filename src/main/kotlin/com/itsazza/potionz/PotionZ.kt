package com.itsazza.potionz

import com.itsazza.potionz.command.PotionZCommand
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

class PotionZ : JavaPlugin() {
    companion object {
        lateinit var instance: PotionZ
            private set
    }

    override fun onEnable() {
        instance = this
        saveDefaultConfig()
        getCommand("potionz")?.setExecutor(PotionZCommand)
        Metrics(this, 12533)
    }
}