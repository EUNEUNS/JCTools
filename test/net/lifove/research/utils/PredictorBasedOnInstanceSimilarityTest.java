package net.lifove.research.utils;

import static org.junit.Assert.*;
import net.lifove.research.utils.PredictorBasedOnInstanceSimilarity;

import org.junit.Test;

public class PredictorBasedOnInstanceSimilarityTest {

	@Test
	public void test() {
		
		String similarity="0.01 0.02 0.03 0.04 0.05 0.06 0.07 0.08 0.09"; //"0.01 0.2 0.03 0.04 0.05 0.06 0.07 0.08 0.09 0.10 0.11 0.12 0.13 0.14";//"0.21 0.22 0.23 0.24 0.25 0.26 0.27 0.28 0.29 0.30 0.31 0.32 0.33 0.34 0.35 0.36 0.37 0.38 0.39 0.40"; //"0.10 0.30 0.50 0.70 0.90 1.0"; //"0.01 0.03 0.05 0.07 0.09 0.1 0.3 0.5 0.7 0.9 0.91 0.93 0.95 0.97 0.99 1.00";
		String sameOneValueTockenAtLeast = "1";// 2 3 4";
		String THRESHOLD = "0.00";
		String ADAPTIVE = "true";
		String HYBRID = "false";
		String TraiingSetGuided="false";
		String[] sims = similarity.split(" ");
		String[] tokenAtLeast = sameOneValueTockenAtLeast.split(" ");
		String printPatch = "false";
		
		String[] args = {"/Users/JC/Documents/UW/ODP/data/FeatureSelection/for_jc/lucene/train_lucene_matrix_original_before_DL_with_label.csv.arff",
				"/Users/JC/Documents/UW/ODP/data/FeatureSelection/for_jc/lucene/test_lucene_matrix_original_before_DL_with_label.csv.arff",
				"/Users/JC/Documents/UW/ODP/data/FeatureSelection/for_jc/jackrabbit/tokens.txt",
				"/Users/JC/Documents/UW/ODP/data/diff/train_lucene_patch_no.csv",
				"/Users/JC/Documents/UW/ODP/data/diff/test_lucene_patch_no.csv",
				"/Users/JC/Documents/UW/ODP/data/diff/lucene/diffs",
				similarity,"1",THRESHOLD,ADAPTIVE,HYBRID,TraiingSetGuided, printPatch};
		
		//PredictorBasedOnInstanceSimilarity.main(args);
		
		/*args[0]="/Users/JC/Documents/UW/ODP/data/FeatureSelection/for_jc/eclipse/train_jdt_matrix_original_before_with_labels.csv.arff";
		args[1]="/Users/JC/Documents/UW/ODP/data/FeatureSelection/for_jc/eclipse/test_jdt_matrix_original_before_with_lables.csv.arff";
		PredictorBasedOnInstanceSimilarity.main(args);
		
		args[0]="/Users/JC/Documents/UW/ODP/data/FeatureSelection/for_jc/jackrabbit/train_jackrabbit_matrix_original_before_DL_with_label.csv.arff";
		args[1]="/Users/JC/Documents/UW/ODP/data/FeatureSelection/for_jc/jackrabbit/test_jackrabbit_matrix_original_before_DL_with_label.csv.arff";
		PredictorBasedOnInstanceSimilarity.main(args);*/
		
		System.out.println("THRESHOLD: >" + THRESHOLD + " ADPATIVE: " + ADAPTIVE  + " HYBRID: " + HYBRID);
		
		for(int j=0;j<tokenAtLeast.length;j++){
			
			args[7] = tokenAtLeast[j];
			System.out.print(args[7] + " ");
				
			for(int i=0;i<sims.length;i++){
				
				args[6] = sims[i];
				
				System.out.print(args[6] + " ");
				
				// original (some tokens are removed if there don't appear in both training set and test sets) AST token based data
				/*args[0]="/Users/JC/Documents/UW/ODP/data/FeatureSelection/for_jc/lucene/train_lucene_matrix_original_before_DL_with_label.csv.arff";
				args[1]="/Users/JC/Documents/UW/ODP/data/FeatureSelection/for_jc/lucene/test_lucene_matrix_original_before_DL_with_label.csv.arff";
				PredictorBasedOnInstanceSimilarity.main(args);
				
				args[0]="/Users/JC/Documents/UW/ODP/data/FeatureSelection/for_jc/eclipse/train_jdt_matrix_original_before_with_labels.csv.arff";
				args[1]="/Users/JC/Documents/UW/ODP/data/FeatureSelection/for_jc/eclipse/test_jdt_matrix_original_before_with_lables.csv.arff";

				PredictorBasedOnInstanceSimilarity.main(args);
				
				args[0]="/Users/JC/Documents/UW/ODP/data/FeatureSelection/for_jc/jackrabbit/train_jackrabbit_matrix_original_before_DL_with_label.csv.arff";
				args[1]="/Users/JC/Documents/UW/ODP/data/FeatureSelection/for_jc/jackrabbit/test_jackrabbit_matrix_original_before_DL_with_label.csv.arff";
				args[2]="/Users/JC/Documents/UW/ODP/data/FeatureSelection/for_jc/jackrabbit/tokens.txt";
				PredictorBasedOnInstanceSimilarity.main(args);*/
				
				// keep all tokens
				args[0]="/Users/JC/Documents/UW/ODP/data/keep_all_tokens/lucene/train_matrix.csv.arff";
				args[1]="/Users/JC/Documents/UW/ODP/data/keep_all_tokens/lucene/test_matrix.csv.arff";
				args[2]="/Users/JC/Documents/UW/ODP/data/keep_all_tokens/lucene/tokens.txt";
				args[3]="/Users/JC/Documents/UW/ODP/data/diff/train_lucene_patch_no.csv";
				args[4]="/Users/JC/Documents/UW/ODP/data/diff/test_lucene_patch_no.csv";
				args[5]="/Users/JC/Documents/UW/ODP/data/diff/lucene/diffs";
				PredictorBasedOnInstanceSimilarity.main(args);
			
				args[0]="/Users/JC/Documents/UW/ODP/data/keep_all_tokens/eclipse/train_matrix.csv.arff";
				args[1]="/Users/JC/Documents/UW/ODP/data/keep_all_tokens/eclipse/test_matrix.csv.arff";
				args[2]="/Users/JC/Documents/UW/ODP/data/keep_all_tokens/eclipse/tokens.txt";
				args[3]="/Users/JC/Documents/UW/ODP/data/diff/train_eclipse_patch_no.csv";
				args[4]="/Users/JC/Documents/UW/ODP/data/diff/test_eclipse_patch_no.csv";
				args[5]="/Users/JC/Documents/UW/ODP/data/diff/eclipse/diffs";
				//PredictorBasedOnInstanceSimilarity.main(args);
				
				/*args[0]="/Users/JC/Documents/UW/ODP/data/keep_all_tokens/jackrabbit/train_matrix.csv.arff";
				args[1]="/Users/JC/Documents/UW/ODP/data/keep_all_tokens/jackrabbit/test_matrix.csv.arff";
				args[2]="/Users/JC/Documents/UW/ODP/data/keep_all_tokens/jackrabbit/tokens.txt";
				args[3]="/Users/JC/Documents/UW/ODP/data/diff/train_jackrabbit_patch_no.csv";
				args[4]="/Users/JC/Documents/UW/ODP/data/diff/test_jackrabbit_patch_no.csv";
				args[5]="/Users/JC/Documents/UW/ODP/data/diff/jackrabbit/diffs";
				PredictorBasedOnInstanceSimilarity.main(args);*/
				// end of keep all tokens
				
				
				// original AST (all) token frequency based data
				/*args[0]="/Users/JC/Documents/UW/ODP/data/AST_frequency/lucene/train_lucene_matrix_frequence.csv.arff";
				args[1]="/Users/JC/Documents/UW/ODP/data/AST_frequency/lucene/test_lucene_matrix_frequence.csv.arff";
				args[2]="/Users/JC/Documents/UW/ODP/data/keep_all_tokens/lucene/tokens.txt";
				PredictorBasedOnInstanceSimilarity.main(args);
				
				args[0]="/Users/JC/Documents/UW/ODP/data/AST_frequency/eclipse/train_eclipse_matrix_frequence.csv.arff";
				args[1]="/Users/JC/Documents/UW/ODP/data/AST_frequency/eclipse/test_eclipse_matrix_frequence.csv.arff";
				args[2]="/Users/JC/Documents/UW/ODP/data/keep_all_tokens/eclipse/tokens.txt";
				PredictorBasedOnInstanceSimilarity.main(args);
				
				args[0]="/Users/JC/Documents/UW/ODP/data/AST_frequency/jackrabbit/train_jackrabbit_matrix_frequence.csv.arff";
				args[1]="/Users/JC/Documents/UW/ODP/data/AST_frequency/jackrabbit/test_jackrabbit_matrix_frequence.csv.arff";
				args[2]="/Users/JC/Documents/UW/ODP/data/keep_all_tokens/jackrabbit/tokens.txt";
				PredictorBasedOnInstanceSimilarity.main(args);*/
				
				
				// bag of words
				/*args[0]="/Users/JC/Documents/UW/ODP/data/BagOfWords/lucene_bag_of_word/train_matrix_bag_word.csv.arff";
				args[1]="/Users/JC/Documents/UW/ODP/data/BagOfWords/lucene_bag_of_word/test_matrix_bag_word.csv.arff";
				args[2]="/Users/JC/Documents/UW/ODP/data/BagOfWords/lucene_bag_of_word/bag_of_word.txt";
				PredictorBasedOnInstanceSimilarity.main(args);
				
				args[0]="/Users/JC/Documents/UW/ODP/data/BagOfWords/eclipse_bag_of_word/train_matrix_bag_word.csv.arff";
				args[1]="/Users/JC/Documents/UW/ODP/data/BagOfWords/eclipse_bag_of_word/test_matrix_bag_word.csv.arff";
				args[2]="/Users/JC/Documents/UW/ODP/data/BagOfWords/eclipse_bag_of_word/bag_of_word.txt";
				PredictorBasedOnInstanceSimilarity.main(args);
				
				args[0]="/Users/JC/Documents/UW/ODP/data/BagOfWords/jackrabbit_bag_of_word/train_matrix_bag_word.csv.arff";
				args[1]="/Users/JC/Documents/UW/ODP/data/BagOfWords/jackrabbit_bag_of_word/test_matrix_bag_word.csv.arff";
				args[2]="/Users/JC/Documents/UW/ODP/data/BagOfWords/jackrabbit_bag_of_word/bag_of_word.txt";
				PredictorBasedOnInstanceSimilarity.main(args);*/
				// end of bag of words
				
				System.out.println("");
			}
		}
	}

}
