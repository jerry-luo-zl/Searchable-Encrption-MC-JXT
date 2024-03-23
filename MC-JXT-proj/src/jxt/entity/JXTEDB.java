package jxt.entity;


import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jxt.entity.JXTTSetBlock;

public class JXTEDB {
	public KeySet K = new KeySet();
    public JXTXSetBlock[] XSet;
    public JXTTSetBlock[] TSet;
    public int tableNum;
    
    // param1: tabNum 
	// param2: recsNum
	// param3: AttrNum
	// param4: JoinAttrNum
    public JXTEDB(int tN,int m, int n, int T) {
  
    	double errRate = Math.pow(2, -20);
    	int capacity = 300000;
    	tableNum = tN;
    	
        XSet= new JXTXSetBlock[tableNum];
        for(int i=0; i<tableNum; i++)
        	XSet[i] = new JXTXSetBlock(capacity, errRate);
        
        TSet = new JXTTSetBlock[tableNum];
        for (int i=0; i<tableNum; i++) {
        	TSet[i] = new JXTTSetBlock();
        }
    }
    
    public JXTEDB(String edbString) {
    	// parse
        Gson gson = new Gson();
        HashMap<String, String> edbMeta = gson.fromJson(edbString, new TypeToken<HashMap<String, String>>(){}.getType());
    	tableNum = Integer.parseInt(edbMeta.get("tableNum"));
    	ArrayList<String> XSetString = gson.fromJson(edbMeta.get("XSet"), new TypeToken<ArrayList<String>>(){}.getType());
    	ArrayList<String> TSetString = gson.fromJson(edbMeta.get("TSet"), new TypeToken<ArrayList<String>>(){}.getType());
    			
    	 for(int i=0; i<tableNum; i++) {
    		XSet[i] = new JXTXSetBlock(XSetString.get(i));
    		TSet[i] = new JXTTSetBlock(TSetString.get(i));
 		 }
    }
    
    public String ToString() {
    	 Gson gson = new Gson();
    	 HashMap<String, String> edbMeta = new HashMap<String, String>();
    	 edbMeta.put("tableNum", Integer.toString(tableNum));
    	 ArrayList<String> XSetString = new ArrayList <String>();
    	 ArrayList<String> TSetString = new ArrayList <String>();
    	 
		 for(int i=0; i<tableNum; i++) {
	     	XSetString.add(XSet[i].ToString());
	     	TSetString.add(TSet[i].ToString());
		 }
		 edbMeta.put("XSet", gson.toJson(XSetString));
		 edbMeta.put("TSet", gson.toJson(TSetString));
		 return gson.toJson(edbMeta);
    }
}
