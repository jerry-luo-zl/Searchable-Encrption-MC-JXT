package util;

public class AVPair {
	public String attr;
	public String word;
	
	public AVPair(String a, String k) {
		this.attr = a;
		this.word = k;
	}
	
	public String GetAttr() { return attr; }
	public String GetWord() { return word; }
	
	public String toString() {
		return "("+attr+","+word+")";
	}
	
}
