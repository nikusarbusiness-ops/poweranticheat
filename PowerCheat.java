package de.powercheat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class PowerCheat extends JavaPlugin {
    private ReplayManager replayManager;
    private CheckManager checkManager;
    private StaffGui staffGui;

    @Override public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("checks.yml");
        saveResourceIfMissing("permissions.yml");
        saveResourceIfMissing("punishments.yml");
        replayManager = new ReplayManager(this);
        checkManager = new CheckManager(this);
        staffGui = new StaffGui(this);
        getServer().getPluginManager().registerEvents(replayManager, this);
        getServer().getPluginManager().registerEvents(checkManager, this);
        getServer().getPluginManager().registerEvents(staffGui, this);
        AdminCommand command = new AdminCommand(this);
        getCommand("ac").setExecutor(command);
        getCommand("PowerCheat").setExecutor(command);
        getCommand("cheatReplay").setExecutor(command);
        getLogger().info("PowerCheat enabled. Automatic punishments are disabled by default.");
    }

    private void saveResourceIfMissing(String name) {
        if (!getDataFolder().toPath().resolve(name).toFile().exists()) saveResource(name, false);
    }
    public ReplayManager replays() { return replayManager; }
    public CheckManager checks() { return checkManager; }
    public StaffGui gui() { return staffGui; }
    public void reloadAll() {
        reloadConfig();
        checkManager.reload();
    }
    @Override public void onDisable() {
        if (replayManager != null) replayManager.flush();
    }
}
