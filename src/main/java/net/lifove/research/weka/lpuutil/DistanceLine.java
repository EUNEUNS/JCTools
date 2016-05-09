package net.lifove.research.weka.lpuutil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DistanceLine implements Comparable<DistanceLine> {
	private Line line;
	private double distance;
	public DistanceLine(Line line, double distance) {
		super();
		this.line = line;
		this.distance = distance;
	}
	public Line getLine() {
		return line;
	}
	public void setLine(Line line) {
		this.line = line;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(int distance) {
		this.distance = distance;
	}
	
	@Override
	public String toString() {
		return this.getLine().toString() + "," + this.getDistance();
	}
	
	@Override
	public int compareTo(DistanceLine other) {
		// TODO Auto-generated method stub
		return (int)(this.getDistance() - other.getDistance());
	}
	
	public static void writeFile(String path, List<DistanceLine> aLines)
	{
		if (path == null)
			return;
		
		try {
			File file= new File(path);
			FileOutputStream fos = new FileOutputStream(file);
			DataOutputStream dos=new DataOutputStream(fos);
			
			for (DistanceLine line: aLines)
			{
				if (line.getLine().isEmpty() == true)
					continue;
				
				dos.write((line.toString() + "\n").getBytes());
			}
			
			//dos.writeBytes();
			dos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public static void writeFileShorterThan(String path, List<DistanceLine> aLines, double distance)
	{
		if (path == null)
			return;
		
		try {
			File file= new File(path);
			FileOutputStream fos = new FileOutputStream(file);
			DataOutputStream dos=new DataOutputStream(fos);
			
			for (DistanceLine line: aLines)
			{
				if (line.getLine().isEmpty() == true)
					continue;
				
				if (line.getDistance() <= distance)
					dos.write((line.toString() + "\n").getBytes());
			}
			
			//dos.writeBytes();
			dos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public static List<DistanceLine> loadFile(String path)
	{
		List<DistanceLine> lineInfo = new ArrayList<DistanceLine>();
		String thisLine = null;

		// Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				String[] contents = thisLine.split("\\,");
				
				String linePath = contents[0];
				long lineRevision = Long.parseLong(contents[1]);
				// int lineNumber = Integer.parseInt(contents[2]);
				int linePos = Integer.parseInt(contents[3]);
				double lineDist = Double.parseDouble(contents[4]);
				
				lineInfo.add(new DistanceLine(new Line(linePath, lineRevision, linePos), lineDist));
			} // end while
			
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
		}
		
		return lineInfo;
	}
}
