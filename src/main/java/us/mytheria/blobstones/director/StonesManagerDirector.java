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
        registerBlobMessage(
                "de_de/blobstones_lang",
                "el_gr/blobstones_lang",
                "es_es/blobstones_lang",
                "fr_fr/blobstones_lang",
                "nl_nl/blobstones_lang",
                "pt_pt/blobstones_lang",
                "ru_ru/blobstones_lang",
                "zh_cn/blobstones_lang");
        registerBlobInventory("ManageFlags",
                "de_de/ManageFlags",
                "el_gr/ManageFlags",
                "es_es/ManageFlags",
                "fr_fr/ManageFlags",
                "nl_nl/ManageFlags",
                "pt_pt/ManageFlags",
                "ru_ru/ManageFlags",
                "zh_cn/ManageFlags");
        registerBlobInventory("ManageMembers",
                "de_de/ManageMembers",
                "el_gr/ManageMembers",
                "es_es/ManageMembers",
                "fr_fr/ManageMembers",
                "nl_nl/ManageMembers",
                "pt_pt/ManageMembers",
                "ru_ru/ManageMembers",
                "zh_cn/ManageMembers");
        registerBlobInventory("ManageOwners",
                "de_de/ManageOwners",
                "el_gr/ManageOwners",
                "es_es/ManageOwners",
                "fr_fr/ManageOwners",
                "nl_nl/ManageOwners",
                "pt_pt/ManageOwners",
                "ru_ru/ManageOwners",
                "zh_cn/ManageOwners");
        registerBlobInventory("ManageProtection",
                "de_de/ManageProtection",
                "el_gr/ManageProtection",
                "es_es/ManageProtection",
                "fr_fr/ManageProtection",
                "nl_nl/ManageProtection",
                "pt_pt/ManageProtection",
                "ru_ru/ManageProtection",
                "zh_cn/ManageProtection");
        registerBlobInventory("WorldNavigator",
                "de_de/WorldNavigator",
                "el_gr/WorldNavigator",
                "es_es/WorldNavigator",
                "fr_fr/WorldNavigator",
                "nl_nl/WorldNavigator",
                "pt_pt/WorldNavigator",
                "ru_ru/WorldNavigator",
                "zh_cn/WorldNavigator");
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