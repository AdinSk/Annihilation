package sk.Adin.Anni.listeners;

import java.util.HashMap;
import java.util.Random;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand.EnumClientCommand;
import sk.Adin.Anni.Anni;
import sk.Adin.Anni.Util;
import sk.Adin.Anni.api.NexusDamageEvent;
import sk.Adin.Anni.api.NexusDestroyEvent;
import sk.Adin.Anni.bar.ActionAPI;
import sk.Adin.Anni.bar.TitleAPI;
import sk.Adin.Anni.chat.ChatUtil;
import sk.Adin.Anni.manager.PhaseManager;
import sk.Adin.Anni.object.GameTeam;
import sk.Adin.Anni.object.Kit;
import sk.Adin.Anni.object.PlayerMeta;
import sk.Adin.Anni.stats.StatType;

public class PlayerListener implements Listener {
    private final Anni plugin;

    private final HashMap<String, Kit> kitsToGive = new HashMap<>();

    public PlayerListener(Anni plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("static-access")
    @EventHandler
    public void onMOTDPing(ServerListPingEvent e) {
        if (plugin.motd) {
            String motd = plugin.getConfig().getString("motdPhase").replaceAll("%PHASE%", ChatUtil.translateRoman(plugin.getPhase()));
            try {
                if (plugin.getPhase() == 0) {
                	e.setMotd(plugin.getConfig().getString("motdLobby").replace("&", "§"));
                } else {
                motd = motd.replaceAll("%TIME%", plugin.getPhaseManager()
                        .timeString(plugin.getPhaseManager().getTime()));
                motd = motd.replaceAll("%PLAYERCOUNT",
                        String.valueOf(Bukkit.getOnlinePlayers().size()));
                motd = motd.replaceAll("%MAXPLAYERS%",
                        String.valueOf(Bukkit.getMaxPlayers()));
                motd = motd.replaceAll("%GREENNEXUS%",
                        String.valueOf(getNexus(GameTeam.GREEN)));
                motd = motd.replaceAll("%GREENCOUNT%",
                        String.valueOf(getPlayers(GameTeam.GREEN)));
                motd = motd.replaceAll("%REDNEXUS%",
                        String.valueOf(getNexus(GameTeam.RED)));
                motd = motd.replaceAll("%REDCOUNT%",
                        String.valueOf(getPlayers(GameTeam.GREEN)));
                motd = motd.replaceAll("%BLUENEXUS%",
                        String.valueOf(getNexus(GameTeam.BLUE)));
                motd = motd.replaceAll("%BLUECOUNT%",
                        String.valueOf(getPlayers(GameTeam.GREEN)));
                motd = motd.replaceAll("%YELLOWNEXUS%",
                        String.valueOf(getNexus(GameTeam.YELLOW)));
                motd = motd.replaceAll("%YELLOWCOUNT%",
                        String.valueOf(getPlayers(GameTeam.GREEN)));
                e.setMotd(ChatColor.translateAlternateColorCodes('&', motd));
                }
            } catch (Exception ex) {
            }
            
        }
    }
    
    private int getNexus(GameTeam t) {
        int health = 0;

        if (t.getNexus() != null)
            health = t.getNexus().getHealth();

        return health;
    }

    private int getPlayers(GameTeam t) {
        int size = 0;

        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerMeta meta = PlayerMeta.getMeta(p);
            if (meta.getTeam() == t)
                size++;
        }

        return size;
    }

    @SuppressWarnings("deprecation")
	@EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        PlayerMeta pmeta = PlayerMeta.getMeta(player);
        Action a = e.getAction();
        if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
            ItemStack handItem = player.getItemInHand();
            if (handItem != null) {
            	int id = plugin.getConfig().getInt("ItemClassID");
                if (handItem.getType() == Material.getMaterial(id)) {
                    if (handItem.getItemMeta().hasDisplayName()) {
                    	String item = plugin.getConfig().getString("JoinItemClassName").replace("&", "§");
                        if (handItem.getItemMeta().getDisplayName()
                                .contains(item)) {
                        	String namei = plugin.getConfig().getString("TitleClassMenu").replace("&", "§");
                            Util.showClassSelector(e.getPlayer(),
                                    namei);
                            return;
                        }
                    }
                }
                if (handItem.getType() == Material.COMPASS) {
                    boolean setCompass = false;
                    boolean setToNext = false;
                    while (!setCompass) {
                        for (GameTeam team : GameTeam.teams()) {
                            if (setToNext) {
                                ItemMeta meta = handItem.getItemMeta();
                                String kompas = plugin.getConfig().getString("CompassItem").replace("&", "§").replace("%TEAM%", String.valueOf(team.toString())).replaceAll("%COLOR%", String.valueOf(team.color()));
                                meta.setDisplayName(kompas);
                     
                                handItem.setItemMeta(meta);
                                player.setCompassTarget(team.getNexus()
                                        .getLocation());
                                setCompass = true;
                                break;
                            }
                            if (handItem.getItemMeta().getDisplayName()
                                    .contains(team.toString()))
                                setToNext = true;
                        }
                    }
                }
            }
        }

        if (e.getClickedBlock() != null) {
            Material clickedType = e.getClickedBlock().getType();
            if (clickedType == Material.SIGN_POST
                    || clickedType == Material.WALL_SIGN) {
                Sign s = (Sign) e.getClickedBlock().getState();
                if (s.getLine(0).contains(ChatColor.DARK_PURPLE + "[Team]")) {
                    String teamName = ChatColor.stripColor(s.getLine(1));
                    GameTeam team = GameTeam.valueOf(teamName.toUpperCase());
                    if (team != null) {
                        if (pmeta.getTeam() == GameTeam.NONE)
                            plugin.joinTeam(e.getPlayer(), teamName);
                    }
                }
            }
        }
    }
    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
    	Player p = e.getPlayer();
    	if (p.getWorld().getName().contains("lobby")) {
    		e.setCancelled(true);
    	}
    }
    @SuppressWarnings("deprecation")
	@EventHandler
    public void onInteractTeam(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Action a = e.getAction();
        if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
            ItemStack handItem = player.getItemInHand();
            if (handItem != null) {
            	int id = plugin.getConfig().getInt("ItemTeamID");
                if (handItem.getType() == Material.getMaterial(id)) {
                    if (handItem.getItemMeta().hasDisplayName()) {
                    	String item = plugin.getConfig().getString("JoinItemTeamName").replace("&", "§");
                        if (handItem.getItemMeta().getDisplayName().contains(item));
                        	plugin.Team(player);
                        }
                    }
                }
        }
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        PlayerMeta meta = PlayerMeta.getMeta(player);
        if (meta.isAlive()) {
            if (kitsToGive.containsKey(e.getPlayer().getName())) {
                meta.setKit(kitsToGive.get(e.getPlayer().getName()));
                kitsToGive.remove(e.getPlayer().getName());
            }
            e.setRespawnLocation(meta.getTeam().getRandomSpawn());
            meta.getKit().give(player, meta.getTeam());
        } else {
            int id = plugin.getConfig().getInt("ItemClassID");
            e.setRespawnLocation(plugin.getMapManager().getLobbySpawnPoint());
            @SuppressWarnings("deprecation")
			ItemStack selector = new ItemStack(Material.getMaterial(id));
            ItemMeta itemMeta = selector.getItemMeta();
            String item = plugin.getConfig().getString("JoinItemClassName").replace("&", "§");
            itemMeta.setDisplayName(item);
            selector.setItemMeta(itemMeta);
            int slot = plugin.getConfig().getInt("JoinItemClassSlot");
            player.getInventory().setItem(slot-1, selector);
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        if (e.getReason()
                .equals(ChatColor.RED + "ANNIHILATION-TRIGGER-KICK-01")) {
            e.setReason(ChatColor.RED
                    + "Nemozes sa pripajat do tejto fazie!");
            e.setLeaveMessage(null);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
    	String prefix = plugin.getConfig().getString("prefix").replace("&", "§");
    	String msg = plugin.getConfig().getString("JoinTitle").replace("&", "§");
    	String msg1 = plugin.getConfig().getString("JoinSubTitle").replace("&", "§");
        final Player player = e.getPlayer();
        plugin.getPhaseManager();
        player.setGameMode(GameMode.SURVIVAL);
		String msg2 = plugin.getConfig().getString("Footer").replace("&", "§").replaceAll(plugin.getPhase() == 0 ? plugin.getConfig().getString("motdLobby") : "%PHASE%", ChatUtil.translateRoman(plugin.getPhase())).replaceAll("%TIME%", PhaseManager.timeString(plugin.getPhaseManager().getTime()));
        e.setJoinMessage("");
        PlayerMeta meta = PlayerMeta.getMeta(player);
        if (Anni.getInstance().getConfig().getBoolean("EnableTab") == true) {
        	TitleAPI.sendTabTitle(player, String.valueOf(plugin
                    .getMapManager().getCurrentMap() == null ? plugin.getConfig().getString("Voting") : plugin.getConfig()
    				.getString("Header").replaceAll("%MAP%", WordUtils.capitalize(plugin.voting.getWinner()))), String.valueOf(plugin.getPhase() == 0 ? plugin.getConfig().getString("motdLobby") : msg2));
        }
        player.sendMessage(prefix + " §7Vitaj v Annihilation!");
        player.sendMessage(prefix + " §7Plugin developed by §eAdin_SK");
        if (plugin.getPhase() > plugin.lastJoinPhase
                && !player.hasPermission("anni.bypass.phaselimiter")) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    player.kickPlayer((ChatColor.RED + "ANNIHILATION-TRIGGER-KICK-01"));
                }
            }, 1l);
            e.setJoinMessage("");
            return;
        }
        if (Anni.getInstance().getConfig().getBoolean("EnableJoinTitle") == true) {
        	TitleAPI.sendTitle(player, 20, 50, 20, msg, msg1);
        }
        

        if (meta.isAlive())
            player.teleport(meta.getTeam().getRandomSpawn());
        else {
        	player.setGameMode(GameMode.SURVIVAL);
            player.teleport(plugin.getMapManager().getLobbySpawnPoint());
            PlayerInventory inv = player.getInventory();
            inv.setHelmet(null);
            inv.setChestplate(null);
            inv.setLeggings(null);
            inv.setBoots(null);
            player.setHealth(20);
            player.setFoodLevel(20);

            player.getInventory().clear();

            for (PotionEffect effect : player.getActivePotionEffects())
                player.removePotionEffect(effect.getType());

            player.setLevel(0);
            player.setExp(0);
            player.setSaturation(20F);
            int id = plugin.getConfig().getInt("ItemClassID");
            @SuppressWarnings("deprecation")
			ItemStack selector = new ItemStack(Material.getMaterial(id));
            ItemMeta itemMeta = selector.getItemMeta();
            String item = plugin.getConfig().getString("JoinItemClassName").replace("&", "§");
            itemMeta.setDisplayName(item);
            Util.giveTeamSelector(player);
            selector.setItemMeta(itemMeta);
        	int slot = plugin.getConfig().getInt("JoinItemClassSlot");
            player.getInventory().setItem(slot-1, selector);

            player.updateInventory();
        }

        if (plugin.useMysql)
            plugin.getDatabaseHandler()
                    .query("INSERT IGNORE INTO `annihilation` (`username`, `kills`, "
                            + "`deaths`, `wins`, `losses`, `nexus_damage`) VALUES "
                            + "('"
                            + player.getName()
                            + "', '0', '0', '0', '0', '0');");

        if (plugin.getPhase() == 0 && plugin.getVotingManager().isRunning()) {
            plugin.checkStarting();
        }

        plugin.getSignHandler().updateSigns(meta.getTeam());
        plugin.getScoreboardHandler().update();
    }
    
    /**
    *
    * @param event - Resets GameTeam When Player Leaves Lobby
    */
    @EventHandler
    public void leaveRemoveGameTeam(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerMeta meta = PlayerMeta.getMeta(player);
        if (event.getPlayer().getWorld().getName().equals("lobby")) {
            if (meta.isAlive())
            meta.setAlive(false);
                meta.setTeam(GameTeam.NONE);
        plugin.getSignHandler().updateSigns(meta.getTeam());
    }
}

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        final Player p = e.getEntity();

        if (plugin.getPhase() > 0) {
            PlayerMeta meta = PlayerMeta.getMeta(p);
            if (!meta.getTeam().getNexus().isAlive()) {
                meta.setAlive(false);
                for (Player pp : Bukkit.getOnlinePlayers())
                    pp.hidePlayer(p);
            }
        }

        plugin.getStatsManager().setValue(StatType.DEATHS, p,
                plugin.getStatsManager().getStat(StatType.DEATHS, p) + 1);

        if (p.getKiller() != null && !p.getKiller().equals(p)) {
            Player killer = p.getKiller();
            plugin.getStatsManager().incrementStat(StatType.KILLS, killer);
            e.setDeathMessage(ChatUtil.formatDeathMessage(p, p.getKiller(),
                    e.getDeathMessage()));

            if (PlayerMeta.getMeta(killer).getKit() == Kit.BERSERKER) {
                addHeart(killer);
            }
        } else
            e.setDeathMessage(ChatUtil.formatDeathMessage(p,
                    e.getDeathMessage()));
        if (p.getKiller() instanceof Player) {
        	p.getKiller().giveExpLevels(plugin.getConfig().getInt("KillXP"));	
        }

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                PacketPlayInClientCommand in = new PacketPlayInClientCommand(
                        EnumClientCommand.PERFORM_RESPAWN);
                EntityPlayer cPlayer = ((CraftPlayer) p).getHandle();
                cPlayer.playerConnection.a(in);
            }
        }, 1l);
    }
    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent e) {
        final Player player = e.getPlayer();
        PlayerMeta meta = PlayerMeta.getMeta(player);
        final String namei = plugin.getConfig().getString("TitleClassMenu").replace("&", "§");
        player.teleport(meta.getTeam().getRandomSpawn());
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
            	Util.showClassSelector(player, namei);
            }
        }, 20L * 1
        );
        if (!plugin.getPortalPlayers().containsKey(player)) {
            plugin.getPortalPlayers().put(player, player.getName());
        }
    }
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getEntity().getWorld().getName().equals("lobby")) {
                e.setCancelled(true);

                if (e.getCause() == DamageCause.VOID)
                    e.getEntity().teleport(
                            plugin.getMapManager().getLobbySpawnPoint());
            }
        }
    }


    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        if (damager instanceof Player) {
            if (damager.getWorld().getName().equals("lobby")) {
                e.setCancelled(true);
                return;
            }
            if (plugin.getPhase() < 1) {
                e.setCancelled(true);
                return;
            }

            Player attacker = (Player) damager;
            if (PlayerMeta.getMeta(attacker).getKit() == Kit.WARRIOR) {
                ItemStack hand = attacker.getItemInHand();
                if (hand != null) {
                    String lowercaseName = hand.getType().toString()
                            .toLowerCase();
                    if (lowercaseName.contains("sword")
                            || lowercaseName.contains("axe"))
                        e.setDamage(e.getDamage() + 1.0);
                }
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
    	String prefix = plugin.getConfig().getString("prefix").replace("&", "§");
        if (plugin.getPhase() > 0) {
            if (Util.isEmptyColumn(e.getBlock().getLocation()))
                e.setCancelled(true);

            if (tooClose(e.getBlock().getLocation())
                    && !e.getPlayer().hasPermission("anni.buildbypass")) {
                e.getPlayer().sendMessage(prefix + " §cNemozes stavat v blizkosti nexusu!");
                e.setCancelled(true);
            }
        } else {
            if (!e.getPlayer().hasPermission("anni.buildbypass"))
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignPlace(SignChangeEvent e) {
        if (e.getPlayer().hasPermission("anni.buildbypass"))
            if (e.getLine(0).toLowerCase().contains("[Shop]".toLowerCase()))
                e.setLine(0, ChatColor.BLACK + "[Shop]");
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
    	String prefix = plugin.getConfig().getString("prefix").replace("&", "§");
        if (plugin.getPhase() > 0) {
            for (GameTeam t : GameTeam.teams()) {
                if (t.getNexus().getLocation()
                        .equals(e.getBlock().getLocation())) {
                    e.setCancelled(true);
                    if (t.getNexus().isAlive())
                        breakNexus(t, e.getPlayer());
                    return;
                }
            }

            if (tooClose(e.getBlock().getLocation())
                    && !e.getPlayer().hasPermission("anni.buildbypass")
                    && e.getBlock().getType() != Material.ENDER_STONE) {
                e.getPlayer().sendMessage(prefix + " §cNemozes nicit v blizkosti nexusu!");
                e.setCancelled(true);
            }
        } else {
            if (!e.getPlayer().hasPermission("anni.buildbypass"))
                e.setCancelled(true);
        }
    }

    private boolean tooClose(Location loc) {
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        for (GameTeam team : GameTeam.teams()) {
            Location nexusLoc = team.getNexus().getLocation();
            double nX = nexusLoc.getX();
            double nY = nexusLoc.getY();
            double nZ = nexusLoc.getZ();
            if (Math.abs(nX - x) <= plugin.build
                    && Math.abs(nY - y) <= plugin.build
                    && Math.abs(nZ - z) <= plugin.build)
                return true;
        }

        return false;
    }

    private void addHeart(Player player) {
        double maxHealth = player.getMaxHealth();
        if (maxHealth < 30.0) {
            double newMaxHealth = maxHealth + 2.0;
            player.setMaxHealth(newMaxHealth);
            player.setHealth(player.getHealth() + 2.0);
        }
    }

    private void breakNexus(final GameTeam victim, Player breaker) {
    	String prefix = plugin.getConfig().getString("prefix").replace("&", "§");
        final GameTeam attacker = PlayerMeta.getMeta(breaker).getTeam();
        if (victim == attacker)
            breaker.sendMessage(prefix + " §cNemozes nicit svoj nexus!");
        else if (plugin.getPhase() == 1)
            breaker.sendMessage(prefix + " §cNemozes nicit nexus ve phase 1!");
        else {
            plugin.getScoreboardHandler().sb.getTeam(victim.name() + "SB")
                    .setPrefix(ChatColor.RESET.toString());
            victim.getNexus().damage(plugin.getPhase() == 5 ? 2 : 1);

            plugin.getStatsManager().incrementStat(StatType.NEXUS_DAMAGE,
                    breaker, plugin.getPhase() == 5 ? 2 : 1);

            String msg = ChatUtil.nexusBreakMessage(breaker, attacker, victim);
            for (Player p : attacker.getPlayers())
                p.sendMessage(msg);

            plugin.getScoreboardHandler().scores.get(victim.name()).setScore(
                    victim.getNexus().getHealth());
            Bukkit.getServer()
                    .getPluginManager()
                    .callEvent(
                            new NexusDamageEvent(breaker, victim, victim
                                    .getNexus().getHealth()));

            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    plugin.getScoreboardHandler().sb.getTeam(
                            victim.name() + "SB").setPrefix(
                            victim.color().toString());
                }
            }, 2L);

            Random r = new Random();
            float pitch = 0.5F + r.nextFloat() * 0.5F;
            victim.getNexus()
                    .getLocation()
                    .getWorld()
                    .playSound(victim.getNexus().getLocation(),
                            Sound.ANVIL_LAND, 1F, pitch);
            }
      
            if (plugin.getPhase() > 1) {
            Random s = new Random();
            float pitch = 5F + s.nextFloat() * 2F;
            Location nexus = victim.getNexus().getLocation().clone();
            Util.ParticleEffect.FLAME.display(pitch, pitch, pitch, pitch, 10, nexus, pitch);
            Util.ParticleEffect.ENCHANTMENT_TABLE.display(pitch, pitch, pitch, pitch, 10, nexus, pitch);

            if (victim.getNexus().getHealth() == 0) {
                plugin.getScoreboardHandler().sb.resetScores(plugin
                        .getScoreboardHandler().scores.remove(victim.name())
                        .getPlayer());
                Bukkit.getServer().getPluginManager()
                        .callEvent(new NexusDestroyEvent(breaker, victim));
                ChatUtil.nexusDestroyed(attacker, victim, breaker);

                plugin.checkWin();

                for (Player p : victim.getPlayers()) {
                    plugin.getStatsManager().incrementStat(StatType.LOSSES, p);
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                	Util.ParticleEffect.EXPLOSION_NORMAL.display(pitch, pitch, pitch, pitch , 1, nexus, 2);
                    player.getWorld().playSound(player.getLocation(),
                            Sound.EXPLODE, 2F, 1.25F);
                }

                for (final Location spawn : victim.getSpawns()) {
                    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                        @Override
                        public void run() {
                            Util.spawnFirework(spawn,
                                    attacker.getColor(attacker),
                                    attacker.getColor(attacker));
                        }
                    }, new Random().nextInt(20));
                }

                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        Location nexus = victim.getNexus().getLocation()
                                .clone();
                        boolean found = false;
                        int y = 0;

                        while (!found) {
                            y++;

                            Block b = nexus.add(0, 1, 0).getBlock();

                            if (b != null && b.getType() == Material.BEACON)
                                b.setType(Material.AIR);

                            if (y > 10)
                                found = true;
                        }
                    }
                });
            }

            plugin.getSignHandler().updateSigns(victim);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity().getWorld().getName().equals("lobby")) {
            event.setCancelled(true);
            event.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
    	String prefix = plugin.getConfig().getString("prefix").replace("&", "§");
     	String msg1 = prefix + " §7Vybrana classa §r";
    	String msg2 = prefix + " §7Tuto classu obdrzis po respawne!";
        Inventory inv = e.getInventory();
        Player player = (Player) e.getWhoClicked();
        String namei = plugin.getConfig().getString("TitleClassMenu").replace("&", "§");
        if (inv.getTitle().startsWith(namei)) {
            if (e.getCurrentItem().getType() == Material.AIR)
                return;
            player.closeInventory();
            e.setCancelled(true);
            String name = e.getCurrentItem().getItemMeta().getDisplayName();
            PlayerMeta meta = PlayerMeta.getMeta(player);

            if (!Kit.valueOf(ChatColor.stripColor(name).toUpperCase())
                    .isOwnedBy(player)) {
                player.sendMessage(prefix + ChatColor.RED + " Nemas pravomoce na tuto classu!");
                return;
            }

        	String msgT = plugin.getConfig().getString("ClassTitleSelect").replace("&", "§").replace("%CLASS%", (name));
        	String msgST = plugin.getConfig().getString("ClassSubTitleSelect").replace("&", "§").replace("%CLASS%", (name));
            
        	player.sendMessage(msg1 + "§e" + (name));
        	TitleAPI.sendTitle(player, 20, 50, 20, msgT, msgST);
        	player.sendMessage(msg2);
            meta.setKit(Kit.getKit(ChatColor.stripColor(name)));
            if (Anni.getInstance().getConfig().getBoolean("EnableClassTitle") == true) {
            	TitleAPI.sendTitle(player, 20, 50, 20, msgT, msgST);
            }
            if (plugin.getPortalPlayers().containsKey(player)) {
                plugin.getPortalPlayers().remove(player);
                player.setHealth(0D);
            }
        }
    }
}
