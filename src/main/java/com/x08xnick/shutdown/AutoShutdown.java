
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
        getLogger().info("AutoShutdown enabled.");
    }

    @Override
    public void onDisable() {
        if (checkerTask != null) {
            checkerTask.cancel();
        }
        getLogger().info("AutoShutdown disabled.");
    }

    private void loadSettings() {
        shutdownAfterMinutes = getConfig().getInt("shutdown-after", 10);
    }

    private void startInactivityChecker() {
        if (checkerTask != null) {
            checkerTask.cancel();
        }

        checkerTask = new BukkitRunnable() {
            private long lastOnlineTime = System.currentTimeMillis();

            @Override
            public void run() {
                int players = Bukkit.getOnlinePlayers().size();
                if (players > 0) {
                    lastOnlineTime = System.currentTimeMillis();
                } else {
                    long inactiveMillis = System.currentTimeMillis() - lastOnlineTime;
                    if (inactiveMillis >= shutdownAfterMinutes * 60 * 1000L) {
                        getLogger().info("No players for " + shutdownAfterMinutes + " minutes. Shutting down.");
                        Bukkit.shutdown();
                    }
                }
            }
        };

        checkerTask.runTaskTimer(this, 20L, 20L * 60); // every minute
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("asd")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("autosd.reload")) {
                    reloadConfig();
                    loadSettings();
                    startInactivityChecker();
                    sender.sendMessage("§aAutoShutdown config reloaded.");
                } else {
                    sender.sendMessage("§cYou don't have permission.");
                }
                return true;
            }
        }
        return false;
    }
}
