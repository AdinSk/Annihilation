package sk.Adin.Anni.maps;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class GameMap {
    private World world;
    private MapLoader mapLoader;

    public GameMap(MapLoader mapLoader) {
        this.mapLoader = mapLoader;
    }

    public boolean loadIntoGame(String worldName) {
        mapLoader.loadMap(worldName);

        WorldCreator wc = new WorldCreator(worldName);
        wc.generator(new VoidGenerator());
        world = Bukkit.createWorld(wc);

        return true;
    }

    public String getName() {
        return world.getName();
    }

    public World getWorld() {
        return world;
    }
}
