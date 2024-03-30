package online.isslavok.randomblock.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

object JoinTabCompleter : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): MutableList<String>? {
        val list: MutableList<String> = ArrayList()
        if (args != null) {
            if (args.size == 1) {
                list.add("solo")
                list.add("doubles")
                list.add("teams")
            }
            if (args.size == 2) {
                list.add("red")
                list.add("blue")
                if ((args[0] == "solo") or (args[0] == "doubles")) {
                    list.add("green")
                    list.add("yellow")
                    list.add("purple")
                    list.add("white")
                    list.add("black")
                    list.add("pink")
                }
                if (args[0] == "solo") {
                    list.add("gold")
                    list.add("gray")
                    list.add("light_blue")
                    list.add("cyan")
                }
            }
        }
        return list
    }
}