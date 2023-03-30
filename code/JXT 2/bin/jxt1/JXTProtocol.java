package jxt1;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Vector;
import jxt1.entity.JXTEDB;
import jxt1.entity.JXTTSetTuple;
import sks.entity.KeywordFiles;
import util.AES;
import util.Hash;
import util.IntAndByte;

public class JXTProtocol {
    public KeywordFiles[] kfs1,kfs2;
    public Pairing pairing = PairingFactory.getPairing("params/curves/a.properties");


    public JXTProtocol(){
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("KeywordFiles1.dat"));
            kfs1 = (KeywordFiles[]) in.readObject();
            in.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("KeywordFiles2.dat"));
            kfs2 = (KeywordFiles[]) in.readObject();
            in.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }



    public JXTEDB Setup(){
        JXTEDB jxtedb = new JXTEDB(kfs1.length+kfs2.length);


        byte[] K_I = {3, 3, 3};
        byte[] K_W = {4, 4, 4};
        byte[] K_Z1 = {5, 5, 5};
        byte[] K_Z2 = {6, 6, 6};
        byte[] K_enc = {7, 7, 7};
        byte[] K_T = {8, 8, 8};
        jxtedb.K_I = K_I;
        jxtedb.K_W = K_W;
        jxtedb.K_Z1 = K_Z1;
        jxtedb.K_Z2 = K_Z2;
        jxtedb.K_enc = K_enc;
        jxtedb.K_T = K_T;

        int n=1;
        for(int j=0; j<kfs1[n].files.length; j++){

            byte ind[] = IntAndByte.toByteArray(kfs1[n].files[j]);
            byte[] xind1 = AES.encrypt(ind, jxtedb.K_I);
            byte[] xw1 = AES.encrypt(kfs1[n].keyword.getBytes(), jxtedb.K_W);


            Element xtag1 = Hash.HashToZr(pairing, xind1).add(Hash.HashToZr(pairing,xw1)).getImmutable();
            jxtedb.XSet1.add(xtag1.toString());
        }

        for(int i=0; i<kfs1.length; i++){
            jxtedb.TSet1[i].keyword_enc = AES.encrypt(kfs1[i].keyword.getBytes(), jxtedb.K_T);
            int cnt = 1;

            jxtedb.TSet1[i].t = new JXTTSetTuple[kfs1[i].files.length];
            byte[] z1 = AES.encrypt(kfs1[i].keyword.concat(String.valueOf(0)).getBytes(), jxtedb.K_Z1);
            byte[] z2 = AES.encrypt(kfs1[i].keyword.concat(String.valueOf(0)).getBytes(), jxtedb.K_Z2);
            for(int j=0; j<kfs1[i].files.length; j++){

                byte ind1[] = IntAndByte.toByteArray(kfs1[i].files[j]);

                byte[] zcnt1 = AES.encrypt(kfs1[i].keyword.concat(String.valueOf(cnt)).getBytes(), jxtedb.K_Z1);
                byte[] zcnt2 = AES.encrypt(kfs1[i].keyword.concat(String.valueOf(cnt)).getBytes(), jxtedb.K_Z2);
                byte[] xind1 = AES.encrypt(ind1, jxtedb.K_I);
                byte[] xw1 = AES.encrypt(kfs1[n].keyword.getBytes(), jxtedb.K_W);
                Element yt1 = Hash.HashToZr(pairing, xind1).sub(Hash.HashToZr(pairing,z1)).sub(Hash.HashToZr(pairing,zcnt1)).getImmutable();
                Element yt2 = Hash.HashToZr(pairing, xw1).sub(Hash.HashToZr(pairing,z2)).sub(Hash.HashToZr(pairing,zcnt2)).getImmutable();



                byte[] kew = AES.encrypt(kfs1[i].keyword.getBytes(), jxtedb.K_enc);

                byte[] ct = AES.encrypt(ind1, kew);
                jxtedb.TSet1[i].t[j] = new JXTTSetTuple();
                jxtedb.TSet1[i].t[j].ct = ct;
                jxtedb.TSet1[i].t[j].yt1 = yt1.duplicate().getImmutable();
                jxtedb.TSet1[i].t[j].yt2 = yt2.duplicate().getImmutable();

                cnt++;
            }
        }




        n=0;
        for(int j=0; j<kfs2[n].files.length; j++){

            byte ind[] = IntAndByte.toByteArray(kfs2[n].files[j]) ;
            byte[] xind2 = AES.encrypt(ind, jxtedb.K_I);
            byte[] xw2 = AES.encrypt(kfs2[n].keyword.getBytes(), jxtedb.K_W);

            Element xtag2 = Hash.HashToZr(pairing, xind2).add(Hash.HashToZr(pairing,xw2)).getImmutable();
            jxtedb.XSet2.add(xtag2.toString());
        }

        for(int i=0; i<kfs2.length; i++){
            jxtedb.TSet2[i].keyword_enc = AES.encrypt(kfs2[i].keyword.getBytes(), jxtedb.K_T);

            int cnt = 1;
            jxtedb.TSet2[i].t = new JXTTSetTuple[kfs2[i].files.length];
            byte[] z1 = AES.encrypt(kfs2[i].keyword.concat(String.valueOf(0)).getBytes(), jxtedb.K_Z1);
            byte[] z2 = AES.encrypt(kfs2[i].keyword.concat(String.valueOf(0)).getBytes(), jxtedb.K_Z2);
            for(int j=0; j<kfs2[i].files.length; j++){ //设置简单的permutation，异或某个数
                byte ind2[] = IntAndByte.toByteArray(kfs2[i].files[j]);

                byte[] zcnt1 = AES.encrypt(kfs2[i].keyword.concat(String.valueOf(cnt)).getBytes(), jxtedb.K_Z1);
                byte[] zcnt2 = AES.encrypt(kfs2[i].keyword.concat(String.valueOf(cnt)).getBytes(), jxtedb.K_Z2);
                byte[] xind2 = AES.encrypt(ind2, jxtedb.K_I);
                byte[] xw2 = AES.encrypt(kfs2[n].keyword.getBytes(), jxtedb.K_W);
                Element yt1 = Hash.HashToZr(pairing, xind2).sub(Hash.HashToZr(pairing,z1)).sub(Hash.HashToZr(pairing,zcnt1)).getImmutable();
                Element yt2 = Hash.HashToZr(pairing, xw2).sub(Hash.HashToZr(pairing,z2)).sub(Hash.HashToZr(pairing,zcnt2)).getImmutable();



                byte[] kew = AES.encrypt(kfs2[i].keyword.getBytes(), jxtedb.K_enc);

                byte[] ct = AES.encrypt(ind2, kew);
                jxtedb.TSet2[i].t[j] = new JXTTSetTuple();
                jxtedb.TSet2[i].t[j].ct = ct;
                jxtedb.TSet2[i].t[j].yt1 = yt1.duplicate().getImmutable();
                jxtedb.TSet2[i].t[j].yt2 = yt2.duplicate().getImmutable();

                cnt++;
            }
        }


        return jxtedb;
    }

    public byte[] ClientGenStag1(String w1, byte[] K_T){

        return AES.encrypt(w1.getBytes(),K_T);
    }
    public byte[] ClientGenStag2(String w2, byte[] K_T){

        return AES.encrypt(w2.getBytes(),K_T);
    }
    public JXTTSetTuple[] SearchStag1(byte[] stag1, JXTEDB jxtedb){
        for(int i=0; i<jxtedb.TSet1.length; i++)
            if(Arrays.equals(stag1, jxtedb.TSet1[i].keyword_enc))
                return jxtedb.TSet1[i].t;

        return null;
    }
    public JXTTSetTuple[] SearchStag2(byte[] stag2, JXTEDB jxtedb){

        for(int i=0; i<jxtedb.TSet2.length; i++)
            if(Arrays.equals(stag2, jxtedb.TSet2[i].keyword_enc))
                return jxtedb.TSet2[i].t;
        return null;
    }
    public Element[] ClientGenxjointoken1(String w1,String w2,  int cntsize, JXTEDB jxtedb){

        Element[] xjointoken1 = new Element[cntsize];

        for(int i=1; i<=cntsize; i++){

            byte[] z11 = AES.encrypt(w1.concat(String.valueOf(i)).getBytes(), jxtedb.K_Z2);
            byte[] z12 = AES.encrypt(w2.concat(String.valueOf(0)).getBytes(), jxtedb.K_Z1);

            xjointoken1[i-1]= Hash.HashToZr(pairing, z11).add(Hash.HashToZr(pairing,z12)).getImmutable();

        }

        return xjointoken1;
    }
    public Element[] ClientGenxjointoken2( String w1,String w2, int cntsize, JXTEDB jxtedb){


        Element[] xjointoken2 = new Element[cntsize];
        for(int i=1; i<=cntsize; i++){


            byte[] z21 = AES.encrypt(w1.concat(String.valueOf(0)).getBytes(), jxtedb.K_Z2);
            byte[] z22 = AES.encrypt(w2.concat(String.valueOf(i)).getBytes(), jxtedb.K_Z1);

            xjointoken2[i-1]= Hash.HashToZr(pairing, z21).add(Hash.HashToZr(pairing,z22)).getImmutable();

        }

        return xjointoken2;
    }

    public boolean IsInXSet(Element xtag, Vector<String> XSet){
        int flag=0;
        for(int i=0; i<XSet.size(); i++)
            if(XSet.get(i).equals(xtag.toString()))
                flag=1;
        if(flag == 1)
            return true;
        else
            return false;
    }

    public Element[] ServerGenxtoken1(Element[] xjointoken1, JXTTSetTuple[] tuple, JXTEDB jxtedb){
        Element[] xtoken1 = new Element[10];

        for(int i=0; i<4; i++){


            xtoken1[i] = xjointoken1[i].add(tuple[i].yt2).getImmutable();

        }

        return xtoken1;
    }

    public Element[] ServerGenxtoken2(Element[] xjointoken2, JXTTSetTuple[] tuple, JXTEDB jxtedb){
        Element[] xtoken2 = new Element[10];

        for(int i=0; i<4; i++){

            xtoken2[i] = xjointoken2[i].add(tuple[i].yt1).getImmutable();

        }

        return xtoken2;
    }
    public Vector<byte[]> Search1(Element[] xtoken1,Element[] xtoken2, JXTTSetTuple[] tuple1, JXTEDB jxtedb){
        Vector<byte[]> ct1 = new Vector<byte[]>();

        for(int i=0; i<4; i++){
            boolean flag = true;
            Element xtag = xtoken1[i].add(xtoken2[i]).getImmutable();
            flag = this.IsInXSet(xtag, jxtedb.XSet1);
            //flag = flag && jxtedb.XSet1.contains(xtag.toString());

            if(flag)
                ct1.add(tuple1[i].ct);
        }

        return ct1;
    }
    public Vector<byte[]> Search2(Element[] xtoken1,Element[] xtoken2, JXTTSetTuple[] tuple2, JXTEDB jxtedb){
        Vector<byte[]> ct2 = new Vector<byte[]>();

        for(int i=0; i<4; i++){
            boolean flag = true;
            Element xtag = xtoken1[i].add(xtoken2[i]).getImmutable();
            flag =  this.IsInXSet(xtag, jxtedb.XSet1);
            //flag = flag && jxtedb.XSet1.contains(xtag.toString());


            if(flag)
                ct2.add(tuple2[i].ct);
        }

        return ct2;
    }


    public byte[][] ClientGetind1(Vector<byte[]> ct1, String w1, byte[] K_enc){
        byte[][] ind1 = new byte[ct1.size()][];
        byte[] K_encw1 = AES.encrypt(w1.getBytes(), K_enc);
        for(int i=0; i<ct1.size(); i++)
            ind1[i] = AES.decrypt(ct1.get(i), K_encw1);
        return ind1;
    }
    public byte[][] ClientGetind2(Vector<byte[]> ct2, String w2, byte[] K_enc){
        byte[][] ind2 = new byte[ct2.size()][];
        byte[] K_encw2 = AES.encrypt(w2.getBytes(), K_enc);
        for(int i=0; i<ct2.size(); i++)
            ind2[i] = AES.decrypt(ct2.get(i), K_encw2);
        return ind2;
    }

    public static void main(String args[]){
        long start, end;

        jxt1.JXTProtocol jxtp = new jxt1.JXTProtocol();
        start = System.nanoTime();
        JXTEDB jxtedb = jxtp.Setup();

        end = System.nanoTime();
        System.out.println("JXTEDB generate time " + (end - start));

        String keywords[] = {"2","6"};
        System.out.print("Keywords: ");
        for(int i=0; i<keywords.length; i++)
            System.out.print(keywords[i] + " ");

        System.out.println("\nClient is generating stag ... ");
        start = System.nanoTime();
        byte[] stag1 = jxtp.ClientGenStag1(keywords[0], jxtedb.K_T);
        byte[] stag2 = jxtp.ClientGenStag2(keywords[1], jxtedb.K_T);

        end = System.nanoTime();
        System.out.println("Stag generate time " + (end - start));

        System.out.println("Server is searching stag in EDB ...");
        start = System.nanoTime();
        JXTTSetTuple[] tuple1 = jxtp.SearchStag1(stag1, jxtedb);
        JXTTSetTuple[] tuple2 = jxtp.SearchStag2(stag2, jxtedb);
        end = System.nanoTime();
        System.out.println("Stag search time " + (end - start));

        System.out.println("\nClient is generating xtoken ... ");
        start = System.nanoTime();
        Element[] xjointoken1 = jxtp.ClientGenxjointoken1(keywords[0],keywords[1], 4, jxtedb);
        Element[] xjointoken2 = jxtp.ClientGenxjointoken2(keywords[0],keywords[1], 4, jxtedb);
        Element[] xtoken1 = jxtp.ServerGenxtoken1(xjointoken1, tuple1, jxtedb);
        Element[] xtoken2 = jxtp.ServerGenxtoken2(xjointoken2, tuple2, jxtedb);

        end = System.nanoTime();
        System.out.println("xtoken generate time " + (end - start));

        System.out.println("Server is searching XSet ...");
        start = System.nanoTime();
        Vector<byte[]> ct1 = jxtp.Search1(xtoken1,xtoken2,tuple1, jxtedb);
        Vector<byte[]> ct2 = jxtp.Search2(xtoken1,xtoken2,tuple2, jxtedb);
        end = System.nanoTime();
        System.out.println("XSet search time " + (end - start));

        System.out.println("Client gets inds.");
        start = System.nanoTime();
        byte[][] ind1 = jxtp.ClientGetind1(ct1, keywords[0], jxtedb.K_enc);
        byte[][] ind2 = jxtp.ClientGetind2(ct2, keywords[1], jxtedb.K_enc);
        end = System.nanoTime();

        for(int i=0; i<ind1.length; i++)
            System.out.println("ind1" + (i+1) + ": " + AES.parseByte2HexStr(ind1[i]));
        for(int i=0; i<ind2.length; i++)
            System.out.println("ind2" + (i+1) + ": " + AES.parseByte2HexStr(ind2[i]));
        end = System.nanoTime();

        System.out.println("Client gets ind time " + (end - start));
    }
}
