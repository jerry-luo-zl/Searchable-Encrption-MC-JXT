package client;

import client.entity.EY;
import client.entity.KV;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import util.AESUtil;
import util.Hash;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class OXT {
    private String K_s = "123";
    private static byte[] K_I = "7966922666f6eb02".getBytes(StandardCharsets.UTF_8);
    private static byte[] K_X = "7868922666f6eb02".getBytes(StandardCharsets.UTF_8);
    private static byte[] K_Z = "8875922666f6eb02".getBytes(StandardCharsets.UTF_8);
    private String K_T = "234";
    int K_P = 3;
    HashMap<String, EY[]> TSet;
    HashSet<String> XSet;
    HashMap<String,byte[]> k_list = new HashMap<>();
    public Pairing pairing = PairingFactory.getPairing("params/curves/d159.properties");
    public Element g = pairing.getG1().newRandomElement().getImmutable();

    public void Setup(KV[] kv_list,HashMap<String,Integer> kn_list){
        TSet = new HashMap<String, EY[]>();
        XSet = new HashSet<String>();
        byte[] K_e = new byte[256];
        String stag="";
        EY[] EY_List = new EY[1];
        for(int i=0; i<kv_list.length; i++){
            String key = kv_list[i].key;
            String value = kv_list[i].value;
            int counter = kv_list[i].counter;


            byte[] xind = AESUtil.encrypt(K_I, value.getBytes(StandardCharsets.UTF_8));
            byte[] z = AESUtil.encrypt(K_Z,(key+","+counter).getBytes(StandardCharsets.UTF_8));
            Element e_xind = Hash.HashToZr(pairing, xind).getImmutable();
            Element y = e_xind.div(Hash.HashToZr(pairing, z)).getImmutable();
            EY tmp = new EY();
            tmp.y = (y.duplicate().getImmutable().toBytes());

            if(counter == 0) {
                K_e = Hash.Get_SHA_256((K_s+","+key).getBytes(StandardCharsets.UTF_8));
                stag = new String(Hash.Get_SHA_256((K_T+","+key).getBytes(StandardCharsets.UTF_8)));
                byte[] e = AESUtil.encrypt(K_e,value.getBytes(StandardCharsets.UTF_8));
                tmp.e = e;
                EY_List = new EY[kn_list.get(key)+1];
                EY_List[0]=(tmp);
            }else {
                byte[] e = AESUtil.encrypt(K_e,value.getBytes(StandardCharsets.UTF_8));
                tmp.e = e;
                EY_List[counter]=(tmp);
                if(counter==kn_list.get(key))
                    TSet.put(stag,EY_List);
            }
            byte[] kxw = AESUtil.encrypt(K_X,(key).getBytes(StandardCharsets.UTF_8));
            Element e_kxw = Hash.HashToZr(pairing, kxw).getImmutable();
            Element xtag = g.powZn(e_kxw.mul(e_xind)).getImmutable();
            byte[] bytex = xtag.getImmutable().toBytes();
            XSet.add(new String(bytex));

        }
    }


    public void Clean(){
        TSet.clear(); XSet.clear();
    }
    public HashMap<String, EY[]> Get_TSet(){ return TSet; }
    public HashSet<String> Get_XSet() {return XSet;}
    public Element Get_g() {return g;}
    public byte[] Get_K_Z() { return K_Z; }
    public byte[] Get_K_X() { return K_X; }
    public String Get_K_S() { return K_s; }
    public String Get_K_T() { return K_T; }
}


