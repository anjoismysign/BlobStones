package us.mytheria.blobstones.director;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.PSRegion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import us.mytheria.bloblib.BlobLibAPI;
import us.mytheria.bloblib.BlobLibAssetAPI;
import us.mytheria.bloblib.entities.inventory.BlobInventory;
import us.mytheria.bloblib.entities.inventory.InventoryButton;
import us.mytheria.blobstones.entities.InventoryType;
import us.mytheria.blobstones.entities.MovementWarmup;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ListenerManager extends StonesManager implements Listener {
    private final InventoryManager inventoryManager;
    private final ConfigManager configManager;
    private List<UUID> viewCooldown;

    public ListenerManager(StonesManagerDirector managerDirector) {
        super(managerDirector);
        inventoryManager = managerDirector.getInventoryManager();
        configManager = managerDirector.getConfigManager();
        viewCooldown = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        inventoryManager.removeMapping(player);
    }

    @EventHandler
    public void onRemoveMembers(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (inventoryManager.getTitle(InventoryType.MANAGE_MEMBERS)
                .compareTo(title) != 0)
            return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();
        InventoryButton returnButton = inventoryManager.getInventory(InventoryType.MANAGE_MEMBERS)
                .getButton("Return");
        if (returnButton == null)
            throw new IllegalStateException("InventoryButton is null. Report to BlobStones developer.");
        if (!returnButton.containsSlot(slot))
            return;
        inventoryManager.openManageProtection(player);
        BlobLibAssetAPI.getSound("Builder.Button-Click").handle(player);
    }

    @EventHandler
    public void onRemoveOwners(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (inventoryManager.getTitle(InventoryType.MANAGE_OWNERS)
                .compareTo(title) != 0)
            return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();
        InventoryButton returnButton = inventoryManager.getInventory(InventoryType.MANAGE_OWNERS)
                .getButton("Return");
        if (returnButton == null)
            throw new IllegalStateException("InventoryButton is null. Report to BlobStones developer.");
        if (!returnButton.containsSlot(slot))
            return;
        inventoryManager.openManageProtection(player);
        BlobLibAssetAPI.getSound("Builder.Button-Click").handle(player);
    }

    @EventHandler
    public void onFlagClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (inventoryManager.getTitle(InventoryType.MANAGE_FLAGS)
                .compareTo(title) != 0)
            return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();
        InventoryButton returnButton = inventoryManager.getInventory(InventoryType.MANAGE_FLAGS)
                .getButton("Return");
        if (returnButton == null)
            throw new IllegalStateException("InventoryButton is null. Report to BlobStones developer.");
        if (returnButton.containsSlot(slot)) {
            inventoryManager.openManageProtection(player);
            BlobLibAssetAPI.getSound("Builder.Button-Click").handle(player);
            return;
        }
        handleStateFlag("PvP", Flags.PVP, slot, player);
        handleStateFlag("Mob-Spawning", Flags.MOB_SPAWNING, slot, player);
        handleStateFlag("Creeper-Explosion", Flags.CREEPER_EXPLOSION, slot, player);
        handleStateFlag("Wither-Damage", Flags.WITHER_DAMAGE, slot, player);
        handleStateFlag("Ghast-Fireball", Flags.GHAST_FIREBALL, slot, player);
        handleStateFlag("Passthrough", Flags.PASSTHROUGH, slot, player);
        handleStateFlag("Use", Flags.USE, slot, player);
    }

    private void handleStateFlag(String key, StateFlag flag, int slot, Player player) {
        InventoryButton witherDamageButton = inventoryManager.getInventory(InventoryType.MANAGE_FLAGS)
                .getButton(key);
        if (witherDamageButton == null)
            throw new IllegalStateException("'" + key + "' InventoryButton is null. Report to BlobStones developer.");
        if (!witherDamageButton.containsSlot(slot))
            return;
        BlobLibAssetAPI.getSound("Builder.Button-Click").handle(player);
        PSRegion region = inventoryManager.getRegion(player);
        ProtectedRegion protectedRegion = region.getWGRegion();
        protectedRegion.setFlag(flag, protectedRegion.getFlag(flag) ==
                StateFlag.State.ALLOW ? StateFlag.State.DENY : StateFlag.State.ALLOW);
        inventoryManager.updateStateFlag(protectedRegion,
                inventoryManager.getCurrentInventory(player),
                witherDamageButton, flag);
    }

    @EventHandler
    public void onManageProtection(InventoryClickEvent event) {
        manageProtection(event, InventoryType.MANAGE_PROTECTION);
    }

//    @EventHandler
//    public void onTaxPayerManageProtection(InventoryClickEvent event) {
//        InventoryType inventoryType = InventoryType.MANAGE_PROTECTION_TAX_PAYER;
//        if (manageProtection(event, inventoryType))
//            return;
//        int slot = event.getRawSlot();
//        Player player = (Player) event.getWhoClicked();
//        InventoryButton bannedPlayersButton = inventoryManager.getInventory(inventoryType)
//                .getButton("Banned-Players");
//        if (bannedPlayersButton == null)
//            throw new IllegalStateException("'Banned-Players' is null. Report to BlobStones developer.");
//        if (bannedPlayersButton.containsSlot(slot)) {
//            BlobLibAssetAPI.getSound("Builder.Button-Click").handle(player);
//            PSRegion region = inventoryManager.getRegion(player);
//            player.closeInventory();
//            //open the banned players inventory
//        }
//    }

    @EventHandler
    public void onWorldNavigator(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (inventoryManager.getTitle(InventoryType.WORLD_NAVIGATOR)
                .compareTo(title) != 0)
            return;
        event.setCancelled(true);
    }

    /**
     * @param event         The event
     * @param inventoryType The inventory type
     * @return If InventoryClickEvent should continue!
     */
    private boolean manageProtection(InventoryClickEvent event, InventoryType inventoryType) {
        String title = event.getView().getTitle();
        if (inventoryManager.getTitle(inventoryType)
                .compareTo(title) != 0)
            return true;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();
        BlobInventory inventory = inventoryManager.getInventory(inventoryType);
        if (inventory == null)
            throw new NullPointerException("Inventory is null. Report to BlobStones developer.");
        InventoryButton membersButton = InventoryManager.getButton(inventory, "Members");
        if (membersButton.containsSlot(slot)) {
            inventoryManager.openManageMembers(player);
            BlobLibAssetAPI.getSound("Builder.Button-Click").handle(player);
            return true;
        }
        InventoryButton ownersButton = InventoryManager.getButton(inventory, "Owners");
        if (ownersButton.containsSlot(slot)) {
            inventoryManager.openManageOwners(player);
            BlobLibAssetAPI.getSound("Builder.Button-Click").handle(player);
            return true;
        }
        InventoryButton flagsButton = InventoryManager.getButton(inventory, "Flags");
        if (flagsButton.containsSlot(slot)) {
            inventoryManager.openManageFlags(player);
            BlobLibAssetAPI.getSound("Builder.Button-Click").handle(player);
            return true;
        }
        boolean enabledTeleport = configManager.getBoolean("Teleport.Enabled");
        if (enabledTeleport) {
            InventoryButton teleportButton = InventoryManager.getButton(inventory, "Teleport");
            if (teleportButton.containsSlot(slot)) {
                PSRegion region = inventoryManager.getRegion(player);
                boolean enabledWarmup = configManager.getBoolean("Teleport.Warmup.Enabled");
                if (!enabledWarmup) {
                    player.teleport(region.getHome());
                    return true;
                }
                int time = configManager.getInteger("Teleport.Warmup.Time");
                MovementWarmup.PLAYER_TELEPORT(time, player, region.getHome(),
                        BlobLibAssetAPI.getMessage("System.Warmup"));
                return true;
            }
        }
        InventoryButton renameButton = InventoryManager.getButton(inventory, "Rename");
        if (renameButton.containsSlot(slot)) {
            BlobLibAssetAPI.getSound("Builder.Button-Click").handle(player);
            PSRegion region = inventoryManager.getRegion(player);
            player.closeInventory();
            BlobLibAPI.addChatListener(player, 300, input -> {
                        region.setName(input);
                        inventoryManager.openManageProtection(player);
                    }, "BlobStone.Rename-Timeout",
                    "BlobStone.Rename");
            return true;
        }
        InventoryButton showButton = InventoryManager.getButton(inventory, "Show");
        if (showButton.containsSlot(slot)) {
            BlobLibAssetAPI.getSound("Builder.Button-Click").handle(player);
            PSRegion region = inventoryManager.getRegion(player);
            player.closeInventory();
            region.toggleHide();
            return true;
        }
        InventoryButton viewButton = InventoryManager.getButton(inventory, "View");
        if (viewButton.containsSlot(slot)) {
            BlobLibAssetAPI.getSound("Builder.Button-Click").handle(player);
            player.closeInventory();
            if (viewCooldown.contains(player.getUniqueId()))
                return true;
            PSRegion region = inventoryManager.getRegion(player);
            viewCooldown.add(player.getUniqueId());
            Bukkit.getScheduler().runTaskLaterAsynchronously(getPlugin(),
                    () -> viewCooldown.remove(player.getUniqueId()), 60);
            RegionUtil.viewRegion(player, region, getPlugin());
            return true;
        }
        return false;
    }
}