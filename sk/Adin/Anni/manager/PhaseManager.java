package sk.Adin.Anni.manager;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import sk.Adin.Anni.Anni;
import sk.Adin.Anni.Util;
import sk.Adin.Anni.bar.ActionAPI;
import sk.Adin.Anni.bar.TitleAPI;
import sk.Adin.Anni.chat.ChatUtil;
import sk.Adin.Anni.object.GameTeam;

public class PhaseManager {
    private long time;
    private final long startTime;
    private final long phaseTime;
    private int phase;
    private boolean isRunning;

    private final Anni plugin;

    private int taskID;

    public PhaseManager(Anni plugin, int start, int period) {
        this.plugin = plugin;
        startTime = start;
        phaseTime = period;
        phase = 0;
    }

    public void start() {
        if (!isRunning) {
            BukkitScheduler scheduler = plugin.getServer().getScheduler();
            taskID = scheduler.scheduleSyncRepeatingTask(plugin,
                    new Runnable() {
                        @Override
                        public void run() {
                            onSecond();
                        }
                    }, 20L, 20L);
            isRunning = true;
        }

        time = -startTime;

        for (Player p : Bukkit.getOnlinePlayers())
            ActionAPI.sendPlayerAnnouncement(p, "§aHra startuje!");

        plugin.getSignHandler().updateSigns(GameTeam.RED);
        plugin.getSignHandler().updateSigns(GameTeam.BLUE);
        plugin.getSignHandler().updateSigns(GameTeam.GREEN);
        plugin.getSignHandler().updateSigns(GameTeam.YELLOW);
    }

    public void stop() {
        if (isRunning) {
            isRunning = false;
            Bukkit.getServer().getScheduler().cancelTask(taskID);
        }
    }

    public void reset() {
        stop();
        time = -startTime;
        phase = 0;
    }

    public long getTime() {
        return time;
    }

    public long getRemainingPhaseTime() {
        if (phase == 5) {
            return phaseTime;
        }
        if (phase >= 1) {
            return time % phaseTime;
        }
        return -time;
    }

    public int getPhase() {
        return phase;
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void onSecond() {
        time++;

        if (getRemainingPhaseTime() == 0) {
            phase++;
            plugin.advancePhase();
        }

        float percent;
        String text;
        String cfg1 = plugin.getConfig().getString("ActionPhase").replace("&", "§").replace("%PHASE%", ChatUtil.translateRoman(phase)).replace("%TIME%", timeString(time));
        String cfg2 = plugin.getConfig().getString("ActionStart").replace("&", "§");
        String text1 = plugin.getConfig().getString("Footer").replace("&", "§").replaceAll(plugin.getPhase() == 0 ? plugin.getConfig().getString("motdLobby") : "%PHASE%", ChatUtil.translateRoman(phase)).replaceAll("%TIME%", timeString(time));

        if (phase == 0) {
            percent = (float) -time / (float) startTime;
            text = cfg2 + -time;
        } else {
            if (phase == 5)
                percent = 1F;
            else
                percent = (float) getRemainingPhaseTime() / (float) phaseTime;
            text = cfg1;

            plugin.getSignHandler().updateSigns(GameTeam.RED);
            plugin.getSignHandler().updateSigns(GameTeam.BLUE);
            plugin.getSignHandler().updateSigns(GameTeam.GREEN);
            plugin.getSignHandler().updateSigns(GameTeam.YELLOW);
        }
        
        for (Player p : Bukkit.getOnlinePlayers())
        	this.Phase(text, text1, p);

        plugin.onSecond();
    }

    public static String timeString(long time) {
        long hours = time / 3600L;
        long minutes = (time - hours * 3600L) / 60L;
        long seconds = time - hours * 3600L - minutes * 60L;
        return String.format(ChatColor.WHITE + "%02d" + ChatColor.GRAY + ":"
                + ChatColor.WHITE + "%02d" + ChatColor.GRAY + ":"
                + ChatColor.WHITE + "%02d", hours, minutes, seconds).replace("-", "");
    }
    public void Phase(String msg1, String msg2, Player p) {
        ActionAPI.sendPlayerAnnouncement(p, msg1);
        if (Anni.getInstance().getConfig().getBoolean("EnableTab") == true) {
		TitleAPI.sendTabTitle(p, String.valueOf(plugin
                .getMapManager().getCurrentMap() == null ? plugin.getConfig().getString("Voting") : plugin.getConfig()
				.getString("Header").replaceAll("%MAP%", WordUtils.capitalize(plugin.voting.getWinner()))), String.valueOf(plugin.getPhase() == 0 ? plugin.getConfig().getString("motdLobby") : msg2));
    }
    }
}
