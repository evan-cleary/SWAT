package com.division.swat.core;

import com.division.battlegrounds.core.BattlegroundCore;
import com.division.battlegrounds.region.RegionFile;
import com.division.swat.config.SWConfig;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

/**
 *
 * @author Evan
 */
public class SWAT extends JavaPlugin {

    private SWConfig config;
    private RegionFile regionFile;
    public static SWAT instance;
    private SWATBattleground swbg;
    private Scoreboard scoreboard;

    @Override
    public void onEnable() {
        this.config = new SWConfig(this);
        this.regionFile = BattlegroundCore.getInstance().getRegionManager().loadRegionFile("SWAT");
        this.regionFile.load();
        this.config.load();
        scoreboard = BattlegroundCore.getInstance().getScoreboardManager().registerNewScoreboard(swbg);
        SWATGametype gametype = new SWATGametype(ChatColor.RED + "[" + ChatColor.DARK_AQUA + "SWAT" + ChatColor.RED + "] " + ChatColor.GREEN + "%s" + ChatColor.GRAY + " has won %s to %s", 3);
        swbg = new SWATBattleground("SWAT", gametype, regionFile.getRegion());
        BattlegroundCore.getInstance().getRegistrar().registerBattleground(this, swbg);
        instance = this;
    }

    public SWATBattleground getBattleground() {
        return swbg;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }
}
