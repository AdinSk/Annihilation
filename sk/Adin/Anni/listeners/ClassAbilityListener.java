package sk.Adin.Anni.listeners;

import java.util.HashMap;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import sk.Adin.Anni.Anni;
import sk.Adin.Anni.object.GameTeam;
import sk.Adin.Anni.object.Kit;
import sk.Adin.Anni.object.PlayerMeta;

public class ClassAbilityListener implements Listener {
    private final HashMap<String, Location> blockLocations = new HashMap<String, Location>();
    private final HashMap<String, Long> cooldowns = new HashMap<String, Long>();
    private final Anni plugin;

    public ClassAbilityListener(Anni plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
            }
        }, 20L, 20L);
    }


    @EventHandler
    public void onScoutGrapple(PlayerFishEvent e) {
        Player player = e.getPlayer();
        player.getItemInHand().setDurability((short) -10);
        // if (e.getState() != State.FISHING && e.getState() != State.IN_GROUND)
        // return;
        if (PlayerMeta.getMeta(player).getKit() != Kit.SCOUT)
            return;
        if (!player.getItemInHand().getItemMeta().getDisplayName()
                .contains("Grapple"))
            return;

        Location hookLoc = e.getHook().getLocation();
        Location playerLoc = player.getLocation();

        double hookX = (int) hookLoc.getX();
        double hookY = (int) hookLoc.getY();
        double hookZ = (int) hookLoc.getZ();

        Material inType = hookLoc.getWorld().getBlockAt(hookLoc).getType();
        if (inType == Material.AIR || inType == Material.WATER
                || inType == Material.LAVA) {
            Material belowType = hookLoc.getWorld()
                    .getBlockAt((int) hookX, (int) (hookY - 0.1), (int) hookZ)
                    .getType();
            if (belowType == Material.AIR || inType == Material.WATER
                    || inType == Material.LAVA)
                return;
        }

        playerLoc.setY(playerLoc.getY() + 0.5);
        player.teleport(playerLoc);

        Vector diff = hookLoc.toVector().subtract(playerLoc.toVector());
        Vector vel = new Vector();
        double d = hookLoc.distance(playerLoc);
        vel.setX((1.0 + 0.07 * d) * diff.getX() / d);
        vel.setY((1.0 + 0.03 * d) * diff.getY() / d + 0.04 * d);
        vel.setZ((1.0 + 0.07 * d) * diff.getZ() / d);
        player.setVelocity(vel);
    }
    @EventHandler
    public void onTakeDamage(EntityDamageByEntityEvent ev)
    {
      if (((ev.getDamager() instanceof Player)) && ((ev.getEntity() instanceof Player))) {
        Player def = (Player)ev.getEntity();
        PlayerMeta defMeta = PlayerMeta.getMeta(def);
        if (defMeta.getKit() == Kit.DEFENDER)
          ev.setDamage(ev.getDamage() / 2.0D);
      }
    }
    @EventHandler
    public void onSnake(PlayerToggleSneakEvent e){
    	if(e.isSneaking()){
            Player player = e.getPlayer();
            Kit kit = PlayerMeta.getMeta(player).getKit();
            if (kit == Kit.SPY) {
            	player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0, true),true);
            }
        }
    }
    @EventHandler
    public void onFallDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;

        Player player = (Player) e.getEntity();
        PlayerMeta meta = PlayerMeta.getMeta(player);
        if (meta.getKit() == Kit.SCOUT && e.getCause() == DamageCause.FALL) {
            if (player.getItemInHand() != null
                    && player.getItemInHand().hasItemMeta()
                    && player.getItemInHand().getItemMeta().hasDisplayName()
                    && player.getItemInHand().getItemMeta().getDisplayName().contains("Grapple"))
                e.setDamage(e.getDamage() / 2.0);
        }
    }
}
