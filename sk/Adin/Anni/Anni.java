package sk.Adin.Anni;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Team;

import sk.Adin.Anni.api.GameStartEvent;
import sk.Adin.Anni.api.IconMenu;
import sk.Adin.Anni.api.PhaseChangeEvent;
import sk.Adin.Anni.bar.ActionAPI;
import sk.Adin.Anni.bar.TitleAPI;
import sk.Adin.Anni.chat.ChatListener;
import sk.Adin.Anni.chat.ChatUtil;
import sk.Adin.Anni.commands.AnniCommand;
import sk.Adin.Anni.commands.ClassCommand;
import sk.Adin.Anni.commands.DistanceCommand;
import sk.Adin.Anni.commands.MapCommand;
import sk.Adin.Anni.commands.StatsCommand;
import sk.Adin.Anni.commands.TeamCommand;
import sk.Adin.Anni.commands.TeamShortcutCommand;
import sk.Adin.Anni.commands.VoteCommand;
import sk.Adin.Anni.listeners.BossListener;
import sk.Adin.Anni.listeners.ClassAbilityListener;
import sk.Adin.Anni.listeners.CraftingListener;
import sk.Adin.Anni.listeners.EnderBrewingStandListener;
import sk.Adin.Anni.listeners.EnderChestListener;
import sk.Adin.Anni.listeners.EnderFurnaceListener;
import sk.Adin.Anni.listeners.PlayerListener;
import sk.Adin.Anni.listeners.ResourceListener;
import sk.Adin.Anni.listeners.SoulboundListener;
import sk.Adin.Anni.listeners.WandListener;
import sk.Adin.Anni.listeners.WorldListener;
import sk.Adin.Anni.manager.BossManager;
import sk.Adin.Anni.manager.ConfigManager;
import sk.Adin.Anni.manager.DatabaseManager;
import sk.Adin.Anni.manager.MapManager;
import sk.Adin.Anni.manager.PhaseManager;
import sk.Adin.Anni.manager.RestartHandler;
import sk.Adin.Anni.manager.ScoreboardManager;
import sk.Adin.Anni.manager.SignManager;
import sk.Adin.Anni.manager.VotingManager;
import sk.Adin.Anni.maps.MapLoader;
import sk.Adin.Anni.object.Boss;
import sk.Adin.Anni.object.GameTeam;
import sk.Adin.Anni.object.Kit;
import sk.Adin.Anni.object.PlayerMeta;
import sk.Adin.Anni.object.Shop;
import sk.Adin.Anni.stats.StatType;
import sk.Adin.Anni.stats.StatsManager;


public final class Anni extends JavaPlugin {
    private ConfigManager configManager;
    public VotingManager voting;
    private MapManager maps;
    private PhaseManager timer;
    private ResourceListener resources;
    private EnderFurnaceListener enderFurnaces;
    private EnderBrewingStandListener enderBrewingStands;
    private EnderChestListener enderChests;
    private StatsManager stats;
    private SignManager sign;
    private ScoreboardManager sb;
    private DatabaseManager db;
    private BossManager boss;

    public static HashMap<String, String> messages = new HashMap<>();

    public boolean useMysql = false;
    public boolean updateAvailable = false;
    public boolean motd = true;
    public String newVersion;
    public static Anni anni;
    private HashMap<Player, String> portal;

    public int build = 1;
    public int lastJoinPhase = 2;
    public int respawn = 10;

    public boolean runCommand = false;
    public List<String> commands = new ArrayList<>();

    public String mysqlName = "annihilation";

    public static Anni getInstance() {
        return anni;
    }
    
    @Override
    public void onEnable() {
    	anni = this;
        configManager = new ConfigManager(this);
        configManager.loadConfigFiles("config.yml", "maps.yml", "shops.yml",
                "stats.yml");

        MapLoader mapLoader = new MapLoader(getLogger(), getDataFolder());

        runCommand = getConfig().contains("commandsToRunAtEndGame");

        if (runCommand) {
            commands = getConfig().getStringList("commandsToRunAtEndGame");
        } else commands = null;

        maps = new MapManager(this, mapLoader,
                configManager.getConfig("maps.yml"));

    	Configuration shops = configManager.getConfig("shops.yml");
        new Shop(this, "Weapon", shops);
        new Shop(this, "Brewing", shops);
        stats = new StatsManager(this, configManager);
        resources = new ResourceListener(this);
        enderFurnaces = new EnderFurnaceListener(this);
        enderBrewingStands = new EnderBrewingStandListener(this);
        enderChests = new EnderChestListener();
        sign = new SignManager(this);
        Configuration config = configManager.getConfig("config.yml");
        portal = new HashMap<Player, String>();
        timer = new PhaseManager(this, config.getInt("start-delay"),
                config.getInt("phase-period"));
        voting = new VotingManager(this);
        sb = new ScoreboardManager();
        boss = new BossManager(this);

        PluginManager pm = getServer().getPluginManager();

        sign.loadSigns();

        String sbb = this.getConfig().getString("LobbySB").replace("&", "§");
        sb.resetScoreboard(sbb);

        build = this.getConfig().getInt("build", 5);
        lastJoinPhase = this.getConfig().getInt("lastJoinPhase", 2);
        respawn = this.getConfig().getInt("bossRespawnDelay", 10);

        pm.registerEvents(resources, this);
        pm.registerEvents(enderFurnaces, this);
        pm.registerEvents(new EnderFurnaceListener(this), this);
        pm.registerEvents(enderBrewingStands, this);
        pm.registerEvents(enderChests, this);
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new WorldListener(), this);
        pm.registerEvents(new SoulboundListener(), this);
        pm.registerEvents(new WandListener(this), this);
        pm.registerEvents(new CraftingListener(), this);
        pm.registerEvents(new ClassAbilityListener(this), this);
        pm.registerEvents(new BossListener(this), this);

        getCommand("annihilation").setExecutor(new AnniCommand(this));
        getCommand("class").setExecutor(new ClassCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(stats));
        getCommand("team").setExecutor(new TeamCommand(this));
        getCommand("vote").setExecutor(new VoteCommand(voting));
        getCommand("red").setExecutor(new TeamShortcutCommand());
        getCommand("green").setExecutor(new TeamShortcutCommand());
        getCommand("yellow").setExecutor(new TeamShortcutCommand());
        getCommand("blue").setExecutor(new TeamShortcutCommand());
        getCommand("distance").setExecutor(new DistanceCommand(this));
        getCommand("map").setExecutor(new MapCommand(this, mapLoader));

        if (config.getString("stats").equalsIgnoreCase("sql"))
            useMysql = true;

        motd = config.getBoolean("enableMotd", true);

        if (useMysql) {
            String host = config.getString("MySQL.host");
            Integer port = config.getInt("MySQL.port");
            String name = config.getString("MySQL.name");
            String user = config.getString("MySQL.user");
            String pass = config.getString("MySQL.pass");
            db = new DatabaseManager(host, port, name, user, pass, this);

            db.query("CREATE TABLE IF NOT EXISTS `" + mysqlName + "` ( `username` varchar(16) NOT NULL, "
                    + "`kills` int(16) NOT NULL, `deaths` int(16) NOT NULL, `wins` int(16) NOT NULL, "
                    + "`losses` int(16) NOT NULL, `nexus_damage` int(16) NOT NULL, "
                    + "UNIQUE KEY `username` (`username`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
        } else
            db = new DatabaseManager(this);

        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            VaultHooks.vault = true;
            if (!VaultHooks.instance().setupPermissions()) {
                VaultHooks.vault = false;
                getLogger().warning("Unable to load Vault: No permission plugin found.");
            } else {
                if (!VaultHooks.instance().setupChat()) {
                    VaultHooks.vault = false;
                    getLogger().warning("Unable to load Vault: No chat plugin found.");
                } else {
                    getLogger().info("Vault hook initalized!");
                }
            }
        } else {
            getLogger().warning("Vault not found! Permissions features disabled.");
        }

        reset();

        ChatUtil.setRoman(getConfig().getBoolean("roman", false));
    }

    public boolean startTimer() {
        if (timer.isRunning())
            return false;

        timer.start();

        return true;
    }

    public void loadMap(final String map) {
        FileConfiguration config = configManager.getConfig("maps.yml");
        ConfigurationSection section = config.getConfigurationSection(map);

        World w = getServer().getWorld(map);

        for (GameTeam team : GameTeam.teams()) {
            String name = team.name().toLowerCase();
            if (section.contains("spawns." + name)) {
                for (String s : section.getStringList("spawns." + name))
                    team.addSpawn(Util.parseLocation(getServer().getWorld(map),
                            s));
            }
            if (section.contains("nexuses." + name)) {
                Location loc = Util.parseLocation(w,
                        section.getString("nexuses." + name));
                team.loadNexus(loc, 75);
            }
            if (section.contains("furnaces." + name)) {
                Location loc = Util.parseLocation(w,
                        section.getString("furnaces." + name));
                enderFurnaces.setFurnaceLocation(team, loc);
                loc.getBlock().setType(Material.BURNING_FURNACE);
            }
            if (section.contains("brewingstands." + name)) {
                Location loc = Util.parseLocation(w,
                        section.getString("brewingstands." + name));
                enderBrewingStands.setBrewingStandLocation(team, loc);
                loc.getBlock().setType(Material.BREWING_STAND);
            }
            if (section.contains("enderchests." + name)) {
                Location loc = Util.parseLocation(w,
                        section.getString("enderchests." + name));
                enderChests.setEnderChestLocation(team, loc);
                loc.getBlock().setType(Material.ENDER_CHEST);
            }
        }

        if (section.contains("bosses")) {
            HashMap<String, Boss> bosses = new HashMap<String, Boss>();
            ConfigurationSection sec = section
                    .getConfigurationSection("bosses");
            for (String boss : sec.getKeys(false)) {
                bosses.put(
                        boss,
                        new Boss(boss, sec.getInt(boss + ".hearts") * 2, sec
                                .getString(boss + ".name"), Util.parseLocation(
                                        w, sec.getString(boss + ".spawn")), Util
                                .parseLocation(w,
                                        sec.getString(boss + ".chest"))));
            }
            boss.loadBosses(bosses);
        }

        if (section.contains("diamonds")) {
            Set<Location> diamonds = new HashSet<>();
            for (String s : section.getStringList("diamonds"))
                diamonds.add(Util.parseLocation(w, s));
            resources.loadDiamonds(diamonds);
        }
    }
    public HashMap<Player, String> getPortalPlayers() {
        return portal;
    }
    public void startGame() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (Player pp : Bukkit.getOnlinePlayers()) {
                p.showPlayer(pp);
                pp.showPlayer(p);
            }
        }

        Bukkit.getPluginManager().callEvent(
                new GameStartEvent(maps.getCurrentMap()));
        sb.scores.clear();

        for (OfflinePlayer score : sb.sb.getPlayers())
            sb.sb.resetScores(score);
    	String sbb = this.getConfig().getString("GameSB").replace("&", "§").replace("%MAP%", WordUtils.capitalize(voting.getWinner()));
        sb.obj.setDisplayName(sbb);

        for (GameTeam t : GameTeam.teams()) {
            sb.scores.put(t.name(), sb.obj.getScore(Bukkit
                    .getOfflinePlayer(WordUtils.capitalize(t.name()
                            .toLowerCase() + " Nexus"))));
            sb.scores.get(t.name()).setScore(t.getNexus().getHealth());

            Team sbt = sb.sb.registerNewTeam(t.name() + "SB");
            sbt.addPlayer(Bukkit.getOfflinePlayer(WordUtils
                    .capitalize(WordUtils.capitalize(t.name().toLowerCase()
                            + " Nexus"))));
            sbt.setPrefix(t.color().toString());
        }

        sb.obj.setDisplayName(sbb);

        for (Player p : getServer().getOnlinePlayers())
            if (PlayerMeta.getMeta(p).getTeam() != GameTeam.NONE)
                Util.sendPlayerToGame(p, this);

        sb.update();

        getServer().getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                for (Player p : getServer().getOnlinePlayers()) {
                    if (PlayerMeta.getMeta(p).getKit() == Kit.SCOUT) {
                        PlayerMeta.getMeta(p).getKit().addScoutParticles(p);
                    }
                }
            }
        }, 0L, 1200L);

        getServer().getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                for (GameTeam t : GameTeam.values()) {
                    if (t != GameTeam.NONE && t.getNexus().isAlive()) {
                        Location nexus = t.getNexus().getLocation().clone();
                        nexus.add(0.5, 0, 0.5);
                        Util.ParticleEffect.PORTAL.getName();
                        Util.ParticleEffect.ENCHANTMENT_TABLE.getName();
                    }
                }
            }
        }, 100L, 5L);
    }

    public void advancePhase() {
        ChatUtil.phaseMessage(timer.getPhase());

        if (timer.getPhase() == 2)
        	boss.spawnBosses();
        	

        if (timer.getPhase() == 3)
            resources.spawnDiamonds();

        Bukkit.getPluginManager().callEvent(
                new PhaseChangeEvent(timer.getPhase()));

        getSignHandler().updateSigns(GameTeam.RED);
        getSignHandler().updateSigns(GameTeam.BLUE);
        getSignHandler().updateSigns(GameTeam.GREEN);
        getSignHandler().updateSigns(GameTeam.YELLOW);
    }

    public void onSecond() {
        long time = timer.getTime();
        String prefix = this.getConfig().getString("prefix").replace("&", "§");
        if (time == -5L) {

            String winner = voting.getWinner();
            voting.end();
            getServer().broadcastMessage(prefix + " §7Hlasovani skoncilo!");
            maps.selectMap(winner);
            String msgT = this.getConfig().getString("MapTitle").replace("&", "§").replace("%MAP%", WordUtils.capitalize(winner));
        	String msgST = this.getConfig().getString("MapSubTitle").replace("&", "§").replace("%MAP%", WordUtils.capitalize(winner));
            getServer().broadcastMessage(prefix + " §7Mapa §e" + WordUtils.capitalize(winner) + " §7byla odhlasovana!");
            loadMap(winner);

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (Anni.getInstance().getConfig().getBoolean("EnableMapTitle") == true) {
                	TitleAPI.sendTitle(p, 20, 50, 20, msgT, msgST);
                }
                p.playSound(p.getLocation(), Sound.ANVIL_USE, 1, 1);
            }
        }

        if (time > -5L) {
        	if (this.getPhase() == 0) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 2F);
                
            }
        	}
        }

        if (time == 0L)
            startGame();
    }

    public int getPhase() {
        return timer.getPhase();
    }

    public MapManager getMapManager() {
        return maps;
    }

    public StatsManager getStatsManager() {
        return stats;
    }

    public DatabaseManager getDatabaseHandler() {
        return db;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public int getPhaseDelay() {
        return configManager.getConfig("config.yml").getInt("phase-period");
    }

    public void log(String m, Level l) {
        getLogger().log(l, m);
    }

    public VotingManager getVotingManager() {
        return voting;
    }

    public ScoreboardManager getScoreboardHandler() {
        return sb;
    }

    public void endGame(GameTeam winner) {
        if (winner == null)
            return;

        ChatUtil.winMessage(winner);
        timer.stop();

        for (Player p : getServer().getOnlinePlayers()) {
            if (PlayerMeta.getMeta(p).getTeam() == winner)
                stats.incrementStat(StatType.WINS, p);
        }

        long restartDelay = configManager.getConfig("config.yml").getLong(
                "restart-delay");
        RestartHandler rs = new RestartHandler(this, restartDelay);
        rs.start(timer.getTime(), winner.getColor(winner));
    }

    public void reset() {
    	String sbb = this.getConfig().getString("LobbySB").replace("&", "§");
        sb.resetScoreboard(sbb);
        maps.reset();
        timer.reset();
        PlayerMeta.reset();
        for (Player p : getServer().getOnlinePlayers()) {
            PlayerMeta.getMeta(p).setTeam(GameTeam.NONE);
            p.teleport(maps.getLobbySpawnPoint());
            ActionAPI.sendPlayerAnnouncement(p, ChatColor.GRAY
                    + "Vitej v Anni!");
            p.setMaxHealth(20D);
            p.setHealth(20D);
            p.setFoodLevel(20);
            p.setSaturation(20F);
            int id = this.getConfig().getInt("ItemClassID");
            @SuppressWarnings("deprecation")
			ItemStack selector = new ItemStack(Material.getMaterial(id));
            ItemMeta itemMeta = selector.getItemMeta();
            String item = this.getConfig().getString("JoinItemClassName").replace("&", "§");
            itemMeta.setDisplayName(item);
            Util.giveTeamSelector(p);
            selector.setItemMeta(itemMeta);
            int slot = this.getConfig().getInt("JoinItemClassSlot");
            p.getInventory().setItem(slot-1, selector);

            p.updateInventory();
        }

        voting.start();
        sb.update();

        for (Player p : Bukkit.getOnlinePlayers()) {
            for (Player pp : Bukkit.getOnlinePlayers()) {
                p.showPlayer(pp);
                pp.showPlayer(p);
            }
        }

        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                for (Player p : getServer().getOnlinePlayers()) {
                    PlayerInventory inv = p.getInventory();
                    inv.setHelmet(null);
                    inv.setChestplate(null);
                    inv.setLeggings(null);
                    inv.setBoots(null);

                    p.getInventory().clear();

                    for (PotionEffect effect : p.getActivePotionEffects())
                        p.removePotionEffect(effect.getType());

                    p.setLevel(0);
                    p.setExp(0);
                    p.setSaturation(20F);

                    Anni plugin = Anni.getInstance();
                    int id = plugin.getConfig().getInt("ItemClassID");
                    @SuppressWarnings("deprecation")
        			ItemStack selector = new ItemStack(Material.getMaterial(id));
                    ItemMeta itemMeta = selector.getItemMeta();
                    String item = plugin.getConfig().getString("JoinItemClassName").replace("&", "§");
                    itemMeta.setDisplayName(item);
                    Util.giveTeamSelector(p);
                    selector.setItemMeta(itemMeta);
                    int slot = plugin.getConfig().getInt("JoinItemClassSlot");
                    p.getInventory().setItem(slot-1, selector);

                    p.updateInventory();
                }

                for (GameTeam t : GameTeam.values())
                    if (t != GameTeam.NONE)
                        sign.updateSigns(t);

                checkStarting();
            }
        }, 2L);
    }

    public void checkWin() {
        int alive = 0;
        GameTeam aliveTeam = null;
        for (GameTeam t : GameTeam.teams()) {
            if (t.getNexus().isAlive()) {
                alive++;
                aliveTeam = t;
            }
        }
        if (alive == 1) {
            endGame(aliveTeam);
        }
    }

    public SignManager getSignHandler() {
        return sign;
    }

    public void setSignHandler(SignManager sign) {
        this.sign = sign;
    }

    public void checkStarting() {
        if (!timer.isRunning()) {
            if (Bukkit.getOnlinePlayers().size() >= getConfig().getInt(
                    "requiredToStart"))
                timer.start();
        }
    }

    public BossManager getBossManager() {
        return boss;
    }

    public PhaseManager getPhaseManager() {
        return timer;
    }

    public void listTeams(CommandSender sender) {
        sender.sendMessage("§8============[ §eTeamy §8]============");
        for (GameTeam t : GameTeam.teams()) {
            int size = 0;

            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerMeta meta = PlayerMeta.getMeta(p);
                if (meta.getTeam() == t)
                    size++;
            }

            if (size != 1) {
                sender.sendMessage(t.coloredName() + " §7- " + size + " §6hracu");
            } else {
                sender.sendMessage(t.coloredName() + " §7- " + size + " §6hrac");
            }
        }
        sender.sendMessage("§8==================================");
    }

    public void joinTeam(Player player, String team) {
    	String prefix = this.getConfig().getString("prefix").replace("&", "§");
        PlayerMeta meta = PlayerMeta.getMeta(player);
        if (meta.getTeam() != GameTeam.NONE && !player.hasPermission("anni.bypass.teamlimitor")) {
            player.sendMessage(prefix +  " §cNemozes menit teamy!");
            return;
        }

        GameTeam target;
        try {
            target = GameTeam.valueOf(team.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(prefix + ChatColor.RED + " §cNeznamy team!");
            listTeams(player);
            return;
        }

        if (Util.isTeamTooBig(target)
                && !player.hasPermission("anni.bypass.teamlimit")) {
            player.sendMessage(prefix + " §cTento team je plny!");
            return;
        }

        if (target.getNexus() != null) {
            if (target.getNexus().getHealth() == 0 && getPhase() > 1) {
                player.sendMessage(prefix + " §cTento team ma zniceny nexus!");
                return;
            }
        }

        if (getPhase() > lastJoinPhase
                && !player.hasPermission("anni.bypass.phaselimiter")) {
            player.kickPlayer(prefix + ChatColor.RED
                    + " Nemozes sa pripajat do tejto fazie!");
            return;
        }
        
    	String msg = this.getConfig().getString("JoinTitleTeam").replace("&", "§").replace("%TEAM%", target.coloredName());
    	String msg1 = this.getConfig().getString("JoinSubTitleTeam").replace("&", "§").replace("%TEAM%", target.coloredName());
        
        player.sendMessage(prefix + " §7Zvoleny team §r"
                + target.coloredName());
        if (Anni.getInstance().getConfig().getBoolean("EnableJoinTeamTitle") == true) {
        	TitleAPI.sendTitle(player, 20, 50, 20, msg, msg1);
        }
        meta.setTeam(target);

        getScoreboardHandler().teams.get(team.toUpperCase()).addPlayer(
                player);

        if (getPhase() > 0) {
            Util.sendPlayerToGame(player, this);
        }

        getSignHandler().updateSigns(GameTeam.RED);
        getSignHandler().updateSigns(GameTeam.BLUE);
        getSignHandler().updateSigns(GameTeam.GREEN);
        getSignHandler().updateSigns(GameTeam.YELLOW);
    }
    public void Team(final Player p) {
    	@SuppressWarnings("deprecation")
		IconMenu menu = new IconMenu("§0§l§nVyber Teamu", 9, new IconMenu.OptionClickEventHandler() {
            @Override
            public void onOptionClick(IconMenu.OptionClickEvent event) {
             
            	String yellow = anni.getConfig().getString("YellowName").replace("&", "§");
            	String red = anni.getConfig().getString("RedName").replace("&", "§");
            	String green = anni.getConfig().getString("GreenName").replace("&", "§");
            	String blue = anni.getConfig().getString("BlueName").replace("&", "§");
            	
                String d = event.getName();
                Player p = event.getPlayer();
                event.setWillClose(true);
                anni.joinTeam(p, ChatColor.stripColor(d.toLowerCase()));
                if (d == yellow) {
                	anni.joinTeam(p, ChatColor.stripColor("§eYellow".toLowerCase()));
                }
                if (d == red) {
                	anni.joinTeam(p, ChatColor.stripColor("§cRed".toLowerCase()));
                }
                if (d == green) {
                	anni.joinTeam(p, ChatColor.stripColor("§aGreen".toLowerCase()));
                }
                if (d == blue) {
                	anni.joinTeam(p, ChatColor.stripColor("§9Blue".toLowerCase()));
                }
                
            }
        }, this).setOption(anni.getConfig().getInt("YellowSlot") -1, new ItemStack(Material.WOOL, GameTeam.YELLOW.getPlayers().size(), DyeColor.YELLOW.getData()), anni.getConfig().getString("YellowName").replace("&", "§"), anni.getConfig().getString("YellowLore").replace("&", "§").replaceAll("%YELLOW%", String.valueOf(GameTeam.YELLOW.getPlayers().size())).replace("%SIPKA%", "»")) 
				.setOption(anni.getConfig().getInt("RedSlot") -1, new ItemStack(Material.WOOL, GameTeam.RED.getPlayers().size(), DyeColor.RED.getData()), anni.getConfig().getString("RedName").replace("&", "§"), anni.getConfig().getString("RedLore").replace("&", "§").replaceAll("%RED%", String.valueOf(GameTeam.RED.getPlayers().size())).replace("%SIPKA%", "»"))
				.setOption(anni.getConfig().getInt("GreenSlot") -1, new ItemStack(Material.WOOL, GameTeam.GREEN.getPlayers().size(), DyeColor.GREEN.getData()), anni.getConfig().getString("GreenName").replace("&", "§"), anni.getConfig().getString("GreenLore").replace("&", "§").replaceAll("%GREEN%", String.valueOf(GameTeam.GREEN.getPlayers().size())).replace("%SIPKA%", "»")) 
    			.setOption(anni.getConfig().getInt("BlueSlot") -1, new ItemStack(Material.WOOL, GameTeam.BLUE.getPlayers().size(), DyeColor.BLUE.getData()), anni.getConfig().getString("BlueName").replace("&", "§"), anni.getConfig().getString("BlueLore").replace("&", "§").replaceAll("%BLUE%", String.valueOf(GameTeam.BLUE.getPlayers().size())).replace("%SIPKA%", "»")); 

        menu.open(p);
    }
    }
