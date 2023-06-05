package us.mytheria.blobstones.entities;

import me.anjoismysign.anjo.entities.Uber;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.BlockVector;
import us.mytheria.bloblib.BlobLibAssetAPI;
import us.mytheria.bloblib.entities.message.BlobMessage;
import us.mytheria.blobstones.BlobStones;
import us.mytheria.blobstones.director.ConfigManager;
import us.mytheria.blobstones.director.ListenerManager;

import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;

public class MovementWarmup implements Listener {
    private static BlobStones plugin;
    private final String key;
    private BlobMessage warmupFailMessage;
    private final ConfigManager configManager;

    public MovementWarmup(ListenerManager listenerManager) {
        this.configManager = listenerManager.getManagerDirector().getConfigManager();
        plugin = listenerManager.getPlugin();
        key = "Listeners.Warmup-PlayerMoveEvent.Enabled";
        reload();
    }

    public void reload() {
        HandlerList.unregisterAll(this);
        if (configManager.getBoolean(key)) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            warmupFailMessage = BlobLibAssetAPI
                    .getMessage("Listeners.Warmup-PlayerMoveEvent.Fail-Message");
            if (warmupFailMessage == null)
                throw new IllegalStateException("Warmup Fail-Message is null. " +
                        "Check config.yml");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        warmup.remove(uuid);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!warmup.contains(uuid))
            return;
        BlockVector from = event.getFrom().toVector().toBlockVector();
        BlockVector to = event.getTo().toVector().toBlockVector();
        if (from.equals(to))
            return;
        warmup.remove(uuid);
        warmupFailMessage.handle(player);
    }

    private static HashSet<UUID> warmup = new HashSet<>();

    private static void ofTicks(long ticks,
                                Player player,
                                Consumer<UUID> consumer,
                                BlobMessage message) {
        if (message == null)
            throw new IllegalArgumentException("Message cannot be null");
        UUID uuid = player.getUniqueId();
        warmup.add(uuid);
        Uber<Long> left = Uber.drive(ticks - 1);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (!warmup.contains(uuid))
                return;
            Player onlinePlayer = Bukkit.getPlayer(uuid);
            if (onlinePlayer == null)
                return;
            long leftTicks = left.thanks();
            long x = leftTicks % 20;
            long seconds = (leftTicks / 20) - x;
            message.modder()
                    .replace("%time%", seconds + "")
                    .get()
                    .handle(onlinePlayer);
            left.talk(leftTicks - 20);
        }, 0L, 20);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!warmup.contains(uuid))
                return;
            consumer.accept(uuid);
        }, ticks);
    }

    private static void ofSeconds(int seconds,
                                  Player player,
                                  Consumer<UUID> consumer,
                                  BlobMessage message) {
        ofTicks(seconds * 20L, player, consumer, message);
    }

    public static void PLAYER_TELEPORT(int seconds,
                                       Player player,
                                       Location location,
                                       BlobMessage message) {
        ofSeconds(seconds, player, uuid -> {
            Player onlinePlayer = Bukkit.getPlayer(uuid);
            if (onlinePlayer == null)
                return;
            onlinePlayer.teleport(location);
            BlobLibAssetAPI.getSound("BlobStones.Teleport").handle(onlinePlayer);
        }, message);
    }
}
