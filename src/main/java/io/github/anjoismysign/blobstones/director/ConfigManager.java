package io.github.anjoismysign.blobstones.director;

import io.github.anjoismysign.bloblib.utilities.TextColor;
import io.github.anjoismysign.blobstones.BlobStones;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager extends StonesManager {
    private Map<String, Object> map;

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
        map = new HashMap<>();
        FileConfiguration configuration = plugin.getConfig();
        map.put("State.Allow", configuration.getString("State.Allow"));
        map.put("State.Deny", configuration.getString("State.Deny"));
        map.put("State.Stock", configuration.getString("State.Stock"));
        map.put("Show.Shown", configuration.getString("Show.Shown"));
        map.put("Show.Hidden", configuration.getString("Show.Hidden"));
        map.put("Teleport.Enabled", configuration.getBoolean("Teleport.Enabled"));
        map.put("Teleport.Warmup.Enabled", configuration.getBoolean("Teleport.Warmup.Enabled"));
        map.put("Teleport.Warmup.Time", configuration.getInt("Teleport.Warmup.Time"));
        map.put("Listeners.Warmup-PlayerMoveEvent.Enabled", configuration.getBoolean("Listeners.Warmup-PlayerMoveEvent.Enabled"));
        map.put("Listeners.Warmup-PlayerMoveEvent.Fail-Message", configuration.getString("Listeners.Warmup-PlayerMoveEvent.Fail-Message"));
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
        return (T) map.get(key);
    }

    /**
     * Will return the value for the given key.
     *
     * @param key the key to get the value for
     * @return the value for the given key
     */
    public Integer getInteger(String key) {
        return get(key, Integer.class);
    }

    /**
     * Will return the value for the given key.
     *
     * @param key the key to get the value for
     * @return the value for the given key
     */
    public Boolean getBoolean(String key) {
        return get(key, Boolean.class);
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
