package us.mytheria.blobstones.director;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.entities.GenericManagerDirector;
import us.mytheria.blobstones.BlobStones;

public class StonesManagerDirector extends GenericManagerDirector<BlobStones> {

    public StonesManagerDirector(BlobStones blobPlugin) {
        super(blobPlugin);
        registerAndUpdateBlobInventory("ManageFlags");
        registerAndUpdateBlobInventory("ManageMembers");
        registerAndUpdateBlobInventory("ManageOwners");
        registerAndUpdateBlobInventory("ManageProtection");
        registerAndUpdateBlobInventory("WorldNavigator");
        addManager("Config", new ConfigManager(this));
        addManager("Inventory", new InventoryManager(this));
        addManager("Listener", new ListenerManager(this));

        getPlugin().getCommand("blobstones").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
                if (!(commandSender instanceof Player player))
                    return false;
                getInventoryManager().openWorldNavigator(player);
                return true;
            }
        });
    }

    @Override
    public void unload() {
        super.unload();
    }

    /**
     * @return The InventoryManager
     */
    public InventoryManager getInventoryManager() {
        return getManager("Inventory", InventoryManager.class);
    }

    /**
     * @return The ConfigManager
     */
    public ConfigManager getConfigManager() {
        return getManager("Config", ConfigManager.class);
    }
}