package net.lifove.research.utils;

import java.io.File;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.MathExpression;

public class SingleValueTransformer {

	/**
	 * @param args 0: datafile path, 1: tranformation option 
	 */
	public static void main(String[] args) {
		new SingleValueTransformer().run(args);
	}

	private void run(String[] args) {
		String filePath = args[1];
		String dirPath = filePath.substring(0,filePath.lastIndexOf(File.separator));
		String fileName =  args[0];
		
		Instances instances = WekaUtils.loadArff(filePath);
		Instances trasformedData = null;
		
		MathExpression mathFilter = new MathExpression();
		
		// +, -, *, /, pow, log,
		//  abs, cos, exp, sqrt, tan, sin, ceil, floor, rint, (, ), 
		//  MEAN, MAX, MIN, SD, COUNT, SUM, SUMSQUARED, ifelse
		String[] expressions = {"A*A","sqrt(A)","log(A)","exp(A)","cos(A)","tan(A)","sin(A)","ceil(A)","floor(A)","A/MEAN"};
		String[] expName = {"square","root","log","exp","cos","tan","sin","ceil","floor","divideByMean"};
		
		for(int i=0; i<expressions.length;i++){
			mathFilter.setExpression(expressions[i]);
			try {
				mathFilter.setInputFormat(instances);
				trasformedData = Filter.useFilter(instances, mathFilter);
			} catch (Exception e) {
				e.printStackTrace();
			}
			WekaUtils.writeADataFile(trasformedData, dirPath + File.separator + fileName + "_" + expName[i] + ".arff");
		}
	}

}
