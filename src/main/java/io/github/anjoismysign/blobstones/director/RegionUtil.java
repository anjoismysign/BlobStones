package io.github.anjoismysign.blobstones.director;

import dev.espi.protectionstones.PSGroupRegion;
import dev.espi.protectionstones.PSMergedRegion;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.utils.ParticlesUtil;
import dev.espi.protectionstones.utils.RegionTraverse;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;


public class RegionUtil {
    private static final int PARTICLE_VIEW_DISTANCE_LIMIT = 150;

    private static boolean handlePinkParticle(Player player, Location location) {
        if (player.getLocation().distance(location) > PARTICLE_VIEW_DISTANCE_LIMIT || Math.abs(location.getY() - player.getLocation().getY()) > 30)
            return false;
        ParticlesUtil.persistRedstoneParticle(player, location, new Particle.DustOptions(Color.fromRGB(233, 30, 99), 2), 30);
        return true;
    }

    private static boolean handleBlueParticle(Player player, Location location) {
        if (player.getLocation().distance(location) > PARTICLE_VIEW_DISTANCE_LIMIT || Math.abs(location.getY() - player.getLocation().getY()) > 30)
            return false;
        ParticlesUtil.persistRedstoneParticle(player, location, new Particle.DustOptions(Color.fromRGB(0, 255, 255), 2), 30);
        return true;
    }

    private static boolean handlePurpleParticle(Player player, Location location) {
        if (player.getLocation().distance(location) > PARTICLE_VIEW_DISTANCE_LIMIT || Math.abs(location.getY() - player.getLocation().getY()) > 30)
            return false;
        ParticlesUtil.persistRedstoneParticle(player, location, new Particle.DustOptions(Color.fromRGB(255, 0, 255), 10), 30);
        return true;
    }

    /**
     * Makes player view the region.
     * Particles are spawned asynchronously.
     * Would be a good idea to use a cooldown.
     *
     * @param player player to show the region to
     * @param region region to view
     * @param plugin plugin to be used to schedule async tasks
     */
    public static void viewRegion(Player player, PSRegion region, JavaPlugin plugin) {
        int playerY = player.getLocation().getBlockY(), minY =
                region.getWGRegion().getMinimumPoint().getBlockY(),
                maxY = region.getWGRegion().getMaximumPoint().getBlockY();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            AtomicInteger modU = new AtomicInteger(0);
            if (region instanceof PSGroupRegion) {
                PSGroupRegion groupRegion = (PSGroupRegion) region;
                for (PSMergedRegion psmr : groupRegion.getMergedRegions()) {
                    handlePurpleParticle(player, new Location(player.getWorld(), 0.5 + psmr.getProtectBlock().getX(), 1.5 + psmr.getProtectBlock().getY(), 0.5 + psmr.getProtectBlock().getZ()));
                    for (int y = minY; y <= maxY; y += 10) {
                        handlePurpleParticle(player, new Location(player.getWorld(), 0.5 + psmr.getProtectBlock().getX(), 0.5 + y, 0.5 + psmr.getProtectBlock().getZ()));
                    }
                }
            } else {
                handlePurpleParticle(player, new Location(player.getWorld(), 0.5 + region.getProtectBlock().getX(), 1.5 + region.getProtectBlock().getY(), 0.5 + region.getProtectBlock().getZ()));
                for (int y = minY; y <= maxY; y += 10) {
                    handlePurpleParticle(player, new Location(player.getWorld(), 0.5 + region.getProtectBlock().getX(), 0.5 + y, 0.5 + region.getProtectBlock().getZ()));
                }
            }
            RegionTraverse.traverseRegionEdge(new HashSet<>(region.getWGRegion().getPoints()),
                    Collections.singletonList(region.getWGRegion()), traverseReturn -> {
                        if (traverseReturn.isVertex) {
                            handleBlueParticle(player, new Location(player.getWorld(), 0.5 + traverseReturn.point.getX(), 0.5 + playerY, 0.5 + traverseReturn.point.getZ()));
                            for (int y = minY; y <= maxY; y += 5) {
                                handleBlueParticle(player, new Location(player.getWorld(), 0.5 + traverseReturn.point.getX(), 0.5 + y, 0.5 + traverseReturn.point.getZ()));
                            }
                        } else {
                            if (modU.get() % 2 == 0) {
                                handlePinkParticle(player, new Location(player.getWorld(), 0.5 + traverseReturn.point.getX(), 0.5 + playerY, 0.5 + traverseReturn.point.getZ()));
                                handlePinkParticle(player, new Location(player.getWorld(), 0.5 + traverseReturn.point.getX(), 0.5 + minY, 0.5 + traverseReturn.point.getZ()));
                                handlePinkParticle(player, new Location(player.getWorld(), 0.5 + traverseReturn.point.getX(), 0.5 + maxY, 0.5 + traverseReturn.point.getZ()));
                            }
                            modU.set((modU.get() + 1) % 2);
                        }
                    });
        });
    }
}
