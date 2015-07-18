package sk.Adin.Anni.commands;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import sk.Adin.Anni.Anni;
import sk.Adin.Anni.stats.StatType;
import sk.Adin.Anni.stats.StatsManager;


public class StatsCommand implements CommandExecutor {
    private StatsManager manager;

    public StatsCommand(StatsManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
            String[] args) {
        if (sender instanceof Player) {
            if (args.length > 0) {
                List<StatType> types = new LinkedList<StatType>(
                        Arrays.asList(StatType.values()));
                Iterator<StatType> iterator = types.iterator();
                while (iterator.hasNext()) {
                    StatType type = iterator.next();
                    boolean keep = false;
                    for (String arg : args) {
                        if (type.name().toLowerCase()
                                .contains(arg.toLowerCase()))
                            keep = true;
                    }
                    if (!keep)
                        iterator.remove();
                }
                listStats((Player) sender,
                        types.toArray(new StatType[types.size()]));
            } else {
                listStats((Player) sender);
            }
        } else {
            //sender.sendMessage(ChatColor.GOLD + _("CRAFTERNEXUS_PREFIX") + ChatColor.RED + _("ERROR_CONSOLE_PLAYERCOMMAND"));
        }

        return true;
    }

    private void listStats(Player player) {
        listStats(player, StatType.values());
    }

    private void listStats(Player player, StatType[] stats) {

        player.sendMessage("§8=========[ §eStats §8]=========");

        for (StatType stat : stats) {
            if (stat == null)
                continue;
            String name = WordUtils.capitalize(stat.name().toLowerCase()
                    .replace('_', ' '));

            player.sendMessage("§a" + name + ": §c"
                    + manager.getStat(stat, player));
        }
        player.sendMessage("§8===========================");
    }
}
