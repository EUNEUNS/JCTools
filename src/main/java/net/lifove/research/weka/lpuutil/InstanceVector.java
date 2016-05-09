package net.lifove.research.weka.lpuutil;

import java.util.List;

public class InstanceVector {
	public static final int START_IDX_ARFF	= 3;
	
	private InstanceItem[] items;	// sorted by index
	
	public InstanceVector(InstanceItem[] items) {
		super();
		this.items = items;
	}
	
	public InstanceItem[] getItems() {
		return items;
	}
	public void setItems(InstanceItem[] items) {
		this.items = items;
	}
	
	public static InstanceVector extractFromArff(String line)
	{
		String content = line.substring(1, line.length() - 1);
		String[] contents = content.split(",");
		
		int size = contents.length - 1 - START_IDX_ARFF;
		InstanceItem[] items = new InstanceItem[size];
		
		int idx = 0;
		for (int i = START_IDX_ARFF; i < contents.length - 1; i++)
		{
			String[] innerContents = contents[i].split(" ");
			items[idx++] = new InstanceItem(innerContents);
		}
		
		return new InstanceVector(items);
	}
	
	public static double calculateShortestDistance(List<InstanceVector> instances, 
			InstanceVector target)
	{
		double shortestDist = -1;
		for (InstanceVector curr: instances)
		{
			double currDist = calculateDistanceManhattan(curr, target);
			if (shortestDist == -1 || shortestDist > currDist)
				shortestDist = currDist;
		}
		
		return shortestDist;
	}

	public static double calculateDistanceManhattan(InstanceVector first, InstanceVector second)
	{
		double distance = 0;
		distance = calculateDistanceManhattan(first, second, true);
		distance += calculateDistanceManhattan(second, first, false);
		
		return distance;
	}
	
	private static double calculateDistanceManhattan(InstanceVector ori, InstanceVector target, 
			boolean bChkMatching)
	{
		double distance = 0;
		for (InstanceItem currItem: ori.getItems())
		{
			int currDist = currItem.getValue();
			for (InstanceItem targetItem: target.getItems())
			{
				if (targetItem.getIdx() > currItem.getIdx())
					break;
				
				else if (targetItem.getIdx() == currItem.getIdx())
				{
					if (bChkMatching == true)
						currDist = Math.abs(targetItem.getValue() - currItem.getValue());
					else
						currDist = 0;
					break;
				}
			}
			
			distance += currDist;
		}
		
		return distance;
	}
}
