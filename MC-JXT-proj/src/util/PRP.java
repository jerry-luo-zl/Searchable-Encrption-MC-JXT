package util;

import java.util.Vector;

public class PRP {
	public static Vector<Integer> genPRP(Vector<Integer> ind) {
		Vector<Integer> rind = new Vector<Integer>();
		Vector<Integer> rv = new Vector<Integer>();
		int cursor = 0;
		while (cursor < ind.size()) {
			int r = (int) (Math.random() * 10000 % ind.size());
			while (rv.contains(r) == true) {
				r = (int) (Math.random() * 10000 % ind.size());
			}
			rv.add(r);
			rind.add(ind.get(r));
			cursor++;
		}
		return rind;
	}
}
