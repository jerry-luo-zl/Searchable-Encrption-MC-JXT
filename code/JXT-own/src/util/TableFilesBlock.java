package util;
import java.util.*;

public class TableFilesBlock {
	// 属性集合
	//public HashMap<Integer,String> attrs = new HashMap<>();
	public Vector<String> attrs = new Vector<String>();
	// records
	public Vector<DataRec> recs = new Vector<DataRec>();
	public int cursor = 0;
	
	// 用属性集合和Record数组来初始化
	public TableFilesBlock(Vector<String> attrs) {
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
		for (String a : attrs) {
			System.out.print(a+" ");
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
	
	public static void main(String[] args) {
		// 生成table1
		Vector<String> attrsTab1 = new Vector<String>();
		attrsTab1.addAll(new ArrayList<String>(Arrays.asList("Num","Name", "Age", "Addr")));
		TableFilesBlock tab1 = new TableFilesBlock(attrsTab1);
		
		tab1.AddRecord(new ArrayList<String>(Arrays.asList("1", "n1", "18", "S")));
		tab1.AddRecord(new ArrayList<String>(Arrays.asList("2", "n2", "20", "B")));
		tab1.AddRecord(new ArrayList<String>(Arrays.asList("3", "n3", "21", "G")));
		tab1.AddRecord(new ArrayList<String>(Arrays.asList("4", "n4", "18", "K")));
		tab1.AddRecord(new ArrayList<String>(Arrays.asList("5", "n1", "27", "Q")));
		tab1.AddRecord(new ArrayList<String>(Arrays.asList("6", "n5", "18", "P")));
	
		// 生成table2
		Vector<String> attrsTab2 = new Vector<String>();
		attrsTab2.addAll(new ArrayList<String>(Arrays.asList("Num", "Name", "Class", "Grade")));
		TableFilesBlock tab2 = new TableFilesBlock(attrsTab2);
		
		tab2.AddRecord(new ArrayList<String>(Arrays.asList("1", "n1", "M", "90")));
		tab2.AddRecord(new ArrayList<String>(Arrays.asList("2", "n2", "Y", "100")));
		tab2.AddRecord(new ArrayList<String>(Arrays.asList("4", "n4", "M", "95")));
		tab2.AddRecord(new ArrayList<String>(Arrays.asList("8", "n8", "M", "100")));
		tab2.AddRecord(new ArrayList<String>(Arrays.asList("9", "n9", "M", "97")));
		tab2.AddRecord(new ArrayList<String>(Arrays.asList("6", "n5", "M", "94")));
		
		/*
		// 输出
		System.out.print("========Tab1========\n");
		tab1.PrintTable();
		System.out.print("========Tab2========\n");
		tab2.PrintTable();
		
		// index
		InvertIndex index1 = new InvertIndex(tab1);
		InvertIndex index2 = new InvertIndex(tab2);
		
		System.out.print("========Index1========\n");
		index1.PrintIndex();

		System.out.print("========Index2========\n");
		index2.PrintIndex();
		*/
		
	}
}
