package id.seria.cyayoluckrng;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import id.seria.cyayoluckrng.config.ConfigManager;
import id.seria.cyayoluckrng.config.RngTable;
import id.seria.cyayoluckrng.event.EventManager;
import id.seria.cyayoluckrng.event.EventState;
import id.seria.cyayoluckrng.event.JoinListener;
import id.seria.cyayoluckrng.placeholder.LuckPlaceholder;
import id.seria.cyayoluckrng.rng.CounterManager;
import id.seria.cyayoluckrng.rng.LuckCalculator;
import id.seria.cyayoluckrng.rng.RngEngine;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

public class CyayoLuckRNG extends JavaPlugin {
    private static CyayoLuckRNG instance;
    private ConfigManager  configManager;
    private EventManager   eventManager;
    private LuckCalculator luckCalculator;
    private RngEngine      rngEngine;
    private CounterManager counterManager;
    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("messages.yml",               false);
        saveResource("tables/agateore.yml",        false);
        saveResource("tables/agatedeepslate.yml",  false);
        saveResource("tables/agaterawblock.yml",   false);

        configManager  = new ConfigManager(this);
        configManager.load();
        adventure      = BukkitAudiences.create(this);
        eventManager   = new EventManager(this);
        eventManager.startTick();
        eventManager.loadState();
        luckCalculator = new LuckCalculator(this);
        rngEngine      = new RngEngine(this);
        counterManager = new CounterManager();

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        new LuckPlaceholder(this).register();
        getCommand("luckrng").setTabCompleter(new LuckRNGTabCompleter(this));
        getLogger().info("CyayoLuckRNG v2.4.0 aktif!");
    }

    @Override
    public void onDisable() {
        if (adventure != null) { adventure.close(); adventure = null; }
        eventManager.stopTick();
        getLogger().info("CyayoLuckRNG dinonaktifkan.");
    }

    private boolean isConsole(CommandSender s) { return s instanceof ConsoleCommandSender; }
    private boolean hasPerm(CommandSender s, String p) { return isConsole(s) || s.hasPermission(p); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("luckrng")) return false;
        if (args.length == 0) { sendHelp(sender); return true; }
        String sub = args[0].toLowerCase();

        if (sub.equals("run")) {
            if (!hasPerm(sender,"luckrng.run")) { msg(sender, cfg().msg("no-permission")); return true; }
            if (args.length<4) { msg(sender, cfg().color("&cUsage: /luckrng run <table> <bonus luck> <player>")); return true; }
            RngTable table = cfg().getTable(args[1]);
            if (table==null) { msg(sender, cfg().msg("table-not-found","{table}",args[1])); return true; }
            double bonusLuck = parseD(args[2].replace("%", ""));
            if (bonusLuck < 0) bonusLuck = 0;
            Player target = Bukkit.getPlayerExact(args[3]);
            if (target==null) { msg(sender, cfg().msg("player-not-found")); return true; }
            rngEngine.run(target, table, bonusLuck);
            return true;
        }

        if (sub.equals("info")) {
            if (!hasPerm(sender,"luckrng.use")) { msg(sender, cfg().msg("no-permission")); return true; }
            if (!(sender instanceof Player)) { msg(sender, cfg().color("&cHanya player!")); return true; }
            Player p = (Player) sender;
            EventState st = eventManager.getState();
            double rawLuck    = luckCalculator.getRawLuck(p);
            double extra      = luckCalculator.getExtraLuck(p);
            double potionLuck = luckCalculator.parsePapi(p, "%luckrng_luck_potion%");
            double permLuck   = luckCalculator.getPermissionLuck(p);
            double globalLuck = cfg().getDefaultLuck();
            msg(p, cfg().luckinfo("header"));
            msg(p, cfg().luckinfo("title"));
            msg(p, cfg().luckinfo("header"));
            msg(p, cfg().luckinfo("global-luck",  "{value}", fv(globalLuck)));
            msg(p, cfg().luckinfo("extra-luck",   "{value}", fv(extra)));
            msg(p, cfg().luckinfo("permission-luck", "{value}", fv(permLuck)));
            msg(p, cfg().luckinfo("potion-luck", "{value}", fv(potionLuck)));
            msg(p, cfg().luckinfo("event-luck",  "{value}", String.valueOf((int)st.aaluck), "{time}", eventManager.formatTime(st.aaluckTime)));
            msg(p, cfg().luckinfo("vpluck",      "{value}", String.valueOf((int)st.vpluck)));
            msg(p, cfg().luckinfo("multiplier",  "{value}", String.valueOf((int)st.luckMultiplier), "{time}", eventManager.formatTime(st.luckMultiplierTime)));
            msg(p, cfg().luckinfo("divider"));
            msg(p, cfg().luckinfo("total",       "{value}", fv(rawLuck), "{cap}", String.valueOf(cfg().getMaxLuckVar())));
            msg(p, cfg().luckinfo("header"));
            return true;
        }

        if (sub.equals("reload")) {
            if (!hasPerm(sender,"luckrng.reload")) { msg(sender, cfg().msg("no-permission")); return true; }
            configManager.load(); luckCalculator = new LuckCalculator(this);
            msg(sender, cfg().msg("reload-done")); return true;
        }

        if (sub.equals("resetcount")) {
            if (!hasPerm(sender,"luckrng.reload")) { msg(sender, cfg().msg("no-permission")); return true; }
            if (args.length<3) { msg(sender, cfg().color("&cUsage: /luckrng resetcount <table|*> <player>")); return true; }
            Player target = Bukkit.getPlayerExact(args[2]);
            if (target==null) { msg(sender, cfg().msg("player-not-found")); return true; }
            if (args[1].equals("*")) {
                counterManager.resetAll(target.getUniqueId());
                msg(sender, cfg().color("&aSemua counter &e" + target.getName() + " &adireset."));
            } else {
                if (cfg().getTable(args[1])==null) { msg(sender, cfg().msg("table-not-found","{table}",args[1])); return true; }
                counterManager.reset(target.getUniqueId(), args[1]);
                msg(sender, cfg().color("&aCounter &e" + args[1] + " &auntuk &e" + target.getName() + " &adireset."));
            }
            return true;
        }

        if (sub.equals("event")) {
            if (!hasPerm(sender,"luckrng.event")) { msg(sender, cfg().msg("no-permission")); return true; }
            if (args.length<4) { msg(sender, cfg().color("&cUsage: /luckrng event <multiplier> <detik> <penyelenggara>")); return true; }
            double mult=parseD(args[1]); long dur=parseL(args[2]); String org=args[3];
            if (dur<=0) { msg(sender, cfg().msg("duration-zero")); return true; }
            eventManager.startLuckEvent(mult, dur, org); return true;
        }
        if (sub.equals("eventend")) {
            if (!hasPerm(sender,"luckrng.event")) { msg(sender, cfg().msg("no-permission")); return true; }
            if (!eventManager.getState().luckMultiplierActive) { msg(sender, cfg().msg("no-event-luck")); return true; }
            eventManager.stopLuckEvent(); return true;
        }

        if (sub.equals("abuse")) {
            if (!hasPerm(sender,"luckrng.abuse")) { msg(sender, cfg().msg("no-permission")); return true; }
            if (args.length<5) { msg(sender, cfg().color("&cUsage: /luckrng abuse <luck> <detik> <luckcap> <penyelenggara>")); return true; }
            double luck=parseD(args[1]); long dur=parseL(args[2]); double cap=parseD(args[3]); String org=args[4];
            if (dur<=0) { msg(sender, cfg().msg("duration-zero")); return true; }
            if (cap<0.75) { msg(sender, cfg().msg("luckcap-too-low")); return true; }
            if (cap>1.0)  { msg(sender, cfg().msg("luckcap-invalid")); return true; }
            eventManager.startAaLuck(luck, dur, cap, org); return true;
        }
        if (sub.equals("abuseend")) {
            if (!hasPerm(sender,"luckrng.abuse")) { msg(sender, cfg().msg("no-permission")); return true; }
            if (!eventManager.getState().aaluckActive) { msg(sender, cfg().msg("no-event-luck")); return true; }
            eventManager.stopAaLuck(); return true;
        }

        if (sub.equals("drop")) {
            if (!hasPerm(sender,"luckrng.drop")) { msg(sender, cfg().msg("no-permission")); return true; }
            if (args.length<4) { msg(sender, cfg().color("&cUsage: /luckrng drop <multiplier> <detik> <penyelenggara>")); return true; }
            double mult=parseD(args[1]); long dur=parseL(args[2]); String org=args[3];
            if (dur<=0) { msg(sender, cfg().msg("duration-zero")); return true; }
            eventManager.startDoubleDrop(mult, dur, org); return true;
        }
        if (sub.equals("dropend")) {
            if (!hasPerm(sender,"luckrng.drop")) { msg(sender, cfg().msg("no-permission")); return true; }
            if (!eventManager.getState().ddropActive) { msg(sender, cfg().msg("no-event-drop")); return true; }
            eventManager.stopDoubleDrop(); return true;
        }

        if (sub.equals("vp")) {
            if (!hasPerm(sender,"luckrng.vp")) { msg(sender, cfg().msg("no-permission")); return true; }
            eventManager.startVpLuck(); return true;
        }
        if (sub.equals("vpend")) {
            if (!hasPerm(sender,"luckrng.vp")) { msg(sender, cfg().msg("no-permission")); return true; }
            if (!eventManager.getState().vpluckActive) { msg(sender, cfg().msg("no-event-luck")); return true; }
            eventManager.stopVpLuck(); return true;
        }

        sendHelp(sender);
        return true;
    }

    private String fv(double val) {
        double r = Math.round(val*10.0)/10.0;
        if (r==Math.floor(r)) return String.valueOf((int)r);
        return String.format("%.1f",r).replace(".",",");
    }

    private void sendHelp(CommandSender s) {
        msg(s, cfg().color("&e&lCyayoLuckRNG &7Commands:"));
        if (hasPerm(s,"luckrng.use"))    msg(s, cfg().color("  &e/luckrng info"));
        if (hasPerm(s,"luckrng.run"))    msg(s, cfg().color("  &e/luckrng run <table> <bonus luck> <player>"));
        if (hasPerm(s,"luckrng.reload")) msg(s, cfg().color("  &e/luckrng resetcount <table|*> <player>"));
        if (hasPerm(s,"luckrng.event"))  msg(s, cfg().color("  &e/luckrng event <mult> <detik> <org> | /luckrng eventend"));
        if (hasPerm(s,"luckrng.abuse"))  msg(s, cfg().color("  &e/luckrng abuse <luck> <detik> <cap> <org> | /luckrng abuseend"));
        if (hasPerm(s,"luckrng.drop"))   msg(s, cfg().color("  &e/luckrng drop <mult> <detik> <org> | /luckrng dropend"));
        if (hasPerm(s,"luckrng.vp"))     msg(s, cfg().color("  &e/luckrng vp | /luckrng vpend"));
        if (hasPerm(s,"luckrng.reload")) msg(s, cfg().color("  &e/luckrng reload"));
    }

    private void msg(CommandSender s, String str) {
        adventure.sender(s).sendMessage(configManager.parse(str));
    }

    private double parseD(String s) { try{return Double.parseDouble(s);}catch(Exception e){return 0;} }
    private long   parseL(String s) { try{return Long.parseLong(s);}   catch(Exception e){return 0;} }
    private ConfigManager cfg()     { return configManager; }

    public static CyayoLuckRNG getInstance()   { return instance; }
    public ConfigManager  getConfigManager()    { return configManager; }
    public EventManager   getEventManager()     { return eventManager; }
    public LuckCalculator getLuckCalculator()   { return luckCalculator; }
    public RngEngine      getRngEngine()        { return rngEngine; }
    public CounterManager getCounterManager()   { return counterManager; }
    public BukkitAudiences getAdventure()        { return adventure; }
}
