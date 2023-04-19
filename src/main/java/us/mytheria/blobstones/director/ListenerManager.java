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
import us.mytheria.bloblib.entities.inventory.InventoryButton;
import us.mytheria.blobstones.entities.InventoryType;

public class ListenerManager extends StonesManager implements Listener {
    private final InventoryManager inventoryManager;

    public ListenerManager(StonesManagerDirector managerDirector) {
        super(managerDirector);
        inventoryManager = managerDirector.getInventoryManager();
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
        InventoryButton membersButton = inventoryManager.getInventory(inventoryType)
                .getButton("Members");
        if (membersButton == null)
            throw new IllegalStateException("InventoryButton is null. Report to BlobStones developer.");
        if (membersButton.containsSlot(slot)) {
            inventoryManager.openManageMembers(player);
            BlobLibAssetAPI.getSound("Builder.Button-Click").handle(player);
            return true;
        }
        InventoryButton ownersButton = inventoryManager.getInventory(inventoryType)
                .getButton("Owners");
        if (ownersButton == null)
            throw new IllegalStateException("InventoryButton is null. Report to BlobStones developer.");
        if (ownersButton.containsSlot(slot)) {
            inventoryManager.openManageOwners(player);
            BlobLibAssetAPI.getSound("Builder.Button-Click").handle(player);
            return true;
        }
        InventoryButton flagsButton = inventoryManager.getInventory(inventoryType)
                .getButton("Flags");
        if (flagsButton == null)
            throw new IllegalStateException("InventoryButton is null. Report to BlobStones developer.");
        if (flagsButton.containsSlot(slot)) {
            inventoryManager.openManageFlags(player);
            BlobLibAssetAPI.getSound("Builder.Button-Click").handle(player);
            return true;
        }
        InventoryButton teleportButton = inventoryManager.getInventory(inventoryType)
                .getButton("Teleport");
        if (teleportButton == null)
            throw new IllegalStateException("InventoryButton is null. Report to BlobStones developer.");
        if (teleportButton.containsSlot(slot)) {
            PSRegion region = inventoryManager.getRegion(player);
            player.teleport(region.getHome());
            BlobLibAssetAPI.getSound("BlobStones.Teleport").handle(player);
            return true;
        }
        InventoryButton renameButton = inventoryManager.getInventory(inventoryType)
                .getButton("Rename");
        if (renameButton == null)
            throw new IllegalStateException("InventoryButton is null. Report to BlobStones developer.");
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
        return false;
    }
}