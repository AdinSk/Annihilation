package sk.Adin.Anni.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import sk.Adin.Anni.Anni;
import sk.Adin.Anni.object.GameTeam;
import sk.Adin.Anni.object.PlayerMeta;


public class DistanceCommand implements CommandExecutor {
    private Anni plugin;

    public DistanceCommand(Anni instance) {
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
            String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            String prefix = plugin.getConfig().getString("prefix").replace("&", "§");

            if (plugin.getPhase() == 0) {
                p.sendMessage(prefix + " §cHra este nebyla odstartovana!");
                return false;
            }

            if (PlayerMeta.getMeta(p).getTeam() == GameTeam.NONE) {
                p.sendMessage(prefix + " §cNejsi pripojeny v zadnem teame!");
                return false;
            }

            p.sendMessage("§8=========[ §eDistance §8]=========");

            for (GameTeam t : GameTeam.values()) {
                if (t != GameTeam.NONE) {
                    showTeam(p, t);
                }
            }

            p.sendMessage("§8==============================");
        } else {
        	String prefix = plugin.getConfig().getString("prefix").replace("&", "§");
            sender.sendMessage(prefix + " §cTento prikaz moze pisat iba hrac!");
        }

        return true;
    }

    private void showTeam(Player p, GameTeam t) {
        try {
            if (t.getNexus() != null && t.getNexus().getHealth() > 0)
                p.sendMessage(t.coloredName() + ChatColor.GRAY + " Nexus Vzdalenost: " + ChatColor.WHITE + ((int) p.getLocation().distance(t.getNexus().getLocation())) + ChatColor.GRAY + " Bloku");
        } catch (IllegalArgumentException ex) {

        }
    }
}
