package de.powercheat;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;

public final class ReplayManager implements Listener {
    private final PowerCheat plugin;
    private final Path folder;
    public ReplayManager(PowerCheat plugin) {
        this.plugin = plugin;
        folder = plugin.getDataFolder().toPath().resolve("replays");
        try { Files.createDirectories(folder); } catch (IOException e) { plugin.getLogger().warning(e.getMessage()); }
    }
    private void record(Player p, String type, String detail) {
        if (!p.isOnline()) return;
        String line = Instant.now() + "|" + p.getUniqueId() + "|" + clean(p.getName()) + "|" +
                type + "|" + clean(detail) + "|" + loc(p.getLocation()) + System.lineSeparator();
        Path file = folder.resolve(p.getUniqueId() + ".log");
        try {
            Files.writeString(file, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            int max = plugin.getConfig().getInt("settings.max-replay-events-per-player", 5000);
            // Simple bounded retention: trim oldest lines after exceeding configured limit.
            var lines = Files.readAllLines(file);
            if (lines.size() > max) Files.write(file, lines.subList(lines.size() - max, lines.size()));
        } catch (IOException e) { plugin.getLogger().warning("Replay write failed: " + e.getMessage()); }
    }
    private String clean(String s) { return s.replace("|", "/").replace("\n", " "); }
    private String loc(Location l) {
        return l.getWorld().getName() + "," + round(l.getX()) + "," + round(l.getY()) + "," +
                round(l.getZ()) + "," + round(l.getYaw()) + "," + round(l.getPitch());
    }
    private String round(double d) { return String.format(java.util.Locale.US, "%.3f", d); }

    @EventHandler public void move(PlayerMoveEvent e) {
        if (e.getTo() != null && (e.getFrom().getBlockX()!=e.getTo().getBlockX() ||
                e.getFrom().getBlockY()!=e.getTo().getBlockY() || e.getFrom().getBlockZ()!=e.getTo().getBlockZ()))
            record(e.getPlayer(), "MOVE", loc(e.getTo()));
    }
    @EventHandler public void interact(PlayerInteractEntityEvent e) { record(e.getPlayer(), "INTERACT_ENTITY", e.getRightClicked().getType().name()); }
    @EventHandler public void damage(org.bukkit.event.entity.EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player p) record(p, "ATTACK", e.getEntity().getType().name());
    }
    @EventHandler public void place(org.bukkit.event.block.BlockPlaceEvent e) { record(e.getPlayer(), "PLACE", e.getBlockPlaced().getType().name()); }
    @EventHandler public void breakBlock(org.bukkit.event.block.BlockBreakEvent e) { record(e.getPlayer(), "BREAK", e.getBlock().getType().name()); }
    @EventHandler public void teleport(PlayerTeleportEvent e) { record(e.getPlayer(), "TELEPORT", e.getCause().name()); }

    public String replayFile(Player p) { return folder.resolve(p.getUniqueId() + ".log").toString(); }
    public java.util.List<String> recent(Player p, int limit) {
        Path f = folder.resolve(p.getUniqueId() + ".log");
        try {
            if (!Files.exists(f)) return java.util.List.of();
            var lines = Files.readAllLines(f);
            return lines.subList(Math.max(0, lines.size()-limit), lines.size());
        } catch (IOException e) { return java.util.List.of(); }
    }
    public void flush() { }
}
