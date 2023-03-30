
import client.OXT;
import client.entity.EY;
import client.entity.KV;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import server.OXT_Server;
import util.AESUtil;
import util.Hash;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Test_OXT {

    public static KV[] kv_list;
    public static HashMap<String,Integer> kn_list = new HashMap<>();
    public static void main(String[] args) throws Exception {
        Pairing pairing = PairingFactory.getPairing("params/curves/d159.properties");
        //Search key
        String[] keyword = {"key_s_0","key_s_1"};

        //initialize an database
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("KV_LIST_15_5.dat"));
            kv_list = (KV[]) in.readObject();
            in.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        for(int i=0;i< kv_list.length;i++){
            kn_list.put(kv_list[i].key,kv_list[i].counter);
        }


        //setup phase
        System.out.println("---------------------OXT Scheme(CRYPTO'13)---------------------");
        OXT oxt = new OXT();
        long start_time, end_time, total_time = 0;
        int count = 100;


        for(int i=0;i<count;i++){
            start_time = System.currentTimeMillis();
            oxt.Setup(kv_list,kn_list);
            end_time = System.currentTimeMillis();
            oxt.Clean();
            System.out.println("Interval:"+(end_time - start_time));
            total_time = total_time + end_time - start_time;
        }
        System.out.println("Setup Time of OXT:" + (total_time) + "ms");

        HashMap<String, EY[]> TSet = oxt.Get_TSet();
        HashSet<String> XSet = oxt.Get_XSet();
        Element g = oxt.Get_g();
        String K_T = oxt.Get_K_T();
        byte[] K_Z = oxt.Get_K_Z();
        byte[] K_X = oxt.Get_K_X();
        String K_S = oxt.Get_K_S();


        //query phase
        OXT_Server oxt_server = new OXT_Server(TSet,XSet);//server receives ciphertext
        long query_total_time = 0;
        for(int c=0;c<count;c++){
            start_time = System.currentTimeMillis();
            String stag = new String(Hash.Get_SHA_256((K_T+","+keyword[0]).getBytes(StandardCharsets.UTF_8)));

            int length = oxt_server.Query_num(stag);
            if(length==0) {
                System.out.println("NULL");
                return;
            }
            Element[][] xtoken = new Element[keyword.length-1][length];
            //byte[][][] xtoken_byte = new byte[keyword.length-1][length][];
            for(int p = 0; p < length; p++){
                byte[] z = AESUtil.encrypt(K_Z,(keyword[0]+","+p).getBytes(StandardCharsets.UTF_8));
                Element e_z = Hash.HashToZr(pairing, z).getImmutable();
                for(int k = 1;k< keyword.length;k++){
                    byte[] kxw = AESUtil.encrypt(K_X,(keyword[k]).getBytes(StandardCharsets.UTF_8));
                    Element e_kxw = Hash.HashToZr(pairing, kxw).getImmutable();
                    xtoken[k-1][p] = g.powZn(e_kxw.mul(e_z)).getImmutable();
                    //xtoken_byte[k-1][p] = xtoken[k-1][p].getImmutable().toBytes();
                }
            }
            byte[] K_e = Hash.Get_SHA_256((K_S+","+keyword[0]).getBytes(StandardCharsets.UTF_8));
            ArrayList<byte[]> C_key = oxt_server.Query_Xtoken(xtoken);
            ArrayList<String> MD_key = new ArrayList<>();
            for(int i=0;i<C_key.size();i++){
                byte[] res = AESUtil.decrypt(K_e,C_key.get(i));
                MD_key.add(new String(res));
                //System.out.println("search results:"+new String(res));
            }
            end_time = System.currentTimeMillis();
            System.out.println("Query Interval:"+(end_time - start_time));
            query_total_time = query_total_time + (end_time - start_time);
            //oxt_server.Store_Query(stag.getBytes(),xtoken_byte,C_key);
        }
        System.out.println("\nSearch Time of OXT:" + (query_total_time) + "ms");
        //oxt_server.Store_Server();
    }

}
