package net.lifove.research.weka.lpuutil;

public class InstanceItem {
	int idx;
	int value;
	public InstanceItem(int idx, int value) {
		super();
		this.idx = idx;
		this.value = value;
	}
	public InstanceItem(String[] content) {
		super();
		if (content.length != 2)
			return;
		
		this.idx = Integer.parseInt(content[0]);
		this.value = Integer.parseInt(content[1]);
	}
	
	public int getIdx() {
		return idx;
	}
	public void setIdx(int idx) {
		this.idx = idx;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
}
