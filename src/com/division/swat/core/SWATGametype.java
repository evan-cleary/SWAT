package com.division.swat.core;

import com.division.battlegrounds.core.BattlegroundCore;
import com.division.battlegrounds.core.Gametype;
import com.division.battlegrounds.event.RoundEndEvent;
import com.division.swat.config.SWConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

/**
 *
 * @author Evan
 */
public class SWATGametype extends Gametype {

    private int teamRedScore = 0;
    private int teamBlueScore = 0;
    private int winScore = 0;
    private Scoreboard scoreboard;
    private OfflinePlayer teamRed = Bukkit.getOfflinePlayer(ChatColor.RED + "Red Team: ");
    private OfflinePlayer teamBlue = Bukkit.getOfflinePlayer(ChatColor.BLUE + "Blue Team: ");

    public SWATGametype(String format, int args) {
        super(format, args);
    }

    @Override
    public void initGametype() {
        this.winScore = SWConfig.getScoreLimit();
        initScoreboard();
    }

    private void initScoreboard() {
        scoreboard = SWAT.instance.getScoreboard();
        scoreboard.registerNewObjective(ChatColor.DARK_GRAY + "Elimination", "teamkills").setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void resetScoreboard() {
        scoreboard.resetScores(teamRed);
        scoreboard.resetScores(teamBlue);
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
        Score score = scoreboard.getObjective(ChatColor.DARK_GRAY + "Elimination").getScore(teamRed);
        score.setScore(teamRedScore);
        BattlegroundCore.getInstance().getScoreboardManager().forceScoreboardUpdate(scoreboard);
        runWinConditions();
    }

    public void incrementBlueScore() {
        teamBlueScore++;
        Score score = scoreboard.getObjective(ChatColor.DARK_GRAY + "Elimination").getScore(teamBlue);
        score.setScore(teamBlueScore);
        BattlegroundCore.getInstance().getScoreboardManager().forceScoreboardUpdate(scoreboard);
        runWinConditions();
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }
}
