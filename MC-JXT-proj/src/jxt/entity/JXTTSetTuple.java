package jxt.entity;

import java.util.Vector;

import com.google.gson.Gson;

public class JXTTSetTuple {
    public byte[] ct;
    public Vector<ytuple> y ;
    public JXTTSetTuple(byte[] ct, Vector<ytuple> y) {
    	this.ct = ct;
    	this.y = y;
    }
    public JXTTSetTuple() {}
}
