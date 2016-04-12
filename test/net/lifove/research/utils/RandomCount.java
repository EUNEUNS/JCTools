package net.lifove.research.utils;
import java.util.ArrayList;
import java.util.Random;


public class RandomCount {

	public static void main(String[] args) {
		new RandomCount().run();
	}

	private void run() {

		int totalNum = 2808; //2820;
		int numPositive = 27;//21;
		int select =  845; //873;
		
		int repeats = 100;

		ArrayList<TestResult> testResults = new ArrayList<TestResult>();

		int currentPosIndex = 0;
		for(int i=0;i<totalNum;i++){

			TestResult tr = new TestResult();

			tr.index = i;

			if(currentPosIndex<numPositive){
				tr.positive = true;
				currentPosIndex++;
			}

			testResults.add(tr);
		}

		ArrayList<TestResult> selectedTR = new ArrayList<TestResult>();
		Random r = new Random();
		
		for(int repeat=0;repeat<repeats;repeat++){
		
			selectedTR.clear();
			
	
			for(int i=0; i< select; i++){
				int selectedIndex = r.nextInt(totalNum); // 0부터 sizeOfCandidates-1사이에서 int 난수 생성
				selectedTR.add(testResults.get(selectedIndex));
	
			}
	
			int countPositive=0;
			for(TestResult testResult:selectedTR){
				if(testResult.positive)
					countPositive++;
			}
			
			System.out.println(countPositive);
		}
	}

}

class TestResult{
	int index = 0;
	boolean positive=false;
}
