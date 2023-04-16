package us.mytheria.blobstones.director;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSProtectBlock;
import dev.espi.protectionstones.PSRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.BlobLibAPI;
import us.mytheria.bloblib.BlobLibAssetAPI;
import us.mytheria.bloblib.entities.BlobEditor;
import us.mytheria.bloblib.entities.BlobSelector;
import us.mytheria.bloblib.entities.inventory.BlobInventory;
import us.mytheria.bloblib.entities.inventory.InventoryButton;
import us.mytheria.bloblib.itemstack.ItemStackBuilder;
import us.mytheria.bloblib.utilities.TextColor;
import us.mytheria.blobstones.entities.InventoryType;

import java.util.*;

public class InventoryManager extends StonesManager {
    private Map<InventoryType, BlobInventory> carriers;
    private Map<InventoryType, String> carrierTitles;
    private final Map<String, BlobEditor<?>> editorMap;
    private final Map<String, BlobInventory> currentInventory;
    private final Map<String, PSRegion> regionMap;

    public InventoryManager(StonesManagerDirector managerDirector) {
        super(managerDirector);
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
        BlobInventory manageAdmins = BlobLibAssetAPI.getBlobInventory("ManageAdmins");
        carriers.put(InventoryType.MANAGE_OWNERS, manageAdmins);
        carrierTitles.put(InventoryType.MANAGE_OWNERS, manageAdmins.getTitle());
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
        List<PSRegion> list = owner.getHomes(player.getWorld());
        BlobSelector<PSRegion> selector = BlobSelector.build(inventory, player.getUniqueId(),
                "PSRegion", list);
        selector.loadCustomPage(0, true, psRegion -> {
            ItemStack current = new ItemStack(Material.STONE);
            PSProtectBlock protectBlock = psRegion.getTypeOptions();
            if (protectBlock != null)
                current = psRegion.getTypeOptions().createItem();
            String displayName = psRegion.getName();
            if (displayName == null) {
                Location location = psRegion.getProtectBlock().getLocation();
                displayName = location.getBlockX() + " / " + location.getBlockY() + " / " + location.getBlockZ();
            }
            ItemStackBuilder builder = ItemStackBuilder.build(current);
            builder.displayName(displayName);
            return builder.build();
        });
        BlobLibAPI.addSelectorListener(player, psRegion -> {
            regionMap.put(player.getName(), psRegion);
            if (psRegion.isOwner(player.getUniqueId()))
                openManageProtection(player);
            else
                player.teleport(psRegion.getHome());
        }, null, selector);
        selector.open();
    }

    /**
     * Will open the manage protection inventory for the given player.
     *
     * @param player the player to open the manage protection inventory for
     */
    public void openManageProtection(Player player) {
        getInventory(InventoryType.MANAGE_PROTECTION).open(player);
    }

    /**
     * Will open the manage members inventory for the given player.
     *
     * @param player the player to open the manage members inventory for
     */
    @SuppressWarnings("DataFlowIssue")
    public void openManageMembers(Player player) {
        getPlugin().getAnjoLogger().log("Check if owners can be members simultaneously");
        BlobInventory inventory = getInventory(InventoryType.MANAGE_MEMBERS).copy();
        PSRegion region = getRegion(player);
        BlobEditor<UUID> editor = BlobEditor.build(inventory, player.getUniqueId(),
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
                    playerSelector.loadCustomPage(0, true, uuid -> {
                        Player onlinePlayer = Bukkit.getPlayer(uuid);
                        ItemStackBuilder builder = ItemStackBuilder.build(Material.PLAYER_HEAD);
                        builder.displayName(onlinePlayer.getName());
                        return builder.build();
                    });
                    BlobLibAPI.addSelectorListener(player, uuid -> {
                        region.addMember(uuid);
                        inventory.buildInventory();
                    }, null, playerSelector);
                }, region.getMembers());
        editor.setItemsPerPage(editor.getSlots("Members") == null
                ? 1 : editor.getSlots("Members").size());
        editor.loadCustomPage(0, true, uuid -> {
            String displayName;
            Player member = Bukkit.getPlayer(uuid);
            if (member != null)
                displayName = member.getName();
            else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                displayName = offlinePlayer.getName();
            }
            ItemStackBuilder builder = ItemStackBuilder.build(Material.LEATHER_HELMET);
            builder.displayName(displayName);
            return builder.build();
        });
        editor.open();
        editorMap.put(player.getName(), editor);
    }

    /**
     * Will open the manage members inventory for the given player.
     *
     * @param player the player to open the manage members inventory for
     */
    public void openManageOwners(Player player) {
        getPlugin().getAnjoLogger().log("Check if owners can be members simultaneously");
        BlobInventory inventory = getInventory(InventoryType.MANAGE_OWNERS).copy();
        PSRegion region = getRegion(player);
        BlobEditor<UUID> editor = BlobEditor.build(inventory, player.getUniqueId(),
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
                                    .filter(uuid -> !region.getOwners()
                                            .contains(uuid))
                                    .toList());
                    playerSelector.loadCustomPage(0, true, uuid -> {
                        Player onlinePlayer = Bukkit.getPlayer(uuid);
                        ItemStackBuilder builder = ItemStackBuilder.build(Material.PLAYER_HEAD);
                        builder.displayName(onlinePlayer.getName());
                        return builder.build();
                    });
                    BlobLibAPI.addSelectorListener(player, uuid -> {
                        region.addOwner(uuid);
                        inventory.buildInventory();
                    }, null, playerSelector);
                }, region.getOwners());
        editor.setItemsPerPage(editor.getSlots("Owners") == null
                ? 1 : editor.getSlots("Owners").size());
        editor.loadCustomPage(0, true, uuid -> {
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
        });
        editor.open();
        editorMap.put(player.getName(), editor);
    }

    /**
     * Will open the manage flags inventory for the given player.
     *
     * @param player the player to open the manage flags inventory for
     */
    public void openManageFlags(Player player) {
        BlobInventory inventory = getInventory(InventoryType.MANAGE_FLAGS).copy();
        PSRegion region = getRegion(player);
        ProtectedRegion protectedRegion = region.getWGRegion();
        InventoryButton button = inventory.getButton("PvP");
        if (button == null)
            throw new IllegalStateException("'PvP' button not found");
        currentInventory.put(player.getName(), inventory);
        StateFlag.State state = protectedRegion.getFlag(Flags.PVP);
        String stateDisplay;
        if (state == StateFlag.State.ALLOW)
            stateDisplay = TextColor.PARSE(getManagerDirector().getConfigManager().getString("State.Allow"));
        else if (state == StateFlag.State.DENY)
            stateDisplay = TextColor.PARSE(getManagerDirector().getConfigManager().getString("State.Deny"));
        else {
            stateDisplay = TextColor.PARSE("&5ERROR");
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
                lore.forEach(s -> {
                    finalLore.add(s.replace("%state%", stateDisplay));
                });
            ItemStackBuilder builder = ItemStackBuilder.build(current);
            builder.displayName(displayName.replace("%state%", stateDisplay));
            builder.lore(finalLore);
            inventory.setButton(slot, builder.build());
        });
    }
}
