package id.seria.cyayoluckrng.event;

import id.seria.cyayoluckrng.CyayoLuckRNG;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final CyayoLuckRNG plugin;
    public JoinListener(CyayoLuckRNG plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.getEventManager().restoreBossBars(e.getPlayer());
    }
}
