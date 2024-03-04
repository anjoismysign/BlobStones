package us.mytheria.blobstones.director;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.entities.GenericManagerDirector;
import us.mytheria.blobstones.BlobStones;
import us.mytheria.blobstones.engine.ProtectionStonesEngine;

public class StonesManagerDirector extends GenericManagerDirector<BlobStones> {

    public StonesManagerDirector(BlobStones blobPlugin) {
        super(blobPlugin);
        registerBlobMessage("es_es/blobstones_lang", "pt_pt/blobstones_lang");
        registerBlobInventory("ManageFlags", "es_es/ManageFlags", "pt_pt/ManageFlags");
        registerBlobInventory("ManageMembers", "es_es/ManageMembers", "pt_pt/ManageMembers");
        registerBlobInventory("ManageOwners", "es_es/ManageOwners", "pt_pt/ManageOwners");
        registerBlobInventory("ManageProtection", "es_es/ManageProtection", "pt_pt/ManageProtection");
        registerBlobInventory("WorldNavigator", "es_es/WorldNavigator", "pt_pt/WorldNavigator");
        addManager("Config", new ConfigManager(this));
        addManager("Engine", new ProtectionStonesEngine(this));

        getPlugin().getCommand("blobstones").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
                if (!(sender instanceof Player player)) {
                    if (args.length < 1) {
                        sender.sendMessage("You must specify a player.");
                        return true;
                    }
                    String arg = args[0];
                    Player target = Bukkit.getPlayer(arg);
                    if (target == null) {
                        sender.sendMessage("Player not found.");
                        return true;
                    }
                    getEngine().openWorldNavigator(target);
                    return true;
                }
                getEngine().openWorldNavigator(player);
                return true;
            }
        });
    }

    @Override
    public void reload() {
        getEngine().reload();
    }

    @Override
    public void unload() {
        super.unload();
    }

    /**
     * @return The InventoryManager
     */
    public ProtectionStonesEngine getEngine() {
        return getManager("Engine", ProtectionStonesEngine.class);
    }

    /**
     * @return The ConfigManager
     */
    public ConfigManager getConfigManager() {
        return getManager("Config", ConfigManager.class);
    }
}