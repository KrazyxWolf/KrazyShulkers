package com.krazy.shulkers.manager;

import net.kyori.adventure.key.Key;

import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import com.krazy.shulkers.KrazyShulkers;
import com.krazy.shulkers.data.Config;
import com.krazy.shulkers.data.MessageKeys;
import com.krazy.shulkers.manager.chestsort.IChestSortManager;
import com.krazy.shulkers.util.KSBPermission;
import com.krazy.shulkers.util.Cooldown;
import com.krazy.shulkers.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ShulkerManager {

    private final KrazyShulkers plugin;
    private HashMap<Inventory, ShulkerOpenData> openShulkerInventories = new HashMap<>();

    private IChestSortManager chestSortManager = new IChestSortManager() {
        
        @Override
        public void sort(Player player, Inventory shulkerInventory) {}

        @Override
        public void setSortable(Inventory shulkerInventory) {}
    };

    public ShulkerManager(KrazyShulkers plugin) {
        this.plugin = plugin;
    }

    public void openShulkerBoxInventory(Player player, ItemStack shulkerStack, SlotType slotType, int rawSlot) {
        Config config = plugin.getSettings();
        //permission check
        if (config.getBoolean("shulkers.requires_permission", true) &&
                !player.hasPermission(KSBPermission.OPEN_SHULKER.toString())) {
            player.sendRichMessage(MessageKeys.NO_PERMISSION.get());
            return;
        }

        // Cooldown check
        long cooldown = config.getLong("shulkers.cooldown", 5L) * 1000;

        if(!Cooldown.checkCooldown(player, "shulker-open", cooldown) && !player.hasPermission("voxshulkers.bypass.cooldown")) {
            player.sendMessage(MessageKeys.COOLDOWN.get("{time}", StringUtil.formattedTime(Cooldown.getCooldown(player, "shulker-open"), config)));
            return;
        }

        // close the player's current inventory if they have one open
        if (player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING||
                openShulkerInventories.containsKey(player.getOpenInventory().getTopInventory())) {
            player.closeInventory();
        }

        // Check end
        Cooldown.setCooldown(player, "shulker-open", cooldown);

        BlockStateMeta bsm = (BlockStateMeta) shulkerStack.getItemMeta();
        assert bsm != null;
        ShulkerBox shulker = (ShulkerBox) bsm.getBlockState();
        String title = plugin.getConfig().getString("shulkers.inventory_name");
        Inventory inventory = Bukkit.createInventory(null, InventoryType.SHULKER_BOX, StringUtil.getMiniMessage().deserialize(formatShulkerPlaceholder(title, shulkerStack)));
        inventory.setContents(shulker.getInventory().getContents());

        // Apply sort
        chestSortManager.sort(player, inventory);

        player.openInventory(inventory);
        ItemStack clone = shulkerStack.clone();
        openShulkerInventories.put(inventory, new ShulkerOpenData(clone, player.getLocation(), slotType, rawSlot));

        sendSoundAndMessage(player, shulkerStack, MessageSoundComb.OPEN);
    }

    public ItemStack closeShulkerBox(Player player, Inventory inventory, Optional<ItemStack> useStack) {
        player.getOpenInventory().getTopInventory();
        if (!openShulkerInventories.containsKey(inventory)) return null;
        ShulkerOpenData shulkerOpenData = openShulkerInventories.remove(inventory);

        ItemStack stackClone = shulkerOpenData.getItemStack();
        if (useStack.isPresent()) {
            stackClone = useStack.get();
        }

        if (player.getOpenInventory().getTopInventory().getType() == InventoryType.SHULKER_BOX) {
            player.closeInventory();
        }

        ItemStack targetItem = stackClone;
        boolean found = false;
        if (shulkerOpenData.getSlotType() == SlotType.HOTBAR) {
            ItemStack is = player.getInventory().getItemInMainHand();
            if (is.equals(stackClone)) {
                targetItem = is;
                found = true;
            }
        } else if (shulkerOpenData.getSlotType() == SlotType.INVENTORY) {
            ItemStack is = player.getInventory().getItem(shulkerOpenData.getRawSlot());
            if (is != null && is.equals(stackClone)) {
                targetItem = is;
                found = true;
            }
        }

        //Keep as fallback
        if (!found) {
            for (ItemStack is : player.getInventory().getContents()) {
                if (is != null && is.equals(stackClone)) {
                    found = true;
                    targetItem = is;
                    break;
                }
            }
        }
        if (!found) {
            plugin.getLogger().severe("WARNING! Player " + player.getName() + " closed a shulkerbox and changes were not saved!");
        }

        BlockStateMeta cMeta = (BlockStateMeta) targetItem.getItemMeta();
        ShulkerBox shulker = (ShulkerBox) cMeta.getBlockState();
        shulker.getInventory().setContents(inventory.getContents());
        cMeta.setBlockState(shulker);
        targetItem.setItemMeta(cMeta);
        player.updateInventory();
        sendSoundAndMessage(player, targetItem, MessageSoundComb.CLOSE);
        return targetItem;
    }

    public boolean isShulkerInventory(Inventory inv) {
        return openShulkerInventories.containsKey(inv);
    }

    public boolean doesPlayerHaveShulkerOpen(UUID uuid) {
        for (Inventory inv : openShulkerInventories.keySet()) {
            for (HumanEntity he : inv.getViewers()) {
                if (he.getUniqueId().equals(uuid)) {
                    return true;
                }
            }
        }
        return false;
    }

    public ItemStack getCorrespondingStack(Inventory inv) {
        ShulkerOpenData sod = openShulkerInventories.getOrDefault(inv, null);
        if (sod == null) return null;
        return sod.getItemStack();
    }

    public ShulkerOpenData getShulkerOpenData(Inventory inv) {
        return openShulkerInventories.getOrDefault(inv, null);
    }

    public void closeAllInventories(boolean disableCall) {
        HashMap<HumanEntity, Inventory> playersToCloseInventory = new HashMap<>();
        for (Inventory inventory : openShulkerInventories.keySet()) {
            for (HumanEntity he : inventory.getViewers()) {
                playersToCloseInventory.put(he, inventory);
            }
        }
        for (Map.Entry<HumanEntity, Inventory> entry : playersToCloseInventory.entrySet()) {
            Player player = (Player) entry.getKey();
            player.closeInventory();
            if (disableCall) {
                closeShulkerBox(player, entry.getValue(), Optional.empty());
            }
        }
    }

    private String getShulkerPlaceholderReplacement(ItemStack shulker) {
        if (shulker == null || shulker.isEmpty()) return "<white>invalid</white>";
        if (shulker.getItemMeta() == null || !shulker.getItemMeta().hasDisplayName())
            return StringUtil.getMiniMessage().serialize(InventoryType.SHULKER_BOX.defaultTitle());
        return StringUtil.getMiniMessage().serialize(shulker.getItemMeta().displayName());
    }
    private String formatShulkerPlaceholder(String message, ItemStack shulker) {
        if (message.isEmpty() || !message.contains("{shulker_name}")) return message;

        return message.replace("{shulker_name}",getShulkerPlaceholderReplacement(shulker));
    }

    private enum MessageSoundComb {
        OPEN,
        CLOSE
    }

    private void sendSoundAndMessage(Player player, ItemStack shulker, MessageSoundComb type) {
        Config config = plugin.getSettings();
        
        if(type.equals(MessageSoundComb.OPEN)) {
            player.sendRichMessage(MessageKeys.OPEN_MESSAGE.get("{shulker_name}", getShulkerPlaceholderReplacement(shulker)));
        } else {        	        	
            player.sendRichMessage(MessageKeys.CLOSE_MESSAGE.get("{shulker_name}", getShulkerPlaceholderReplacement(shulker)));
        }
        
        Sound toPlay = null;

        if(type.equals(MessageSoundComb.OPEN)) {
            String key = config.getString("sounds.open_sound", "minecraft:block.shulker_box.open").toLowerCase();
        	
            if(!key.contains(":")) key = "minecraft:" + key;
        	
            toPlay = Registry.SOUNDS.get(Key.key(key));
        } else {
            String key = config.getString("sounds.close_sound", "minecraft:block.shulker_box.close");
        	
            if(!key.contains(":")) key = "minecraft:" + key;
        	
            toPlay = Registry.SOUNDS.get(Key.key(key));
        }
        
        if(toPlay != null) player.playSound(player.getLocation(), toPlay, 0.5f, 1.0f);
    }

    public void setChestSortManager(IChestSortManager chestSortManager) {
        this.chestSortManager = chestSortManager;
    }
}