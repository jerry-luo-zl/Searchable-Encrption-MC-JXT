package jxt.entity;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import it.unisa.dia.gas.jpbc.Element;

public class ytupleAdapter extends TypeAdapter<ytuple> {
	 
    @Override
    public void write(JsonWriter out, ytuple value) throws IOException {
        out.beginObject();
        out.name("t").value(Integer.toString(value.t));
        out.name("y1").value(new String(value.y1.toBytes()));
        out.name("y2").value(new String(value.y2.toBytes()));
        out.endObject();
    }
 
    @Override
    public ytuple read(JsonReader in) throws IOException {
    	ytuple y = new ytuple();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "t":
                    y.t = in.nextInt();
                    break;
                case "y1":
                    y.y1.setFromBytes(in.nextString().getBytes());
                    break;
                case "y2":
                	y.y2.setFromBytes(in.nextString().getBytes());
                    break;
            }
        }
        in.endObject();
        return y;
    }
}