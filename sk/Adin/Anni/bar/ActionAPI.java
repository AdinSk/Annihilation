package sk.Adin.Anni.bar;


import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

import org.bukkit.Bukkit;

public class ActionAPI {
 

	public static void sendAnnouncement(Player p, String msg) {
		String s = ChatColor.translateAlternateColorCodes('&', msg);

		IChatBaseComponent icbc = ChatSerializer.a("{\"text\": \"" + s + "\"}");
		PacketPlayOutChat bar = new PacketPlayOutChat(icbc, (byte) 2);
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(bar);
	}

	public static void sendPlayerAnnouncement(Player p, String msg) {
		ActionAPI.sendAnnouncement(p, msg);
	}

	public static void sendServerAnnouncement(String msg) {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			ActionAPI.sendAnnouncement(p, msg);
		}
	}
}
