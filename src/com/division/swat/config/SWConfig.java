package com.division.swat.config;

import com.division.battlegrounds.config.BattlegroundConfig;
import com.division.battlegrounds.core.BattlegroundCore;
import com.division.swat.core.SWAT;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author Evan
 */
public class SWConfig extends BattlegroundConfig {

    private boolean changed = false;
    private static int scoreLimit;
    private static Location redSpawn;
    private static Location blueSpawn;
    private static int minPlayers;
    private static int maxPlayers;

    public SWConfig(SWAT instance) {
        super(BattlegroundCore.getInstance(), instance.getName());
    }

    @Override
    public void load() {
        try {
            config.load(configFile);
        } catch (Exception ex) {
        }
        if (!config.contains("settings.scorelimit")) {
            config.set("settings.scorelimit", 25);
            changed = true;
        } else {
            scoreLimit = config.getInt("settings.scorelimit");
        }
        if (!config.contains("settings.minplayers")) {
            config.set("settings.minplayers", 5);
            changed = true;
        } else {
            minPlayers = config.getInt("settings.minplayers");
        }
        if (!config.contains("settings.maxplayers")) {
            config.set("settings.maxplayers", 10);
            changed = true;
        } else {
            maxPlayers = config.getInt("settings.maxplayers");
        }
        if (!config.contains("settings.spawns.red")) {
            config.set("settings.spawns.red.world", "DEFAULT");
            config.set("settings.spawns.red.x", "DEFAULT");
            config.set("settings.spawns.red.y", "DEFAULT");
            config.set("settings.spawns.red.z", "DEFAULT");
            changed = true;
        } else {
            World world;
            world = Bukkit.getServer().getWorld(config.getString("settings.spawns.red.world"));
            int x = config.getInt("settings.spawns.red.x");
            int y = config.getInt("settings.spawns.red.y");
            int z = config.getInt("settings.spawns.red.z");
            Location loc = new Location(world, x, y, z);
            redSpawn = loc;
        }

        if (!config.contains("settings.spawns.blue")) {
            config.set("settings.spawns.blue.world", "DEFAULT");
            config.set("settings.spawns.blue.x", "DEFAULT");
            config.set("settings.spawns.blue.y", "DEFAULT");
            config.set("settings.spawns.blue.z", "DEFAULT");
            changed = true;
        } else {
            World world;
            world = Bukkit.getServer().getWorld(config.getString("settings.spawns.blue.world"));
            int x = config.getInt("settings.spawns.blue.x");
            int y = config.getInt("settings.spawns.blue.y");
            int z = config.getInt("settings.spawns.blue.z");
            Location loc = new Location(world, x, y, z);
            blueSpawn = loc;
        }

        if (changed) {
            try {
                changed = false;
                config.save(configFile);
                load();
            } catch (IOException ex) {
            }
        }
    }

    public static int getScoreLimit() {
        return scoreLimit;
    }

    public static Location getBlueSpawn() {
        return blueSpawn;
    }

    public static Location getRedSpawn() {
        return redSpawn;
    }

    public static int getMaxPlayers() {
        return maxPlayers;
    }

    public static int getMinPlayers() {
        return minPlayers;
    }
}
