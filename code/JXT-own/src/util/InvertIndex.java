package util;

import java.util.HashMap;
import java.util.Vector;

public class InvertIndex {
	
	public Vector<String> attrs = new Vector<String>();
	
	public HashMap<String, Vector<Integer>> index = new HashMap<String, Vector<Integer>>();
	
	public InvertIndex(TableFilesBlock t) {
		this.attrs = t.attrs;
		for (DataRec r : t.recs) {
			for (AVPair a : r.tuples) {
				boolean flag =false;
				/*
				for (AVPair k : index.keySet()) {
					if (k!=null && k.GetAttr().equals(a.attr) && k.GetWord().equals(a.word)) {
						flag = true;
						break;
					}
				}
				
				if (flag == false) {
					Vector<Integer> recInds = new Vector<Integer>();
					index.put(a, recInds);
				}
				*/
				
				if (index.containsKey(a.toString()) == false) {
					Vector<Integer> recInds = new Vector<Integer>();
					index.put(a.toString(), recInds);
				}
				index.get(a.toString()).add(r.id);
			}
		}
	}
	
	public void PrintIndex() {
		for (String a : index.keySet()) {
			System.out.print(a+ ": ");
			for (int id : index.get(a)) {
				System.out.print(String.valueOf(id)+" ");
			}
			System.out.print("\n");
		}
		System.out.print("\n");
	} 
}
