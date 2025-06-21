package com.krazy.shulkers.util;

public enum KSBPermission {

    ADMIN("krazyshulkers.admin"),
    OPEN_SHULKER("krazyshulkers.use"),
    RECEIVE_ALERTS("krazyshulkers.notify"),
    BYPASS_COOLDOWN("krazyshulkers.bypasscooldown");

    private String value;

    private KSBPermission(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}