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
    public JXTTSetBlock[][] TSet;
    public JXTXSetBlock[][] XSet;




    public JXTEDB(int n1,int n2){
        TSet= new JXTTSetBlock[n1][n2];
        for(int i=0; i<n1; i++)
            for(int j=0;j<n2;j++)
                TSet[i][j] = new JXTTSetBlock();


        XSet= new JXTXSetBlock[n1][n2];
        for(int i=0; i<n1; i++)
                for(int j=0;j<n2;j++)
                    XSet[i][j] = new JXTXSetBlock();





    }

}
