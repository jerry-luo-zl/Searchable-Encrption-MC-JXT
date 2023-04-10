package jxt1.entity;


import jxt1.entity.JXTTSetBlock;
import java.lang.Math;

import java.util.HashMap;
import java.util.Vector;

public class JXTEDB {
	public KeySet K = new KeySet();
    //public JXTTSetBlock[][] TSet;
    public JXTXSetBlock[] XSet;
    public JXTTArrBlock[] TArr;
    
    // param1: tabNum 
	// param2: recsNum
	// param3: AttrNum
	// param4: JoinAttrNum
    public JXTEDB(int tabNum,int m, int n, int T) {
    	
    	/*
        TSet= new JXTTSetBlock[tabNum][m*n];
        for(int i=0; i<tabNum; i++)
            for(int j=0;j<m*n;j++)
                TSet[i][j] = new JXTTSetBlock();
        */
    	
    	double errRate = Math.pow(2, -20);
    	int capacity = 300000;
    	
        XSet= new JXTXSetBlock[tabNum];
        for(int i=0; i<tabNum; i++)
        	XSet[i] = new JXTXSetBlock(capacity, errRate);
        
        TArr = new JXTTArrBlock[tabNum];
        for (int i=0; i<tabNum; i++) {
        	TArr[i] = new JXTTArrBlock();
        }
       
    }

}
