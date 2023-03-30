package util;

import java.util.*;

import it.unisa.dia.gas.jpbc.Pairing;


public class DataRec {
	
	public int id;
	
	public Vector<AVPair> tuples = new Vector<AVPair>();
	
	public DataRec(int id, Vector<String> attrs, Vector<String> t) {
		this.id = id;
		
		for (int i=0; i < attrs.size(); i++) {
			this.tuples.add(new AVPair(attrs.get(i), t.get(i)));
		}
	}
	
	public String GetValue(String attr) {
		for (AVPair a:tuples) {
			if (a.GetAttr().equals(attr))
				return a.toString();
		}
		return null;
	}
	
	 
}
