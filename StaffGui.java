package de.powercheat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

public final class StaffGui implements Listener {
    private final PowerCheat plugin;
    private final Map<UUID, Boolean> alerts = new HashMap<>();
    public StaffGui(PowerCheat plugin) { this.plugin = plugin; }
    public void open(Player p) {
        if (!allowed(p, "powercheat.admin")) { p.sendMessage("§cKeine Berechtigung."); return; }
        Inventory inv = Bukkit.createInventory(null, 27, "§8PowerCheat §7| Admin");
        item(inv, 10, Material.IRON_SWORD, "§cCombat Checks", "§7Status in checks.yml");
        item(inv, 11, Material.FEATHER, "§bMovement Checks", "§7Status in checks.yml");
        item(inv, 12, Material.CHEST, "§ePlayer Checks", "§7Status in checks.yml");
        item(inv, 13, Material.PAPER, "§aPermissions", "§7Rechteverwaltung per OP/Permissions");
        item(inv, 14, Material.BELL, "§6Alerts", "§7Klicken: Alerts umschalten");
        item(inv, 15, Material.BOOK, "§dReplays", "§7Öffnet /cheatReplay");
        item(inv, 16, Material.COMPARATOR, "§fReload", "§7Konfiguration neu laden");
        p.openInventory(inv);
    }
    private boolean allowed(Player p, String perm) { return p.isOp() || p.hasPermission(perm); }
    private void item(Inventory inv, int slot, Material mat, String name, String lore) {
        ItemStack stack = new ItemStack(mat); ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name); meta.setLore(List.of(lore)); stack.setItemMeta(meta); inv.setItem(slot, stack);
    }
    @EventHandler public void click(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p) || !e.getView().getTitle().startsWith("§8PowerCheat")) return;
        e.setCancelled(true);
        if (!allowed(p, "powercheat.admin")) return;
        switch (e.getRawSlot()) {
            case 14 -> {
                boolean enabled = !alerts.getOrDefault(p.getUniqueId(), true);
                alerts.put(p.getUniqueId(), enabled);
                if (enabled) p.addScoreboardTag("pc-alerts"); else p.removeScoreboardTag("pc-alerts");
                p.sendMessage("§6PowerCheat Alerts: " + (enabled ? "§aAN" : "§cAUS"));
            }
            case 15 -> p.performCommand("cheatReplay");
            case 16 -> { plugin.reloadAll(); p.sendMessage("§aPowerCheat-Konfiguration neu geladen."); }
            default -> p.sendMessage("§7Diese Check-Kategorie wird über checks.yml konfiguriert.");
        }
    }
}
