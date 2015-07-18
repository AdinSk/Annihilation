package sk.Adin.Anni.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import sk.Adin.Anni.object.Boss;

public class BossSpawnEvent extends Event {

    private Boss b;
    
    public BossSpawnEvent(Boss b) {
        this.b = b;
    }
    
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Boss getBoss() {
        return b;
    }
}
