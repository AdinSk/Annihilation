package sk.Adin.Anni.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TreeMap;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import sk.Adin.Anni.Anni;

public class ConfigManager {
    private static class Configuration {
        private final File configFile;
        private YamlConfiguration config;

        public Configuration(File configFile) {
            this.configFile = configFile;
            config = new YamlConfiguration();
        }

        public YamlConfiguration getConfig() {
            return config;
        }

        public void load() throws IOException, InvalidConfigurationException {
            config.load(configFile);
        }

        public void save() throws IOException {
            config.save(configFile);
        }
    }

    private final Anni plugin;
    private final File configFolder;
    private final TreeMap<String, Configuration> configs = new TreeMap<String, Configuration>(
            String.CASE_INSENSITIVE_ORDER);

    public ConfigManager(Anni plugin) {
        this.plugin = plugin;
        configFolder = plugin.getDataFolder();
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }
    }

    public void loadConfigFile(String filename) {
        loadConfigFiles(filename);
    }

    public void loadConfigFiles(String... filenames) {
        for (String filename : filenames) {
            File configFile = new File(configFolder, filename);
            Configuration config;
            try {
                if (!configFile.exists()) {
                    configFile.createNewFile();
                    InputStream in = plugin.getResource(filename);
                    if (in != null) {
                        try {
                            OutputStream out = new FileOutputStream(configFile);
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }
                            out.close();
                            in.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        plugin.getLogger().warning(
                                "Default configuration for " + filename
                                        + " missing");
                    }
                }
                config = new Configuration(configFile);
                config.load();
                configs.put(filename, config);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    public void save(String filename) {
        if (configs.containsKey(filename)) {
            try {
                configs.get(filename).save();
            } catch (Exception e) {
                printException(e, filename);
            }
        }
    }

    public void reload(String filename) {
        if (configs.containsKey(filename)) {
            try {
                configs.get(filename).load();
            } catch (Exception e) {
                printException(e, filename);
            }
        }
    }

    public YamlConfiguration getConfig(String filename) {
        if (configs.containsKey(filename))
            return configs.get(filename).getConfig();
        else
            return null;
    }

    private void printException(Exception e, String filename) {
        if (e instanceof IOException) {
            plugin.getLogger().severe(
                    "I/O exception while handling " + filename);
        } else if (e instanceof InvalidConfigurationException) {
            plugin.getLogger().severe("Invalid configuration in " + filename);
        }
        e.printStackTrace();
    }
}
