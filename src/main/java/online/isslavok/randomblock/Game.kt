package online.isslavok.randomblock

import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.util.SideEffectSet
import com.sk89q.worldedit.world.block.BlockTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.title.Title
import online.isslavok.randomblock.commands.GameController
import org.bukkit.*
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.CopyOnWriteArrayList


class Game (
    private val server: Server,
    private val name: String,
    private val schematic: File,
    private val teams: List<Team>,
    private var gameState: String = "waiting",
)
    {
    private val world = server.getWorld(name)
    private var players = CopyOnWriteArrayList<Player>()
    private var borderTask: BukkitTask? = null
    private var endGameTask: BukkitTask? = null
    private var startGameTask: BukkitTask? = null
    private var bossBar = Bukkit.createBossBar("Ожидание игроков", BarColor.RED, BarStyle.SOLID)

    fun regenerate() {
        server.logger.info("Regenerating map ($name)...")
        gameState = "waiting"
        players.clear()
        bossBar.setTitle("Ожидание игроков")

        if (world != null) {
            world.worldBorder.setCenter(0.5, 0.5)
            world.worldBorder.size = 80.0

            val entities = world.entities
            for (entity in entities) {
                if (entity !is Player) {
                    println(entity)
                    entity.remove()
                }
            }
            createEditSession(world).use { session ->
                val location1 = Location(world, 40.0, -20.0, 40.0)
                val location2 = Location(world, -40.0, -64.0, -40.0)
                val region =
                    CuboidRegion(BukkitAdapter.asBlockVector(location1), BukkitAdapter.asBlockVector(location2))
                session.setBlocks(region, BlockTypes.AIR?.defaultState)
            }
            createEditSession(world).use { session ->
                val location1 = Location(world, 40.0, -20.0, 40.0)
                val location2 = Location(world, -40.0, -20.0, -40.0)
                val region = CuboidRegion(BukkitAdapter.asBlockVector(location1), BukkitAdapter.asBlockVector(location2))
                session.setBlocks(region, BlockTypes.BARRIER?.defaultState)
            }
            createEditSession(world).use { session ->
                val format = ClipboardFormats.findByFile(schematic)
                val reader = format!!.getReader(FileInputStream(schematic))
                val schematic = reader.read()
                val operation = ClipboardHolder(schematic)
                    .createPaste(session)
                    .to(BukkitAdapter.asBlockVector(Location(world, 0.0, -39.0, 0.0)))
                    .build()
                Operations.complete(operation)
            }
        } else server.logger.warning("world null error")

        for (team in teams) {
            team.clearTeam()
            createEditSession(team.spawn.world).use { session ->
                val format = ClipboardFormats.findByFile(team.schematic)
                val reader = format!!.getReader(FileInputStream(team.schematic))
                val schematic = reader.read()
                val operation = ClipboardHolder(schematic)
                    .createPaste(session)
                    .to(BukkitAdapter.asBlockVector(team.spawn))
                    .build()
                Operations.complete(operation)
            }
        }

        server.logger.info("Map '$name' regenerated")
    }

    private fun createEditSession(bukkitWorld: World): EditSession {
        val session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(bukkitWorld))
        session.sideEffectApplier = SideEffectSet.defaults()
        return session
    }

    private fun startingGame() {
        gameState = "starting"
        println("[$name] Starting...")
        startTimeout(30)
        bossBar.style = BarStyle.SEGMENTED_10
    }

    private fun startGame() {
        gameState = "playing"
        println("[$name] Start")
        if (world != null) {
            for (team in teams) {
                createEditSession(world).use { session ->
                    val location1 = Location(world, team.spawn.x+1, team.spawn.y-1, team.spawn.z+1)
                    val location2 = Location(world, team.spawn.x-1, team.spawn.y-1, team.spawn.z-1)
                    val region =
                        CuboidRegion(BukkitAdapter.asBlockVector(location1), BukkitAdapter.asBlockVector(location2))
                    session.setBlocks(region, BlockTypes.AIR?.defaultState)
                }
            }
            players.forEach { p: Player ->
                GameController.resetPlayer(p)
                p.gameMode = GameMode.SURVIVAL
            }
            runLaterSync({
                for (team in teams) {
                    createEditSession(world).use { session ->
                        val location1 = Location(world, team.spawn.x+2, team.spawn.y-3, team.spawn.z+2)
                        val location2 = Location(world, team.spawn.x-2, team.spawn.y+2, team.spawn.z-2)
                        val region =
                            CuboidRegion(BukkitAdapter.asBlockVector(location1), BukkitAdapter.asBlockVector(location2))
                        session.setBlocks(region, BlockTypes.AIR?.defaultState)
                    }
                }
            }, 5)
            borderTimeout(300)
            bossBar.style = BarStyle.SEGMENTED_20
            runLaterSync({
                giveItems()
            }, 100)
        }
        endCheck()
    }

    private fun cancelStartCheck() {
        if (gameState != "starting") return
        if (teams.size >= 10) {
            if (getCountTeams() < 2) {
                startGameTask?.cancel()
                bossBar.style = BarStyle.SOLID
                bossBar.progress = 1.0
                bossBar.setTitle("Ожидание игроков")
                gameState = "waiting"
            }
        } else if (teams.size >= 6) {
            if (getCountTeams() < 2) {
                startGameTask?.cancel()
                bossBar.style = BarStyle.SOLID
                bossBar.progress = 1.0
                bossBar.setTitle("Ожидание игроков")
                gameState = "waiting"
            }
        } else if (teams.size >= 2) {
            if (getCountTeams() < 2) {
                startGameTask?.cancel()
                bossBar.style = BarStyle.SOLID
                bossBar.progress = 1.0
                bossBar.setTitle("Ожидание игроков")
                gameState = "waiting"
            }
        }
    }

    private fun startCheck() {
        if (gameState != "waiting") return
        if (teams.size >= 10) {
            if (getCountTeams() >= 2) {
                startingGame()
            }
        } else if (teams.size >= 6) {
            if (getCountTeams() >= 2) {
                startingGame()
            }
        } else if (teams.size >= 2) {
            if (getCountTeams() >= 2) {
                startingGame()
            }
        }
    }

    private fun borderTimeout(secondsRemain: Int) {
        if (gameState != "playing") return
        if (secondsRemain > 0) {
            borderTask = runLaterSync({
                if (secondsRemain > 60) {
                    bossBar.setTitle("Барьер начнет двигаться через ${secondsRemain / 60} минут")
                } else {
                    bossBar.setTitle("Барьер начнет двигаться через $secondsRemain секунд")
                }
                bossBar.progress = secondsRemain / 300.0
                borderTimeout(secondsRemain - 1)
            }, 20)
            return
        }
        runSync {
            world?.worldBorder?.setSize(2.0, 120L)
            endTimeout(300)
        }
    }

    private fun startTimeout(secondsRemain: Int) {
        if (gameState != "starting") return
        if (secondsRemain > 0) {
            startGameTask = runLaterAsync({
                if (secondsRemain <= 5) {
                    for (player in world!!.players) {
                        player.showTitle(
                            Title.title(
                                Component.text(secondsRemain),
                                Component.empty()
                            )
                        )
                    }
                }
                bossBar.setTitle("Игра начнется через $secondsRemain секунд")
                bossBar.progress = secondsRemain / 30.0
                startTimeout(secondsRemain - 1)
            }, 20)
            return
        }
        for (player in world!!.players) {
            player.showTitle(
                Title.title(
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("Игра началась!")
                )
            )
        }
        runSync {
            startGame()
        }
    }

    private fun endTimeout(secondsRemain: Int) {
            if (gameState != "playing") return
            if (secondsRemain > 0) {
                endGameTask = runLaterAsync({
                    if (secondsRemain > 60) {
                        bossBar.setTitle("До конца игры ${secondsRemain / 60} минут")
                    } else {
                        bossBar.setTitle("До конца игры $secondsRemain секунд")
                    }
                    bossBar.progress = secondsRemain / 300.0
                    endTimeout(secondsRemain - 1)
                }, 20)
                return
            }
            runSync {
                endGame()
            }
        }

    private fun endGame() {
        if (gameState != "playing") return

        if (borderTask != null) {
            borderTask?.cancel()
        } else println("border task = null")
        endGameTask?.cancel()

        bossBar.setTitle("Игра окончена")
        bossBar.progress = 1.0
        bossBar.style = BarStyle.SOLID

        if (world != null) {
            var message: TextComponent = Component.text().build()
            val remainingTeams = getTeams()
            if (remainingTeams.size > 1) {
                if (teams[0].size > 1) {
                    message = Component.text().content("Победили команды: ").build()
                    for (team in remainingTeams) {
                        message = Component.text().append(message).append(Component.text(team.displayName, team.color)).build()
                        if (team != remainingTeams.last()) {
                            message = Component.text().append(message).append(Component.text(", ")).build()
                        }
                    }
                } else {
                    message = Component.text("Победили игроки ${players.joinToString(", ") { it.name }}")
                }
            } else if (remainingTeams.size == 1) {
                message = if (teams[0].size > 1) {
                    Component.text().content("Победила ").append(Component.text(remainingTeams[0].displayName, remainingTeams[0].color)).append(Component.text(" команда")).build()
                } else {
                    Component.text("Победил игрок ${players[0].name}")
                }
            } else if (remainingTeams.size < 1) {
                message = Component.text("Нет победителей", NamedTextColor.RED)
            }
            for (player in world.players) {
                if (player.location.y < -64) {
                    player.teleport(Location(world, 0.0, -30.0, 0.0))
                }
                player.showTitle(
                    Title.title(
                        Component.empty(),
                        message,
                    )
                )
            }
        }
        for (player in players) {
            player.gameMode = GameMode.SPECTATOR
        }
        runLaterSync({
            if (world != null) {
                for (player in world.players) {
                    bossBar.removePlayer(player)
                    GameController.lobby(player)
                }
                regenerate()
            }
        }, 200)
    }

    private fun endCheck() {
        if (gameState != "playing") return
        if (getCountTeams() <= 1) {
            endGame()
        }
    }

    private fun getCountTeams(): Int {
        var count = 0
        for (team in teams) {
            if (team.countPlayers > 0) {
                count++
            }
        }
        return count
    }

    private fun getTeams(): CopyOnWriteArrayList<Team> {
            val t = CopyOnWriteArrayList<Team>()
            for (team in teams) {
                if (team.countPlayers > 0) {
                    t.add(team)
                }
            }
            return t
        }

    private fun spawnPlayer(player: Player, team: Team) {
        GameController.resetPlayer(player)
        player.gameMode = GameMode.ADVENTURE
        val key = NamespacedKey(GameController.instance, "RandomBlock")
        val leaveItem = ItemStack(Material.BARRIER)
        val meta = leaveItem.itemMeta
        meta.displayName(Component.text("Выйти из игры", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
        meta.persistentDataContainer.set(key, PersistentDataType.STRING, "leave")
        leaveItem.setItemMeta(meta)
        player.inventory.setItem(8,  leaveItem)
        team.addPlayer(player)
        players.add(player)
        bossBar.addPlayer(player)
        for (p in world!!.players) {
            p.sendMessage(Component.text("Игрок ${player.name} зашел в игру", NamedTextColor.YELLOW))
        }
        startCheck()
    }

    fun join(player: Player, teamName: String? = null) {
        leave(player)
        if ((gameState == "waiting") or (gameState == "starting")) {
            if (teamName != null) {
                for (team in teams) {
                    if (team.name == teamName) {
                        if (team.size > team.countPlayers) {
                            spawnPlayer(player, team)
                            return
                        }
                    }
                }
                join(player)
                return
            }  else {
                for (team in teams) {
                    if (team.size > team.countPlayers) {
                        spawnPlayer(player, team)
                        return
                    }
                }
            }
        }
        joinSpectate(player)
    }

    fun leave(player: Player) {
        bossBar.removePlayer(player)
        val team = getPlayerTeam(player)
        if (team != null) {
            team.removePlayer(player)
            players.remove(player)
            cancelStartCheck()
            endCheck()
            for (p in world!!.players) {
                p.sendMessage(Component.text("Игрок ${player.name} вышел из игры", NamedTextColor.YELLOW))
            }
        }
    }

    fun death(player: Player): Boolean {
        if (players.any {it.name == player.name}) {
            val team = getPlayerTeam(player)
            if (team != null) {
                player.gameMode = GameMode.SPECTATOR
                players.remove(player)
                team.removePlayer(player)
                if (player.location.y < -64) {
                    player.teleport(Location(world, 0.0, -30.0, 0.0))
                }
                endCheck()
            }
            return true
        }
        return false
    }

    private fun getPlayerTeam(player: Player): Team? {
        for (team in teams) {
            if (team.checkPlayer(player)) {
                return team
            }
        }
        return null
    }

    private fun joinSpectate(player: Player) {
        player.gameMode = GameMode.SPECTATOR
        player.teleport(Location(server.getWorld(name), 0.0, -30.0, 0.0))
        bossBar.addPlayer(player)
    }

    private fun runLaterAsync(runnable: Runnable?, delay: Long): BukkitTask {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(
            GameController.instance,
            runnable!!,
            delay
        )
    }

    private fun runSync(runnable: Runnable?): BukkitTask {
        return Bukkit.getScheduler().runTask(GameController.instance, runnable!!)
    }

    private fun runLaterSync(runnable: Runnable? ,delay: Long): BukkitTask {
        return Bukkit.getScheduler().runTaskLater(GameController.instance, runnable!!, delay)
    }

    private fun giveItems() {
        if (gameState != "playing") return
        for (player in players) {
            player.inventory.addItem(ItemStack(getRandomItem()))
        }
        runLaterSync({
            giveItems()
         }, 100)
    }

    private fun getRandomItem(): Material {
        val restrictedMaterials = listOf(
            Material.TUFF_SLAB,
            Material.TUFF_STAIRS,
            Material.TUFF_WALL,
            Material.CHISELED_TUFF,
            Material.POLISHED_TUFF,
            Material.POLISHED_TUFF_SLAB,
            Material.POLISHED_TUFF_STAIRS,
            Material.POLISHED_TUFF_WALL,
            Material.TUFF_BRICKS,
            Material.TUFF_BRICK_SLAB,
            Material.TUFF_BRICK_STAIRS,
            Material.TUFF_BRICK_WALL,
            Material.CHISELED_TUFF_BRICKS,
            Material.CHISELED_COPPER,
            Material.EXPOSED_CHISELED_COPPER,
            Material.WEATHERED_CHISELED_COPPER,
            Material.OXIDIZED_CHISELED_COPPER,
            Material.WAXED_CHISELED_COPPER,
            Material.WAXED_EXPOSED_CHISELED_COPPER,
            Material.WAXED_WEATHERED_CHISELED_COPPER,
            Material.WAXED_OXIDIZED_CHISELED_COPPER,
            Material.COPPER_DOOR,
            Material.EXPOSED_COPPER_DOOR,
            Material.WEATHERED_COPPER_DOOR,
            Material.OXIDIZED_COPPER_DOOR,
            Material.WAXED_COPPER_DOOR,
            Material.WAXED_EXPOSED_COPPER_DOOR,
            Material.WAXED_WEATHERED_COPPER_DOOR,
            Material.WAXED_OXIDIZED_COPPER_DOOR,
            Material.COPPER_TRAPDOOR,
            Material.EXPOSED_COPPER_TRAPDOOR,
            Material.WEATHERED_COPPER_TRAPDOOR,
            Material.OXIDIZED_COPPER_TRAPDOOR,
            Material.WAXED_COPPER_TRAPDOOR,
            Material.WAXED_EXPOSED_COPPER_TRAPDOOR,
            Material.WAXED_WEATHERED_COPPER_TRAPDOOR,
            Material.WAXED_OXIDIZED_COPPER_TRAPDOOR,
            Material.CRAFTER,
            Material.BREEZE_SPAWN_EGG,
            Material.COPPER_GRATE,
            Material.EXPOSED_COPPER_GRATE,
            Material.WEATHERED_COPPER_GRATE,
            Material.OXIDIZED_COPPER_GRATE,
            Material.WAXED_COPPER_GRATE,
            Material.WAXED_EXPOSED_COPPER_GRATE,
            Material.WAXED_WEATHERED_COPPER_GRATE,
            Material.WAXED_OXIDIZED_COPPER_GRATE,
            Material.COPPER_BULB,
            Material.EXPOSED_COPPER_BULB,
            Material.WEATHERED_COPPER_BULB,
            Material.OXIDIZED_COPPER_BULB,
            Material.WAXED_COPPER_BULB,
            Material.WAXED_EXPOSED_COPPER_BULB,
            Material.WAXED_WEATHERED_COPPER_BULB,
            Material.WAXED_OXIDIZED_COPPER_BULB,
            Material.TRIAL_SPAWNER,
            Material.TRIAL_KEY,
            Material.AIR,
            Material.BEDROCK,
            Material.COMMAND_BLOCK,
            Material.COMMAND_BLOCK_MINECART,
            Material.REPEATING_COMMAND_BLOCK,
            Material.CHAIN_COMMAND_BLOCK,
            Material.END_PORTAL_FRAME,
            Material.DEBUG_STICK,
            Material.BARRIER,
            Material.STRUCTURE_VOID,
            Material.STRUCTURE_BLOCK,
            Material.JIGSAW,
            Material.WITHER_SPAWN_EGG,
            Material.ENDER_DRAGON_SPAWN_EGG,
            Material.TRIDENT,
            Material.WATER,
            Material.LAVA,
            Material.TALL_SEAGRASS,
            Material.PISTON_HEAD,
            Material.MOVING_PISTON,
            Material.WALL_TORCH,
            Material.FIRE,
            Material.SOUL_FIRE,
            Material.REDSTONE_WIRE,
            Material.OAK_WALL_SIGN,
            Material.SPRUCE_WALL_SIGN,
            Material.BIRCH_WALL_SIGN,
            Material.ACACIA_WALL_SIGN,
            Material.CHERRY_WALL_SIGN,
            Material.JUNGLE_WALL_SIGN,
            Material.DARK_OAK_WALL_SIGN,
            Material.MANGROVE_WALL_SIGN,
            Material.BAMBOO_WALL_SIGN,
            Material.OAK_WALL_HANGING_SIGN,
            Material.SPRUCE_WALL_HANGING_SIGN,
            Material.BIRCH_WALL_HANGING_SIGN,
            Material.ACACIA_WALL_HANGING_SIGN,
            Material.CHERRY_WALL_HANGING_SIGN,
            Material.JUNGLE_WALL_HANGING_SIGN,
            Material.DARK_OAK_WALL_HANGING_SIGN,
            Material.MANGROVE_WALL_HANGING_SIGN,
            Material.CRIMSON_WALL_HANGING_SIGN,
            Material.WARPED_WALL_HANGING_SIGN,
            Material.BAMBOO_WALL_HANGING_SIGN,
            Material.REDSTONE_WALL_TORCH,
            Material.SOUL_WALL_TORCH,
            Material.NETHER_PORTAL,
            Material.ATTACHED_PUMPKIN_STEM,
            Material.ATTACHED_MELON_STEM,
            Material.PUMPKIN_STEM,
            Material.MELON_STEM,
            Material.WATER_CAULDRON,
            Material.LAVA_CAULDRON,
            Material.POWDER_SNOW_CAULDRON,
            Material.END_PORTAL,
            Material.COCOA,
            Material.TRIPWIRE,
            Material.POTTED_TORCHFLOWER,
            Material.POTTED_OAK_SAPLING,
            Material.POTTED_SPRUCE_SAPLING,
            Material.POTTED_BIRCH_SAPLING,
            Material.POTTED_JUNGLE_SAPLING,
            Material.POTTED_ACACIA_SAPLING,
            Material.POTTED_CHERRY_SAPLING,
            Material.POTTED_DARK_OAK_SAPLING,
            Material.POTTED_MANGROVE_PROPAGULE,
            Material.POTTED_FERN,
            Material.POTTED_DANDELION,
            Material.POTTED_POPPY,
            Material.POTTED_BLUE_ORCHID,
            Material.POTTED_ALLIUM,
            Material.POTTED_AZURE_BLUET,
            Material.POTTED_RED_TULIP,
            Material.POTTED_ORANGE_TULIP,
            Material.POTTED_WHITE_TULIP,
            Material.POTTED_PINK_TULIP,
            Material.POTTED_OXEYE_DAISY,
            Material.POTTED_CORNFLOWER,
            Material.POTTED_LILY_OF_THE_VALLEY,
            Material.POTTED_WITHER_ROSE,
            Material.POTTED_RED_MUSHROOM,
            Material.POTTED_BROWN_MUSHROOM,
            Material.POTTED_DEAD_BUSH,
            Material.POTTED_CACTUS,
            Material.CARROTS,
            Material.POTATOES,
            Material.SKELETON_WALL_SKULL,
            Material.WITHER_SKELETON_WALL_SKULL,
            Material.ZOMBIE_WALL_HEAD,
            Material.PLAYER_WALL_HEAD,
            Material.CREEPER_WALL_HEAD,
            Material.DRAGON_WALL_HEAD,
            Material.PIGLIN_WALL_HEAD,
            Material.WHITE_WALL_BANNER,
            Material.ORANGE_WALL_BANNER,
            Material.MAGENTA_WALL_BANNER,
            Material.LIGHT_BLUE_WALL_BANNER,
            Material.YELLOW_WALL_BANNER,
            Material.LIME_WALL_BANNER,
            Material.PINK_WALL_BANNER,
            Material.GRAY_WALL_BANNER,
            Material.LIGHT_GRAY_WALL_BANNER,
            Material.CYAN_WALL_BANNER,
            Material.PURPLE_WALL_BANNER,
            Material.BLUE_WALL_BANNER,
            Material.BROWN_WALL_BANNER,
            Material.GREEN_WALL_BANNER,
            Material.RED_WALL_BANNER,
            Material.BLACK_WALL_BANNER,
            Material.TORCHFLOWER_CROP,
            Material.PITCHER_CROP,
            Material.BEETROOTS,
            Material.END_GATEWAY,
            Material.FROSTED_ICE,
            Material.KELP_PLANT,
            Material.DEAD_TUBE_CORAL_WALL_FAN,
            Material.DEAD_BRAIN_CORAL_WALL_FAN,
            Material.DEAD_BUBBLE_CORAL_WALL_FAN,
            Material.DEAD_FIRE_CORAL_WALL_FAN,
            Material.DEAD_HORN_CORAL_WALL_FAN,
            Material.TUBE_CORAL_WALL_FAN,
            Material.BRAIN_CORAL_WALL_FAN,
            Material.BUBBLE_CORAL_WALL_FAN,
            Material.FIRE_CORAL_WALL_FAN,
            Material.HORN_CORAL_WALL_FAN,
            Material.BAMBOO_SAPLING,
            Material.POTTED_BAMBOO,
            Material.VOID_AIR,
            Material.CAVE_AIR,
            Material.BUBBLE_COLUMN,
            Material.SWEET_BERRY_BUSH,
            Material.WEEPING_VINES_PLANT,
            Material.TWISTING_VINES_PLANT,
            Material.CRIMSON_WALL_SIGN,
            Material.WARPED_WALL_SIGN,
            Material.POTTED_CRIMSON_FUNGUS,
            Material.POTTED_WARPED_FUNGUS,
            Material.POTTED_CRIMSON_ROOTS,
            Material.POTTED_WARPED_ROOTS,
            Material.CANDLE_CAKE,
            Material.WHITE_CANDLE_CAKE,
            Material.ORANGE_CANDLE_CAKE,
            Material.MAGENTA_CANDLE_CAKE,
            Material.LIGHT_BLUE_CANDLE_CAKE,
            Material.YELLOW_CANDLE_CAKE,
            Material.LIME_CANDLE_CAKE,
            Material.PINK_CANDLE_CAKE,
            Material.GRAY_CANDLE_CAKE,
            Material.LIGHT_GRAY_CANDLE_CAKE,
            Material.CYAN_CANDLE_CAKE,
            Material.PURPLE_CANDLE_CAKE,
            Material.BLUE_CANDLE_CAKE,
            Material.BROWN_CANDLE_CAKE,
            Material.GREEN_CANDLE_CAKE,
            Material.RED_CANDLE_CAKE,
            Material.BLACK_CANDLE_CAKE,
            Material.POWDER_SNOW,
            Material.CAVE_VINES,
            Material.CAVE_VINES_PLANT,
            Material.BIG_DRIPLEAF_STEM,
            Material.POTTED_AZALEA_BUSH,
            Material.POTTED_FLOWERING_AZALEA_BUSH,
        )
        var item = Material.entries.random()
        while (item in restrictedMaterials) {
            item = Material.entries.random()
        }
        return item
    }
}