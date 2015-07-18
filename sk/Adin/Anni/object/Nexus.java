package sk.Adin.Anni.object;

import org.bukkit.Location;
import org.bukkit.Material;

public class Nexus {
    private final GameTeam team;
    private final Location location;
    private int health;

    public Nexus(GameTeam team, Location location, int health) {
        this.team = team;
        this.location = location;
        this.health = health;

        location.getBlock().setType(Material.ENDER_STONE);
    }

    public GameTeam getTeam() {
        return team;
    }

    public Location getLocation() {
        return location;
    }

    public int getHealth() {
        return health;
    }

    public void damage(int amount) {
        health -= amount;
        if (health <= 0) {
            health = 0;
            location.getBlock().setType(Material.BEDROCK);
        }
    }

    public boolean isAlive() {
        return health > 0;
    }
}
