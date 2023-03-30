package util;

public class AVPair {
	public Integer attr;
	public String word;
	
	public AVPair(Integer a, String k) {
		this.attr = a;
		this.word = k;
	}
	
	public Integer GetAttr() { return attr; }
	public String GetWord() { return word; }
	
	public String toString() {
		return "("+String.valueOf(attr)+","+word+")";
	}
	
}
