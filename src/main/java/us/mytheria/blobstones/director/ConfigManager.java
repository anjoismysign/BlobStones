package us.mytheria.blobstones.director;

import org.bukkit.configuration.file.FileConfiguration;
import us.mytheria.bloblib.utilities.TextColor;
import us.mytheria.blobstones.BlobStones;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager extends StonesManager {
    private Map<String, Object> stringMap;

    public ConfigManager(StonesManagerDirector managerDirector) {
        super(managerDirector);
        reload();
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
        stringMap.put("State.Stock", configuration.getString("State.Stock"));
        stringMap.put("Show.Shown", configuration.getString("Show.Shown"));
        stringMap.put("Show.Hidden", configuration.getString("Show.Hidden"));
    }

    /**
     * Will return the value for the given key.
     *
     * @param key   the key to get the value for
     * @param clazz the class of the value
     * @param <T>   the type of the value
     * @return the value for the given key
     */
    @SuppressWarnings({"unchecked", "unused"})
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

    /**
     * Will parse the string for the given key.
     *
     * @param key the key to get the value for
     * @return the parsed string for the given key
     */
    public String getAndParseString(String key) {
        return TextColor.PARSE(getString(key));
    }
}
