package id.seria.cyayoluckrng.gui;

import id.seria.cyayoluckrng.CyayoLuckRNG;
import id.seria.cyayoluckrng.config.RngTable;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class StatsGUI implements InventoryHolder {

    private final CyayoLuckRNG plugin;
    private final Inventory inventory;
    private final Player target;

    public StatsGUI(CyayoLuckRNG plugin, Player target) {
        this.plugin = plugin;
        this.target = target;
        
        FileConfiguration config = plugin.getConfigManager().getStatsGuiConfig();
        String titleFormat = config.getString("title", "&8Player Stats: &b{player}");
        String title = plugin.getConfigManager().color(applyPapi(titleFormat.replace("{player}", target.getName())));
        
        int rows = config.getInt("rows", 3);
        int size = rows * 9;
        
        this.inventory = Bukkit.createInventory(this, size, title);
        
        setupMenu();
    }

    private void setupMenu() {
        FileConfiguration config = plugin.getConfigManager().getStatsGuiConfig();
        List<String> layout = config.getStringList("layout");
        ConfigurationSection itemSec = config.getConfigurationSection("items");
        if (itemSec == null) return;

        for (int r = 0; r < layout.size(); r++) {
            String line = layout.get(r);
            for (int c = 0; c < Math.min(line.length(), 9); c++) {
                int slot = r * 9 + c;
                if (slot >= inventory.getSize()) break;
                
                char symbol = line.charAt(c);
                if (symbol == ' ') continue;

                switch (symbol) {
                    case 'F': inventory.setItem(slot, createItem(itemSec.getConfigurationSection("filler"))); break;
                    case 'D': inventory.setItem(slot, createItem(itemSec.getConfigurationSection("divider"))); break;
                    case 'H': inventory.setItem(slot, createItem(itemSec.getConfigurationSection("head"))); break;
                    case 'L': inventory.setItem(slot, createItem(itemSec.getConfigurationSection("luck"))); break;
                    case 'T': inventory.setItem(slot, createTableItem(itemSec.getConfigurationSection("table"))); break;
                }
            }
        }
    }

    private ItemStack createItem(ConfigurationSection section) {
        if (section == null) return new ItemStack(Material.AIR);
        
        String matStr = section.getString("material", "STONE").toUpperCase();
        if (matStr.equals("PLAYER_HEAD")) matStr = "SKULL_ITEM";
        
        Material mat;
        try { mat = Material.valueOf(matStr); } catch (Exception e) { mat = Material.STONE; }

        ItemStack item;
        if (matStr.equals("SKULL_ITEM")) item = new ItemStack(mat, 1, (short) 3);
        else item = new ItemStack(mat);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = section.getString("name", " ");
            meta.setDisplayName(plugin.getConfigManager().color(applyPapi(applyManual(name))));
            
            List<String> lore = section.getStringList("lore");
            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(plugin.getConfigManager().color(applyPapi(applyManual(line))));
                }
                meta.setLore(coloredLore);
            }

            if (meta instanceof SkullMeta) {
                try { ((SkullMeta) meta).setOwner(target.getName()); } catch (Exception ignored) {}
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createTableItem(ConfigurationSection section) {
        if (section == null) return new ItemStack(Material.AIR);
        ItemStack item = createItem(section);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            String format = section.getString("format", "&7- &e{name}&7: &b{count} runs");
            List<String> originalLore = meta.getLore();
            List<String> finalLore = new ArrayList<>();
            
            if (originalLore != null) {
                for (String line : originalLore) {
                    if (line.contains("{list}")) {
                        boolean foundAny = false;
                        for (RngTable table : plugin.getConfigManager().getTables().values()) {
                            // Mencoba ambil via PAPI dulu
                            String countStr = applyPapi("%luckrng_count_" + table.getKey() + "%");
                            long count = 0;
                            try {
                                count = Long.parseLong(countStr);
                            } catch (Exception e) {
                                // Fallback: ambil langsung dari database internal jika PAPI gagal parsing
                                count = plugin.getCounterManager().getCount(target.getUniqueId(), table.getKey());
                            }
                            
                            if (count > 0) {
                                String entry = format
                                        .replace("{name}", table.getName())
                                        .replace("{key}", table.getKey())
                                        .replace("{count}", String.valueOf(count));
                                
                                finalLore.add(plugin.getConfigManager().color(applyPapi(entry)));
                                foundAny = true;
                            }
                        }
                        if (!foundAny) finalLore.add(plugin.getConfigManager().color("&cTidak ada riwayat run tercatat."));
                    } else {
                        finalLore.add(line);
                    }
                }
            }
            meta.setLore(finalLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String applyManual(String text) {
        if (text == null) return "";
        return text
            .replace("{player}", target.getName())
            .replace("{uuid}", target.getUniqueId().toString())
            .replace("{world}", target.getWorld().getName())
            .replace("{cap}", String.valueOf(plugin.getConfigManager().getMaxLuckVar()));
    }

    private String applyPapi(String text) {
        if (text == null) return "";
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(target, text);
        }
        return text;
    }

    @Override
    public Inventory getInventory() { return inventory; }
}
