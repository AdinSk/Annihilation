package sk.Adin.Anni.manager;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import sk.Adin.Anni.object.GameTeam;

public class ScoreboardManager {
    public Scoreboard sb;
    public Objective obj;
    
    public HashMap<String, Score> scores = new HashMap<>();
    public HashMap<String, Team> teams = new HashMap<>();
    
    public void update() {
        for (Player p : Bukkit.getOnlinePlayers())
            p.setScoreboard(sb);
    }
    
    public void resetScoreboard(String objName) {
        sb = null;
        obj = null;
        
        scores.clear();
        teams.clear();
        
        for (Player p : Bukkit.getOnlinePlayers())
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        
        sb = Bukkit.getScoreboardManager().getNewScoreboard();
        obj = sb.registerNewObjective("anni", "dummy");
        
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(objName);
        
        setTeam(GameTeam.RED);
        setTeam(GameTeam.BLUE);
        setTeam(GameTeam.GREEN);
        setTeam(GameTeam.YELLOW);
    }
    
    public void setTeam(GameTeam t) {
        teams.put(t.name(), sb.registerNewTeam(t.name()));
        Team sbt = teams.get(t.name());
        sbt.setAllowFriendlyFire(false);
        sbt.setCanSeeFriendlyInvisibles(true);
        sbt.setPrefix(t.color().toString());
    }
}
