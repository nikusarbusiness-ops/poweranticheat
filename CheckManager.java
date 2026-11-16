package de.powercheat;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.*;

public final class CheckManager implements Listener {
    private final PowerCheat plugin;
    private final Map<UUID, Double> violations = new HashMap<>();
    private final Map<UUID, Long> lastMove = new HashMap<>();
    public CheckManager(PowerCheat plugin) { this.plugin = plugin; }
    public void reload() { violations.clear(); lastMove.clear(); }
    public double vl(UUID id) { return violations.getOrDefault(id, 0.0); }
    public void flag(Player p, String check, double amount, String detail) {
        double next = vl(p.getUniqueId()) + amount;
        violations.put(p.getUniqueId(), next);
        plugin.replays(); // keep replay manager initialized
        String msg = "§8[§cPowerCheat§8] §e" + p.getName() + " §7flagged §c" + check +
                " §7(VL: §c" + String.format(Locale.US, "%.1f", next) + "§7) §8" + detail;
        for (Player staff : plugin.getServer().getOnlinePlayers()) {
            if (staff.isOp() || staff.hasPermission("powercheat.alerts"))
                if (staff.getScoreboardTags().contains("pc-alerts") || staff.isOp()) staff.sendMessage(msg);
        }
        plugin.getLogger().info(msg.replace('§', '&'));
        // Deliberately no automatic punishment from this starter heuristic.
    }
    @EventHandler public void movement(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (e.getTo() == null || p.isInsideVehicle() || p.isFlying() || p.getAllowFlight() ||
                p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) return;
        long now = System.nanoTime();
        long previous = lastMove.getOrDefault(p.getUniqueId(), now);
        lastMove.put(p.getUniqueId(), now);
        if (e.getFrom().getY() == e.getTo().getY()) {
            double dx = e.getTo().getX() - e.getFrom().getX();
            double dz = e.getTo().getZ() - e.getFrom().getZ();
            double horizontal = Math.sqrt(dx*dx + dz*dz);
            double pingFactor = Math.max(0.0, Math.min(0.35, p.getPing() / 1000.0));
            double threshold = plugin.getConfig().getDouble("checks.movement.suspicious-horizontal-speed", 1.25) + pingFactor;
            if (horizontal > threshold && now - previous > 0) {
                // A single large movement is only recorded as a low-confidence signal.
                flag(p, "movement-anomaly", 0.25, "horizontal=" + String.format(Locale.US, "%.2f", horizontal));
            }
        }
    }
    @EventHandler public void quit(PlayerQuitEvent e) { lastMove.remove(e.getPlayer().getUniqueId()); }
}
