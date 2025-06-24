package com.krazy.shulkers.data;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.krazy.shulkers.KrazyShulkers;
import com.krazy.shulkers.util.StringUtil;

import net.kyori.adventure.text.Component;

public enum MessageKeys {

    PREFIX("messages.prefix", "<#de99fc><b>SHULKERS</b></#de99fc> <dark_gray>»</dark_gray>"),
    RELOAD("messages.reload", "{prefix} <gray>Configuration reloaded successfully.</gray>"),
    COOLDOWN("messages.cooldown", "{prefix} <red>You have to wait</red> <aqua>{time}</aqua> <red>before using this again.</red>"),
    OPEN_MESSAGE("messages.open_message", "{prefix} <gray>Opening shulkerbox (</gray>{shulker_name}<gray>)...</gray>"),
    CLOSE_MESSAGE("messages.close_message", "{prefix} <gray>Closing shulkerbox (</gray>{shulker_name}<gray>)...</gray>"),
    NO_PERMISSION("messages.no_permission", "{prefix} <red>No permission.</red>"),
    ILLEGAL_ACTION("messages.illegal_action", "{prefix} <white>{player}</white> <gray>tried to trigger</gray> <white>{event}</white> <gray>while a shulker box was open.</gray>"),
    DISABLED_WORLD("messages.disabled_world", "{prefix} <red>You can't open shulkers in this world.</red>"),
    DISABLED_REGION("messages.disabled_region", "{prefix} <red>You can't open shulkers in this region.</red>"),
    ILLEGAL_INTERACTION("messages.illegal_interaction", "{prefix} <white>{player}</white> <gray>is trying to store a shulker box while its inventory is open. Possible illegal mod involved.</gray>");
    
    private String path;
    private String fallback;
    private final KrazyShulkers plugin = KrazyShulkers.getInstance();
    private Config config = plugin.getSettings();

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
        return get(Map.of());
    }

    public String get(String key, String value) {
        if(key == null || value == null) return get();

        return get(Map.of(key, value));
    }

    public String get(Map<String, String> replacements) {
        String prefix = PREFIX.getString();
        String message = getString().replace("{prefix}", prefix);

        if(replacements.isEmpty()) return message;

        for(Map.Entry<String, String> values : replacements.entrySet()) {
            message = message.replace(values.getKey(), values.getValue());
        }

        return message;
    }

    public static void send(CommandSender sender, String message) {
        if(message.isBlank()) return;

        sender.sendRichMessage(message);
    }

    public static void broadcast(String message, String permission) {
        if(message.isBlank()) return;

        Component text = StringUtil.getMiniMessage().deserialize(message);

        if(permission == null || permission.isBlank()) {
            Bukkit.broadcast(text);
        } else {
            Bukkit.broadcast(text, permission);
        }
    }

    private String getString() {
        if(exists()) return config.getString(path);

        return getFallback();
    }

    private boolean exists() {
        return config.contains(path);
    }
}