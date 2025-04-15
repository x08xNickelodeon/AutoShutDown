package com.x08xnick.shutdown;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoShutdown extends JavaPlugin {

    private BukkitRunnable checkerTask;
    private int shutdownAfterMinutes;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        startInactivityChecker();
        getLogger().info("§e[AutoShutdown] Enabled. Shutdown time: " + shutdownAfterMinutes + " minutes of inactivity.");
    }

    @Override
    public void onDisable() {
        if (checkerTask != null) {
            checkerTask.cancel();
        }
        getLogger().info("§c[AutoShutdown] Disabled.");
    }

    private void loadSettings() {
        shutdownAfterMinutes = getConfig().getInt("shutdown-after", 10);

        if (shutdownAfterMinutes > 10) {
            getLogger().warning("§e[AutoShutdown] Config 'shutdown-after' was greater than 10. Resetting to 10.");
            shutdownAfterMinutes = 10;
            getConfig().set("shutdown-after", 10);
            saveConfig();
        }
    }

    private void startInactivityChecker() {
        if (checkerTask != null) {
            checkerTask.cancel();
        }

        checkerTask = new BukkitRunnable() {
            private long lastOnlineTime = System.currentTimeMillis();
            private int lastLoggedMinute = -1;

            @Override
            public void run() {
                int players = Bukkit.getOnlinePlayers().size();
                if (players > 0) {
                    lastOnlineTime = System.currentTimeMillis();
                    lastLoggedMinute = -1; // reset tracker
                } else {
                    long inactiveMillis = System.currentTimeMillis() - lastOnlineTime;
                    int inactiveMinutes = (int) (inactiveMillis / 60000);

                    if (inactiveMinutes != lastLoggedMinute && inactiveMinutes < shutdownAfterMinutes) {
                        Bukkit.getConsoleSender().sendMessage("§e[AutoShutdown] No players online for " + inactiveMinutes + " minute(s).");
                        lastLoggedMinute = inactiveMinutes;
                    }

                    if (inactiveMinutes >= shutdownAfterMinutes) {
                        Bukkit.getConsoleSender().sendMessage("§c[AutoShutdown] No players for " + shutdownAfterMinutes + " minutes. Shutting down.");
                        Bukkit.shutdown();
                    }
                }
            }
        };

        checkerTask.runTaskTimer(this, 20L, 20L * 60); // every 1 minute
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("asd")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("autosd.reload")) {
                    reloadConfig();
                    loadSettings();
                    startInactivityChecker();
                    sender.sendMessage("§a[AutoShutdown] Config reloaded.");
                } else {
                    sender.sendMessage("§cYou don't have permission.");
                }
                return true;
            }
        }
        return false;
    }
}
