package jxt1.entity;

import java.util.Vector;

import util.IntAndByte;

public class KeySet {
	public byte[] K_I;
    public byte[] K_W;
    public byte[] K_Z1;
    public byte[] K_Z2;
    public byte[] K_enc;
    public byte[] K_T;
    public byte[] K_C1;
    public byte[] K_C2;
    
    public void initKey(Vector<Integer> keys) {
    	K_I = IntAndByte.toByteArray(keys.get(0));
    	K_W = IntAndByte.toByteArray(keys.get(1));
    	K_Z1 = IntAndByte.toByteArray(keys.get(2));
    	K_Z2 = IntAndByte.toByteArray(keys.get(3));
    	K_enc = IntAndByte.toByteArray(keys.get(4));
    	K_T = IntAndByte.toByteArray(keys.get(5));
    	K_C1 = IntAndByte.toByteArray(keys.get(6));
    	K_C2 = IntAndByte.toByteArray(keys.get(7));
    }
}