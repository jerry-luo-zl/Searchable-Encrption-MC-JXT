package jxt.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

public class JXTXSetBlock {
    public BloomFilter<CharSequence> xtagLst;
    
    public JXTXSetBlock(int capacity, double errRate) {
    	xtagLst = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 
 				capacity, errRate);
    }
    
    public JXTXSetBlock(String XSetString) {
    	
    	ByteArrayInputStream in = new ByteArrayInputStream(XSetString.getBytes());
    	try {
			xtagLst = BloomFilter.readFrom(in, Funnels.stringFunnel(StandardCharsets.UTF_8));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public String ToString() {
    	
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
        	xtagLst.writeTo(stream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         String finalString = new String(stream.toByteArray());

         return finalString;
    }
}
