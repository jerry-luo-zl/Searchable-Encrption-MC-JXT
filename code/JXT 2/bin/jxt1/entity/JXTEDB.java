package jxt1.entity;


import jxt1.entity.JXTTSetBlock;

import java.util.Vector;

public class JXTEDB {
    public byte[] K_I;
    public byte[] K_W;
    public byte[] K_Z1;
    public byte[] K_Z2;
    public byte[] K_enc;
    public byte[] K_T;
    public JXTTSetBlock[] TSet1;
    public Vector<String> XSet1;
    public JXTTSetBlock[] TSet2;
    public Vector<String> XSet2;



    public JXTEDB(int n){
        TSet1= new JXTTSetBlock[n];
        for(int i=0; i<n; i++)
            TSet1[i] = new JXTTSetBlock();
        XSet1= new Vector<String>();
        TSet2= new JXTTSetBlock[n];
        for(int i=0; i<n; i++)
            TSet2[i] = new JXTTSetBlock();
        XSet2= new Vector<String>();
    }

}
