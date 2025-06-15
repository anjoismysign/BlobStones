package io.github.anjoismysign.blobstones.entities;

import io.github.anjoismysign.anjo.entities.Uber;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.api.BlobLibSoundAPI;
import io.github.anjoismysign.bloblib.entities.MinecraftTimeUnit;
import io.github.anjoismysign.bloblib.entities.message.BlobMessage;
import io.github.anjoismysign.blobstones.BlobStones;
import io.github.anjoismysign.blobstones.director.ConfigManager;
import io.github.anjoismysign.blobstones.engine.StonesEngine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;

import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;

public class MovementWarmup implements Listener {
    private static BlobStones plugin;
    private static final HashSet<UUID> warmup = new HashSet<>();
    private final String key;
    private BlobMessage warmupFailMessage;
    private final ConfigManager configManager;

    public MovementWarmup(StonesEngine engine) {
        if (plugin == null)
            plugin = engine.getPlugin();
        this.configManager = engine.getManagerDirector().getConfigManager();
        key = "Listeners.Warmup-PlayerMoveEvent.Enabled";
        reload();
    }

    public void reload() {
        HandlerList.unregisterAll(this);
        if (configManager.getBoolean(key)) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            warmupFailMessage = BlobLibMessageAPI.getInstance().getMessage(configManager.getString("Listeners.Warmup-PlayerMoveEvent.Fail-Message"));
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
        warmupFailMessage.localize(player.getLocale()).handle(player);
    }

    private static void ofTicks(long ticks,
                                Player player,
                                Consumer<UUID> consumer,
                                BlobMessage message) {
        if (message == null)
            throw new IllegalArgumentException("Message cannot be null");
        UUID uuid = player.getUniqueId();
        warmup.add(uuid);
        Uber<Long> left =
                Uber.drive(ticks - 1);//-1 for aesthetic purposes
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!warmup.contains(uuid)) {
                    cancel();
                    return;
                }
                Player onlinePlayer = Bukkit.getPlayer(uuid);
                if (onlinePlayer == null) {
                    cancel();
                    return;
                }
                long leftTicks = left.thanks();
                double seconds = MinecraftTimeUnit.SECONDS.convert(leftTicks, MinecraftTimeUnit.TICKS);
                long leftSeconds = (long) seconds;
                message.modder()
                        .replace("%time%", leftSeconds + "")
                        .get()
                        .handle(onlinePlayer);
                left.talk(leftTicks - 20);
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!warmup.contains(uuid))
                return;
            warmup.remove(uuid);
            consumer.accept(uuid);
        }, ticks);
    }

    private static void ofSeconds(int seconds,
                                  Player player,
                                  Consumer<UUID> consumer,
                                  BlobMessage message) {
        long l = (long) MinecraftTimeUnit.TICKS.convert(seconds, MinecraftTimeUnit.SECONDS);
        ofTicks(l, player, consumer, message);
    }

    public static void PLAYER_TELEPORT(int seconds,
                                       Player player,
                                       Location location) {
        ofSeconds(seconds, player, uuid -> {
            Player onlinePlayer = Bukkit.getPlayer(uuid);
            if (onlinePlayer == null)
                return;
            onlinePlayer.teleport(location);
            BlobLibSoundAPI.getInstance().getSound("BlobStones.Teleport").handle(onlinePlayer);
        }, BlobLibMessageAPI.getInstance().getMessage("System.Teleport-Warmup", player));
    }
}
