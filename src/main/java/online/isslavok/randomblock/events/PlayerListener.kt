package online.isslavok.randomblock.events

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import online.isslavok.randomblock.commands.GameController
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType


object PlayerListener : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.joinMessage(null)
        playerDeath(event.player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        event.quitMessage(null)
        playerDeath(event.player)
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        event.isCancelled = true
        val message = event.deathMessage()
        if (message != null) {
            for (player in event.entity.world.players) {
                player.sendMessage(message)
            }
        }
        Bukkit.getScheduler().runTaskLater(
            GameController.instance,
            Runnable{playerDeath(event.player)},
            1,
        )
    }

    @EventHandler
    fun onPlayerDamage(event: EntityDamageEvent) {
        if (event.entity is  Player) {
            if (event.entity.world.name == "world") {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event.block.world.name == "world") return
        val location = event.block.location
        val block = Location(Bukkit.getWorld(location.world.name + "_map"), location.x, location.y, location.z).block
        if (block.type == event.block.type) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerClicks(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item
        if (item != null) {
            val key = NamespacedKey(GameController.instance, "RandomBlock")
            val container = item.itemMeta.persistentDataContainer
            if (container.has(key, PersistentDataType.STRING)) {
                val value = container[key, PersistentDataType.STRING]
                if (value == "leave") {
                    GameController.getGame("solo").leave(player)
                    GameController.getGame("doubles").leave(player)
                    GameController.getGame("teams").leave(player)
                    GameController.lobby(player)
                }
            }
        }
    }

    private fun playerDeath(player: Player) {
        val gameSolo = GameController.getGame("solo")
        if (gameSolo.death(player)) {
            return
        }
        val gameDoubles = GameController.getGame("doubles")
        if (gameDoubles.death(player)) {
            return
        }
        val gameTeams = GameController.getGame("teams")
        if (gameTeams.death(player)) {
            return
        }
        GameController.lobby(player)
    }
}