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
import us.mytheria.bloblib.entities.BlobEditor;
import us.mytheria.bloblib.entities.inventory.InventoryButton;
import us.mytheria.blobstones.entities.InventoryType;

import java.util.UUID;

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
        if (returnButton.containsSlot(slot)) {
            inventoryManager.openManageProtection(player);
            return;
        }
        InventoryButton inventoryButton = inventoryManager.getInventory(InventoryType.MANAGE_MEMBERS)
                .getButton("Remove");
        {
            if (inventoryButton == null)
                throw new IllegalStateException("InventoryButton is null. Report to BlobStones developer.");
            if (!inventoryButton.containsSlot(slot))
                return;
            BlobEditor<UUID> editor = inventoryManager
                    .getUUIDEditor(player);
            PSRegion region = inventoryManager.getRegion(player);
            editor.removeElement(player, region::removeMember);
        }
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
        if (returnButton.containsSlot(slot)) {
            inventoryManager.openManageProtection(player);
            return;
        }
        InventoryButton inventoryButton = inventoryManager.getInventory(InventoryType.MANAGE_OWNERS)
                .getButton("Remove");
        {
            if (inventoryButton == null)
                throw new IllegalStateException("InventoryButton is null. Report to BlobStones developer.");
            if (!inventoryButton.containsSlot(slot))
                return;
            BlobEditor<UUID> editor = inventoryManager
                    .getUUIDEditor(player);
            PSRegion region = inventoryManager.getRegion(player);
            editor.removeElement(player, region::removeOwner);
        }
    }

    @EventHandler
    public void onFlagClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (inventoryManager.getTitle(InventoryType.MANAGE_MEMBERS)
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
            return;
        }
        InventoryButton pvpButton = inventoryManager.getInventory(InventoryType.MANAGE_FLAGS)
                .getButton("PvP");
        if (pvpButton == null)
            throw new IllegalStateException("InventoryButton is null. Report to BlobStones developer.");
        if (!pvpButton.containsSlot(slot))
            return;
        PSRegion region = inventoryManager.getRegion(player);
        ProtectedRegion protectedRegion = region.getWGRegion();
        protectedRegion.setFlag(Flags.PVP, protectedRegion.getFlag(Flags.PVP) ==
                StateFlag.State.ALLOW ? StateFlag.State.DENY : StateFlag.State.ALLOW);
        inventoryManager.getCurrentInventory(player).buildInventory();
    }

    @EventHandler
    public void onManageProtection(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (inventoryManager.getTitle(InventoryType.MANAGE_PROTECTION)
                .compareTo(title) != 0)
            return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();
        InventoryButton returnButton = inventoryManager.getInventory(InventoryType.MANAGE_PROTECTION)
                .getButton("Return");
        if (returnButton == null)
            throw new IllegalStateException("InventoryButton is null. Report to BlobStones developer.");
        if (returnButton.containsSlot(slot)) {
            inventoryManager.openManageProtection(player);
            return;
        }
        InventoryButton membersButton = inventoryManager.getInventory(InventoryType.MANAGE_PROTECTION)
                .getButton("Members");
        if (membersButton == null)
            throw new IllegalStateException("InventoryButton is null. Report to BlobStones developer.");
        if (membersButton.containsSlot(slot)) {
            inventoryManager.openManageMembers(player);
            return;
        }
        InventoryButton ownersButton = inventoryManager.getInventory(InventoryType.MANAGE_PROTECTION)
                .getButton("Owners");
        if (ownersButton == null)
            throw new IllegalStateException("InventoryButton is null. Report to BlobStones developer.");
        if (ownersButton.containsSlot(slot)) {
            inventoryManager.openManageOwners(player);
            return;
        }
        InventoryButton flagsButton = inventoryManager.getInventory(InventoryType.MANAGE_PROTECTION)
                .getButton("Flags");
        if (flagsButton == null)
            throw new IllegalStateException("InventoryButton is null. Report to BlobStones developer.");
        if (flagsButton.containsSlot(slot)) {
            inventoryManager.openManageFlags(player);
            return;
        }
        InventoryButton teleportButton = inventoryManager.getInventory(InventoryType.MANAGE_PROTECTION)
                .getButton("Teleport");
        if (teleportButton == null)
            throw new IllegalStateException("InventoryButton is null. Report to BlobStones developer.");
        if (teleportButton.containsSlot(slot)) {
            PSRegion region = inventoryManager.getRegion(player);
            player.teleport(region.getHome());
            return;
        }
        InventoryButton renameButton = inventoryManager.getInventory(InventoryType.MANAGE_PROTECTION)
                .getButton("Rename");
        if (renameButton == null)
            throw new IllegalStateException("InventoryButton is null. Report to BlobStones developer.");
        if (renameButton.containsSlot(slot)) {
            PSRegion region = inventoryManager.getRegion(player);
            BlobLibAPI.addChatListener(player, 300, region::setName, "BlobStone.Rename-Timeout",
                    "BlobStone.Rename");
        }
    }

    @EventHandler
    public void onWorldNavigator(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (inventoryManager.getTitle(InventoryType.WORLD_NAVIGATOR)
                .compareTo(title) != 0)
            return;
        event.setCancelled(true);
    }
}
