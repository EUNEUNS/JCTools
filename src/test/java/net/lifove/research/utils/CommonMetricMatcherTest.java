package net.lifove.research.utils;

import static org.junit.Assert.*;
import net.lifove.research.utils.MatrixUtil;

import org.junit.Test;

public class CommonMetricMatcherTest {

	@Test
	public void testMatch() {
		String path = System.getProperty("user.home") + "/Documents/HDP/data/NASA/" ;
		String[] args = {"NASA4",path + "PC5.arff","NASA5",path + "MC2.arff"};
		CommonMetricMatcher.match(args);
		
		
		args[2] = "NASA5";
		args[3] = path + "MC2.arff";
		CommonMetricMatcher.match(args);
	}
}
