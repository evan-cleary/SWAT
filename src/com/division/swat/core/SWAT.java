package com.division.swat.core;

import com.division.battlegrounds.core.BattlegroundCore;
import com.division.battlegrounds.region.RegionFile;
import com.division.swat.config.SWConfig;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Evan
 */
public class SWAT extends JavaPlugin {

    private SWConfig config;
    private RegionFile regionFile;
    public static SWAT instance;
    private SWATBattleground swbg;

    @Override
    public void onEnable() {
        instance = this;
        this.config = new SWConfig(this);
        this.regionFile = BattlegroundCore.getInstance().getRegionManager().loadRegionFile("SWAT");
        this.regionFile.load();
        this.config.load();
        SWATGametype gametype = new SWATGametype(ChatColor.RED + "[" + ChatColor.DARK_AQUA + "SWAT" + ChatColor.RED + "] " + ChatColor.GREEN + "%s" + ChatColor.GRAY + " has won %s to %s", 3);
        swbg = new SWATBattleground("SWAT", gametype, regionFile.getRegion());
        BattlegroundCore.getInstance().getRegistrar().registerBattleground(this, swbg);
    }
    
    public SWATBattleground getBattleground(){
        return swbg;
    }
}
