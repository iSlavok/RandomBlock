package online.isslavok.randomblock.commands

import online.isslavok.randomblock.Game
import online.isslavok.randomblock.RandomBlock
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player

object GameController {
    private var games: MutableMap<String, Game> = mutableMapOf()
    lateinit var instance: RandomBlock
    private lateinit var lobby: Location

    fun initGames(solo: Game, doubles: Game, teams: Game) {
        games["solo"] = solo
        games["doubles"] = doubles
        games["teams"] = teams
    }

    fun initInstance (instance: RandomBlock) {
        this.instance = instance
    }

    fun initLobby(location: Location) {
        lobby = location
    }

    fun getGame(game: String): Game {
        return games[game]!!
    }

    fun lobby(player: Player) {
        resetPlayer(player)
        player.teleport(lobby)
        player.gameMode = GameMode.ADVENTURE
    }

    fun resetPlayer(player: Player) {
        player.health = 20.0;
        player.foodLevel = 20;
        player.saturation = 20.0f;
        player.exp = 0.0f;
        player.totalExperience = 0;
        player.level = 0
        player.inventory.clear()
        player.enderChest.clear()
        player.closeInventory()
    }
}