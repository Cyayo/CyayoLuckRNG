package id.seria.cyayoluckrng.rng;

import org.bukkit.entity.Player;

import id.seria.cyayoluckrng.CyayoLuckRNG;
import me.clip.placeholderapi.PlaceholderAPI;

public class LuckCalculator {
    private final CyayoLuckRNG plugin;
    public LuckCalculator(CyayoLuckRNG plugin) { this.plugin=plugin; }

    public double getRawLuck(Player player) {
        double luck = plugin.getConfigManager().getDefaultLuck();
        luck += parsePapi(player, plugin.getConfigManager().getPotionLuckPlaceholder());
        if (plugin.getEventManager().getState().aaluckActive) luck += plugin.getEventManager().getState().aaluck;
        if (plugin.getEventManager().getState().vpluckActive) luck += plugin.getEventManager().getState().vpluck;
        for (String ph : plugin.getConfigManager().getExtraLuckPlaceholders()) luck += parsePapi(player, ph);
        luck += getPermissionLuck(player);
        double mult = plugin.getEventManager().getState().luckMultiplier;
        if (mult>1) luck *= mult;
        return luck;
    }

    public double getLuckBonus(Player player) {
        double luck = getRawLuck(player);
        int max = plugin.getConfigManager().getMaxLuckVar();
        return Math.min(luck, max) / max;
    }

    public double getLuckBonus(Player player, double bonusLuckPercent) {
        double bonusMultiplier = 1.0 + Math.max(0.0, bonusLuckPercent) / 100.0;
        double luck = getRawLuck(player) * bonusMultiplier;
        int max = plugin.getConfigManager().getMaxLuckVar();
        return Math.min(luck, max) / max;
    }

    public double getExtraLuck(Player player) {
        double extra=0;
        for (String ph : plugin.getConfigManager().getExtraLuckPlaceholders()) extra += parsePapi(player, ph);
        return extra;
    }

    public double getPermissionLuck(Player player) {
        double luck = 0;
        for (java.util.Map.Entry<String, Double> entry : plugin.getConfigManager().getPermissionLuck().entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                luck += entry.getValue();
            }
        }
        return luck;
    }

    public double parsePapi(Player player, String ph) {
        try {
            String val = PlaceholderAPI.setPlaceholders(player, ph);
            if (val==null||val.isEmpty()||val.equals(ph)) return 0;
            return Double.parseDouble(val.trim().replace(",","."));
        } catch (Exception e) { return 0; }
    }
}
