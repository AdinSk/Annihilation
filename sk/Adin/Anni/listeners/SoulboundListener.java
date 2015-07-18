package sk.Adin.Anni.listeners;

import java.util.Arrays;
import java.util.Iterator;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import sk.Adin.Anni.manager.SoundManager;

public class SoulboundListener implements Listener {
    private static final String soulboundTag = ChatColor.GOLD + "Soulbound";

    @EventHandler
    public void onSoulboundDrop(PlayerDropItemEvent e) {
        if (isSoulbound(e.getItemDrop().getItemStack())) {
            Player p = e.getPlayer();
            SoundManager.playSoundForPlayer(p, Sound.BLAZE_HIT, 1F, 0.25F, 0.5F);
            e.getItemDrop().remove();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Iterator<ItemStack> it = e.getDrops().iterator();
        while (it.hasNext()) {
            if (isSoulbound(it.next()))
                it.remove();
        }
    }

    public static boolean isSoulbound(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (item.hasItemMeta())
            if (meta.hasLore())
                if (meta.getLore().contains(soulboundTag))
                    return true;
        return false;
    }
    
    public static void soulbind(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (!meta.hasLore())
            meta.setLore(Arrays.asList(soulboundTag));
        else
            meta.getLore().add(soulboundTag);
        stack.setItemMeta(meta);
    }
}
