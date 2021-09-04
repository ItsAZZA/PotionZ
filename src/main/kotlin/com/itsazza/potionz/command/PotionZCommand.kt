package com.itsazza.potionz.command

import com.itsazza.potionz.PotionZ
import com.itsazza.potionz.menu.PotionMainMenu
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object PotionZCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true
        if (args.isEmpty()) {
            PotionMainMenu().open(sender)
            return true
        }

        when (args[0]) {
            "reload" -> {
                if (sender.hasPermission("potionz.reload")) {
                    sender.sendMessage("§eReloaded configuration!")
                    PotionZ.instance.reloadConfig()
                }
            }
            else -> PotionMainMenu().open(sender)
        }
        return true
    }
}