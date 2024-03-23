package jxt.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JXTTSetBlock {
	
	public HashMap<String, Vector<JXTTSetTuple>> arr = new HashMap<String, Vector<JXTTSetTuple>>();
	public byte[] keyword_enc;

	public JXTTSetBlock() {};
	
	public JXTTSetBlock(String TSetString) {
		Gson gson = new Gson();
		arr = gson.fromJson(TSetString, new TypeToken<HashMap<String, Vector<JXTTSetTuple>>>(){}.getType());
	};
	
	
	public String ToString() {
		Gson gson = new GsonBuilder()
                .registerTypeAdapter(JXTTSetTuple.class, new JXTTSetTupleAdapter()).create();
		String arrString = gson.toJson(arr);
		return arrString;
	}
}
