package io.github.anjoismysign.blobstones.engine;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSProtectBlock;
import dev.espi.protectionstones.PSRegion;
import io.github.anjoismysign.bloblib.api.BlobLibListenerAPI;
import io.github.anjoismysign.bloblib.api.BlobLibSoundAPI;
import io.github.anjoismysign.bloblib.entities.inventory.BlobInventory;
import io.github.anjoismysign.bloblib.entities.inventory.BlobInventoryTracker;
import io.github.anjoismysign.bloblib.entities.inventory.InventoryButton;
import io.github.anjoismysign.bloblib.entities.inventory.InventoryDataRegistry;
import io.github.anjoismysign.bloblib.itemstack.ItemStackBuilder;
import io.github.anjoismysign.blobstones.director.ConfigManager;
import io.github.anjoismysign.blobstones.director.RegionUtil;
import io.github.anjoismysign.blobstones.director.StonesManagerDirector;
import io.github.anjoismysign.blobstones.entities.InventoryType;
import io.github.anjoismysign.blobstones.entities.MovementWarmup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProtectionStonesEngine extends StonesEngine {
    private final Map<String, PSRegion> regionMap;

    public ProtectionStonesEngine(StonesManagerDirector managerDirector) {
        super(managerDirector);
        regionMap = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
        reload();
    }

    @Override
    public void reload() {
        carriers = new HashMap<>();
        registerWorldNavigator();
        registerManageProtection();
        registerManageMembers();
        registerManageFlags();
        registerManageOwners();
    }

    private void registerWorldNavigator() {
        carriers.put(InventoryType.WORLD_NAVIGATOR, "WorldNavigator");
    }

    private void registerManageProtection() {
        carriers.put(InventoryType.MANAGE_PROTECTION, "ManageProtection");
        InventoryDataRegistry<InventoryButton> registry =
                getInventoryAPI().getInventoryDataRegistry("ManageProtection");
        ConfigManager configManager = getConfigManager();
        registry.onClick("Show", event -> {
            Player player = (Player) event.getWhoClicked();
            BlobLibSoundAPI.getInstance().getSound("Builder.Button-Click").handle(player);
            PSRegion region = getRegion(player);
            player.closeInventory();
            region.toggleHide();
        });
        registry.onClick("Members", event -> {
            Player player = (Player) event.getWhoClicked();
            openManageMembers(player);
            BlobLibSoundAPI.getInstance().getSound("Builder.Button-Click").handle(player);
        });
        registry.onClick("Owners", event -> {
            Player player = (Player) event.getWhoClicked();
            openManageOwners(player);
            BlobLibSoundAPI.getInstance().getSound("Builder.Button-Click").handle(player);
        });
        registry.onClick("Flags", event -> {
            Player player = (Player) event.getWhoClicked();
            openManageFlags(player);
            BlobLibSoundAPI.getInstance().getSound("Builder.Button-Click").handle(player);
        });
        registry.onClick("Teleport", event -> {
            boolean enabledTeleport = configManager.getBoolean("Teleport.Enabled");
            if (!enabledTeleport)
                return;
            Player player = (Player) event.getWhoClicked();
            PSRegion region = getRegion(player);
            boolean enabledWarmup = configManager.getBoolean("Teleport.Warmup.Enabled");
            if (!enabledWarmup) {
                player.teleport(region.getHome());
                return;
            }
            int time = configManager.getInteger("Teleport.Warmup.Time");
            player.closeInventory();
            MovementWarmup.PLAYER_TELEPORT(time, player, region.getHome());
        });
        registry.onClick("Rename", event -> {
            Player player = (Player) event.getWhoClicked();
            BlobLibSoundAPI.getInstance().getSound("Builder.Button-Click").handle(player);
            PSRegion region = getRegion(player);
            player.closeInventory();
            BlobLibListenerAPI.getInstance().addChatListener(player, 300, input -> {
                        region.setName(input);
                        openManageProtection(player);
                    }, "BlobStone.Rename-Timeout",
                    "BlobStone.Rename");
        });
        registry.onClick("Show", event -> {
            Player player = (Player) event.getWhoClicked();
            BlobLibSoundAPI.getInstance().getSound("Builder.Button-Click").handle(player);
            PSRegion region = getRegion(player);
            player.closeInventory();
            region.toggleHide();
        });
        registry.onClick("View", event -> {
            Player player = (Player) event.getWhoClicked();
            BlobLibSoundAPI.getInstance().getSound("Builder.Button-Click").handle(player);
            player.closeInventory();
            if (viewCooldown.contains(player.getUniqueId()))
                return;
            PSRegion region = getRegion(player);
            viewCooldown.add(player.getUniqueId());
            Bukkit.getScheduler().runTaskLaterAsynchronously(getPlugin(),
                    () -> viewCooldown.remove(player.getUniqueId()), 60);
            RegionUtil.viewRegion(player, region, getPlugin());
        });
    }

    private void registerManageMembers() {
        carriers.put(InventoryType.MANAGE_MEMBERS, "ManageMembers");
    }

    private void registerManageFlags() {
        carriers.put(InventoryType.MANAGE_FLAGS, "ManageFlags");
        InventoryDataRegistry<InventoryButton> registry =
                getInventoryAPI().getInventoryDataRegistry("ManageFlags");
        registry.onClick("PvP", event -> {
            Player player = (Player) event.getWhoClicked();
            StateFlag flag = Flags.PVP;
            BlobLibSoundAPI.getInstance().getSound("Builder.Button-Click").handle(player);
            PSRegion region = getRegion(player);
            ProtectedRegion protectedRegion = region.getWGRegion();
            protectedRegion.setFlag(flag, protectedRegion.getFlag(flag) ==
                    StateFlag.State.ALLOW ? StateFlag.State.DENY : StateFlag.State.ALLOW);
            updateStateFlag(protectedRegion,
                    getCurrentInventory(player),
                    "PvP", flag);
        });
        registry.onClick("Mob-Spawning", event -> {
            Player player = (Player) event.getWhoClicked();
            StateFlag flag = Flags.MOB_SPAWNING;
            BlobLibSoundAPI.getInstance().getSound("Builder.Button-Click").handle(player);
            PSRegion region = getRegion(player);
            ProtectedRegion protectedRegion = region.getWGRegion();
            protectedRegion.setFlag(flag, protectedRegion.getFlag(flag) ==
                    StateFlag.State.ALLOW ? StateFlag.State.DENY : StateFlag.State.ALLOW);
            updateStateFlag(protectedRegion,
                    getCurrentInventory(player),
                    "Mob-Spawning", flag);
        });
        registry.onClick("Creeper-Explosion", event -> {
            Player player = (Player) event.getWhoClicked();
            StateFlag flag = Flags.CREEPER_EXPLOSION;
            BlobLibSoundAPI.getInstance().getSound("Builder.Button-Click").handle(player);
            PSRegion region = getRegion(player);
            ProtectedRegion protectedRegion = region.getWGRegion();
            protectedRegion.setFlag(flag, protectedRegion.getFlag(flag) ==
                    StateFlag.State.ALLOW ? StateFlag.State.DENY : StateFlag.State.ALLOW);
            updateStateFlag(protectedRegion,
                    getCurrentInventory(player),
                    "Creeper-Explosion", flag);
        });
        registry.onClick("Wither-Damage", event -> {
            Player player = (Player) event.getWhoClicked();
            StateFlag flag = Flags.WITHER_DAMAGE;
            BlobLibSoundAPI.getInstance().getSound("Builder.Button-Click").handle(player);
            PSRegion region = getRegion(player);
            ProtectedRegion protectedRegion = region.getWGRegion();
            protectedRegion.setFlag(flag, protectedRegion.getFlag(flag) ==
                    StateFlag.State.ALLOW ? StateFlag.State.DENY : StateFlag.State.ALLOW);
            updateStateFlag(protectedRegion,
                    getCurrentInventory(player),
                    "Wither-Damage", flag);
        });
        registry.onClick("Ghast-Fireball", event -> {
            Player player = (Player) event.getWhoClicked();
            StateFlag flag = Flags.GHAST_FIREBALL;
            BlobLibSoundAPI.getInstance().getSound("Builder.Button-Click").handle(player);
            PSRegion region = getRegion(player);
            ProtectedRegion protectedRegion = region.getWGRegion();
            protectedRegion.setFlag(flag, protectedRegion.getFlag(flag) ==
                    StateFlag.State.ALLOW ? StateFlag.State.DENY : StateFlag.State.ALLOW);
            updateStateFlag(protectedRegion,
                    getCurrentInventory(player),
                    "Ghast-Fireball", flag);
        });
        registry.onClick("Passthrough", event -> {
            Player player = (Player) event.getWhoClicked();
            StateFlag flag = Flags.PASSTHROUGH;
            BlobLibSoundAPI.getInstance().getSound("Builder.Button-Click").handle(player);
            PSRegion region = getRegion(player);
            ProtectedRegion protectedRegion = region.getWGRegion();
            protectedRegion.setFlag(flag, protectedRegion.getFlag(flag) ==
                    StateFlag.State.ALLOW ? StateFlag.State.DENY : StateFlag.State.ALLOW);
            updateStateFlag(protectedRegion,
                    getCurrentInventory(player),
                    "Passthrough", flag);
        });
        registry.onClick("Use", event -> {
            Player player = (Player) event.getWhoClicked();
            StateFlag flag = Flags.USE;
            BlobLibSoundAPI.getInstance().getSound("Builder.Button-Click").handle(player);
            PSRegion region = getRegion(player);
            ProtectedRegion protectedRegion = region.getWGRegion();
            protectedRegion.setFlag(flag, protectedRegion.getFlag(flag) ==
                    StateFlag.State.ALLOW ? StateFlag.State.DENY : StateFlag.State.ALLOW);
            updateStateFlag(protectedRegion,
                    getCurrentInventory(player),
                    "Use", flag);
        });
        registry.onClick("Return", event -> {
            Player player = (Player) event.getWhoClicked();
            BlobLibSoundAPI.getInstance().getSound("Builder.Button-Click").handle(player);
            openManageProtection(player);
        });
    }

    private void registerManageOwners() {
        carriers.put(InventoryType.MANAGE_OWNERS, "ManageOwners");
    }

    /**
     * Opens the world navigator for the given player.
     * If player is already viewing a selector, will not
     * proceed.
     *
     * @param player the player to open the world navigator for
     */
    public void openWorldNavigator(Player player) {
        if (getManagerDirector().getSelectorManager().getSelectorListener(player) != null)
            return;
        ConfigManager configManager = getConfigManager();
        PSPlayer owner = PSPlayer.fromPlayer(player);
        boolean enabledTeleport = configManager.getBoolean("Teleport.Enabled");
        List<PSRegion> list = owner.getHomes(player.getWorld());
        if (!enabledTeleport)
            list = list.stream().filter(psRegion -> psRegion.isOwner(player.getUniqueId()))
                    .collect(Collectors.toList());
        List<PSRegion> finalList = list;
        getInventoryAPI().customSelector(carriers.get(InventoryType.WORLD_NAVIGATOR),
                player,
                "ProtectionStones",
                "PSRegion",
                () -> finalList,
                psRegion -> {
                    regionMap.put(player.getName(), psRegion);
                    if (psRegion.isOwner(player.getUniqueId())) {
                        openManageProtection(player);
                    } else {
                        if (!enabledTeleport)
                            return;
                        boolean enabledWarmup = configManager.getBoolean("Teleport.Warmup.Enabled");
                        if (!enabledWarmup) {
                            player.teleport(psRegion.getHome());
                            return;
                        }
                        int time = configManager.getInteger("Teleport.Warmup.Time");
                        player.closeInventory();
                        MovementWarmup.PLAYER_TELEPORT(time, player, psRegion.getHome());
                    }
                },
                psRegion -> {
                    ItemStack current = new ItemStack(Material.STONE);
                    PSProtectBlock protectBlock = psRegion.getTypeOptions();
                    if (protectBlock != null)
                        current = psRegion.getTypeOptions().createItem();
                    String displayName = psRegion.getName();
                    if (displayName == null) {
                        Location location = psRegion.getProtectBlock().getLocation();
                        displayName = ChatColor.WHITE.toString() + location.getBlockX() + " / " + location.getBlockY() + " / " + location.getBlockZ();
                    } else
                        displayName = ChatColor.WHITE + psRegion.getName();
                    ItemStackBuilder builder = ItemStackBuilder.build(current);
                    builder.displayName(displayName);
                    return builder.build();
                }, null);
    }

    /**
     * Will open the manage protection inventory for the given player.
     *
     * @param player the player to open the manage protection inventory for
     */
    public void openManageProtection(Player player) {
        ConfigManager configManager = getConfigManager();
        BlobInventoryTracker tracker = tracker(InventoryType.MANAGE_PROTECTION, player);
        BlobInventory inventory = tracker.getInventory();
        PSRegion region = getRegion(player);
        inventory.modder("Show", modder -> {
            modder.replace("%show%", region.isHidden() ?
                    configManager.getAndParseString("Show.Hidden") :
                    configManager.getAndParseString("Show.Shown"));
        });
        boolean enabledTeleport = configManager.getBoolean("Teleport.Enabled");
        if (!enabledTeleport) {
            InventoryButton background = getButton(inventory, "Background");
            ItemStack backgroundItem = inventory.getButton(background.getSlots()
                    .stream().findFirst().orElseThrow(() -> new ConcurrentModificationException("'Background' button not found in 'ManageProtection' inventory!")));
            InventoryButton teleportButton = getButton(inventory, "Teleport");
            teleportButton.getSlots().forEach(slot -> inventory.setButton(slot, backgroundItem));
        }
        inventory.open(player);
    }

    /**
     * Will open the manage members inventory for the given player.
     *
     * @param player the player to open the manage members inventory for
     */
    public void openManageMembers(Player player) {
        PSRegion region = getRegion(player);
        getInventoryAPI().customEditor(
                inventoryKey(InventoryType.MANAGE_MEMBERS),
                player,
                "Members",
                "UUID",
                () -> Bukkit.getOnlinePlayers().stream()
                        .map(Player::getUniqueId)
                        .filter(uuid -> !player.getUniqueId().equals(uuid))
                        .filter(uuid -> !region.getMembers().contains(uuid))
                        .toList(),
                uuid -> {
                    region.addMember(uuid);
                    openManageMembers(player);
                },
                uuid -> {
                    Player onlinePlayer = Bukkit.getPlayer(uuid);
                    ItemStackBuilder builder = ItemStackBuilder.build(Material.LEATHER_HELMET);
                    builder.displayName(onlinePlayer.getName());
                    return builder.build();
                },
                region::getMembers,
                uuid -> {
                    String displayName;
                    Player member = Bukkit.getPlayer(uuid);
                    if (member != null)
                        displayName = member.getName();
                    else {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                        displayName = offlinePlayer.getName();
                    }
                    ItemStackBuilder builder = ItemStackBuilder.build(Material.WOODEN_HOE);
                    builder.displayName(displayName);
                    return builder.build();
                },
                uuid -> {
                    region.removeMember(uuid);
                    openManageMembers(player);
                }, this::openManageProtection);
    }

    /**
     * Will open the manage members inventory for the given player.
     *
     * @param player the player to open the manage members inventory for
     */
    public void openManageOwners(Player player) {
        PSRegion region = getRegion(player);
        getInventoryAPI().customEditor(
                inventoryKey(InventoryType.MANAGE_OWNERS),
                player,
                "Owners",
                "UUID",
                () -> Bukkit.getOnlinePlayers().stream()
                        .map(Player::getUniqueId)
                        .filter(uuid -> !player.getUniqueId().equals(uuid))
                        .filter(uuid -> !region.getOwners().contains(uuid))
                        .toList(),
                uuid -> {
                    region.addOwner(uuid);
                    openManageOwners(player);
                },
                uuid -> {
                    Player onlinePlayer = Bukkit.getPlayer(uuid);
                    ItemStackBuilder builder = ItemStackBuilder.build(Material.LEATHER_HELMET);
                    builder.displayName(onlinePlayer.getName());
                    return builder.build();
                },
                region::getOwners,
                uuid -> {
                    String displayName;
                    Player owner = Bukkit.getPlayer(uuid);
                    if (owner != null)
                        displayName = owner.getName();
                    else {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                        displayName = offlinePlayer.getName();
                    }
                    ItemStackBuilder builder = ItemStackBuilder.build(Material.DIAMOND_HOE);
                    builder.displayName(displayName);
                    return builder.build();
                },
                uuid -> {
                    if (region.getOwners().size() > 1)
                        region.removeOwner(uuid);
                    if (uuid.equals(player.getUniqueId())) {
                        removeMapping(player);
                        player.closeInventory();
                        return;
                    }
                    openManageOwners(player);
                }, this::openManageProtection);
    }

    /**
     * Will open the manage flags inventory for the given player.
     *
     * @param player the player to open the manage flags inventory for
     */
    public void openManageFlags(Player player) {
        BlobInventoryTracker tracker = tracker(InventoryType.MANAGE_FLAGS, player);
        BlobInventory inventory = tracker.getInventory();
        PSRegion region = getRegion(player);
        ProtectedRegion protectedRegion = region.getWGRegion();
        currentInventory.put(player.getName(), inventory);
        updateStateFlag(protectedRegion, inventory, "PvP", Flags.PVP);
        updateStateFlag(protectedRegion, inventory, "Mob-Spawning", Flags.MOB_SPAWNING);
        updateStateFlag(protectedRegion, inventory, "Creeper-Explosion", Flags.CREEPER_EXPLOSION);
        updateStateFlag(protectedRegion, inventory, "Wither-Damage", Flags.WITHER_DAMAGE);
        updateStateFlag(protectedRegion, inventory, "Ghast-Fireball", Flags.GHAST_FIREBALL);
        updateStateFlag(protectedRegion, inventory, "Passthrough", Flags.PASSTHROUGH);
        updateStateFlag(protectedRegion, inventory, "Use", Flags.USE);
        inventory.open(player);
    }

    private void updateStateFlag(ProtectedRegion protectedRegion, BlobInventory inventory, String button, StateFlag flag) {
        ConfigManager configManager = getConfigManager();
        StateFlag.State state = protectedRegion.getFlag(flag);
        String stateDisplay;
        if (state == StateFlag.State.ALLOW)
            stateDisplay = configManager.getAndParseString("State.Allow");
        else if (state == StateFlag.State.DENY)
            stateDisplay = configManager.getAndParseString("State.Deny");
        else {
            stateDisplay = configManager.getAndParseString("State.Stock");
        }
        inventory.getButton(button).getSlots().forEach(slot -> {
            ItemStack current = inventory.getButton(slot);
            List<String> lore = null;
            String displayName = "";
            ItemMeta meta = current.getItemMeta();
            if (meta != null) {
                lore = meta.getLore();
                displayName = meta.getDisplayName();
            }
            List<String> finalLore = new ArrayList<>();
            if (lore != null)
                lore.forEach(s -> finalLore.add(s.replace("%state%", stateDisplay)));
            ItemStackBuilder builder = ItemStackBuilder.build(current);
            builder.displayName(displayName.replace("%state%", stateDisplay));
            builder.lore(finalLore);
            inventory.setButton(slot, builder.build());
        });
    }

    /**
     * Will remove the mapping for the given player.
     *
     * @param player the player to remove the mapping for
     */
    public void removeMapping(Player player) {
        regionMap.remove(player.getName());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeMapping(event.getPlayer());
    }

    /**
     * Will retrieve the region that's currently being viewed by the given player.
     *
     * @param player the player to get the region for
     * @return the region that's currently being viewed by the given player
     */
    public PSRegion getRegion(Player player) {
        return regionMap.get(player.getName());
    }
}
