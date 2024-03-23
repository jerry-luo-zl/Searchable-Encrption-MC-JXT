package jxt.entity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class JXTTSetTupleAdapter extends TypeAdapter<JXTTSetTuple> {
		 
    @Override
    public void write(JsonWriter out, JXTTSetTuple value) throws IOException {
        out.beginObject();
        out.name("ct").value(new String(value.ct));
        Gson gson = new GsonBuilder()
                //为User注册TypeAdapter
                .registerTypeAdapter(ytuple.class, new ytupleAdapter())
                .create();
        out.name("y").value(gson.toJson(value.y));
        out.endObject();
    }
 
    @Override
    public JXTTSetTuple read(JsonReader in) throws IOException {
    	JXTTSetTuple value = new JXTTSetTuple();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "ct":
                	value.ct = in.nextString().getBytes();
                    break;
                case "y":
                	Gson gson = new GsonBuilder()
                    //为User注册TypeAdapter
                    .registerTypeAdapter(ytuple.class, new ytupleAdapter())
                    .create();
                	value.y = gson.fromJson(in.nextString(), new TypeToken<Vector<ytuple>>(){}.getType());
                    break;
            }
        }
        in.endObject();
        return value;
    }
}
