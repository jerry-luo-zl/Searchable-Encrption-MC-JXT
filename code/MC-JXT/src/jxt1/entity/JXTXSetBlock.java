package jxt1.entity;

import java.nio.charset.StandardCharsets;
import java.util.Vector;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

public class JXTXSetBlock {
    public Vector<String> xtag= new Vector<String>();
    public BloomFilter<CharSequence> xtagLst;
    
    public JXTXSetBlock(int capacity, double errRate) {
    	xtagLst = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 
 				capacity, errRate);
    }
}


