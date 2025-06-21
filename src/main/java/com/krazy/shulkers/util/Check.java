package com.krazy.shulkers.util;

import org.bukkit.Location;

import com.krazy.shulkers.KrazyShulkers;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

public class Check {

	private static final KrazyShulkers plugin = KrazyShulkers.getInstance();
	
	public static boolean isWorldDisabled(String world, String e) {
		for(String s : plugin.getSettings().getStringList("events." + e + ".disabled_worlds")) {
			if(s == null) return false;
			if(world.equals(s)) return true;
		}
		return false;
	}
	
	public static boolean isRegionDisabled(Location l, String e) {
		RegionContainer cont = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionQuery query = cont.createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(l));
		
		for(String s : plugin.getSettings().getStringList("events." + e + ".disabled_regions")) {
			if(s == null) return false;
			for(ProtectedRegion region : set) {
				if(region.getId().equalsIgnoreCase(s)) return true;
			}
		}
		return false;
	}
}