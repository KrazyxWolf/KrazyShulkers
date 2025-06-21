package com.krazy.shulkers.util;

import com.krazy.shulkers.data.Config;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

public class StringUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().tags(StandardTags.defaults()).build();

    public static MiniMessage getMiniMessage() {
        return MINI_MESSAGE;
    }

    public static String formattedTime(long millis, Config config) {
    	long days = NumberUtil.divideExact(millis, 86400000);
    	long hours = NumberUtil.divideExact(millis, 3600000);
    	long minutes = NumberUtil.divideExact(millis, 60000);
    	long seconds = NumberUtil.divideExact(millis, 1000);

        String d = config.getString("units.day", "d");
        String h = config.getString("units.hour", "h");
        String m = config.getString("units.minute", "m");
        String s = config.getString("units.second", "s");
    	
    	if(days > 0) {
    		long hour = hours - (days * 24);
    		long minute = minutes - (hours * 60);
    		long second = seconds - (minutes * 60);
    		return String.format("%02d" + d + " %02d" + h + " %02d" + m + " %02d" + s, days, hour, minute, second);
    	} else if(hours > 0) {
    		return String.format("%02d" + h + " %02d" + m + " %02d" + s, hours, minutes - (hours * 60), seconds - (minutes * 60));
    	} else if(minutes > 0) {
    		return String.format("%02d" + m + " %02d" + s, minutes, seconds - (minutes * 60));
    	} else {
    		return seconds + s;
    	}
    }
}