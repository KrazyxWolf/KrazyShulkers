package com.krazy.shulkers.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.krazy.shulkers.KrazyShulkers;
import com.krazy.shulkers.data.MessageKeys;
import com.krazy.shulkers.manager.ShulkerOpenData;
import com.krazy.shulkers.util.KSBPermission;
import com.krazy.shulkers.util.MaterialUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InventoryCloseListener implements Listener {

    private final KrazyShulkers plugin;

    public InventoryCloseListener(KrazyShulkers plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        Inventory inventory = e.getInventory();

        if (inventory.getType() != InventoryType.SHULKER_BOX) return; //validate inventory type
        if (inventory.getHolder() != null || e.getInventory().getLocation() != null) {
            return; //check that the shulker inventory is not a block inventory
        }
        if (!plugin.getShulkerManager().isShulkerInventory(inventory)) {
            return; //check that the inventory belongs to the plugin
        }

        plugin.getShulkerManager().closeShulkerBox(player, inventory, Optional.empty());
        player.setItemOnCursor(null); //Workaround for Shulker box caught in cursor after opening with right click in inventory
    }

    //todo view-mode only
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent e) {
        ItemStack droppedItem = e.getItemDrop().getItemStack();

        if (!MaterialUtil.isShulkerBox(droppedItem.getType())) return; //check if the dropped item is a shulker box
        if (e.getPlayer().getOpenInventory().getType() != InventoryType.SHULKER_BOX) {
            return; //check if the open inventory is one from a shulker box
        }
        if (e.getPlayer().getOpenInventory().getTopInventory().getLocation() != null) {
            return; //check if the shulker is a block
        }
        if (!plugin.getShulkerManager().doesPlayerHaveShulkerOpen(e.getPlayer().getUniqueId())) {
            return; //check if the inventory belongs to the plugin
        }
        
        ItemStack corresponding = plugin.getShulkerManager().getCorrespondingStack(e.getPlayer().getOpenInventory().getTopInventory());
        
        if (corresponding == null) {
            return;
        } else if (!corresponding.equals(droppedItem)) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (plugin.getSettings().getBoolean("shulkers.disable_movement_check")) {
            return;
        }
        if (e.getPlayer().getOpenInventory().getType() != InventoryType.SHULKER_BOX) {
            return; //check if the open inventory is one from a shulker box
        }
        if (e.getPlayer().getOpenInventory().getTopInventory().getLocation() != null) {
            return; //check if the shulker is a block
        }
        if (!plugin.getShulkerManager().doesPlayerHaveShulkerOpen(e.getPlayer().getUniqueId())) {
            return; //check if the inventory belongs to the plugin
        }

        ShulkerOpenData sod = plugin.getShulkerManager().getShulkerOpenData(e.getPlayer().getOpenInventory().getTopInventory());
        
        if (sod == null || e.getTo() == null) return;
        if (sod.getOpenLocation().distance(e.getTo()) > 1) {
            plugin.getShulkerManager().closeShulkerBox(e.getPlayer(), e.getPlayer().getOpenInventory().getTopInventory(), Optional.empty());
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getPlayer().getOpenInventory().getType() != InventoryType.SHULKER_BOX) {
            return; //check if the open inventory is one from a shulker box
        }
        if (e.getPlayer().getOpenInventory().getTopInventory().getLocation() != null) {
            return; //check if the shulker is a block
        }
        if (!plugin.getShulkerManager().doesPlayerHaveShulkerOpen(e.getPlayer().getUniqueId())) {
            return; //check if the inventory belongs to the plugin
        }

        plugin.getShulkerManager().closeShulkerBox(e.getPlayer(), e.getPlayer().getOpenInventory().getTopInventory(), Optional.empty());
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        Player player = (Player) e.getWhoClicked();

        if (player.getOpenInventory().getType() != InventoryType.SHULKER_BOX) {
            return; //check if the open inventory is one from a shulker box
        }
        if (player.getOpenInventory().getTopInventory().getLocation() != null) return; //check if the shulker is a block
        if (!plugin.getShulkerManager().doesPlayerHaveShulkerOpen(player.getUniqueId())) {
            return; //check if the inventory belongs to the plugin
        }

        /*
         *   This should prevent some kind of exploit in which a player closes an inventory
         *   locally, the packet is not sent, and then tries to dye the shulker to try and
         *   duplicate items
         *
         */
        if (MaterialUtil.isShulkerBox(e.getRecipe().getResult().getType())) {
            e.setCancelled(true);
        }

        plugin.getShulkerManager().closeShulkerBox(player, player.getOpenInventory().getTopInventory(), Optional.empty());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        if (player.getOpenInventory().getType() != InventoryType.SHULKER_BOX) {
            return; //check if the open inventory is one from a shulker box
        }
        if (player.getOpenInventory().getTopInventory().getLocation() != null) return; //check if the shulker is a block
        if (!plugin.getShulkerManager().doesPlayerHaveShulkerOpen(player.getUniqueId())) {
            return; //check if the inventory belongs to the plugin
        }
        
        plugin.getShulkerManager().closeShulkerBox(player, player.getOpenInventory().getTopInventory(), Optional.empty());
    }

    /*
        This should prevent someone from trying to duplicate items by closing an inventory locally and then opening
        another one. The server should not allow the player to open another inventory before closing the one that's
        already open, but this is just to be safe.
    */
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (plugin.getShulkerManager().doesPlayerHaveShulkerOpen(e.getPlayer().getUniqueId()) && !plugin.getShulkerManager().isShulkerInventory(e.getInventory())
                && e.getPlayer().getOpenInventory().getTopInventory().getType() == InventoryType.SHULKER_BOX) {
            plugin.getShulkerManager().closeShulkerBox((Player) e.getPlayer(), e.getPlayer().getOpenInventory().getTopInventory(), Optional.empty());
        }
    }


    /*
     * This prevents players from moving shulkers around in their inventories
     * while a shulker box inventory opened through by the plugin is still open.
     * 
     * Also this works for read only mode
     */
    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory clickedInventory = e.getClickedInventory();
        
        if (clickedInventory == null) return;

        Inventory topInventory = player.getOpenInventory().getTopInventory();
        ItemStack correspondingStack = plugin.getShulkerManager().getCorrespondingStack(topInventory);

        if (correspondingStack == null) return;

        if (plugin.getSettings().getBoolean("shulkers.read_only")) {
            e.setCancelled(true);
        }

        ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || !MaterialUtil.isShulkerBox(clickedItem.getType())) return;

        if (correspondingStack.equals(clickedItem)) {
            e.setCancelled(true);
        }
    }

    /*
     * This prevents players from placing a shulker box
     * while a shulker box inventory opened through by the plugin is still open.
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        notify(e.getPlayer(), e.getEventName(), e);
    }

    /*
     * This prevents players from using the chat
     * to remove the shulker box from their inventory while 
     * a shulker box inventory opened by the plugin is still open.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        notify(e.getPlayer(), e.getEventName(), e);
    }

    /*
     * This prevents players from interacting with
     * entities such as item frames while a shulker box 
     * inventory opened through by the plugin is still open.
     */
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        notify(e.getPlayer(), e.getEventName(), e);
    }

    /*
     * This prevents players from using server commands
     * to remove the shulker box from their inventory while 
     * a shulker box inventory opened by the plugin is still open.
     */
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        notify(e.getPlayer(), e.getEventName(), e);
    }

    private void cancelEvent(Player player, Cancellable cancellable) {
        cancellable.setCancelled(true);
    }

    private void notify(Player player, String event, Cancellable cancellable) {
        if (!shouldCancelIfShulkerOpen(player)) return;

        cancelEvent(player, cancellable);

        if(plugin.getSettings().getBoolean("events." + event + ".notify_violations", false)) {
            Map<String, String> replacements = new HashMap<>();

            replacements.put("{player}", player.getName());
            replacements.put("{event}", event);

            MessageKeys.broadcast(player, MessageKeys.ILLEGAL_ACTION.get(replacements), KSBPermission.RECEIVE_ALERTS.toString());
        }
    }

    private boolean shouldCancelIfShulkerOpen(Player player) {
        if (player.getOpenInventory().getType() != InventoryType.SHULKER_BOX) {
            return false; //check if the open inventory is one from a shulker box
        }
        if (player.getOpenInventory().getTopInventory().getLocation() != null) return false; //check if the shulker is a block
        if (!plugin.getShulkerManager().doesPlayerHaveShulkerOpen(player.getUniqueId())) {
            return false; //check if the inventory belongs to the plugin
        }

        return true;
    }
}