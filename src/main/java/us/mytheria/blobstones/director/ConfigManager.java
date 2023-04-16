package us.mytheria.blobstones.director;

import org.bukkit.configuration.file.FileConfiguration;
import us.mytheria.blobstones.BlobStones;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager extends StonesManager {
    private Map<String, Object> stringMap;

    public ConfigManager(StonesManagerDirector managerDirector) {
        super(managerDirector);
    }

    @Override
    public void reload() {
        super.reload();
        BlobStones plugin = getManagerDirector().getPlugin();
        plugin.reloadConfig();
        plugin.saveDefaultConfig();
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
        stringMap = new HashMap<>();
        FileConfiguration configuration = plugin.getConfig();
        stringMap.put("State.Allow", configuration.getString("State.Allow"));
        stringMap.put("State.Deny", configuration.getString("State.Deny"));
    }

    /**
     * Will return the value for the given key.
     *
     * @param key   the key to get the value for
     * @param clazz the class of the value
     * @param <T>   the type of the value
     * @return the value for the given key
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        return (T) stringMap.get(key);
    }

    /**
     * Will return the value for the given key.
     *
     * @param key the key to get the value for
     * @return the value for the given key
     */
    public String getString(String key) {
        return get(key, String.class);
    }
}