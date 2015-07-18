package sk.Adin.Anni.listeners;

import java.util.HashMap;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import sk.Adin.Anni.object.PlayerMeta;
import sk.Adin.Anni.Anni;
import sk.Adin.Anni.object.GameTeam;

public class EnderChestListener implements Listener {
	private HashMap<GameTeam, Location> enderchests = new HashMap<GameTeam, Location>();
	private HashMap<String, Inventory> inventories = new HashMap<String, Inventory>();

	@EventHandler
	public void onChestOpen(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		Block b = e.getClickedBlock();
		if (b.getType() != Material.ENDER_CHEST) {
			return;
		}

		Location loc = b.getLocation();
		Player player = e.getPlayer();
		GameTeam team = PlayerMeta.getMeta(player).getTeam();
		if (team == null || !enderchests.containsKey(team)) {
			return;
		}

		if (enderchests.get(team).equals(loc)) {
			e.setCancelled(true);
			openEnderChest(player);
		} else if (enderchests.containsValue(loc)) {
			e.setCancelled(true);
		}
	}

	public void setEnderChestLocation(GameTeam team, Location loc) {
		enderchests.put(team, loc);
	}

	private void openEnderChest(Player player) {
		GameTeam team = PlayerMeta.getMeta(player).getTeam();
		String name = player.getName();
		if (!inventories.containsKey(name)) {
			Inventory inv = Bukkit.createInventory(null, 9, team.color() + "§l" + name);
			inventories.put(name, inv);
			
		}
		player.openInventory(inventories.get(name));
		player.sendMessage(Anni.getInstance().getConfig().getString("prefix").replace("&", "§") + " §7Tato Ender Truhla chrani tvoje veci! Funguje ako EnderChest.");
	}

	@EventHandler
	public void onEnderChestBreak(BlockBreakEvent e) {
		if (enderchests.values().contains(e.getBlock().getLocation())) {
			e.setCancelled(true);
		}
	}
}
