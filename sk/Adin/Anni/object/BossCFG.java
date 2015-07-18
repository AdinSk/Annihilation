package sk.Adin.Anni.object;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import sk.Adin.Anni.Anni;


public class BossCFG {

	public static ItemStack[] BossLoot() {
        File file;
        FileConfiguration config;
        file = new File("plugins/Anni/config.yml");
        config = YamlConfiguration.loadConfiguration(file);
        int nb = config.getInt("Boss-loot.Item-Nb");
        ItemStack[] item = new ItemStack[nb];
        for (int i = 0; i < nb; i++) {
            item[i] = config.getItemStack("Boss-loot.Item" + i);
        }
        return item;
    }
	 public static FileConfiguration ItemStackToConfig(FileConfiguration config, String path, ItemStack[] items) {
	        if (config == null) {
	            return null;
	        }
	        config.set(path + ".Item-Nb", Integer.valueOf(items.length));
	        int i = 0;
	        for (ItemStack it : items) {
	            config.set(path + ".Item" + i, it);

	            i++;
	        }
	        return config;
	    }

	    public static String InventoryToString(Inventory invInventory) {
	        String serialization = invInventory.getSize() + ";";
	        for (int i = 0; i < invInventory.getSize(); i++) {
	            ItemStack is = invInventory.getItem(i);
	            if (is != null) {
	                String serializedItemStack = new String();

	                String isType = String.valueOf(is.getType().getId());
	                serializedItemStack = serializedItemStack + "t@" + isType;
	                if (is.getDurability() != 0) {
	                    String isDurability = String.valueOf(is.getDurability());
	                    serializedItemStack = serializedItemStack + ":d@" + isDurability;
	                }
	                if (is.getAmount() != 1) {
	                    String isAmount = String.valueOf(is.getAmount());
	                    serializedItemStack = serializedItemStack + ":a@" + isAmount;
	                }
	                Map isEnch = is.getEnchantments();
	                Iterator it;
	                if (isEnch.size() > 0) {
	                    for (it = isEnch.entrySet().iterator(); it.hasNext();) {
	                        Map.Entry ench = (Map.Entry) it.next();
	                        serializedItemStack = serializedItemStack + ":e@" + ((Enchantment) ench.getKey()).getId() + "@" + ench.getValue();
	                    }
	                }
	                serialization = serialization + i + "#" + serializedItemStack + ";";
	            }
	        }
	        return serialization;
	    }

	    public static Inventory StringToInventory(String invString) {
	        String[] serializedBlocks = invString.split(";");
	        String invInfo = serializedBlocks[0];
	        Inventory deserializedInventory = Bukkit.getServer().createInventory(null, Integer.valueOf(invInfo).intValue());
	        for (int i = 1; i < serializedBlocks.length; i++) {
	            String[] serializedBlock = serializedBlocks[i].split("#");
	            int stackPosition = Integer.valueOf(serializedBlock[0]).intValue();
	            if (stackPosition < deserializedInventory.getSize()) {
	                ItemStack is = null;
	                Boolean createdItemStack = Boolean.valueOf(false);

	                String[] serializedItemStack = serializedBlock[1].split(":");
	                for (String itemInfo : serializedItemStack) {
	                    String[] itemAttribute = itemInfo.split("@");
	                    if (itemAttribute[0].equals("t")) {
	                        is = new ItemStack(Material.getMaterial(Integer.valueOf(itemAttribute[1]).intValue()));
	                        createdItemStack = Boolean.valueOf(true);
	                    } else if ((itemAttribute[0].equals("d")) && (createdItemStack.booleanValue())) {
	                        is.setDurability(Short.valueOf(itemAttribute[1]).shortValue());
	                    } else if ((itemAttribute[0].equals("a")) && (createdItemStack.booleanValue())) {
	                        is.setAmount(Integer.valueOf(itemAttribute[1]).intValue());
	                    } else if ((itemAttribute[0].equals("e")) && (createdItemStack.booleanValue())) {
	                        is.addEnchantment(Enchantment.getById(Integer.valueOf(itemAttribute[1]).intValue()), Integer.valueOf(itemAttribute[2]).intValue());
	                    }
	                }
	                deserializedInventory.setItem(stackPosition, is);
	            }
	        }
	        return deserializedInventory;
	    
	    }
	}
