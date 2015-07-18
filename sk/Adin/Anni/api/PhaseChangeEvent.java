package sk.Adin.Anni.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PhaseChangeEvent extends Event {

    private int p;
    
    public PhaseChangeEvent(int p) {
        this.p = p;
    }
    
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public int getNewPhase() {
        return p;
    }
}
