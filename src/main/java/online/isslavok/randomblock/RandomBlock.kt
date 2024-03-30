package online.isslavok.randomblock

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.util.TriState
import online.isslavok.randomblock.commands.GameController
import online.isslavok.randomblock.commands.Join
import online.isslavok.randomblock.commands.JoinTabCompleter
import online.isslavok.randomblock.commands.Leave
import online.isslavok.randomblock.events.PlayerListener
import org.bukkit.*
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class RandomBlock : JavaPlugin() {

    override fun onEnable() {
        val pluginFolder = File(this.dataFolder, "schematic")
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs()
        }

        getCommand("join")?.setExecutor(Join)
        getCommand("join")?.tabCompleter = JoinTabCompleter
        getCommand("leave")?.setExecutor(Leave)

        server.pluginManager.registerEvents(PlayerListener, this)

        createWorld("random_block_solo")
        createWorld("random_block_doubles")
        createWorld("random_block_teams")
        createWorld("random_block_solo_map")
        createWorld("random_block_doubles_map")
        createWorld("random_block_teams_map")

        val soloTeams: List<Team> = mutableListOf(
            Team(
                displayName="красная",
                name="red",
                color=NamedTextColor.RED,
                spawn=Location(server.getWorld("random_block_solo"), -14.5, -37.0, -25.5),
                schematic=File(this.dataFolder, "schematic/RB_solo_red.schem"),
                size = 1
            ),
            Team(
                displayName="белая",
                name="white",
                color=NamedTextColor.WHITE,
                spawn=Location(server.getWorld("random_block_solo"), 0.5, -37.0, -29.5),
                schematic=File(this.dataFolder, "schematic/RB_solo_white.schem"),
                size = 1
            ),
            Team(
                displayName="серая",
                name="gray",
                color=NamedTextColor.GRAY,
                spawn=Location(server.getWorld("random_block_solo"), 15.5, -37.0, -25.5),
                schematic=File(this.dataFolder, "schematic/RB_solo_gray.schem"),
                size = 1
            ),
            Team(
                displayName="Желтая",
                name="yellow",
                color=NamedTextColor.YELLOW,
                spawn=Location(server.getWorld("random_block_solo"), 26.5, -37.0, -14.5),
                schematic=File(this.dataFolder, "schematic/RB_solo_yellow.schem"),
                size = 1
            ),
            Team(
                displayName="зеленая",
                name="green",
                color=NamedTextColor.GREEN,
                spawn=Location(server.getWorld("random_block_solo"), 30.5, -37.0, 0.5),
                schematic=File(this.dataFolder, "schematic/RB_solo_green.schem"),
                size = 1
            ),
            Team(
                displayName="розовая",
                name="pink",
                color=NamedTextColor.LIGHT_PURPLE,
                spawn=Location(server.getWorld("random_block_solo"), 26.5, -37.0, 15.5),
                schematic=File(this.dataFolder, "schematic/RB_solo_pink.schem"),
                size = 1
            ),
            Team(
                displayName="черная",
                name="black",
                color=NamedTextColor.BLACK,
                spawn=Location(server.getWorld("random_block_solo"), 15.5, -37.0, 26.5),
                schematic=File(this.dataFolder, "schematic/RB_solo_black.schem"),
                size = 1
            ),
            Team(
                displayName="бирюзовая",
                name="cyan",
                color=NamedTextColor.DARK_AQUA,
                spawn=Location(server.getWorld("random_block_solo"), 0.5, -37.0, 30.5),
                schematic=File(this.dataFolder, "schematic/RB_solo_cyan.schem"),
                size = 1
            ),
            Team(
                displayName="синяя",
                name="blue",
                color=NamedTextColor.BLUE,
                spawn=Location(server.getWorld("random_block_solo"), -14.5, -37.0, 26.5),
                schematic=File(this.dataFolder, "schematic/RB_solo_blue.schem"),
                size = 1
            ),
            Team(
                displayName="голубая",
                name="light_blue",
                color=NamedTextColor.AQUA,
                spawn=Location(server.getWorld("random_block_solo"), -25.5, -37.0, 15.5),
                schematic=File(this.dataFolder, "schematic/RB_solo_light_blue.schem"),
                size = 1
            ),
            Team(
                displayName="золотая",
                name="gold",
                color=NamedTextColor.GOLD,
                spawn=Location(server.getWorld("random_block_solo"), -29.5, -37.0, 0.5),
                schematic=File(this.dataFolder, "schematic/RB_solo_gold.schem"),
                size = 1
            ),
            Team(
                displayName="фиолетовая",
                name="purple",
                color=NamedTextColor.DARK_PURPLE,
                spawn=Location(server.getWorld("random_block_solo"), -25.5, -37.0, -14.5),
                schematic=File(this.dataFolder, "schematic/RB_solo_purple.schem"),
                size = 1
            ),
        )
        val soloGame = Game(server, "random_block_solo", File(this.dataFolder, "schematic/RB_solo.schem"), soloTeams)
        soloGame.regenerate()

        val doublesTeams: List<Team> = mutableListOf(
            Team(
                displayName="белая",
                name="white_doubles",
                color=NamedTextColor.WHITE,
                spawn=Location(server.getWorld("random_block_doubles"), 0.5, -37.0, -27.5),
                schematic=File(this.dataFolder, "schematic/RB_doubles_island.schem"),
                size = 2
            ),
            Team(
                displayName="желтая",
                name="yellow_doubles",
                color=NamedTextColor.YELLOW,
                spawn=Location(server.getWorld("random_block_doubles"), 20.5, -37.0, -19.5),
                schematic=File(this.dataFolder, "schematic/RB_doubles_island.schem"),
                size = 2
            ),
            Team(
                displayName="зеленая",
                name="green_doubles",
                color=NamedTextColor.GREEN,
                spawn=Location(server.getWorld("random_block_doubles"), 28.5, -37.0, 0.5),
                schematic=File(this.dataFolder, "schematic/RB_doubles_island.schem"),
                size = 2
            ),
            Team(
                displayName="розовая",
                name="pink_doubles",
                color=NamedTextColor.LIGHT_PURPLE,
                spawn=Location(server.getWorld("random_block_doubles"), 20.5, -37.0, 20.5),
                schematic=File(this.dataFolder, "schematic/RB_doubles_island.schem"),
                size = 2
            ),
            Team(
                displayName="черная",
                name="black_doubles",
                color=NamedTextColor.BLACK,
                spawn=Location(server.getWorld("random_block_doubles"), 0.5, -37.0, 28.5),
                schematic=File(this.dataFolder, "schematic/RB_doubles_island.schem"),
                size = 2
            ),
            Team(
                displayName="синяя",
                name="blue_doubles",
                color=NamedTextColor.BLUE,
                spawn=Location(server.getWorld("random_block_doubles"), -19.5, -37.0, 20.5),
                schematic=File(this.dataFolder, "schematic/RB_doubles_island.schem"),
                size = 2
            ),
            Team(
                displayName="фиолетовая",
                name="purple_doubles",
                color=NamedTextColor.DARK_PURPLE,
                spawn=Location(server.getWorld("random_block_doubles"), -27.5, -37.0, 0.5),
                schematic=File(this.dataFolder, "schematic/RB_doubles_island.schem"),
                size = 2
            ),
            Team(
                displayName="красная",
                name="red_doubles",
                color=NamedTextColor.RED,
                spawn=Location(server.getWorld("random_block_doubles"), -19.5, -37.0, -19.5),
                schematic=File(this.dataFolder, "schematic/RB_doubles_island.schem"),
                size = 2
            ),
        )
        val doublesGame = Game(server, "random_block_doubles", File(this.dataFolder, "schematic/RB_doubles.schem"), doublesTeams)
        doublesGame.regenerate()

        val teamsTeams: List<Team> = mutableListOf(
            Team(
                displayName="синяя",
                name="blue_teams",
                color=NamedTextColor.BLUE,
                spawn=Location(server.getWorld("random_block_teams"), 0.5, -37.0, 25.5),
                schematic=File(this.dataFolder, "schematic/RB_teams_blue.schem"),
                size = 8
            ),
            Team(
                displayName="красная",
                name="red_teams",
                color=NamedTextColor.RED,
                spawn=Location(server.getWorld("random_block_teams"), 0.5, -37.0, -24.5),
                schematic=File(this.dataFolder, "schematic/RB_teams_red.schem"),
                size = 8
            ),
        )
        val teamsGame = Game(server, "random_block_teams", File(this.dataFolder, "schematic/RB_teams.schem"), teamsTeams)
        teamsGame.regenerate()

        GameController.initGames(soloGame, doublesGame, teamsGame)
        GameController.initInstance(this)
        GameController.initLobby(Location(server.getWorld("world"), 0.5, 100.0, 0.5))
    }

    override fun onDisable() {
        val teams = Bukkit.getScoreboardManager().mainScoreboard.teams
        for (team in teams) {
            team.unregister()
        }
    }

    private fun createWorld(name: String) {
        server.logger.info("Creating world...")
        WorldCreator(name)
            .type(WorldType.FLAT)
            .keepSpawnLoaded(TriState.TRUE)
            .generatorSettings("{\"layers\": [], \"biome\":\"the_void\"}")
            .generateStructures(false)
            .createWorld()
        val world = server.getWorld(name)
        if (world != null) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
            world.setGameRule(GameRule.DO_MOB_LOOT, false)
        }
        server.logger.info("World `$name` created")
    }
}
