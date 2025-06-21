package com.krazy.shulkers.util;

public class NumberUtil {
    
    public static long divideExact(double n1, double n2) {
    	Double result = n1 / n2;
    	
    	if(result < 0.0) return 0;
    	
    	return result.longValue();
    }
}