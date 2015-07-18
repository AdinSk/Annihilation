package sk.Adin.Anni.listeners;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;

import sk.Adin.Anni.Util;

public class WorldListener implements Listener {
    private static final Set<EntityType> hostileEntityTypes = new HashSet<EntityType>() {
        private static final long serialVersionUID = 42L;
        {
            add(EntityType.SKELETON);
            add(EntityType.CREEPER);
            add(EntityType.ZOMBIE);
            add(EntityType.SPIDER);
            add(EntityType.BAT);
            add(EntityType.ENDERMAN);
            add(EntityType.SLIME);
            add(EntityType.WITCH);
        }
    };

    @EventHandler
    public void onWaterFlow(BlockFromToEvent e) {
        if (Util.isEmptyColumn(e.getToBlock().getLocation()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) {
        if (isHostile(e.getEntityType()))
            e.setCancelled(true);
    }

    private boolean isHostile(EntityType entityType) {
        return hostileEntityTypes.contains(entityType);
    }
}
