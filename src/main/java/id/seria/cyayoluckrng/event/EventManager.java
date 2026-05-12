package id.seria.cyayoluckrng.event;

import id.seria.cyayoluckrng.CyayoLuckRNG;
import id.seria.cyayoluckrng.config.ConfigManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

public class EventManager {
    private final CyayoLuckRNG plugin;
    private final EventState state;
    private BukkitTask tickTask;
    private BossBar luckBar, aaluckBar, ddropBar, vpluckBar;

    public EventManager(CyayoLuckRNG plugin) { this.plugin=plugin; this.state=new EventState(); }

    public void startTick() {
        tickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }
    public void stopTick() { if (tickTask!=null) tickTask.cancel(); saveState(); removeAll(); }

    private void tick() {
        if (state.luckMultiplierActive) { state.luckMultiplierTime--;
            if (state.luckMultiplierTime>0) updateBar(luckBar, bb("luck-multiplier",fmt(state.luckMultiplier),ft(state.luckMultiplierTime)), (float) ((double)state.luckMultiplierTime/state.luckMultiplierTotal));
            else { bc("luck-end","{value}",fmt(state.luckMultiplier)); removeBar(luckBar); luckBar=null; state.luckMultiplier=0; state.luckMultiplierTime=0; state.luckMultiplierTotal=0; state.luckMultiplierActive=false; } }
        if (state.aaluckActive) { state.aaluckTime--;
            if (state.aaluckTime>0) updateBar(aaluckBar, bb("admin-abuse",fmt(state.aaluck),ft(state.aaluckTime)), (float) ((double)state.aaluckTime/state.aaluckTotal));
            else { bc("aaluck-end","{value}",fmt(state.aaluck)); removeBar(aaluckBar); aaluckBar=null; state.aaluck=0; state.aaluckTime=0; state.aaluckTotal=0; state.aaluckActive=false; plugin.getConfigManager().resetLuckCap(); } }
        if (state.ddropActive) { state.ddropTime--;
            if (state.ddropTime>0) updateBar(ddropBar, bb("double-drop",fmt(state.ddrop),ft(state.ddropTime)), (float) ((double)state.ddropTime/state.ddropTotal));
            else { bc("ddrop-end","{value}",fmt(state.ddrop)); removeBar(ddropBar); ddropBar=null; state.ddrop=0; state.ddropTime=0; state.ddropTotal=0; state.ddropActive=false; } }
        if (state.vpluckActive) { state.vpluckTime--;
            if (state.vpluckTime>0) updateBar(vpluckBar, bb("vpluck",fmt(state.vpluck),ft(state.vpluckTime)), (float) ((double)state.vpluckTime/state.vpluckTotal));
            else { bc("vpluck-end"); removeBar(vpluckBar); vpluckBar=null; state.vpluck=0; state.vpluckTime=0; state.vpluckTotal=0; state.vpluckActive=false; } }
    }

    public void startLuckEvent(double mult, long dur, String org) {
        if (state.luckMultiplierActive) { state.luckMultiplierTime+=dur; state.luckMultiplierTotal+=dur; Bukkit.broadcastMessage(cfg().broadcast("luck-extend","{organizer}",org,"{time}",ft(dur),"{value}",fmt(state.luckMultiplier))); }
        else { state.luckMultiplier=mult; state.luckMultiplierTime=dur; state.luckMultiplierTotal=dur; state.luckMultiplierActive=true; Bukkit.broadcastMessage(cfg().broadcast("luck-start","{organizer}",org,"{value}",fmt(mult),"{time}",ft(dur))); }
        luckBar=makeBar(luckBar, bb("luck-multiplier",fmt(state.luckMultiplier),ft(state.luckMultiplierTime)), barColor("bossbar-luck-color")); playStart();
    }
    public void stopLuckEvent() { Bukkit.broadcastMessage(cfg().broadcast("luck-stop-admin","{value}",fmt(state.luckMultiplier))); removeBar(luckBar); luckBar=null; state.luckMultiplier=0; state.luckMultiplierTime=0; state.luckMultiplierTotal=0; state.luckMultiplierActive=false; playStop(); }

    public void startAaLuck(double luck, long dur, double cap, String org) {
        if (state.aaluckActive) { state.aaluckTime+=dur; state.aaluckTotal+=dur; Bukkit.broadcastMessage(cfg().broadcast("aaluck-extend","{organizer}",org,"{time}",ft(dur),"{value}",fmt(state.aaluck))); }
        else { state.aaluck=luck; state.aaluckTime=dur; state.aaluckTotal=dur; state.aaluckActive=true; plugin.getConfigManager().setLuckCap(cap); Bukkit.broadcastMessage(cfg().broadcast("aaluck-start","{organizer}",org,"{value}",fmt(luck),"{time}",ft(dur))); }
        aaluckBar=makeBar(aaluckBar, bb("admin-abuse",fmt(state.aaluck),ft(state.aaluckTime)), barColor("bossbar-luck-color")); playStart();
    }
    public void stopAaLuck() { Bukkit.broadcastMessage(cfg().broadcast("aaluck-stop-admin","{value}",fmt(state.aaluck))); removeBar(aaluckBar); aaluckBar=null; state.aaluck=0; state.aaluckTime=0; state.aaluckTotal=0; state.aaluckActive=false; plugin.getConfigManager().resetLuckCap(); playStop(); }

    public void startDoubleDrop(double mult, long dur, String org) {
        if (state.ddropActive) { state.ddropTime+=dur; state.ddropTotal+=dur; Bukkit.broadcastMessage(cfg().broadcast("ddrop-extend","{organizer}",org,"{time}",ft(dur),"{value}",fmt(state.ddrop))); }
        else { state.ddrop=mult; state.ddropTime=dur; state.ddropTotal=dur; state.ddropActive=true; Bukkit.broadcastMessage(cfg().broadcast("ddrop-start","{organizer}",org,"{value}",fmt(mult),"{time}",ft(dur))); }
        ddropBar=makeBar(ddropBar, bb("double-drop",fmt(state.ddrop),ft(state.ddropTime)), barColor("bossbar-drop-color")); playStart();
    }
    public void stopDoubleDrop() { Bukkit.broadcastMessage(cfg().broadcast("ddrop-stop-admin","{value}",fmt(state.ddrop))); removeBar(ddropBar); ddropBar=null; state.ddrop=0; state.ddropTime=0; state.ddropTotal=0; state.ddropActive=false; playStop(); }

    public void startVpLuck() {
        long dur=cfg().getVpluckDuration(); double val=cfg().getVpluckValue();
        if (state.vpluckActive) { state.vpluckTime+=dur; state.vpluckTotal+=dur; Bukkit.broadcastMessage(cfg().broadcast("vpluck-extend","{time}",ft(dur))); }
        else { state.vpluck=val; state.vpluckTime=dur; state.vpluckTotal=dur; state.vpluckActive=true; Bukkit.broadcastMessage(cfg().broadcast("vpluck-start","{value}",fmt(val),"{time}",ft(dur))); }
        vpluckBar=makeBar(vpluckBar, bb("vpluck",fmt(state.vpluck),ft(state.vpluckTime)), barColor("bossbar-vpluck-color")); playStart();
    }
    public void stopVpLuck() { Bukkit.broadcastMessage(cfg().broadcast("vpluck-end")); removeBar(vpluckBar); vpluckBar=null; state.vpluck=0; state.vpluckTime=0; state.vpluckTotal=0; state.vpluckActive=false; playStop(); }

    public void restoreBossBars(Player p) {
        if (state.luckMultiplierActive && luckBar!=null)  plugin.getAdventure().player(p).showBossBar(luckBar);
        if (state.aaluckActive         && aaluckBar!=null) plugin.getAdventure().player(p).showBossBar(aaluckBar);
        if (state.ddropActive          && ddropBar!=null)  plugin.getAdventure().player(p).showBossBar(ddropBar);
        if (state.vpluckActive         && vpluckBar!=null) plugin.getAdventure().player(p).showBossBar(vpluckBar);
    }

    private BossBar makeBar(BossBar ex, Component title, BossBar.Color color) {
        if (ex!=null) { ex.name(title); ex.progress(1.0f); return ex; }
        BossBar bar = BossBar.bossBar(title, 1.0f, color, BossBar.Overlay.PROGRESS);
        plugin.getAdventure().all().showBossBar(bar); return bar;
    }
    private void updateBar(BossBar bar, Component title, float progress) {
        if (bar==null) return; bar.name(title); bar.progress(Math.min(1.0f,Math.max(0.0f,progress)));
    }
    private void removeBar(BossBar bar) { if (bar!=null) plugin.getAdventure().all().hideBossBar(bar); }
    private void removeAll() { removeBar(luckBar); removeBar(aaluckBar); removeBar(ddropBar); removeBar(vpluckBar); }
    private void bc(String key, String... kv) { plugin.getAdventure().all().sendMessage(cfg().parse(cfg().broadcastRaw(key, kv))); }
    public void saveState() {
        File file = new File(plugin.getDataFolder(), "state.yml");
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("luck.multiplier", state.luckMultiplier);
        yaml.set("luck.time",       state.luckMultiplierTime);
        yaml.set("luck.total",      state.luckMultiplierTotal);
        yaml.set("luck.active",     state.luckMultiplierActive);
        yaml.set("aa.luck",         state.aaluck);
        yaml.set("aa.time",         state.aaluckTime);
        yaml.set("aa.total",        state.aaluckTotal);
        yaml.set("aa.active",       state.aaluckActive);
        yaml.set("aa.cap",          plugin.getConfigManager().getLuckCap());
        yaml.set("drop.multiplier", state.ddrop);
        yaml.set("drop.time",       state.ddropTime);
        yaml.set("drop.total",      state.ddropTotal);
        yaml.set("drop.active",     state.ddropActive);
        yaml.set("vp.luck",         state.vpluck);
        yaml.set("vp.time",         state.vpluckTime);
        yaml.set("vp.total",        state.vpluckTotal);
        yaml.set("vp.active",       state.vpluckActive);
        try { yaml.save(file); } catch (Exception e) { plugin.getLogger().warning("Gagal simpan state.yml: " + e.getMessage()); }
    }

    public void loadState() {
        File file = new File(plugin.getDataFolder(), "state.yml");
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        state.luckMultiplier       = yaml.getDouble("luck.multiplier");
        state.luckMultiplierTime   = yaml.getLong("luck.time");
        state.luckMultiplierTotal  = yaml.getLong("luck.total");
        state.luckMultiplierActive = yaml.getBoolean("luck.active");
        state.aaluck               = yaml.getDouble("aa.luck");
        state.aaluckTime           = yaml.getLong("aa.time");
        state.aaluckTotal          = yaml.getLong("aa.total");
        state.aaluckActive         = yaml.getBoolean("aa.active");
        state.ddrop                = yaml.getDouble("drop.multiplier");
        state.ddropTime            = yaml.getLong("drop.time");
        state.ddropTotal           = yaml.getLong("drop.total");
        state.ddropActive          = yaml.getBoolean("drop.active");
        state.vpluck               = yaml.getDouble("vp.luck");
        state.vpluckTime           = yaml.getLong("vp.time");
        state.vpluckTotal          = yaml.getLong("vp.total");
        state.vpluckActive         = yaml.getBoolean("vp.active");

        if (state.luckMultiplierActive) luckBar = makeBar(null, bb("luck-multiplier",fmt(state.luckMultiplier),ft(state.luckMultiplierTime)), barColor("bossbar-luck-color"));
        if (state.aaluckActive)         aaluckBar = makeBar(null, bb("admin-abuse",fmt(state.aaluck),ft(state.aaluckTime)), barColor("bossbar-luck-color"));
        if (state.ddropActive)          ddropBar = makeBar(null, bb("double-drop",fmt(state.ddrop),ft(state.ddropTime)), barColor("bossbar-drop-color"));
        if (state.vpluckActive)         vpluckBar = makeBar(null, bb("vpluck",fmt(state.vpluck),ft(state.vpluckTime)), barColor("bossbar-vpluck-color"));
        
        if (state.aaluckActive) plugin.getConfigManager().setLuckCap(yaml.getDouble("aa.cap", 0.75));
    }

    private Component bb(String key, String value, String time) {
        String raw = cfg().bossbarRaw(key,"{value}",value,"{time}",time);
        return cfg().parse(raw);
    }
    private void playStart() { playSound(cfg().getSoundStart()); }
    private void playStop()  { playSound(cfg().getSoundStop()); }
    private void playSound(String s) { if (s==null||s.isEmpty()) return; for (Player p:Bukkit.getOnlinePlayers()) { try{p.playSound(p.getLocation(),s,1f,1f);}catch(Exception ignored){} } }
    private BossBar.Color barColor(String k) { 
        try {
            String colorName = plugin.getConfig().getString(k, "PURPLE").toUpperCase();
            // Map Bukkit BarColor to Adventure BossBar.Color
            switch (colorName) {
                case "PINK": return BossBar.Color.PINK;
                case "BLUE": return BossBar.Color.BLUE;
                case "RED": return BossBar.Color.RED;
                case "GREEN": return BossBar.Color.GREEN;
                case "YELLOW": return BossBar.Color.YELLOW;
                case "WHITE": return BossBar.Color.WHITE;
                default: return BossBar.Color.PURPLE;
            }
        } catch(Exception e) { return BossBar.Color.PURPLE; } 
    }
    private String fmt(double v) { return String.valueOf((int)v); }
    public String formatTime(long sec) { long h=sec/3600,m=(sec%3600)/60,s=sec%60; if(h>0) return h+"j "+m+"m "+s+"d"; if(m>0) return m+"m "+s+"d"; return s+"d"; }
    private String ft(long sec) { return formatTime(sec); }
    private id.seria.cyayoluckrng.config.ConfigManager cfg() { return plugin.getConfigManager(); }
    public EventState getState() { return state; }
}
