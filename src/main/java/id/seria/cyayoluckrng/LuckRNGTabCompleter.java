package id.seria.cyayoluckrng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class LuckRNGTabCompleter implements TabCompleter {
    private final CyayoLuckRNG plugin;
    public LuckRNGTabCompleter(CyayoLuckRNG plugin) { this.plugin = plugin; }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        boolean con = !(sender instanceof Player);

        if (args.length == 1) {
            String[] subs = {"info","run","event","eventend","abuse","abuseend","drop","dropend","vp","vpend","reload","resetcount"};
            for (String s : subs) {
                if (!s.startsWith(args[0].toLowerCase())) continue;
                if (s.equals("info")   && (con||sender.hasPermission("luckrng.use")))    result.add(s);
                if (s.equals("run")    && (con||sender.hasPermission("luckrng.run")))    result.add(s);
                if ((s.equals("event")||s.equals("eventend"))  && (con||sender.hasPermission("luckrng.event")))  result.add(s);
                if ((s.equals("abuse")||s.equals("abuseend"))  && (con||sender.hasPermission("luckrng.abuse")))  result.add(s);
                if ((s.equals("drop") ||s.equals("dropend"))   && (con||sender.hasPermission("luckrng.drop")))   result.add(s);
                if ((s.equals("vp")   ||s.equals("vpend"))     && (con||sender.hasPermission("luckrng.vp")))     result.add(s);
                if ((s.equals("reload")||s.equals("resetcount"))&&(con||sender.hasPermission("luckrng.reload"))) result.add(s);
            }
            return result;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("run") && (con||sender.hasPermission("luckrng.run"))) {
            if (args.length==2) { for (String k:plugin.getConfigManager().getTables().keySet()) if(k.startsWith(args[1].toLowerCase())) result.add(k); }
            else if (args.length==3) result.addAll(Arrays.asList("0","10","20","30","50","100"));
            else if (args.length==4) result.addAll(onlinePlayers(args[3]));
        }
        if (sub.equals("resetcount") && (con||sender.hasPermission("luckrng.reload"))) {
            if (args.length==2) { result.add("*"); for (String k:plugin.getConfigManager().getTables().keySet()) if(k.startsWith(args[1].toLowerCase())) result.add(k); }
            else if (args.length==3) result.addAll(onlinePlayers(args[2]));
        }
        if (sub.equals("event") && (con||sender.hasPermission("luckrng.event"))) {
            if (args.length==2) result.addAll(Arrays.asList("2","3","5","10"));
            else if (args.length==3) result.addAll(Arrays.asList("300","600","1800","3600","7200"));
            else if (args.length==4) result.addAll(onlinePlayers(args[3]));
        }
        if (sub.equals("abuse") && (con||sender.hasPermission("luckrng.abuse"))) {
            if (args.length==2) result.addAll(Arrays.asList("100","250","500","1000","20000000"));
            else if (args.length==3) result.addAll(Arrays.asList("300","600","1800","3600","7200"));
            else if (args.length==4) result.addAll(Arrays.asList("0.75","0.80","0.85","0.90","1.0"));
            else if (args.length==5) result.addAll(onlinePlayers(args[4]));
        }
        if (sub.equals("drop") && (con||sender.hasPermission("luckrng.drop"))) {
            if (args.length==2) result.addAll(Arrays.asList("2","3","5"));
            else if (args.length==3) result.addAll(Arrays.asList("300","600","1800","3600","7200"));
            else if (args.length==4) result.addAll(onlinePlayers(args[3]));
        }
        return result;
    }

    private List<String> onlinePlayers(String prefix) {
        List<String> names = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers())
            if (p.getName().toLowerCase().startsWith(prefix.toLowerCase())) names.add(p.getName());
        return names;
    }
}
