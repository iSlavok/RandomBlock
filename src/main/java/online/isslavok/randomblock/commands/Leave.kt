package online.isslavok.randomblock.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object Leave : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Команду может использовать только игрок")
            return true
        }
        GameController.getGame("solo").leave(sender)
        GameController.getGame("doubles").leave(sender)
        GameController.getGame("teams").leave(sender)
        GameController.lobby(sender)
        return true
    }
}