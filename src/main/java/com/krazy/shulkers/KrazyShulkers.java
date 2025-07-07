package com.krazy.shulkers;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.krazy.shulkers.commands.MainCommand;
import com.krazy.shulkers.data.Config;
import com.krazy.shulkers.listeners.InteractListener;
import com.krazy.shulkers.listeners.InventoryCloseListener;
import com.krazy.shulkers.manager.ShulkerManager;
import com.krazy.shulkers.manager.chestsort.ChestSortManagerImpl;

public class KrazyShulkers extends JavaPlugin {

    private Config settings;
    private boolean hasWorldGuard;
    private ShulkerManager shulkerManager;

    @Override
    public void onEnable() {
        settings = new Config(this);
        settings.setup();
        shulkerManager = new ShulkerManager(this);
        new InteractListener(this);
        new InventoryCloseListener(this);
        new MainCommand(this);

        loadHooks();
    }

    @Override
    public void onDisable() {
        shulkerManager.closeAllInventories(true);
    }

    private void loadHooks() {
        PluginManager manager = getServer().getPluginManager();

        if(manager.isPluginEnabled("WorldGuard")) {
            if(settings.getBoolean("shulkers.enable_worldguard_hook", true)) {
                hasWorldGuard = true;
                getLogger().info("KrazyShulkers successfully hooked to WorldGuard");
            }
        }

        if(manager.isPluginEnabled("ChestSort")) {
            if(settings.getBoolean("shulkers.enable_chest_sort", true)) {
                shulkerManager.setChestSortManager(new ChestSortManagerImpl());
                getLogger().info("KrazyShulkers successfully hooked to ChestSort");
            }
        }
    }

    public int getServerVersion() {
        return Integer.parseInt(Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.")[1]);
    }

    public Config getSettings() {
        return settings;
    }

    public boolean hasWorldGuard() {
        return hasWorldGuard;
    }

    public ShulkerManager getShulkerManager() {
        return shulkerManager;
    }

    public static KrazyShulkers getInstance() {
        return JavaPlugin.getPlugin(KrazyShulkers.class);
    }
}