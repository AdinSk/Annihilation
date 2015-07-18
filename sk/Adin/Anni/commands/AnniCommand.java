package sk.Adin.Anni.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import sk.Adin.Anni.Anni;

public class AnniCommand implements CommandExecutor {
    private Anni plugin;

    public AnniCommand(Anni plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = plugin.getConfig().getString("prefix").replace("&", "§");
        
        if (args.length == 0) {
            sender.sendMessage(prefix + " §eAnnihilation §cv" + plugin.getDescription().getVersion() + " §7by §ecoasterman10 §7& §estuntguy3000.");
            sender.sendMessage(prefix + " §7Recoded by §eAdin_SK");
            sender.sendMessage("§8=-=-=-=-=-=[ §ePOMOC§8 ]=-=-=-=-=-=");
            sender.sendMessage(prefix + " §e/anni §7- §aZobrazi info o pluginu");
            sender.sendMessage(prefix + " §e/anni start§7- §aStartne hru");
            sender.sendMessage(prefix + " §e/stats §7- §aZobrazi staty");
            sender.sendMessage(prefix + " §e/team §7- §aZobrazi info o teamoch");
            sender.sendMessage(prefix + " §e/team <team> §7- §aPripoji vas do teamu");
        }
        
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("start")) {
                if (sender.hasPermission("anni.command.start")) {
                    if (!plugin.startTimer()) {
                        sender.sendMessage(prefix + " §cHra uz startnuta je!");
                    } else {
                        sender.sendMessage(prefix + " §aHra byla odstartovana!");
                    }
                } else sender.sendMessage(prefix + " §cNemas pravo na tento prikaz!");
            }
        }
        return false;
    }
}
