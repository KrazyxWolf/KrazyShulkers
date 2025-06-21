package com.krazy.shulkers.manager;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class ShulkerOpenData {

    private final ItemStack itemStack;
    private final Location openLocation;
    private final SlotType slotType;
    private final int rawSlot;

    public ShulkerOpenData(ItemStack itemStack, Location openLocation, SlotType slotType, int rawSlot) {
        this.itemStack = itemStack;
        this.openLocation = openLocation;
        this.slotType = slotType;
        this.rawSlot = rawSlot;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
    
    public Location getOpenLocation() {
        return openLocation;
    }

    public SlotType getSlotType() {
        return slotType;
    }

    public int getRawSlot() {
        return rawSlot;
    }
}