package net.lifove.research.utils;

import java.text.DecimalFormat;

public class DecimalUtil {

	public static String twoDecimal(double number){
		DecimalFormat dec = new DecimalFormat("0.00");
		return dec.format(number);
	}
	
	public static String threeDecimal(double number){
		DecimalFormat dec = new DecimalFormat("0.000");
		return dec.format(number);
	}
}
