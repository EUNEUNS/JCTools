package net.lifove.research.utils;

import java.util.ArrayList;

public class Measures {
	ArrayList<Double> precisions, recalls, fmeasures, fmeasuresOnVarThresholds, AUPRCs, AUCs, MCCs, pds, pfs, bals, FPRs, FNRs;
	
	public Measures(){
		precisions = new ArrayList<Double>();
		recalls = new ArrayList<Double>();
		fmeasures = new ArrayList<Double>();
		fmeasuresOnVarThresholds = new ArrayList<Double>();
		AUPRCs = new ArrayList<Double>();
		AUCs = new ArrayList<Double>();
		MCCs = new ArrayList<Double>();
		pds = new ArrayList<Double>();
		pfs = new ArrayList<Double>();
		bals = new ArrayList<Double>();
		FPRs = new ArrayList<Double>();
		FNRs = new ArrayList<Double>();
	}
	
	public int size(){
		return fmeasures.size();
	}

	public void setAllMeasures(double precision,double recall,double fmeasure,double fmeasureOnVarThresholds,double AUPRC,double AUC,double MCC, double pd,double pf,double bal){
		precisions.add(precision);
		recalls.add(recall);
		fmeasures.add(fmeasure);
		fmeasuresOnVarThresholds.add(fmeasureOnVarThresholds);
		AUPRCs.add(AUPRC);
		AUCs.add(AUC);
		MCCs.add(MCC);
		pds.add(pd);
		pfs.add(pf);
		bals.add(bal);
	}
	
	public void setFPR(double fpr) {
		FPRs.add(fpr);
	}
	
	public void setFNR(double fnr) {
		FNRs.add(fnr);
	}
	
	public void setPrecision(double precision) {
		precisions.add(precision);
	}
	
	public void setRecall(double recall) {
		recalls.add(recall);
	}
	
	public void setFmeasure(double fmeasure) {
		fmeasures.add(fmeasure);
	}
	
	public void setFmeasureByVarThresholds(double fmeasure) {
		fmeasuresOnVarThresholds.add(fmeasure);
	}
	
	public void setAUPRC(double AUPRC) {
		AUPRCs.add(AUPRC);
	}

	public void setAUC(double AUC) {
		AUCs.add(AUC);
	}
	
	public void setMCC(double MCC) {
		AUCs.add(MCC);
	}

	public void setPd(double pd) {
		pds.add(pd);
	}

	public void setPf(double pf) {
		pfs.add(pf);
	}

	public void setBal(double bal) {
		bals.add(bal);
	}
	
	public ArrayList<Double> getFPRs() {
		return FPRs;
	}
	
	public ArrayList<Double> getFNRs() {
		return FNRs;
	}
	
	public ArrayList<Double> getPrecisions() {
		return precisions;
	}
	
	public ArrayList<Double> getRecalls() {
		return recalls;
	}
	
	public ArrayList<Double> getFmeasures() {
		return fmeasures;
	}
	
	public ArrayList<Double> getFmeasureOnVarThresholds() {
		return fmeasuresOnVarThresholds;
	}
	
	public ArrayList<Double> getAUPRCs() {
		return AUPRCs;
	}
	
	public ArrayList<Double> getAUCs() {
		return AUCs;
	}
	
	public ArrayList<Double> getMCCs() {
		return MCCs;
	}

	public ArrayList<Double> getPds() {
		return pds;
	}

	public ArrayList<Double> getPfs() {
		return pfs;
	}

	public ArrayList<Double> getBals() {
		return bals;
	}
}
