package server;

import client.entity.EY;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import server.entity.oxt_query;
import server.entity.oxt_server;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class OXT_Server {
    HashMap<String, EY[]> TSet;
    HashSet<String> XSet;
    EY[] EY_List ;
    public static Pairing pairing = PairingFactory.getPairing("params/curves/d159.properties");
    public OXT_Server(HashMap<String, EY[]> fp, HashSet<String> pf){
        TSet = fp;
        XSet = pf;
    }

    public int Query_num(String stag){
        EY_List = TSet.get(stag);
        if(EY_List == null)
            return 0;
        return  EY_List.length;
    }

    public ArrayList<byte[]> Query_Xtoken(Element[][] xtoken){
        ArrayList<byte[]> C_key = new ArrayList<>();
        for (int i = 0;i<EY_List.length;i++ ) {
            EY entity = EY_List[i];
            Element y =  pairing.getZr().newElementFromBytes((entity.y));
            boolean tag = true;
            for(int k=0;k<xtoken.length;k++){
                Element xtag = xtoken[k][i].powZn(y).getImmutable();
                byte[] bytex = xtag.getImmutable().toBytes();
                String xtag_st = new String(bytex);
                if(!XSet.contains(xtag_st)){
                    tag = false;
                    break;
                }
            }
            if(tag == true){
                C_key.add(entity.e);
            }
        }
        return  C_key;
    }
    public void Store_Server(){
        oxt_server entity = new oxt_server();
        entity.TSet = TSet;
        entity.XSet = XSet;
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("OXT_Server_Size.dat"));
            out.writeObject(entity);
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void Store_Query(byte[] hash,byte[][][] xtoken, ArrayList<byte[]> R_key){
        oxt_query entity = new oxt_query();
        entity.hash = hash;
        entity.xtoken = xtoken;
        entity.R_key = R_key;
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("OXT_Query_Size.dat"));
            out.writeObject(entity);
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



}
