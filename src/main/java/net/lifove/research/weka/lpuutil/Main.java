package net.lifove.research.weka.lpuutil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.lifove.research.utils.WekaUtils;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.Remove;

public class Main {
	private static final int TYPE_POS	= 0;
	private static final int TYPE_UNLABEL	= 1;
	private static final int TYPE_TEST	= 2;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		for (String arg: args)
			System.out.println(arg);
		
		String option = args[0];
		
		if (option.compareToIgnoreCase("-classify") == 0)
			classify(args);
		else if (option.compareToIgnoreCase("-editdistance") == 0)
			calculateEditDistance(args);
		else if (option.compareToIgnoreCase("-selectTop") == 0)
			selectTop(args);
		else if (option.compareToIgnoreCase("-transform") == 0)
			transform(args);
		else if (option.compareToIgnoreCase("-attribute") == 0)
			countAttributes(args);
	}
	
	private static void classify(String[] args)
	{
		String classifierName = args[1];
		String trainingFile = args[2];
		String testFile = args[3];
		
		Instances trainingSet = readDataset(trainingFile);
		Instances testSet = readDataset(testFile);
		
		Remove rm = new Remove();
		int[] removecIdx = new int[3];
		removecIdx[0] = 0; removecIdx[1] = 1; removecIdx[2] = 2;
		rm.setAttributeIndicesArray(removecIdx);
		
		
		FilteredClassifier classifier = new FilteredClassifier();
		classifier.setFilter(rm);
		
		if (classifierName.compareToIgnoreCase("rf") == 0)
			classifier.setClassifier(new RandomForest());
		else if (classifierName.compareToIgnoreCase("nb") == 0)
			classifier.setClassifier(new NaiveBayes());
		
		try {
			classifier.buildClassifier(trainingSet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		for (int i = 0; i < testSet.numInstances(); i++)
		{
			double pred;
			double dist[];
			
			try {
				pred = classifier.classifyInstance(testSet.instance(i));
				dist = classifier.distributionForInstance(testSet.instance(i));
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			
			if (pred != 0)
				continue;
			
			String signature = testSet.instance(i).stringValue(0) + "," + (int)testSet.instance(i).value(1) + ",1," + (int)testSet.instance(i).value(2);
			System.out.println(signature + "," + testSet.classAttribute().value((int) pred) + "," + dist[(int) pred]);
		}
	}
	
	private static void calculateEditDistance(String[] args)
	{
		String inputFile = args[1];
		String outputFile = args[2];
		
		List<InstanceVector> buggyInstances = readBuggyInstances(inputFile);
		calculatedDistance(inputFile, buggyInstances, outputFile);
	}
	
	private static List<InstanceVector> readBuggyInstances(String input)
	{
		String thisLine = null;
		List<InstanceVector> buggyInstances = new ArrayList<InstanceVector>();

		// Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(input));
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				
				if (thisLine.toLowerCase().indexOf("\"buggy\"") != -1)
				{
					InstanceVector newInstance = InstanceVector.extractFromArff(thisLine);
					buggyInstances.add(newInstance);
				}
			} // end while
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
		}
		
		return buggyInstances;
	}
	
	private static void calculatedDistance(String input, List<InstanceVector> buggyInstances, 
			String output)
	{
		String thisLine = null;

		// Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(input));
			
			File file= new File(output);
			FileOutputStream fos = new FileOutputStream(file);
			DataOutputStream dos=new DataOutputStream(fos);
			
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				
				if (thisLine.toLowerCase().indexOf("\"clean\"") != -1)
				{
					InstanceVector newInstance = InstanceVector.extractFromArff(thisLine);
					double shortestDist = InstanceVector.calculateShortestDistance(buggyInstances, 
							newInstance);
					
					Line line = Line.extractFromArff(thisLine);
					DistanceLine distanceLine = new DistanceLine(line, shortestDist);
					
					dos.write((distanceLine.toString() + "\n").getBytes());
				}
			} // end while
			
			dos.close();
			fos.close();
			
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
		}
	}
	
	private static void selectTop(String[] args)
	{
		String inputFile = args[1];
		String outputFile = args[2];
		int top = Integer.parseInt(args[3]);
		
		List<DistanceLine> lines = DistanceLine.loadFile(inputFile);
		Collections.sort(lines);
		
		int pos = lines.size() * top / 100;
		double distance = lines.get(pos).getDistance();
		
		DistanceLine.writeFileShorterThan(outputFile, lines, distance);
	}
	
	private static void transform(String[] args)
	{
		String to = args[1];
		String inputFile = args[2];
		String outputFile = args[3];
		String classAttributeName = args[4];
		String posLabel = args[5];
		
		
		
		if (to.compareToIgnoreCase("lpu") == 0)
		{
			Instances instances = WekaUtils.loadArff(inputFile, classAttributeName);
			int posLabelIndex = instances.attribute(classAttributeName).indexOfValue(posLabel);
			String negLabel = WekaUtils.getNegClassStringValue(instances, classAttributeName, posLabel);
			
			// transform for positive / unlabel / test LPU datasets
			transformToLPU(inputFile, outputFile + ".pos", TYPE_POS,posLabelIndex,posLabel,negLabel);
			transformToLPU(inputFile, outputFile + ".unlabel", TYPE_UNLABEL,posLabelIndex,posLabel,negLabel);
			transformToLPU(inputFile, outputFile + ".test", TYPE_TEST,posLabelIndex,posLabel,negLabel);
		}
		else
		{
			String arffFile = args[4];
			classAttributeName = args[5];
			posLabel = args[6];
			
			Instances instances = WekaUtils.loadArff(arffFile, classAttributeName);
			int posLabelIndex = instances.attribute(classAttributeName).indexOfValue(posLabel);
			String negLabel = WekaUtils.getNegClassStringValue(instances, classAttributeName, posLabel);
			
			
			int classIndex = instances.classIndex(); 
			
			String prepend = readARFFPrepend(arffFile,classAttributeName,posLabel,posLabelIndex);
			transformToARFF(inputFile, outputFile, classIndex, prepend,posLabel,negLabel,posLabelIndex);
		}
	}
	
	private static void transformToLPU(String input, String output, int type,int posLabelIndex,String posLabel,String negLabel)
	{
		String thisLine = null;
		boolean bFoundData = false;

		// Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(input));
			
			File file= new File(output);
			FileOutputStream fos = new FileOutputStream(file);
			DataOutputStream dos=new DataOutputStream(fos);
			
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				
				// check if the loop can process data lines.
				if (bFoundData == false && thisLine.toLowerCase().indexOf("@data") != -1)
				{
					bFoundData = true;
					continue;
				}
				
				if (bFoundData == false || thisLine.equals(""))
					continue;
				
				// process data lines
				String transformed = transformToLPU(thisLine.trim(),posLabelIndex,posLabel,negLabel);
				String writingStr = null;
				if (type == TYPE_POS)
				{
					if (transformed.startsWith("+1 ") == false)
						continue;
					
					writingStr = transformed.substring(3);
				}
				else if (type == TYPE_UNLABEL)
				{
					if (transformed.startsWith("-1 ") == false)
						continue;
					
					writingStr = transformed.substring(3);
				}
				else
					writingStr = transformed;
				
				dos.write((writingStr + "\n").getBytes());
			} // end while
			
			dos.close();
			fos.close();
			
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
		}
	}
	
	private static String readARFFPrepend(String input,String classAttributeName,String posLabel,int posValueIndex)
	{
		boolean bFoundData = false;
		String thisLine = null;
		String content = null;
		
		Instances instances = WekaUtils.loadArff(input);

		// Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(input));
			
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				
				// the content includes by @data
				if (bFoundData == false && thisLine.toLowerCase().indexOf("@data") != -1)
				{
					content += "\n" + thisLine;
					bFoundData = true;
					continue;
				}
				
				String negLabel = WekaUtils.getNegClassStringValue(instances, classAttributeName, posLabel);
				int negValueIndex = WekaUtils.getClassValueIndex(instances,negLabel);
					
				// the content includes only positive instances
				if (bFoundData == true && 
						(
								(thisLine.toLowerCase().indexOf(posLabel.toLowerCase()) == -1 && negValueIndex==0)
								||
								(thisLine.toLowerCase().indexOf(negLabel.toLowerCase()) != -1)
						)
					)
					continue;
				
				if (content == null)
					content = thisLine;
				else
					content += "\n" + thisLine;
			} // end while
			
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
		}
		
		return content;
	}
	
	private static int getClassIndex(String input)
	{
		boolean bFoundData = false;
		String thisLine = null;
		int index = -1;

		// Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(input));
			
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				if (bFoundData == false && thisLine.toLowerCase().indexOf("@data") != -1)
				{
					bFoundData = true;
					continue;
				}
				
				if (bFoundData == false)
					continue;
				
				index = findClassIndex(thisLine);
				if (index != -1)
					break;
			} // end while
			
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
		}
		
		return index;
	}
	
	private static int findClassIndex(String line)
	{
		Pattern p = Pattern.compile("[1-9][0-9]+");
		Matcher m = p.matcher(line);
		
		int index = -1;
		while(m.find() == true)
		{
			index = Integer.parseInt(m.group());
		}
		
		return index;
	}
	
	/**
	 * @param input lpu file
	 * @param output the name of new arff file labeled by lpu
	 * @param classIndex classIndex
	 * @param prepend arff structure + positive instances
	 */
	private static void transformToARFF(String input, String output, int classIndex, String prepend,String posLabel,String negLabel,int posLabelIndex)
	{
		String thisLine = null;

		// Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(input));
			
			File file= new File(output);
			FileOutputStream fos = new FileOutputStream(file);
			DataOutputStream dos=new DataOutputStream(fos);
			
			dos.write((prepend + "\n").getBytes());
			
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				String transformed = transformToARFF(thisLine.trim(),classIndex,posLabel,negLabel,posLabelIndex);
				dos.write((transformed + "\n").getBytes());
			} // end while
			
			dos.close();
			fos.close();
			
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
		}
	}
	
	private static String transformToLPU(String line,int posLabelIndex,String posLabel,String negLabel)
	{
		String content = line.substring(1, line.length() - 1);
		String[] contents = content.split(",");
		String returnStr = "";
		
		if (contents[contents.length - 1].indexOf(posLabel) != -1)
			returnStr = "+1";
		
		if (contents[contents.length - 1].indexOf(negLabel) != -1)
			returnStr = "-1";
		
		// no label value means the index of instance label value is o
		if(returnStr.equals("")){
			if(posLabelIndex==0)
				returnStr = "+1";
			else
				returnStr = "-1";
		}
			
		for (int i = 3; i < contents.length - 1; i++)
			returnStr += " " + contents[i].replaceFirst(" ", ":");
		
		return returnStr;
	}
	
	private static String transformToARFF(String line, int classIndex,String posLabel,String negLabel,int posLabelIndex)
	{
		String[] contents = line.split(" ");
		String returnStr = null;
		
		int idxBegin = 0;
		String classStr = "\"" + negLabel + "\"";
		
		if (contents[0].compareTo("+1") == 0)
		{
			idxBegin++;
			classStr = "\"" + posLabel + "\"";
		}
		
		returnStr = "{"; //1 0,2 0";
		
		for (int i = idxBegin; i < contents.length; i++)
		{
			if (returnStr == null)
				returnStr = contents[i].replaceFirst(":", " ");
			else
				returnStr += "," + contents[i].replaceFirst(":", " ");
		}
		
		if(posLabelIndex==0)
			returnStr += "," + classIndex + " "+ classStr + "}";
		else
			returnStr += "}";
		
		return returnStr;
	}
	
	private static void countAttributes(String args[])
	{
		String inputFile = args[1];
		int noAttributes = countAttributes(inputFile);
		
		System.out.println(inputFile + ": " + noAttributes);
	}
	
	private static int countAttributes(String arffFile)
	{
		String thisLine = null;
		int noAttribute = 0;

		// Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(arffFile));
			
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				if (thisLine.toLowerCase().indexOf("@data") != -1)
					break;
				
				if (thisLine.toLowerCase().indexOf("@attribute") != -1)
					noAttribute++;
			} // end while
			
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
		}
		
		return noAttribute;
	}
	
	private static Instances readDataset(String filename)
	{
		DataSource source = null;
		Instances dataset = null;
		
		try {
			source = new DataSource(filename);
			dataset = source.getDataSet();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (dataset == null)
		{
			System.out.println("Cannot load file!");
			return null;
		}
		
		dataset.setClassIndex(dataset.numAttributes() - 1);
		return dataset;
	}
}
