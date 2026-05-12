package id.seria.cyayoluckrng.rng;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class CounterManager {

    private final Map<UUID, Map<String, Long>> counters = new HashMap<>();

    public void increment(UUID uuid, String tableKey) {
        counters.computeIfAbsent(uuid, k -> new HashMap<>())
                .merge(tableKey.toLowerCase(), 1L, Long::sum);
    }

    public long getCount(UUID uuid, String tableKey) {
        Map<String, Long> pCounters = counters.get(uuid);
        if (pCounters == null) return 0;
        return pCounters.getOrDefault(tableKey.toLowerCase(), 0L);
    }

    public Map<UUID, Map<String, Long>> getAllCounters() {
        return counters;
    }

    public void resetAll(UUID uuid) {
        counters.remove(uuid);
    }

    public void reset(UUID uuid, String tableKey) {
        Map<String, Long> pCounters = counters.get(uuid);
        if (pCounters != null) {
            pCounters.remove(tableKey.toLowerCase());
        }
    }

    public List<Map.Entry<UUID, Long>> getTopForTable(String tableKey, int limit) {
        String key = tableKey.toLowerCase();
        return counters.entrySet().stream()
                .filter(e -> e.getValue().containsKey(key))
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().get(key)))
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void load(File file) {
        if (!file.exists()) return;
        
        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            Map<UUID, Map<String, Long>> tempCounters = new HashMap<>();
            
            for (String uuidStr : yaml.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    Map<String, Long> pCounters = new HashMap<>();
                    ConfigurationSection sec = yaml.getConfigurationSection(uuidStr);
                    if (sec != null) {
                        for (String tableKey : sec.getKeys(false)) {
                            pCounters.put(tableKey.toLowerCase(), sec.getLong(tableKey));
                        }
                    }
                    tempCounters.put(uuid, pCounters);
                } catch (Exception ignored) {}
            }
            
            // Atomic update: Hanya timpa jika data valid terdeteksi
            if (!tempCounters.isEmpty() || yaml.getKeys(false).isEmpty()) {
                this.counters.clear();
                this.counters.putAll(tempCounters);
            }
        } catch (Exception e) {
            System.err.println("[CyayoLuckRNG] Gagal memuat counters.yml: " + e.getMessage());
        }
    }

    public void save(File file) {
        // Jangan timpa file jika data di RAM benar-benar kosong tapi file ada
        if (counters.isEmpty() && file.exists() && file.length() > 0) return;

        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, Map<String, Long>> entry : counters.entrySet()) {
            String uuidStr = entry.getKey().toString();
            for (Map.Entry<String, Long> countEntry : entry.getValue().entrySet()) {
                yaml.set(uuidStr + "." + countEntry.getKey(), countEntry.getValue());
            }
        }
        
        try {
            yaml.save(file);
        } catch (Exception e) {
            System.err.println("[CyayoLuckRNG] Gagal menyimpan counters.yml: " + e.getMessage());
        }
    }
}
