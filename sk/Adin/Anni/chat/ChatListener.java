package sk.Adin.Anni.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import sk.Adin.Anni.Anni;
import sk.Adin.Anni.object.GameTeam;
import sk.Adin.Anni.object.PlayerMeta;

public class ChatListener implements Listener {
    private final Anni plugin;

    public ChatListener(Anni plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent e) {
        Player sender = e.getPlayer();
        PlayerMeta meta = PlayerMeta.getMeta(sender);
        GameTeam team = meta.getTeam();
        boolean isAll = false;
        boolean dead = !meta.isAlive() && plugin.getPhase() > 0;
        String msg = e.getMessage();

        if (e.getMessage().startsWith(plugin.getConfig().getString("AllChatText")) && !e.getMessage().equalsIgnoreCase(plugin.getConfig().getString("AllChatText"))) {
            isAll = true;
            msg = msg.substring(1);
        }

        if (team == GameTeam.NONE)
            isAll = true;

        if (isAll)
            ChatUtil.allMessage(team, sender, msg, dead);
        else
            ChatUtil.teamMessage(team, sender, msg, dead);

        e.setCancelled(true);
    }
}
