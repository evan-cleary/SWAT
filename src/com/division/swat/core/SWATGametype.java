package com.division.swat.core;

import com.division.battlegrounds.core.Gametype;
import com.division.battlegrounds.event.RoundEndEvent;
import com.division.swat.config.SWConfig;
import org.bukkit.Bukkit;

/**
 *
 * @author Evan
 */
public class SWATGametype extends Gametype {

    private int teamRedScore = 0;
    private int teamBlueScore = 0;
    private int winScore = 0;

    public SWATGametype(String format, int args) {
        super(format, args);
    }

    @Override
    public void initGametype() {
        winScore = SWConfig.getScoreLimit();
    }

    @Override
    public void runWinConditions() {
        String teamWin = "";
        int loseScore = 0;
        if (teamRedScore == winScore) {
            teamWin = "Red Team";
            loseScore = teamBlueScore;
        } else if (teamBlueScore == winScore) {
            teamWin = "Blue Team";
            loseScore = teamRedScore;
        }
        if (!teamWin.equals("")) {
            RoundEndEvent evt = new RoundEndEvent(SWAT.instance.getBattleground());
            Bukkit.getServer().getPluginManager().callEvent(evt);
            Bukkit.getServer().broadcastMessage(format(new Object[]{teamWin, winScore, loseScore}));
        }
    }

    public void incrementRedScore() {
        teamRedScore++;
        runWinConditions();
    }

    public void incrementBlueScore() {
        teamBlueScore++;
        runWinConditions();
    }
}
