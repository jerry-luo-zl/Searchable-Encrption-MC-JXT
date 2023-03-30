package server.entity;

import it.unisa.dia.gas.jpbc.Element;

import java.io.Serializable;
import java.util.ArrayList;

public class oxt_query implements Serializable {
    // token
    public byte[] hash;
    public byte[][][] xtoken;

    //reply
    public  ArrayList<byte[]> R_key;
}
