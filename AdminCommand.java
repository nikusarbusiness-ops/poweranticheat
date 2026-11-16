package de.powercheat;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.Locale;

public final class AdminCommand implements CommandExecutor {
    private final PowerCheat plugin;
    public AdminCommand(PowerCheat plugin) { this.plugin = plugin; }
    private boolean admin(CommandSender s) {
        if (s instanceof Player p && p.isOp()) return true;
        return s.hasPermission("powercheat.admin");
    }
    @Override public boolean onCommand(CommandSender s, Command c, String label, String[] a) {
        String cmd = c.getName().toLowerCase(Locale.ROOT);
        if (cmd.equals("powercheat") || cmd.equals("pc")) {
            if (!(s instanceof Player p)) { s.sendMessage("Nur ingame."); return true; }
            plugin.gui().open(p); return true;
        }
        if (cmd.equals("cheatreplay")) {
            if (!(s instanceof Player viewer) || !(viewer.isOp() || viewer.hasPermission("powercheat.replay"))) {
                s.sendMessage("§cKeine Replay-Berechtigung."); return true;
            }
            if (a.length == 0) {
                s.sendMessage("§6PowerCheat Replays:");
                for (Player online : Bukkit.getOnlinePlayers())
                    if (!plugin.replays().recent(online, 1).isEmpty())
                        s.sendMessage("§e" + online.getName() + " §7- /cheatReplay " + online.getName());
                s.sendMessage("§7Offline-Spieler: UUID-Dateien unter plugins/PowerCheat/replays/");
                return true;
            }
            Player target = Bukkit.getPlayerExact(a[0]);
            if (target == null) { s.sendMessage("§cSpieler muss online sein, um den Namen aufzulösen."); return true; }
            s.sendMessage("§6Letzte Replay-Ereignisse von §e" + target.getName());
            var lines = plugin.replays().recent(target, 20);
            if (lines.isEmpty()) s.sendMessage("§7Keine Aufzeichnungen vorhanden.");
            else for (String line : lines) s.sendMessage("§8" + line);
            return true;
        }
        if (!admin(s)) { s.sendMessage("§cKeine Berechtigung."); return true; }
        if (a.length == 0) { s.sendMessage("§e/ac alerts|info <player>|checks|reload|violations <player>"); return true; }
        switch (a[0].toLowerCase(Locale.ROOT)) {
            case "alerts" -> {
                if (!(s instanceof Player p)) { s.sendMessage("Nur ingame."); return true; }
                if (p.getScoreboardTags().contains("pc-alerts")) p.removeScoreboardTag("pc-alerts");
                else p.addScoreboardTag("pc-alerts");
                s.sendMessage("§aAlerts " + (p.getScoreboardTags().contains("pc-alerts") ? "aktiviert." : "deaktiviert."));
            }
            case "checks" -> s.sendMessage("§eChecks: Combat, Movement, Player. Konfiguration: plugins/PowerCheat/checks.yml");
            case "reload" -> { plugin.reloadAll(); s.sendMessage("§aPowerCheat neu geladen."); }
            case "info", "violations" -> {
                if (a.length < 2) { s.sendMessage("§cBitte Spieler angeben."); return true; }
                Player target = Bukkit.getPlayerExact(a[1]);
                if (target == null) { s.sendMessage("§cSpieler nicht online."); return true; }
                s.sendMessage("§e" + target.getName() + " §7UUID: " + target.getUniqueId());
                s.sendMessage("§7Violation Level: §c" + plugin.checks().vl(target.getUniqueId()));
                s.sendMessage("§7Ping: " + target.getPing() + "ms | Replay: " + plugin.replays().replayFile(target));
            }
            default -> s.sendMessage("§e/ac alerts|info <player>|checks|reload|violations <player>");
        }
        return true;
    }
}
