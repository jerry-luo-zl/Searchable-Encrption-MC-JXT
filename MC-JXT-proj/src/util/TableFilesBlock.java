package util;
import java.util.*;

public class TableFilesBlock {
	// 属性集合
	//public HashMap<Integer,String> attrs = new HashMap<>();
	public Vector<Integer> attrs = new Vector<Integer>();
	// records
	public Vector<DataRec> recs = new Vector<DataRec>();
	public int cursor = 0;
	
	// 用属性集合和Record数组来初始化
	public TableFilesBlock(Vector<Integer> attrs) {
		this.attrs = attrs;
	}
	
	public int GetRecsNum() { return this.recs.size(); }
	
	public int GetAttrsNum() { return this.attrs.size(); }
	
	public void AddRecord(ArrayList<String> tmpA) {
		
		Vector<String> tmpV = new Vector<String>();
		tmpV.addAll(tmpA);
		this.recs.add(new DataRec(++this.cursor, attrs, tmpV));
		
	}
	
	public void PrintTable() {
		System.out.print("ID"+" ");
		for (Integer a : attrs) {
			System.out.print(String.valueOf(a)+" ");
		}
		
		System.out.print("\n");
		
		for (int i=0; i<this.recs.size(); i++) {
			DataRec t = this.recs.get(i);
			System.out.print(String.valueOf(t.id) + " ");
			for (int j=0; j<t.tuples.size(); j++) {
				System.out.print(t.tuples.get(j).GetWord()+" ");
			}
			System.out.print("\n");
		}
	}
}
