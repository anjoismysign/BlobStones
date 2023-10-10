package us.mytheria.blobstones.engine;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.api.BlobLibInventoryAPI;
import us.mytheria.bloblib.entities.inventory.BlobInventory;
import us.mytheria.bloblib.entities.inventory.BlobInventoryTracker;
import us.mytheria.bloblib.entities.inventory.InventoryButton;
import us.mytheria.blobstones.director.ConfigManager;
import us.mytheria.blobstones.director.StonesManager;
import us.mytheria.blobstones.director.StonesManagerDirector;
import us.mytheria.blobstones.entities.InventoryType;
import us.mytheria.blobstones.entities.MovementWarmup;

import java.util.*;

public class StonesEngine extends StonesManager implements Listener {
    private final ConfigManager configManager;
    private final BlobLibInventoryAPI inventoryAPI;
    private final MovementWarmup warmup;

    protected Map<InventoryType, String> carriers;
    protected final Map<String, BlobInventory> currentInventory;
    protected final List<UUID> viewCooldown;

    public StonesEngine(StonesManagerDirector managerDirector) {
        super(managerDirector);
        warmup = new MovementWarmup(this);
        inventoryAPI = BlobLibInventoryAPI.getInstance();
        configManager = managerDirector.getConfigManager();
        currentInventory = new HashMap<>();
        viewCooldown = new ArrayList<>();
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
     * Gets an InventoryButton from the given inventory
     * handling the case where the button is null,
     * throwing a NullPointerException.
     *
     * @param inventory the inventory to get the button from
     * @param name      the name of the button
     * @return the button
     */
    @NotNull
    public InventoryButton getButton(BlobInventory inventory, String name) {
        InventoryButton button = inventory.getButton(name);
        Objects.requireNonNull(button, "'" + name + "' button not found");
        return button;
    }

    /**
     * Gets the inventory key for the given inventory type.
     *
     * @param type the type of inventory to get the key for
     * @return the key
     */
    protected String inventoryKey(InventoryType type) {
        return carriers.get(type);
    }

    /**
     * Will open the given inventory for the given player,
     * localizing the inventory to the player.
     *
     * @param type   the type of inventory to open
     * @param player the player to open the inventory for
     * @return the inventory that was opened
     */
    public BlobInventoryTracker tracker(InventoryType type, Player player) {
        return inventoryAPI.trackInventory(player, inventoryKey(type));
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public BlobLibInventoryAPI getInventoryAPI() {
        return inventoryAPI;
    }
}
