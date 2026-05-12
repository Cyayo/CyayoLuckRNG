package id.seria.cyayoluckrng.placeholder;

import id.seria.cyayoluckrng.CyayoLuckRNG;
import id.seria.cyayoluckrng.event.EventState;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Identifier: luckrng
 *
 * COUNTER:
 *   %luckrng_count_<table>%                - jumlah run table untuk player ini
 *
 * LEADERBOARD (per table):
 *   %luckrng_top_<table>_<rank>_name%      - nama player di rank N (1-10)
 *   %luckrng_top_<table>_<rank>_count%     - jumlah run player di rank N
 *   Contoh: %luckrng_top_agateore_1_name%  -> nama player #1 agateore
 *            %luckrng_top_agateore_1_count% -> jumlah run #1 agateore
 *
 * LUCK:
 *   %luckrng_luck_total%            - total luck visual (1 desimal)
 *   %luckrng_luck_total_capped%     - total luck setelah di-cap
 *   %luckrng_luck_potion%           - luck potion per-player (default 0)
 *   %luckrng_luck_extra%            - luck dari extra-luck-placeholders
 *   %luckrng_drop_multiplier%       - multiplier drop aktif
 *   %luckrng_luckbonus%             - bonus normalized 0.0-1.0 (debug)
 *   %luckrng_luck_multiplier%       - nilai luck multiplier
 *   %luckrng_luck_multiplier_time%  - sisa waktu detik
 *   %luckrng_luck_multiplier_active%
 *   %luckrng_aaluck%                - nilai admin abuse luck
 *   %luckrng_aaluck_time%
 *   %luckrng_aaluck_active%
 *   %luckrng_aaluck_cap%
 *   %luckrng_ddrop%                 - nilai double drop
 *   %luckrng_ddrop_time%
 *   %luckrng_ddrop_active%
 *   %luckrng_vpluck%                - nilai voteparty luck
 *   %luckrng_vpluck_time%
 *   %luckrng_vpluck_active%
 */
public class LuckPlaceholder extends PlaceholderExpansion {
    private final CyayoLuckRNG plugin;
    public LuckPlaceholder(CyayoLuckRNG plugin) { this.plugin = plugin; }

    @Override public String getIdentifier() { return "luckrng"; }
    @Override public String getAuthor()     { return "Cyayo"; }
    @Override public String getVersion()    { return "2.4.0"; }
    @Override public boolean persist()      { return false; }
    @Override public boolean canRegister()  { return true; }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        EventState state = plugin.getEventManager().getState();
        String p = params.toLowerCase();

        // ── Counter per table ─────────────────────────────────
        // %luckrng_count_<table>%
        if (p.startsWith("count_")) {
            if (player == null) return "0";
            String tableKey = p.substring(6);
            return String.valueOf(plugin.getCounterManager().getCount(player.getUniqueId(), tableKey));
        }

        // ── Leaderboard per table ─────────────────────────────
        // %luckrng_top_<table>_<rank>_name%
        // %luckrng_top_<table>_<rank>_count%
        if (p.startsWith("top_")) {
            // format: top_<table>_<rank>_<name|count>
            // split dari belakang: last part = name/count, second last = rank, rest = table
            String[] parts = p.split("_");
            if (parts.length >= 4) {
                String type  = parts[parts.length - 1]; // "name" atau "count"
                String rankStr = parts[parts.length - 2];
                // tableKey = join parts[1..length-3]
                StringBuilder tableBuilder = new StringBuilder();
                for (int i = 1; i <= parts.length - 3; i++) {
                    if (i > 1) tableBuilder.append("_");
                    tableBuilder.append(parts[i]);
                }
                String tableKey = tableBuilder.toString();

                try {
                    int rank = Integer.parseInt(rankStr);
                    if (rank < 1) rank = 1;

                    List<Map.Entry<UUID, Long>> top = plugin.getCounterManager()
                        .getTopForTable(tableKey, rank);

                    if (rank > top.size()) {
                        return type.equals("count") ? "0" : "-";
                    }

                    Map.Entry<UUID, Long> entry = top.get(rank - 1);

                    if (type.equals("count")) {
                        return String.valueOf(entry.getValue());
                    } else {
                        // name — coba ambil dari online player, fallback UUID
                        Player target = Bukkit.getPlayer(entry.getKey());
                        if (target != null) return target.getName();
                        // Offline player name lookup
                        try {
                            return Bukkit.getOfflinePlayer(entry.getKey()).getName();
                        } catch (Exception e) {
                            return entry.getKey().toString().substring(0, 8);
                        }
                    }
                } catch (NumberFormatException e) {
                    return "-";
                }
            }
            return "-";
        }

        // ── Luck placeholders ─────────────────────────────────
        if (player == null) return "0";

        switch (p) {
            case "luck_total": {
                double raw = plugin.getLuckCalculator().getRawLuck(player);
                return fv(raw);
            }
            case "luck_total_capped": {
                double raw = plugin.getLuckCalculator().getRawLuck(player);
                return fv(Math.min(raw, plugin.getConfigManager().getMaxLuckVar()));
            }
            case "luck_potion":   return fv(plugin.getLuckCalculator().parsePapi(player, plugin.getConfigManager().getPotionLuckPlaceholder()));
            case "luck_global":   return fv(plugin.getConfigManager().getDefaultLuck());
            case "luck_permission": return fv(plugin.getLuckCalculator().getPermissionLuck(player));
            case "luck_extra":    return fv(plugin.getLuckCalculator().getExtraLuck(player));
            case "drop_multiplier": return state.ddropActive ? String.valueOf((int) state.ddrop) : "1";
            case "luckbonus":     return String.format("%.4f", plugin.getLuckCalculator().getLuckBonus(player));

            case "luck_multiplier":        return String.valueOf((int) state.luckMultiplier);
            case "luck_multiplier_time":   return String.valueOf(state.luckMultiplierTime);
            case "luck_multiplier_active": return state.luckMultiplierActive ? "true" : "false";

            case "aaluck":        return String.valueOf((int) state.aaluck);
            case "aaluck_time":   return String.valueOf(state.aaluckTime);
            case "aaluck_active": return state.aaluckActive ? "true" : "false";
            case "aaluck_cap":    return state.aaluckActive
                ? String.valueOf(plugin.getConfigManager().getLuckCap()) : "0.75";

            case "ddrop":         return String.valueOf((int) state.ddrop);
            case "ddrop_time":    return String.valueOf(state.ddropTime);
            case "ddrop_active":  return state.ddropActive ? "true" : "false";

            case "vpluck":        return String.valueOf((int) state.vpluck);
            case "vpluck_time":   return String.valueOf(state.vpluckTime);
            case "vpluck_active": return state.vpluckActive ? "true" : "false";

            default: return null;
        }
    }

    /** Format visual: 1 desimal, koma jika tidak bulat */
    private String fv(double val) {
        double r = Math.round(val * 10.0) / 10.0;
        if (r == Math.floor(r)) return String.valueOf((int) r);
        return String.format("%.1f", r).replace(".", ",");
    }
}
