package online.isslavok.randomblock.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object Join : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Команду может использовать только игрок")
            return true
        }
        if (args != null) {
            if (args.isEmpty()) {
                sender.sendMessage(Component.text("Вы не выбрали игру", NamedTextColor.RED))
                return false
            }
            if (args[0] == "solo") {
                GameController.getGame("doubles").leave(sender)
                GameController.getGame("teams").leave(sender)
                var team: String? = null
                if (args.size == 2) {
                    team = when(args[1]) {
                        "red" -> "red"
                        "blue" -> "blue"
                        "green" -> "green"
                        "yellow" -> "yellow"
                        "purple" -> "purple"
                        "gold" -> "gold"
                        "white" -> "white"
                        "black" -> "black"
                        "pink" -> "pink"
                        "gray" -> "gray"
                        "light_blue" -> "light_blue"
                        "cyan" -> "cyan"
                        else -> {
                            sender.sendMessage(Component.text("Не правильно выбрана команда", NamedTextColor.RED))
                            return false
                        }
                    }
                }
                GameController.getGame(args[0]).join(sender, team)
                return true
            }
            if (args[0] == "doubles") {
                GameController.getGame("solo").leave(sender)
                GameController.getGame("teams").leave(sender)
                var team: String? = null
                if (args.size == 2) {
                    team = when(args[1]) {
                        "red" -> "red_doubles"
                        "blue" -> "blue_doubles"
                        "green" -> "green_doubles"
                        "yellow" -> "yellow_doubles"
                        "purple" -> "purple_doubles"
                        "pink" -> "pink_doubles"
                        "white" -> "white_doubles"
                        "black" -> "black_doubles"
                        else -> {
                            sender.sendMessage(Component.text("Не правильно выбрана команда", NamedTextColor.RED))
                            return false
                        }
                    }
                }
                GameController.getGame(args[0]).join(sender, team)
                return true
            }
            if (args[0] == "teams") {
                GameController.getGame("solo").leave(sender)
                GameController.getGame("doubles").leave(sender)
                var team: String? = null
                if (args.size == 2) {
                    team = when(args[1]) {
                        "red" -> "red_teams"
                        "blue" -> "blue_teams"
                        else -> {
                            sender.sendMessage(Component.text("Не правильно выбрана команда", NamedTextColor.RED))
                            return false
                        }
                    }
                }
                GameController.getGame(args[0]).join(sender, team)
                return true
            }
            sender.sendMessage(Component.text("Не выбрана игра", NamedTextColor.RED))
            return false
        }
        sender.sendMessage(Component.text("Ошибка", NamedTextColor.DARK_RED))
        return false
    }
}