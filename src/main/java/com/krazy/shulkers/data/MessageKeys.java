package com.krazy.shulkers.data;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import com.krazy.shulkers.KrazyShulkers;

public enum MessageKeys {

    PREFIX("messages.prefix", "<aqua>[</aqua><yellow>BSB</yellow><aqua>]</aqua>"),
    RELOAD("messages.reload", "{prefix} <gray>Configuration reloaded successfully.</gray>"),
    COOLDOWN("messages.cooldown", "{prefix} <red>You have to wait</red> <aqua>{time}</aqua> <red>before using this again.</red>"),
    OPEN_MESSAGE("messages.open_message", "{prefix} <gray>Opening shulkerbox (</gray>{shulker_name}<gray>)...</gray>"),
    CLOSE_MESSAGE("messages.close_message", "{prefix} <gray>Closing shulkerbox (</gray>{shulker_name}<gray>)...</gray>"),
    NO_PERMISSION("messages.no_permission", "{prefix} <red>No permission.</red>"),
    DISABLED_WORLD("messages.disabled_world", "{prefix} <red>You can't open shulkers in this world.</red>"),
    DISABLED_REGION("messages.disabled_region", "{prefix} <red>You can't open shulkers in this region.</red>"),
    ILLEGAL_INTERACTION("messages.illegal_interaction", "{prefix} <white>{player}</white> <gray>is trying to store a shulkerbox while its inventory is open. Possible illegal mod involved.</gray>");
    
    private String path;
    private String fallback;
    private final KrazyShulkers plugin = KrazyShulkers.getInstance();
    private FileConfiguration config = plugin.getSettings().getFile();

    private MessageKeys(String path, String fallback) {
        this.path = path;
        this.fallback = fallback;
    }

    public String getPath() {
        return path;
    }

    public String getFallback() {
        return fallback;
    }

    public String get() {
        return get(null, null);
    }

    public String get(String key, String value) {
        String prefix = PREFIX.getString();
        String message = getString().replace("{prefix}", prefix);

        if(key == null && value == null) return message;

        return message = message.replace(key, value);
    }

    public static void send(CommandSender sender, String message) {
        if(message.isBlank()) return;

        sender.sendRichMessage(message);
    }

    public String getString() {
        if(exists()) return config.getString(path);

        return getFallback();
    }

    public boolean exists() {
        return config.contains(path);
    }
}