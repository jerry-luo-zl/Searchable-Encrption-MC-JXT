package util;

import java.util.*;

public class TableManager {
	public Vector<TableFilesBlock> tabs = new Vector<TableFilesBlock>();
	
	
	public void GenDemoTab(){
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
		
		tabs.add(tab1);
		tabs.add(tab2);
		// 输出
		//tab1.PrintTable();
		//tab2.PrintTable();
	}
}

