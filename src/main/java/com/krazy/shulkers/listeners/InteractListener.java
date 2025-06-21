package com.krazy.shulkers.listeners;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import com.krazy.shulkers.KrazyShulkers;
import com.krazy.shulkers.data.MessageKeys;
import com.krazy.shulkers.manager.ShulkerOpenData;
import com.krazy.shulkers.manager.SlotType;
import com.krazy.shulkers.util.Check;
import com.krazy.shulkers.util.KSBPermission;
import com.krazy.shulkers.util.MaterialUtil;
import com.krazy.shulkers.util.StringUtil;

public class InteractListener implements Listener {

    private final KrazyShulkers plugin;

    public InteractListener(KrazyShulkers plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (e.getAction() == Action.RIGHT_CLICK_AIR) {
            if (!plugin.getSettings().getBoolean("shulker.enable_right_click_open", true)) {
                return;
            }

            ItemStack item = e.getPlayer().getInventory().getItemInMainHand();

            if (item.getAmount() > 1 || item.getAmount() < 1) return; // Do not open if stacked: compatible stacking plugin
            if (!MaterialUtil.isShulkerBox(item.getType())) return;
            if (Check.isWorldDisabled(player.getWorld().getName(), e.getEventName())) {
                MessageKeys.send(player, MessageKeys.DISABLED_WORLD.get());
                return;
            }
            if (Check.isRegionDisabled(player.getLocation(), e.getEventName())) {
                MessageKeys.send(player, MessageKeys.DISABLED_REGION.get());
                return;
            }

            BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
            assert bsm != null;
            plugin.getShulkerManager().openShulkerBoxInventory(e.getPlayer(), item, SlotType.HOTBAR, e.getPlayer().getInventory().getHeldItemSlot());
        } else if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            /*
             * This prevents an exploit in which a player closes an inventory
             * locally, the packet is not sent, and then tries to store the shulker
             * in a decorated pot and duplicates items
             */
            Block block = e.getClickedBlock();

            if(block == null) return;
            if(!block.getType().name().equals("DECORATED_POT")) return;

            Inventory top = player.getOpenInventory().getTopInventory();

            if(top.getType() != InventoryType.SHULKER_BOX) return;

            ShulkerOpenData data = plugin.getShulkerManager().getShulkerOpenData(top);

            if(data == null) return;

            ItemStack item = e.getItem();

            if(item != null && item.equals(data.getItemStack())) {
                plugin.getLogger().severe(player.getName() + " is trying to store an open shulker in a pot. Possible illegal mod involved.");

                if(plugin.getSettings().getBoolean("extra.warn_staffs", true)) {
                    plugin.getServer().broadcast(StringUtil.getMiniMessage().deserialize(MessageKeys.ILLEGAL_INTERACTION.get("{player}", player.getName())), KSBPermission.RECEIVE_ALERTS.toString());
                }

                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if(!plugin.getSettings().getBoolean("shulkers.enable_inventory_click_open", false)) {
            return;
        }

        Player player = (Player) e.getWhoClicked();
        Inventory clickedInventory = e.getClickedInventory();

        if (e.getClick() != ClickType.RIGHT) return;
        if (clickedInventory == null) return;
        if (clickedInventory.getType() != InventoryType.PLAYER) {
            return;
        }

        ItemStack clicked = e.getCurrentItem();

        if(clicked != null && clicked.getAmount() != 1) return; // Do not open if stacked: compatible stacking plugin

        boolean isShulker = clicked!=null && MaterialUtil.isShulkerBox(clicked.getType());

        if (player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) {
            if(!isShulker) {
                return;
            }
        }
        if(!isShulker) return;

        e.setCancelled(true);
        ItemStack oldItem = clicked.clone();
        // Run the handler in 1 tick to not desync the inventory.
        Bukkit.getScheduler().runTask(plugin, () -> {
            ItemStack currentItem = clickedInventory.getItem(e.getSlot());
            // Make sure the item has not changed since the click.
            if (oldItem.equals(currentItem)) {
                plugin.getShulkerManager().openShulkerBoxInventory(player, currentItem, SlotType.INVENTORY, e.getRawSlot());
            }
        });
    }
}