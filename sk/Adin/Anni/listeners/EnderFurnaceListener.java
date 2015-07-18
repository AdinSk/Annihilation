package sk.Adin.Anni.listeners;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryFurnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.TileEntityFurnace;
import sk.Adin.Anni.object.PlayerMeta;
import sk.Adin.Anni.Anni;
import sk.Adin.Anni.object.GameTeam;

public class EnderFurnaceListener implements Listener {
	private HashMap<GameTeam, Location> locations;
	private HashMap<String, VirtualFurnace> furnaces;

	public EnderFurnaceListener(Anni plugin) {
		locations = new HashMap<GameTeam, Location>();
		furnaces = new HashMap<String, VirtualFurnace>();

		Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			public void run() {
				for (VirtualFurnace f : furnaces.values()) {
					try {
						f.c();
					} catch (Exception e) {

					}
				}
			}
		}, 0L, 1L);
	}

	public void setFurnaceLocation(GameTeam team, Location loc) {
		locations.put(team, loc);
	}

	@EventHandler
	public void onFurnaceOpen(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		Block b = e.getClickedBlock();
		if (b.getType() != Material.BURNING_FURNACE) {
			return;
		}

		Location loc = b.getLocation();
		Player player = e.getPlayer();
		GameTeam team = PlayerMeta.getMeta(player).getTeam();
		if (team == null || !locations.containsKey(team)) {
			return;
		}

		if (locations.get(team).equals(loc)) {
			EntityPlayer handle = ((CraftPlayer) player).getHandle();
			handle.openContainer(getFurnace(player));
			player.sendMessage(Anni.getInstance().getConfig().getString("prefix").replace("&", "§") + " §7Tato Ender Pec chrani tvoje veci! Funguje ako EnderFurance.");
		} else if (locations.containsValue(loc)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onFurnaceBreak(BlockBreakEvent e) {
		if (locations.values().contains(e.getBlock().getLocation())) {
			e.setCancelled(true);
		}
	}

	private VirtualFurnace getFurnace(Player player) {
		if (!furnaces.containsKey(player.getName())) {
			EntityPlayer handle = ((CraftPlayer) player).getHandle();
			furnaces.put(player.getName(), new VirtualFurnace(handle));
		}
		return furnaces.get(player.getName());
	}

	private class VirtualFurnace extends TileEntityFurnace {
		public VirtualFurnace(EntityHuman entity) {
			world = entity.world;
		}

		public InventoryHolder getOwner() {
			return new InventoryHolder() {
				public Inventory getInventory() {
					return new CraftInventoryFurnace(VirtualFurnace.this);
				}
			};
		}
	}
}
