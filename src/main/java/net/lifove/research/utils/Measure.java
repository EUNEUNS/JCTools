package net.lifove.research.utils;

public class Measure {
	double precision, recall, fmeasure, avgFmeasureOnVarThresholds,PRC, AUC, MCC, pd, pf, bal,FPR, FNR;
	int FP, TP, FN, TN;
	
	public Measure(){
		
	}
	
	public Measure(double fmeasure,double avgFmeasureOnVarThresholds,
					double PRC,double AUC, double MCC, double pd,double pf,double bal,
						double FPR, double FNR,
						int TP, int FP, int TN, int FN){
		this.fmeasure = fmeasure;
		this.avgFmeasureOnVarThresholds = avgFmeasureOnVarThresholds;
		this.PRC = PRC;
		this.AUC = AUC;
		this.MCC = MCC;
		this.pd = pd;
		this.pf = pf;
		this.bal = bal;
		this.FPR = FPR;
		this.FNR = FNR;
		this.TP = TP;
		this.FP = FP;
		this.TN = TN;
		this.FN = FN;
	}
	
	public Measure(double precision, double recall, double fmeasure,
				int TP, int FP, int TN, int FN){
		this.precision = precision;
		this.recall = recall;
		this.fmeasure = fmeasure;
		this.TP = TP;
		this.FP = FP;
		this.TN = TN;
		this.FN = FN;
	}
	
	public void addFPR(double fpr){
		FPR = fpr;
	}
	
	public void addFNR(double fnr){
		FNR = fnr;
	}
	
	public void addTP(int tp){
		TP = tp;
	}
	
	public void addFP(int fp){
		FP = fp;
	}
	
	public void addTN(int tn){
		TN = tn;
	}
	
	public void addFN(int fn){
		FN = fn;
	}
	
	public void addPrecision(double prec){
		precision = prec;
	}
	
	public void addRecall(double recall){
		this.recall = recall;
	}
	
	public double getFPR(){
		return FPR;
	}
	
	public double getFNR(){
		return FNR;
	}
	
	public int getTP(){
		return TP;
	}
	
	public int getFP(){
		return FP;
	}
	
	public int getTN(){
		return TN;
	}
	
	public int getFN(){
		return FN;
	}
	
	public double getPrecision(){
		return precision;
	}
	
	public double getRecall(){
		return recall;
	}
	
	public void addFmeasure(double f){
		fmeasure = f;
	}
	
	public void addAUC(double auc){
		AUC = auc;
	}

	public double getFmeasure() {
		return fmeasure;
	}

	public double getAUC() {
		return AUC;
	}

	public double getPd() {
		return pd;
	}

	public double getPf() {
		return pf;
	}

	public double getBal() {
		return bal;
	}

	public double getFmeasureOnVarThresholds() {
		return avgFmeasureOnVarThresholds;
	}

	public double getPRC() {
		return PRC;
	}

	public double getMCC() {
		// TODO Auto-generated method stub
		return MCC;
	}
	
}
