package sk.Adin.Anni.commands;

import sk.Adin.Anni.Anni;
import sk.Adin.Anni.maps.MapLoader;
import sk.Adin.Anni.maps.VoidGenerator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MapCommand implements CommandExecutor {
    private MapLoader loader;
    private Anni plugin;

    public MapCommand(Anni plugin, MapLoader loader) {
        this.plugin = plugin;
        this.loader = loader;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String red = ChatColor.RED.toString();
        final String prefix = plugin.getConfig().getString("prefix").replace("&","§");

        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("edit")) {
                    if (p.hasPermission("anni.map.edit")) {
                        loader.loadMap(args[1]);
                        WorldCreator wc = new WorldCreator(args[1]);
                        wc.generator(new VoidGenerator());
                        Bukkit.createWorld(wc);
                        sender.sendMessage(prefix + " §7Mapa §e" + args[1]
                                + " §7nacitana! Mozes editovat.");
                        if (sender instanceof Player) {
                            sender.sendMessage(prefix + " §7Teleportuju...");
                            World w = Bukkit.getWorld(args[1]);
                            Location loc = w.getSpawnLocation();
                            loc.setY(w.getHighestBlockYAt(loc));
                            ((Player) sender).teleport(loc);
                        }
                    } else sender.sendMessage(prefix + red + " Nemas pravo na tento prikaz!");
                    return true;
                }
                if (args[0].equalsIgnoreCase("save")) {
                    if (p.hasPermission("anni.map.save")) {
                        if (Bukkit.getWorld(args[1]) != null) {
                            Bukkit.getWorld(args[1]).save();
                            final CommandSender s = sender;
                            final String mapName = args[1];
                            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    s.sendMessage(prefix + " §7Mapa §e" + mapName
                                            + " §7ulozena.");
                                    loader.saveMap(mapName);
                                }
                            }, 40L);
                        }
                    } else sender.sendMessage(prefix + red + " Nemas pravo na tento prikaz!");
                    return true;
                }
            }

            sender.sendMessage(prefix + red + " Prikaz: /map <save/edit> <nazov>");
        } else {
            sender.sendMessage(prefix + red + " Error!");
        }
        return true;
    }


}
