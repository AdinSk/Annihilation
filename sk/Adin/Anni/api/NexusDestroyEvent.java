package sk.Adin.Anni.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import sk.Adin.Anni.object.GameTeam;

public class NexusDestroyEvent extends Event {

    private Player p;
    private GameTeam t;
    
    public NexusDestroyEvent(Player p, GameTeam t) {
        this.p = p;
        this.t = t;
    }
    
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return p;
    }
    
    public GameTeam getTeam() {
        return t;
    }
}
