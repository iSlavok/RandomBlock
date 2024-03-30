package online.isslavok.randomblock

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import java.io.File

class Team (
    val displayName: String,
    val name: String,
    val color: NamedTextColor,
    val spawn: Location,
    val schematic: File,
    val size: Int,
    var countPlayers: Int = 0,
)
    {
    private val board: Scoreboard = Bukkit.getScoreboardManager().mainScoreboard
    private var team: Team = board.registerNewTeam(name)

    init {
        team.displayName(Component.text(displayName, color))
        team.color(color)
        team.setAllowFriendlyFire(false)
    }

    fun addPlayer(player: Player) {
        countPlayers++
        player.teleport(spawn)
        player.gameMode = GameMode.ADVENTURE
        team.addPlayer(player)
        player.displayName(Component.text(player.name, color))
    }

    fun removePlayer(player: Player) {
        countPlayers--
        team.removePlayer(player)
        player.displayName(Component.text(player.name))
    }

    fun checkPlayer(player: Player): Boolean {
        return team.hasPlayer(player)
    }

    fun clearTeam() {
        for (player in team.players)
            if (player is Player) {
                removePlayer(player)
            }
    }
}