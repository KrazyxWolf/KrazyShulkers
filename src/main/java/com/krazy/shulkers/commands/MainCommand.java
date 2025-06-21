package com.krazy.shulkers.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.krazy.shulkers.KrazyShulkers;
import com.krazy.shulkers.data.MessageKeys;
import com.krazy.shulkers.util.KSBPermission;

public class MainCommand implements CommandExecutor {

    private final KrazyShulkers plugin;

    public MainCommand(KrazyShulkers plugin) {
        this.plugin = plugin;
        plugin.getCommand("ksb").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(KSBPermission.ADMIN.toString())) {
            MessageKeys.send(sender, MessageKeys.NO_PERMISSION.get());
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.getSettings().reload();
            MessageKeys.send(sender, MessageKeys.RELOAD.get());
        }
        return false;
    }
}