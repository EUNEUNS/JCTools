package net.lifove.research.utils;

import static org.junit.Assert.*;
import net.lifove.research.utils.DecimalUtil;
import net.lifove.research.utils.PredictorBasedOnTokenFrequency;

import org.junit.Test;

public class PredictorBasedOnTokenFrequencyTest {

	@Test
	public void test() {
		String probabilityCutoff="0.50";
		String[] args = {"/Users/JC/Documents/UW/ODP/data/keep_all_tokens/lucene/train_matrix.csv.arff",
				"/Users/JC/Documents/UW/ODP/data/keep_all_tokens/lucene/test_matrix.csv.arff",probabilityCutoff};
		
		//PredictorBasedOnTokenFrequency.main(args);

		//args[0] = "/Users/JC/Documents/UW/ODP/data/keep_all_tokens/eclipse/train_matrix.csv.arff";
		//args[1] = "/Users/JC/Documents/UW/ODP/data/keep_all_tokens/eclipse/test_matrix.csv.arff";
		//PredictorBasedOnTokenFrequency.main(args);
		
		//args[0] = "/Users/JC/Documents/UW/ODP/data/keep_all_tokens/jackrabbit/train_matrix.csv.arff";
		//args[1] = "/Users/JC/Documents/UW/ODP/data/keep_all_tokens/jackrabbit/test_matrix.csv.arff";
		//PredictorBasedOnTokenFrequency.main(args);
		
		//args[0]="/Users/JC/Documents/UW/ODP/data/AST_frequency/lucene/train_lucene_matrix_frequence.csv.arff";
		//args[1]="/Users/JC/Documents/UW/ODP/data/AST_frequency/lucene/test_lucene_matrix_frequence.csv.arff";
		//PredictorBasedOnTokenFrequency.main(args);
		
		//args[0]="/Users/JC/Documents/UW/ODP/data/AST_frequency/eclipse/train_eclipse_matrix_frequence.csv.arff";
		//args[1]="/Users/JC/Documents/UW/ODP/data/AST_frequency/eclipse/test_eclipse_matrix_frequence.csv.arff";
		//PredictorBasedOnTokenFrequency.main(args);
		
		//args[0]="/Users/JC/Documents/UW/ODP/data/AST_frequency/jackrabbit/train_jackrabbit_matrix_frequence.csv.arff";
		//args[1]="/Users/JC/Documents/UW/ODP/data/AST_frequency/jackrabbit/test_jackrabbit_matrix_frequence.csv.arff";
		
		//PredictorBasedOnTokenFrequency.main(args);*/
		
		// bag of words
		//args[0]="/Users/JC/Documents/UW/ODP/data/BagOfWords/lucene_bag_of_word/train_matrix_bag_word.csv.arff";
		//args[1]="/Users/JC/Documents/UW/ODP/data/BagOfWords/lucene_bag_of_word/test_matrix_bag_word.csv.arff";
		
		//args[0]="/Users/JC/Documents/UW/ODP/data/BagOfWords/eclipse_bag_of_word/train_matrix_bag_word.csv.arff";
		//args[1]="/Users/JC/Documents/UW/ODP/data/BagOfWords/eclipse_bag_of_word/test_matrix_bag_word.csv.arff";
		
		//args[0]="/Users/JC/Documents/UW/ODP/data/BagOfWords/jackrabbit_bag_of_word/train_matrix_bag_word.csv.arff";
		//args[1]="/Users/JC/Documents/UW/ODP/data/BagOfWords/jackrabbit_bag_of_word/test_matrix_bag_word.csv.arff";
		
		for(int i=0;i<100;i++){
			args[2] = DecimalUtil.twoDecimal(i*0.01);
			System.out.print(args[2] + "\t");
			PredictorBasedOnTokenFrequency.main(args);
		}
	}

}
