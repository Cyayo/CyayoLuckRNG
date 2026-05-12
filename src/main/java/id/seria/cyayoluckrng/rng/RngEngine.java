package id.seria.cyayoluckrng.rng;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import id.seria.cyayoluckrng.CyayoLuckRNG;
import id.seria.cyayoluckrng.config.RngTable;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;

public class RngEngine {
    private final CyayoLuckRNG plugin;
    private final Random random = new Random();
    private final Map<java.util.UUID, Map<String, PendingMessage>> queue = new java.util.HashMap<>();
    private boolean taskStarted = false;

    public RngEngine(CyayoLuckRNG plugin) { this.plugin = plugin; }

    private void startTask() {
        if (taskStarted) return;
        taskStarted = true;
        org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, this::flushQueue, 1L, 1L);
    }

    public void run(Player player, RngTable table) {
        run(player, table, 0.0);
    }

    public void run(Player player, RngTable table, double bonusLuckPercent) {
        double luckBonus = table.usesLuck() ? plugin.getLuckCalculator().getLuckBonus(player, bonusLuckPercent) : 0.0;
        double luckCap   = table.usesLuck() ? plugin.getConfigManager().getLuckCap() : 1.0;

        // Capping the luck bonus itself
        double finalLuckBonus = Math.min(luckBonus, luckCap);

        double ddropVal = plugin.getEventManager().getState().ddrop;
        boolean ddropOn = plugin.getEventManager().getState().ddropActive;
        boolean useDdrop = table.usesDoubleDrop() && ddropOn && ddropVal > 1;
        int itemAmount = useDdrop ? Math.max(1, (int) ddropVal) : 1;

        int rollsMin = table.getRollsMin(), rollsMax = table.getRollsMax();
        int rollCount;
        if (rollsMax <= rollsMin) {
            rollCount = rollsMin;
        } else {
            int r1 = rollsMin + random.nextInt(rollsMax - rollsMin + 1);
            int r2 = rollsMin + random.nextInt(rollsMax - rollsMin + 1);
            rollCount = Math.min(r1, r2);
        }
        rollCount = Math.max(1, rollCount);

        List<RngTable.RngEntry> items = table.getItems();
        if (items.isEmpty()) return;

        RngTable.RngEntry defaultItem = items.get(items.size() - 1);
        List<RngTable.RngEntry> rngItems = items.subList(0, items.size() - 1);

        Map<RngTable.RngEntry, Integer> results = new java.util.LinkedHashMap<>();
        for (int roll = 0; roll < rollCount; roll++) {
            boolean dropped = false;
            for (RngTable.RngEntry entry : rngItems) {
                double eff = entry.base - (entry.base * finalLuckBonus);
                if (random.nextInt(Math.max(1, (int) Math.round(eff))) == 0) {
                    results.put(entry, results.getOrDefault(entry, 0) + itemAmount);
                    dropped = true;
                    break;
                }
            }
            if (!dropped) results.put(defaultItem, results.getOrDefault(defaultItem, 0) + itemAmount);
        }

        startTask();
        results.forEach((entry, totalAmount) -> {
            // Berikan item segera
            String display = giveItem(player, table, entry, totalAmount);
            if (display != null) {
                // Masukkan pesan ke antrian untuk digabung di akhir tick
                queueMessage(player, table, entry, totalAmount, display);
            }
        });

        // Counter increment 1x per run (bukan per roll)
        plugin.getCounterManager().increment(player.getUniqueId(), table.getKey());
    }

    private void queueMessage(Player player, RngTable table, RngTable.RngEntry entry, int amount, String display) {
        java.util.UUID uuid = player.getUniqueId();
        String key = table.getKey() + ":" + entry.hashCode(); // Gunakan hash entry sebagai pembeda unik
        
        queue.computeIfAbsent(uuid, k -> new java.util.HashMap<>())
             .compute(key, (k, v) -> {
                 if (v == null) return new PendingMessage(player, table, entry, amount, display);
                 v.amount += amount;
                 return v;
             });
    }

    private void flushQueue() {
        if (queue.isEmpty()) return;
        
        queue.forEach((uuid, messages) -> {
            Player player = org.bukkit.Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) return;

            messages.values().forEach(msg -> {
                // Pesan ke player
                String rcv = msg.table.getReceiveMessage();
                if (rcv != null && !rcv.isEmpty()) {
                    String finalItemDisplay = (msg.amount > 1) ? msg.amount + "x " + msg.display : msg.display;
                    String text = rcv
                        .replace("{amount}", String.valueOf(msg.amount))
                        .replace("{item}", finalItemDisplay);
                    plugin.getAdventure().player(player).sendMessage(plugin.getConfigManager().parse(text));
                }

                // Broadcast
                if (msg.entry.broadcast != null && !msg.entry.broadcast.isEmpty()) {
                    String bc = msg.entry.broadcast
                        .replace("{player}", player.getName())
                        .replace("{amount}", String.valueOf(msg.amount))
                        .replace("{item}", msg.display);
                    plugin.getAdventure().all().sendMessage(plugin.getConfigManager().parse(bc));
                }
            });
        });
        queue.clear();
    }

    private static class PendingMessage {
        Player player;
        RngTable table;
        RngTable.RngEntry entry;
        int amount;
        String display;

        PendingMessage(Player p, RngTable t, RngTable.RngEntry e, int a, String d) {
            this.player = p; this.table = t; this.entry = e; this.amount = a; this.display = d;
        }
    }

    private String giveItem(Player player, RngTable table, RngTable.RngEntry entry, int amount) {
        switch (table.getType()) {
            case MMOITEMS: return giveMmo(player, entry, amount);
            case VANILLA:  return giveVanilla(player, entry, amount);
            case COMMAND:
                if (runCommand(player, entry, amount)) return entry.display;
                return null;
            default: return null;
        }
    }

    private String giveMmo(Player player, RngTable.RngEntry entry, int amount) {
        try {
            Type type = MMOItems.plugin.getTypes().get(entry.mmoType);
            if (type == null) { warn("Type tidak ditemukan: " + entry.mmoType); return null; }
            MMOItemTemplate tpl = MMOItems.plugin.getTemplates().getTemplate(type, entry.mmoId);
            if (tpl == null) { warn("Template tidak ditemukan: " + entry.mmoType + ":" + entry.mmoId); return null; }
            ItemStack item = new ItemStackBuilder(tpl.newBuilder(0, null).build()).buildNBT().toItem();
            if (item == null) return null;
            item.setAmount(Math.max(1, amount));
            String name = entry.mmoId;
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) name = meta.getDisplayName();
            Map<Integer, ItemStack> lo = player.getInventory().addItem(item);
            for (ItemStack left : lo.values()) player.getWorld().dropItemNaturally(player.getLocation(), left);
            return name;
        } catch (Exception e) { warn("Gagal give MMO " + entry.mmoType + ":" + entry.mmoId + " - " + e.getMessage()); return null; }
    }

    private String giveVanilla(Player player, RngTable.RngEntry entry, int amount) {
        try {
            Material mat = Material.valueOf(entry.material.toUpperCase());
            ItemStack item = new ItemStack(mat, Math.max(1, entry.amount * amount));
            String name = mat.name().toLowerCase().replace("_", " ");
            Map<Integer, ItemStack> lo = player.getInventory().addItem(item);
            for (ItemStack left : lo.values()) player.getWorld().dropItemNaturally(player.getLocation(), left);
            return name;
        } catch (Exception e) { warn("Gagal give vanilla " + entry.material); return null; }
    }

    private boolean runCommand(Player player, RngTable.RngEntry entry, int amount) {
        try {
            for (int i = 0; i < amount; i++) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), entry.command.replace("{player}", player.getName()));
            }
        }
        catch (Exception e) {
            warn("Gagal run command: " + entry.command);
            return false;
        }
        return true;
    }

    private void warn(String msg) { plugin.getLogger().warning(msg); }
}
