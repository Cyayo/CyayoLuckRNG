package id.seria.cyayoluckrng.rng;

import java.util.*;

/**
 * Counter berapa kali player menerima drop dari suatu table.
 * Counter di-increment 1x per eksekusi /luckrng run (bukan per roll).
 *
 * Placeholders:
 *   %luckrng_count_<table>%              - jumlah run player di table ini
 *   %luckrng_top_<table>_<rank>_name%   - nama player di rank N leaderboard table ini
 *   %luckrng_top_<table>_<rank>_count%  - jumlah run player di rank N
 */
public class CounterManager {

    // uuid -> tableKey -> count
    private final Map<UUID, Map<String, Long>> counters = new HashMap<>();

    public void increment(UUID uuid, String tableKey) {
        counters.computeIfAbsent(uuid, k -> new HashMap<>())
            .merge(tableKey.toLowerCase(), 1L, Long::sum);
    }

    public long getCount(UUID uuid, String tableKey) {
        Map<String, Long> playerMap = counters.get(uuid);
        if (playerMap == null) return 0;
        return playerMap.getOrDefault(tableKey.toLowerCase(), 0L);
    }

    public void reset(UUID uuid, String tableKey) {
        Map<String, Long> playerMap = counters.get(uuid);
        if (playerMap != null) playerMap.remove(tableKey.toLowerCase());
    }

    public void resetAll(UUID uuid) {
        counters.remove(uuid);
    }

    /**
     * Ambil top N player untuk table tertentu.
     * Return: list of [uuid, count] sorted descending
     */
    public List<Map.Entry<UUID, Long>> getTopForTable(String tableKey, int limit) {
        String key = tableKey.toLowerCase();
        List<Map.Entry<UUID, Long>> result = new ArrayList<>();

        for (Map.Entry<UUID, Map<String, Long>> entry : counters.entrySet()) {
            Long count = entry.getValue().get(key);
            if (count != null && count > 0) {
                result.add(new AbstractMap.SimpleEntry<>(entry.getKey(), count));
            }
        }

        result.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        if (result.size() > limit) return result.subList(0, limit);
        return result;
    }

    public Map<UUID, Map<String, Long>> getAllCounters() { return counters; }
}
