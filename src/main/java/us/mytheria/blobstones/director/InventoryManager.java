package us.mytheria.blobstones.director;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSProtectBlock;
import dev.espi.protectionstones.PSRegion;
import me.anjoismysign.anjo.entities.Uber;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.BlobLibAssetAPI;
import us.mytheria.bloblib.entities.BlobEditor;
import us.mytheria.bloblib.entities.BlobSelector;
import us.mytheria.bloblib.entities.inventory.BlobInventory;
import us.mytheria.bloblib.entities.inventory.InventoryButton;
import us.mytheria.bloblib.itemstack.ItemStackBuilder;
import us.mytheria.bloblib.itemstack.ItemStackModder;
import us.mytheria.blobstones.entities.InventoryType;
import us.mytheria.blobstones.entities.MovementWarmup;

import java.util.*;
import java.util.stream.Collectors;

public class InventoryManager extends StonesManager {
    private final ConfigManager configManager;

    private Map<InventoryType, BlobInventory> carriers;
    private Map<InventoryType, String> carrierTitles;
    private final Map<String, BlobEditor<?>> editorMap;
    private final Map<String, BlobInventory> currentInventory;
    private final Map<String, PSRegion> regionMap;

    public InventoryManager(StonesManagerDirector managerDirector) {
        super(managerDirector);
        this.configManager = managerDirector.getConfigManager();
        editorMap = new HashMap<>();
        currentInventory = new HashMap<>();
        regionMap = new HashMap<>();
        reload();
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void reload() {
        super.reload();
        carriers = new HashMap<>();
        carrierTitles = new HashMap<>();
        BlobInventory worldNavigator = BlobLibAssetAPI.getBlobInventory("WorldNavigator");
        carriers.put(InventoryType.WORLD_NAVIGATOR, worldNavigator);
        carrierTitles.put(InventoryType.WORLD_NAVIGATOR, worldNavigator.getTitle());
        BlobInventory manageProtection = BlobLibAssetAPI.getBlobInventory("ManageProtection");
        carriers.put(InventoryType.MANAGE_PROTECTION, manageProtection);
        carrierTitles.put(InventoryType.MANAGE_PROTECTION, manageProtection.getTitle());
        BlobInventory manageMembers = BlobLibAssetAPI.getBlobInventory("ManageMembers");
        carriers.put(InventoryType.MANAGE_MEMBERS, manageMembers);
        carrierTitles.put(InventoryType.MANAGE_MEMBERS, manageMembers.getTitle());
        BlobInventory manageFlags = BlobLibAssetAPI.getBlobInventory("ManageFlags");
        carriers.put(InventoryType.MANAGE_FLAGS, manageFlags);
        carrierTitles.put(InventoryType.MANAGE_FLAGS, manageFlags.getTitle());
        BlobInventory manageOwners = BlobLibAssetAPI.getBlobInventory("ManageOwners");
        carriers.put(InventoryType.MANAGE_OWNERS, manageOwners);
        carrierTitles.put(InventoryType.MANAGE_OWNERS, manageOwners.getTitle());
//        BlobInventory manageProtectionTaxPayer = BlobLibAssetAPI.getBlobInventory("ManageProtection.yml");
//        carriers.put(InventoryType.MANAGE_PROTECTION_TAX_PAYER, manageProtectionTaxPayer);
//        carrierTitles.put(InventoryType.MANAGE_PROTECTION_TAX_PAYER, manageProtectionTaxPayer.getTitle());
//        BlobInventory manageBanned = BlobLibAssetAPI.getBlobInventory("ManageBanned");
//        carriers.put(InventoryType.MANAGE_BANNED, manageBanned);
//        carrierTitles.put(InventoryType.MANAGE_BANNED, manageBanned.getTitle());
    }

    /**
     * Will remove the mapping for the given player.
     *
     * @param player the player to remove the mapping for
     */
    public void removeMapping(Player player) {
        editorMap.remove(player.getName());
        regionMap.remove(player.getName());
    }

    /**
     * Gets the title for the given inventory type.
     * If no title is found, will throw an exception.
     *
     * @param type the type to get the title for
     * @return the title for the given inventory type
     */
    @NotNull
    public String getTitle(InventoryType type) {
        return Optional.ofNullable(carrierTitles.get(type))
                .orElseThrow(() -> new IllegalArgumentException
                        ("No title for inventory type " + type.name()));
    }

    /**
     * Gets the inventory for the given type.
     *
     * @param type the type to get the inventory for
     * @return the inventory for the given type
     */
    public BlobInventory getInventory(InventoryType type) {
        return carriers.get(type);
    }

    /**
     * Will open the given inventory for the given player.
     *
     * @param player the player to open the inventory for
     * @return the inventory that was opened
     */
    public BlobInventory getCurrentInventory(Player player) {
        return currentInventory.get(player.getName());
    }

    /**
     * Returns the manage members editor for the given player.
     *
     * @param player the player to get the editor for
     * @return the manage members editor for the given player
     */
    @SuppressWarnings("unchecked")
    public BlobEditor<UUID> getUUIDEditor(Player player) {
        return (BlobEditor<UUID>) editorMap.get(player.getName());
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
        BlobInventory inventory = getInventory(InventoryType.WORLD_NAVIGATOR).copy();
        PSPlayer owner = PSPlayer.fromPlayer(player);
        boolean enabledTeleport = configManager.getBoolean("Teleport.Enabled");
        List<PSRegion> list = owner.getHomes(player.getWorld());
        if (!enabledTeleport)
            list = list.stream().filter(psRegion -> psRegion.isOwner(player.getUniqueId()))
                    .collect(Collectors.toList());
        BlobSelector<PSRegion> selector = BlobSelector.build(inventory, player.getUniqueId(),
                "PSRegion", list);
        selector.setItemsPerPage(selector.getSlots("ProtectionStones")
                == null ? 1 : selector.getSlots("ProtectionStones").size());
        selector.selectElement(player, psRegion -> {
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
                MovementWarmup.PLAYER_TELEPORT(time, player, psRegion.getHome(),
                        BlobLibAssetAPI.getMessage("System.Warmup"));
            }
        }, null, psRegion -> {
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
        });
    }

    /**
     * Will open the manage protection inventory for the given player.
     *
     * @param player the player to open the manage protection inventory for
     */
    public void openManageProtection(Player player) {
        BlobInventory inventory = getInventory(InventoryType.MANAGE_PROTECTION).copy();
        PSRegion region = getRegion(player);
        InventoryButton showButton = getButton(inventory, "Show");
        showButton.getSlots().forEach(slot -> {
            ItemStack itemStack = inventory.getButton(slot);
            ItemStackModder modder = ItemStackModder.mod(itemStack.clone());
            modder.replace("%show%", region.isHidden() ?
                    configManager.getAndParseString("Show.Hidden") :
                    configManager.getAndParseString("Show.Shown"));
        });
        boolean enabledTeleport = configManager.getBoolean("Teleport.Enabled");
        if (!enabledTeleport) {
            InventoryButton background = getButton(inventory, "Background");
            ItemStack backgroundItem = inventory.getButton(background.getSlots().stream().findFirst().orElseThrow(() -> {
                throw new IllegalStateException("'Background' button not found in 'ManageProtection' inventory!");
            }));
            InventoryButton teleportButton = getButton(inventory, "Teleport");
            teleportButton.getSlots().forEach(slot -> {
                inventory.setButton(slot, backgroundItem);
            });
        }
        inventory.open(player);
    }

//    /**
//     * Will open the manage protection inventory assuming the given player is the
//     * original owner / taxpayer of the PSRegion.
//     *
//     * @param player the player to open the manage protection inventory for
//     */
//    public void openTaxPayerManageProtection(Player player) {
//        getInventory(InventoryType.MANAGE_PROTECTION_TAX_PAYER).open(player);
//    }

    /**
     * Will open the manage members inventory for the given player.
     *
     * @param player the player to open the manage members inventory for
     */
    @SuppressWarnings("DataFlowIssue")
    public void openManageMembers(Player player) {
        BlobInventory inventory = getInventory(InventoryType.MANAGE_MEMBERS).copy();
        PSRegion region = getRegion(player);
        Uber<BlobEditor<UUID>> uber = Uber.fly();
        uber.talk(BlobEditor.build(inventory, player.getUniqueId(),
                "UUID", owner -> {
                    /*
                     * Will manage adding online players to the PSRegion
                     * I'm not used to document inside the code, but this time
                     * it was really messy since I am doing this in a hurry lol
                     */
                    Collection<UUID> onlinePlayers = Bukkit.getOnlinePlayers().stream()
                            .map(Player::getUniqueId)
                            .toList();
                    BlobSelector<UUID> playerSelector = BlobSelector.COLLECTION_INJECTION(player.getUniqueId(),
                            "UUID", onlinePlayers.stream()
                                    .filter(uuid -> !region.getMembers()
                                            .contains(uuid))
                                    .toList());
                    playerSelector.setItemsPerPage(playerSelector.getSlots("Members")
                            == null ? 1 : playerSelector.getSlots("Members").size());
                    playerSelector.selectElement(player, uuid -> {
                        region.addMember(uuid);
                        openManageMembers(player);
                    }, null, uuid -> {
                        Player onlinePlayer = Bukkit.getPlayer(uuid);
                        ItemStackBuilder builder = ItemStackBuilder.build(Material.PLAYER_HEAD);
                        builder.displayName(onlinePlayer.getName());
                        return builder.build();
                    });
                }, region.getMembers()));
        BlobEditor<UUID> editor = uber.thanks();
        editor.setItemsPerPage(editor.getSlots("Members") == null
                ? 1 : editor.getSlots("Members").size());
        editor.manage(player, uuid -> {
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
        }, uuid -> {
            region.removeMember(uuid);
            openManageMembers(player);
        });
        editorMap.put(player.getName(), editor);
    }

    /**
     * Will open the manage members inventory for the given player.
     *
     * @param player the player to open the manage members inventory for
     */
    public void openManageOwners(Player player) {
        BlobInventory inventory = getInventory(InventoryType.MANAGE_OWNERS).copy();
        PSRegion region = getRegion(player);
        Uber<BlobEditor<UUID>> uber = Uber.fly();
        uber.talk(BlobEditor.build(inventory, player.getUniqueId(),
                "UUID", owner -> {
                    /*
                     * Will manage adding online players to the PSRegion
                     * I'm not used to document inside the code, but this time
                     * it was really messy since I am doing this in a hurry lol
                     */
                    Collection<UUID> onlinePlayers = Bukkit.getOnlinePlayers().stream()
                            .map(Player::getUniqueId)
                            .filter(uuid -> !region.getOwners()
                                    .contains(uuid))
                            .toList();
                    BlobSelector<UUID> playerSelector = BlobSelector.COLLECTION_INJECTION(player.getUniqueId(),
                            "UUID", onlinePlayers.stream()
                                    .toList());
                    playerSelector.setItemsPerPage(playerSelector.getSlots("Owners")
                            == null ? 1 : playerSelector.getSlots("Owners").size());
                    playerSelector.selectElement(player, uuid -> {
                        region.addOwner(uuid);
                        openManageOwners(player);
                    }, null, uuid -> {
                        Player onlinePlayer = Bukkit.getPlayer(uuid);
                        ItemStackBuilder builder = ItemStackBuilder.build(Material.DIAMOND_HOE);
                        builder.displayName(onlinePlayer.getName());
                        return builder.build();
                    });
                }, region.getOwners()));
        BlobEditor<UUID> editor = uber.thanks();
        editor.setItemsPerPage(editor.getSlots("Owners") == null
                ? 1 : editor.getSlots("Owners").size());
        editor.manage(player, uuid -> {
            // TaxAutopayer is the original owner of the region!
            if (region.getTaxAutopayer().compareTo(uuid) == 0)
                return null;
            String displayName;
            Player member = Bukkit.getPlayer(uuid);
            if (member != null)
                displayName = member.getName();
            else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                displayName = offlinePlayer.getName();
            }
            ItemStackBuilder builder = ItemStackBuilder.build(Material.IRON_HELMET);
            builder.displayName(displayName);
            return builder.build();
        }, uuid -> {
            region.removeOwner(uuid);
            openManageOwners(player);
        });
        editorMap.put(player.getName(), editor);
    }

//    public void openManageBanned(Player player) {
//        BlobInventory inventory = getInventory(InventoryType.MANAGE_BANNED).copy();
//        PSRegion region = getRegion(player);
//        Uber<BlobEditor<UUID>> uber = Uber.fly();
//        uber.talk(BlobEditor.build(inventory, player.getUniqueId(),
//                "UUID", owner -> {
//                    /*
//                     * Will manage adding online players to the PSRegion
//                     * I'm not used to document inside the code, but this time
//                     * it was really messy since I am doing this in a hurry lol
//                     */
//                    Collection<UUID> onlinePlayers = Bukkit.getOnlinePlayers().stream()
//                            .map(Player::getUniqueId)
//                            .toList();
//                    BlobSelector<UUID> playerSelector = BlobSelector.COLLECTION_INJECTION(player.getUniqueId(),
//                            "UUID", onlinePlayers.stream()
//                                    .filter(uuid -> !region.get
//                                            .contains(uuid))
//                                    .toList());
//                    playerSelector.setItemsPerPage(playerSelector.getSlots("Members")
//                            == null ? 1 : playerSelector.getSlots("Members").size());
//                    playerSelector.selectElement(player, uuid -> {
//                        region.addMember(uuid);
//                        openManageMembers(player);
//                    }, null, uuid -> {
//                        Player onlinePlayer = Bukkit.getPlayer(uuid);
//                        ItemStackBuilder builder = ItemStackBuilder.build(Material.PLAYER_HEAD);
//                        builder.displayName(onlinePlayer.getName());
//                        return builder.build();
//                    });
//                }, region.getMembers()));
//        BlobEditor<UUID> editor = uber.thanks();
//        editor.setItemsPerPage(editor.getSlots("Members") == null
//                ? 1 : editor.getSlots("Members").size());
//        editor.manage(player, uuid -> {
//            String displayName;
//            Player member = Bukkit.getPlayer(uuid);
//            if (member != null)
//                displayName = member.getName();
//            else {
//                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
//                displayName = offlinePlayer.getName();
//            }
//            ItemStackBuilder builder = ItemStackBuilder.build(Material.WOODEN_HOE);
//            builder.displayName(displayName);
//            return builder.build();
//        }, uuid -> {
//            region.removeMember(uuid);
//            openManageMembers(player);
//        });
//        editorMap.put(player.getName(), editor);
//    }

    /**
     * Will open the manage flags inventory for the given player.
     *
     * @param player the player to open the manage flags inventory for
     */
    public void openManageFlags(Player player) {
        BlobInventory inventory = getInventory(InventoryType.MANAGE_FLAGS).copy();
        PSRegion region = getRegion(player);
        ProtectedRegion protectedRegion = region.getWGRegion();
        InventoryButton pvpButton = getButton(inventory, "PvP");
        InventoryButton mobSpawningButton = getButton(inventory, "Mob-Spawning");
        InventoryButton creeperExplosionButton = getButton(inventory, "Creeper-Explosion");
        InventoryButton witherDamageButton = getButton(inventory, "Wither-Damage");
        InventoryButton ghastFireballButton = getButton(inventory, "Ghast-Fireball");
        InventoryButton passthroughButton = getButton(inventory, "Passthrough");
        InventoryButton useButton = getButton(inventory, "Use");
        currentInventory.put(player.getName(), inventory);
        updateStateFlag(protectedRegion, inventory, pvpButton, Flags.PVP);
        updateStateFlag(protectedRegion, inventory, mobSpawningButton, Flags.MOB_SPAWNING);
        updateStateFlag(protectedRegion, inventory, creeperExplosionButton, Flags.CREEPER_EXPLOSION);
        updateStateFlag(protectedRegion, inventory, witherDamageButton, Flags.WITHER_DAMAGE);
        updateStateFlag(protectedRegion, inventory, ghastFireballButton, Flags.GHAST_FIREBALL);
        updateStateFlag(protectedRegion, inventory, passthroughButton, Flags.PASSTHROUGH);
        updateStateFlag(protectedRegion, inventory, useButton, Flags.USE);
        inventory.open(player);
    }

    @NotNull
    protected static InventoryButton getButton(BlobInventory inventory, String name) {
        InventoryButton button = inventory.getButton(name);
        if (button == null)
            throw new NullPointerException("'" + name + "' button not found");
        return button;
    }

    protected void updateStateFlag(ProtectedRegion protectedRegion, BlobInventory inventory, InventoryButton button, StateFlag flag) {
        StateFlag.State state = protectedRegion.getFlag(flag);
        String stateDisplay;
        if (state == StateFlag.State.ALLOW)
            stateDisplay = configManager.getAndParseString("State.Allow");
        else if (state == StateFlag.State.DENY)
            stateDisplay = configManager.getAndParseString("State.Deny");
        else {
            stateDisplay = configManager.getAndParseString("State.Stock");
        }
        button.getSlots().forEach(slot -> {
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
}
