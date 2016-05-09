package net.lifove.research.utils;

import java.util.ArrayList;

public class MetricValueAggregator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String[] params = {"data/class_sopra01.csv",""};
		
		MetricValueAggregator runner = new MetricValueAggregator();
		runner.run(params);
		params[0] = "data/class_sopra02.csv";
		runner.run(params);
		params[0] = "data/class_sopra03.csv";
		runner.run(params);
		params[0] = "data/class_sopra04.csv";
		runner.run(params);
		params[0] = "data/class_sopra05.csv";
		runner.run(params);
		params[0] = "data/class_sopra06.csv";
		runner.run(params);
		params[0] = "data/class_sopra07.csv";
		runner.run(params);
		params[0] = "data/class_sopra08.csv";
		runner.run(params);
		params[0] = "data/class_sopra09.csv";
		runner.run(params);
		params[0] = "data/class_sopra10.csv";
		runner.run(params);
	}

	void run(String[] args){
		String file=args[0];
		String header = FileUtil.getFirstLine(file);
		ArrayList<String> body = FileUtil.getLines(file,true);
		
		ArrayList<String> maxValues = ArrayListUtil.maxInColumns(body);
		ArrayList<String> minValues = ArrayListUtil.minInColumns(body);
		ArrayList<String> averageValues = ArrayListUtil.averageInColumns(body);
		ArrayList<String> sumValues = ArrayListUtil.sumInColumns(body);

		header = header.replace(",",",MAX") + "," + header.replace(",",",MIN") + "," + header.replace(",",",AVG") + "," + header.replace(",",",SUM");
		maxValues.addAll(minValues);
		maxValues.addAll(averageValues);
		maxValues.addAll(sumValues);
		
		//System.out.println(header);
		for(String value:maxValues){
			System.out.print(value + ",");
		}
		System.out.println();
	}
}
