package net.lifove.research.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.primitives.Doubles;

/**
 * This class provides useful methods for array related variables/objects.
 * @author JC
 *
 */
public class ArrayListUtil {
	
	/**
	 * To get a index from the string array of metric names with a specific metric name
	 * 
	 * @param metricName the metric name we want to know its index in the string array
	 * @param metricNames String array that contains all metric names
	 * @return index for metricName
	 */
	public static int getMetricIndex(String metricName,String[] metricNames){
		
		for(int i=0; i<metricNames.length;i++){
			if(metricNames[i].equals(metricName))
				return i;
		}		
		
		return -1;
	}
	
	/**
	 * To get a line with the biggest value in a certain column.
	 * 
	 * @param lines all lines of ArrysString<String>
	 * @param idxForLookAtValue
	 * @param valueNotConsidering
	 * @return a line with the best value in a designated column
	 */
	public static String getLineWithBestValue(ArrayList<String> lines,int idxForLookAtValue, double valueNotConsidering){
		
		double bestValueLookAt = -1.0;
		String bestLineReturn = "";
		
		for(int i=0;i<lines.size();i++){
		
			String[] splitLine = lines.get(i).split(",");
			
			double lookAtValue = Double.parseDouble(splitLine[idxForLookAtValue]);
			
			if(lookAtValue>bestValueLookAt && lookAtValue!=lookAtValue){
				bestValueLookAt = lookAtValue;
				bestLineReturn = lines.get(i);
			}
		}
		
		return bestLineReturn;
	}
	
	/**
	 * Merge two ArrayLists by a key. 
	 * 
	 * @param listOne the first ArrayList to be merged
	 * @param listTwo the second ArrayList merged with listOne
	 * @param keyIndex the index of the key string in an element (line splitting with commas). We assume that both listOne and listTow have the key in the same index
	 * @param insertingIndex listTwo without key will be inserted from InseringIndex of listOne.
	 * @return Merged ArrayList will be returned. (an element is the line string splitting with commas)
	 */
	static public ArrayList<String> mergeArrayListsHorizontally(ArrayList<String> listOne, ArrayList<String> listTwo,int keyIndex,int insertingIndex){
		ArrayList<String> mergedArrayList = new ArrayList<String>();
		HashMap<String,String> hashMapForMerge = getHashMapFromArrayList(listTwo,0);
		
		for(String lineOne:listOne){
			String[] splittingLine = lineOne.split(","); 
			String key = splittingLine[keyIndex];
			String lineTwo = hashMapForMerge.get(key);
			
			mergedArrayList.add(mergeStringLines(lineOne,lineTwo,keyIndex,insertingIndex));
		}
			
		return mergedArrayList;
	}
	
	/**
	 * To merge two ArrayList<String> with the same number of lines horizontally
	 * 
	 * @param linesOne First ArrayList<String>
	 * @param linesTwo Second ArrayList<String>
	 * @return
	 */
	static public ArrayList<String> mergeSameLengthArrayList(ArrayList<String> linesOne,ArrayList<String> linesTwo){
		
		if(linesOne.size()!=linesTwo.size()){
			System.out.println("Two array lists should have the same size. Exit!");
			System.exit(0);
		}
		
		for(int i=0;i<linesOne.size();i++){
			linesOne.set(i, linesOne.get(i) +"," + linesTwo.get(i));
		}
		
		return linesOne; 
	}
	
	static public String mergeStringLines(String lineOne,String lineTwo,int keyIndex,int insertingIndex){
		String mergedLine = "";
		String[] splittingLine = lineOne.split(",");
		String lineTwoWOKey = removeColumn(lineTwo,keyIndex);
		
		if(splittingLine.length <= insertingIndex)		
			mergedLine = lineOne + "," + lineTwoWOKey;
		else{
			mergedLine = getMergedLine(lineOne,lineTwoWOKey,insertingIndex);
		}
		
		return mergedLine;
	}
	
	static String getMergedLine(String lineOne, String lineTwo, int insertingIndex){
		ArrayList<String> lstLineOne = new ArrayList<String>(Arrays.asList(lineOne.split(",")));
		ArrayList<String> lstLineTwo = new ArrayList<String>(Arrays.asList(lineTwo.split(",")));
		
		for(int i=0; i < lstLineTwo.size(); i++){
			lstLineOne.add(insertingIndex+i,lstLineTwo.get(i));
		}
		
		return strArrayListToString(lstLineOne); 
	}
	
	static String removeColumn(String line,int index){
		
		ArrayList<String> splittingLine = new ArrayList<String>(Arrays.asList(line.split(",")));
		
		splittingLine.remove(index);

		return strArrayListToString(splittingLine);
	}
	
	static void removeColumn(ArrayList<String> lines,int index){
		for(int i=0; i<lines.size();i++){
			lines.set(i, removeColumn(lines.get(i),index));
		}
	}
	
	static String strArrayListToString(ArrayList<String> lstLine){
		String line = "";
		
		for(int i=0; i < lstLine.size(); i++){
			line = line + lstLine.get(i);
			
			if(i!=lstLine.size()-1)
				line = line + ",";
		}
		
		return line;
	}
	
	static public HashMap<String,String> getHashMapFromArrayList(ArrayList<String> lines,int keyIndex){
		HashMap<String,String> map = new HashMap<String,String>();
		
		for(String line: lines){
			String key = line.split(",")[keyIndex];
			map.put(key, line);
		}
		
		return map;
	}
	
	static public ArrayList<ArrayList<String>> valuesInColumns(ArrayList<String> lines){
	
		ArrayList<ArrayList<String>> valuesInColumns = new ArrayList<ArrayList<String>>();
		
		for(int row=0; row < lines.size();row++){
			String[] values = lines.get(row).replace("<,>","<>").split(",");
			for(int i=0;i<values.length;i++){
				
				if(valuesInColumns.size() <= i){
					valuesInColumns.add(new ArrayList<String>());
				}
				
				valuesInColumns.get(i).add(values[i]);
			}
		}

		return valuesInColumns;
	}
	
	static public ArrayList<String> sumInColumns(ArrayList<String> lines){
	
		ArrayList<ArrayList<String>> valuesInColumns = valuesInColumns(lines);
		ArrayList<String> result = new ArrayList<String>();
		
		for(int i=0;i< valuesInColumns.size();i++){
			ArrayList<String> values = valuesInColumns.get(i);
			Double sum = 0.0;
			for(String value:values){
				try{
					sum = sum + Double.parseDouble(value);
				}catch(Exception e){
					sum = null;
					break;
				}
			}
			result.add(sum+"");
		}
		
		return result;
	}
	
	static public ArrayList<String> minInColumns(ArrayList<String> lines){
		
		ArrayList<ArrayList<String>> valuesInColumns = valuesInColumns(lines);
		ArrayList<String> result = new ArrayList<String>();
		
		for(int i=0;i< valuesInColumns.size();i++){
			ArrayList<String> values = valuesInColumns.get(i);
			Double min = null;
			for(String value:values){
				try{
					
					Double currentValue = Double.parseDouble(value);
					
					if(min==null)
						min = currentValue;
					
					if (min > currentValue)
						min = currentValue;
					
				}catch(Exception e){
					min = null;
					break;
				}
			}
			result.add(min+"");
		}
		
		return result;
	}
	
	static public ArrayList<String> maxInColumns(ArrayList<String> lines){
		
		ArrayList<ArrayList<String>> valuesInColumns = valuesInColumns(lines);
		ArrayList<String> result = new ArrayList<String>();
		
		for(int i=0;i< valuesInColumns.size();i++){
			ArrayList<String> values = valuesInColumns.get(i);
			Double max = null;
			
			for(String value:values){
				try{
					
					Double currentValue = Double.parseDouble(value);
					
					if(max==null)
						max = currentValue;
					
					if (max < currentValue)
						max = currentValue;
					
				}catch(Exception e){
					max = null;
					break;
				}
			}
			result.add(max+"");
		}
		
		return result;
	}
	
	static public ArrayList<String> averageInColumns(ArrayList<String> lines){
		
		ArrayList<ArrayList<String>> valuesInColumns = valuesInColumns(lines);
		ArrayList<String> result = new ArrayList<String>();
		
		for(int i=0;i< valuesInColumns.size();i++){
			ArrayList<String> values = valuesInColumns.get(i);
			Double sum = 0.0;
			for(String value:values){
				try{
					sum = sum + Double.parseDouble(value);
				}catch(Exception e){
					sum = null;
					break;
				}
			}
			if(sum==null)
				result.add(sum+"");
			else
				result.add(sum/values.size()+"");
		}
		
		return result;
	}
	
	static public double getAverage(ArrayList<Double> values){
		double sum=0;
		
		for(double value:values){
			sum+=value;
		}
		return sum/values.size();
	}
	
	static public double getMedian(ArrayList<Double> values){
		DescriptiveStatistics stat = new DescriptiveStatistics(getDoublePrimitive(values));	
		return stat.getPercentile(50);
	}

	static public double getMedianFromIntegerArrayList(ArrayList<Integer> values){
		DescriptiveStatistics stat = new DescriptiveStatistics(getDoublePrimitiveFromIntegerArrayList(values));	
		return stat.getPercentile(50);
	}
	
	static public double getMeanFromIntegerArrayList(ArrayList<Integer> values){
		DescriptiveStatistics stat = new DescriptiveStatistics(getDoublePrimitiveFromIntegerArrayList(values));	
		return stat.getMean();
	}
	
	public static double[] getDoublePrimitive(ArrayList<Double> values) {
		/*double[] primValues = new double[values.size()];
		for(int i=0; i< values.size();i++)
			primValues[i] = values.get(i);*/
		return Doubles.toArray(values);
	}
	
	public static double[] getDoublePrimitiveFromIntegerArrayList(ArrayList<Integer> values) {
		double[] primValues = new double[values.size()];
		for(int i=0; i< values.size();i++)
			primValues[i] = values.get(i);
		return primValues;
	}
	
	public static int[] getIntPromitive(ArrayList<Integer> values){
		int[] primValues = new int[values.size()];
		for(int i=0; i< values.size();i++)
			primValues[i] = values.get(i);
		return primValues;
	}
	
	public static List<Integer> sortByValue(final Map<Integer,Double> map){
		List<Integer> list = new ArrayList<Integer>();
		list.addAll(map.keySet());

		Collections.sort(list,new Comparator<Object>(){

			@SuppressWarnings("unchecked")
			public int compare(Object o1,Object o2){
				Object v1 = map.get(o1);
				Object v2 = map.get(o2);

				return ((Comparable<Object>) v1).compareTo(v2);
			}

		});
		Collections.reverse(list); // if commented, asc
		return list;
	}
	

	public static String getCommaSeperatedString(ArrayList<Integer> values,int offset) {
		
		String strValues = "";
		
		for(Integer value:values){
			strValues += (value+1) + ",";
		}
		
		return strValues;
	}
	 
}
