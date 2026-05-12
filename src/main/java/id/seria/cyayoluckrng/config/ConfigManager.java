package id.seria.cyayoluckrng.config;

import id.seria.cyayoluckrng.CyayoLuckRNG;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ConfigManager {
    private static final Pattern HEX = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private final CyayoLuckRNG plugin;

    private String prefix;
    private int maxLuckVar;
    private double luckCap, luckCapDefault;
    private long vpluckDuration;
    private double vpluckValue;
    private String soundStart, soundStop;
    private List<String> extraLuckPlaceholders = new ArrayList<>();
    private String potionLuckPlaceholder;
    private double defaultLuck;
    private final Map<String, Double> permissionLuck = new HashMap<>();

    private final Map<String,String> messages   = new HashMap<>();
    private final Map<String,String> broadcasts = new HashMap<>();
    private final Map<String,String> bossbars   = new HashMap<>();
    private final List<String> luckinfo        = new ArrayList<>();
    private final Map<String,RngTable> tables   = new LinkedHashMap<>();
    private FileConfiguration statsGuiConfig;

    public ConfigManager(CyayoLuckRNG plugin) { this.plugin = plugin; }

    public void load() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();
        prefix         = cfg.getString("prefix", "&7[&6LuckRNG&7]");
        maxLuckVar     = cfg.getInt("maxluckvar", 1000);
        defaultLuck    = cfg.getDouble("default-luck", 0.0);
        luckCap        = cfg.getDouble("luckcap", 0.75);
        luckCapDefault = luckCap;
        vpluckDuration = cfg.getLong("vpluck-duration", 3600);
        vpluckValue    = cfg.getDouble("vpluck-value", 100);
        soundStart     = cfg.getString("sounds.event-start", "minecraft:ui.toast.challenge_complete");
        soundStop      = cfg.getString("sounds.event-stop", "");
        extraLuckPlaceholders = cfg.getStringList("extra-luck-placeholders");
        potionLuckPlaceholder = cfg.getString("potion-luck-placeholder", "");
        permissionLuck.clear();
        ConfigurationSection permSec = cfg.getConfigurationSection("permission-luck");
        if (permSec != null) {
            for (String key : permSec.getKeys(true)) {
                if (!permSec.isConfigurationSection(key)) {
                    permissionLuck.put(key, permSec.getDouble(key));
                }
            }
            plugin.getLogger().info("Loaded " + permissionLuck.size() + " permission luck entries.");
        }
        loadSection(cfg, "bossbars", bossbars);

        File msgFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!msgFile.exists()) plugin.saveResource("messages.yml", false);
        FileConfiguration msgCfg = YamlConfiguration.loadConfiguration(msgFile);
        loadSection(msgCfg, "broadcasts", broadcasts);
        loadSection(msgCfg, "messages",   messages);
        
        luckinfo.clear();
        luckinfo.addAll(msgCfg.getStringList("luckinfo.text"));

        File guiFile = new File(plugin.getDataFolder(), "stats-gui.yml");
        if (!guiFile.exists()) plugin.saveResource("stats-gui.yml", false);
        statsGuiConfig = YamlConfiguration.loadConfiguration(guiFile);

        tables.clear();
        File dir = new File(plugin.getDataFolder(), "tables");
        if (!dir.exists()) dir.mkdirs();
        File[] files = dir.listFiles();
        if (files != null) {
            Arrays.sort(files);
            for (File f : files) {
                if (!f.getName().endsWith(".yml")) continue;
                String key = f.getName().replace(".yml","").toLowerCase();
                try {
                    RngTable t = loadTable(key, YamlConfiguration.loadConfiguration(f));
                    if (t != null) { tables.put(key, t); plugin.getLogger().info("Loaded table: "+key); }
                } catch (Exception e) { plugin.getLogger().warning("Error table "+f.getName()+": "+e.getMessage()); }
            }
        }
        plugin.getLogger().info("Loaded "+tables.size()+" tables.");
    }

    private void loadSection(FileConfiguration cfg, String section, Map<String,String> map) {
        map.clear();
        ConfigurationSection sec = cfg.getConfigurationSection(section);
        if (sec != null) for (String k : sec.getKeys(false)) map.put(k, sec.getString(k,""));
    }

    private RngTable loadTable(String key, FileConfiguration cfg) {
        String name       = cfg.getString("name", key);
        boolean useLuck   = cfg.getBoolean("use-luck", true);
        boolean useDdrop  = cfg.getBoolean("use-double-drop", true);
        int rollsMin      = cfg.getInt("rolls-min", 1);
        int rollsMax      = cfg.getInt("rolls-max", 1);
        String rcvMsg     = cfg.getString("receive-message", null);
        if (rcvMsg != null && rcvMsg.isEmpty()) rcvMsg = null;

        String typeStr = cfg.getString("type","mmoitems").toLowerCase();
        RngTable.TableType type;
        switch (typeStr) {
            case "vanilla": type=RngTable.TableType.VANILLA; break;
            case "command": type=RngTable.TableType.COMMAND; break;
            default:        type=RngTable.TableType.MMOITEMS; break;
        }

        List<RngTable.RngEntry> items = new ArrayList<>();
        for (Map<?,?> raw : cfg.getMapList("items")) {
            int base = raw.containsKey("base") ? Integer.parseInt(String.valueOf(raw.get("base"))) : 1;
            String bc = raw.containsKey("broadcast") ? String.valueOf(raw.get("broadcast")) : null;
            if (bc != null && bc.isEmpty()) bc = null;

            if (type == RngTable.TableType.MMOITEMS)
                items.add(RngTable.RngEntry.mmoitem(String.valueOf(raw.get("type")), String.valueOf(raw.get("id")), base, bc));
            else if (type == RngTable.TableType.VANILLA) {
                int amt = raw.containsKey("amount") ? Integer.parseInt(String.valueOf(raw.get("amount"))) : 1;
                items.add(RngTable.RngEntry.vanilla(String.valueOf(raw.get("material")), amt, base, bc));
            } else {
                String disp = raw.containsKey("display") ? String.valueOf(raw.get("display")) : String.valueOf(raw.get("command"));
                items.add(RngTable.RngEntry.command(String.valueOf(raw.get("command")), disp, base, bc));
            }
        }
        return items.isEmpty() ? null : new RngTable(key, name, type, useLuck, useDdrop, rollsMin, rollsMax, rcvMsg, items);
    }

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    public String color(String text) {
        if (text == null || text.isEmpty()) return "";

        // Global Fix: Handle multi-line strings
        if (text.contains("\n")) {
            String[] lines = text.split("\n", -1);
            for (int i = 0; i < lines.length; i++) {
                lines[i] = color(lines[i]);
            }
            return String.join("\n", lines);
        }
        return LEGACY_SERIALIZER.serialize(parse(text));
    }

    public Component parse(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        String processed = text;
        processed = processed.replaceAll("[§&]x[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])[§&]([0-9a-fA-F])", "<#$1$2$3$4$5$6>");
        processed = processed.replaceAll("&#([a-fA-F0-9]{6})", "<#$1>");
        processed = processed.replace("§", "&")
                .replace("&0", "<reset><black>").replace("&1", "<reset><dark_blue>").replace("&2", "<reset><dark_green>")
                .replace("&3", "<reset><dark_aqua>").replace("&4", "<reset><dark_red>").replace("&5", "<reset><dark_purple>")
                .replace("&6", "<reset><gold>").replace("&7", "<reset><gray>").replace("&8", "<reset><dark_gray>")
                .replace("&9", "<reset><blue>").replace("&a", "<reset><green>").replace("&b", "<reset><aqua>")
                .replace("&c", "<reset><red>").replace("&d", "<reset><light_purple>").replace("&e", "<reset><yellow>")
                .replace("&f", "<reset><white>")
                .replace("&l", "<bold>").replace("&m", "<strikethrough>")
                .replace("&n", "<underline>").replace("&o", "<italic>").replace("&r", "<reset>")
                .replace("&k", "<obfuscated>");
        try {
            return MINI_MESSAGE.deserialize(processed);
        } catch (Exception e) {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        }
    }
    public String msg(String key, String... kv) { return color(msgRaw(key, kv)); }
    public String broadcast(String key, String... kv) { return color(broadcastRaw(key, kv)); }
    public String bossbar(String key, String... kv) { return color(bossbarRaw(key, kv)); }
    public List<String> getLuckInfo() { return luckinfo; }

    public String msgRaw(String key, String... kv) {
        String out = prefix + " " + messages.getOrDefault(key, "&c[?" + key + "]");
        for (int i = 0; i + 1 < kv.length; i += 2) out = out.replace(kv[i], kv[i + 1]);
        return out;
    }
    public String broadcastRaw(String key, String... kv) {
        String out = broadcasts.getOrDefault(key, key);
        for (int i = 0; i + 1 < kv.length; i += 2) out = out.replace(kv[i], kv[i + 1]);
        return out;
    }
    public String bossbarRaw(String key, String... kv) {
        String out = bossbars.getOrDefault(key, key);
        for (int i = 0; i + 1 < kv.length; i += 2) out = out.replace(kv[i], kv[i + 1]);
        return out;
    }

    public void setLuckCap(double cap) { this.luckCap=cap; }
    public void resetLuckCap()         { this.luckCap=luckCapDefault; }

    public String  getPrefix()                     { return color(prefix); }
    public int     getMaxLuckVar()                 { return maxLuckVar; }
    public double  getLuckCap()                    { return luckCap; }
    public long    getVpluckDuration()             { return vpluckDuration; }
    public double  getVpluckValue()                { return vpluckValue; }
    public String  getSoundStart()                 { return soundStart; }
    public String  getSoundStop()                  { return soundStop; }
    public List<String> getExtraLuckPlaceholders() { return extraLuckPlaceholders; }
    public String  getPotionLuckPlaceholder()      { return potionLuckPlaceholder; }
    public double  getDefaultLuck()                { return defaultLuck; }
    public Map<String, Double> getPermissionLuck() { return permissionLuck; }
    public Map<String,RngTable> getTables()        { return tables; }
    public RngTable getTable(String k)             { return tables.get(k.toLowerCase()); }
    public FileConfiguration getStatsGuiConfig()   { return statsGuiConfig; }
}
