package jxt1;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Vector;
import jxt1.entity.JXTEDB;
import jxt1.entity.JXTTSetTuple;
import jxt1.entity.KeywordFilesBlock;
import jxt1.entity.ytuple;
import sks.entity.KeywordFiles;
import util.AES;
import util.Hash;
import util.IntAndByte;

public class JXTProtocol {

    public Pairing pairing = PairingFactory.getPairing("params/curves/a.properties");
    public KeywordFilesBlock[] kfs;





    public JXTProtocol(){
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("KeywordFiles11.dat"));
            kfs= new KeywordFilesBlock[3];
            for(int i=0; i<3; i++)
                kfs[i]=new KeywordFilesBlock();
            kfs[0].kfs = (KeywordFiles[]) in.readObject();


            in.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("KeywordFiles22.dat"));
            kfs[1].kfs = (KeywordFiles[]) in.readObject();
            in.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("KeywordFiles33.dat"));
            kfs[2].kfs = (KeywordFiles[]) in.readObject();
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
        /*将明文数据库作为输入，并生成加密数据库EDB，该数据库将外包给(不受信任的)服务器。
        加密数据库EDB由两种数据结构组成——TSet和XSet。
        EDBSetup协议还生成一个密钥K，该密钥存储在客户端本地，随后用于生成查询令牌。
         */


        JXTEDB jxtedb = new JXTEDB(3,kfs[0].kfs.length+kfs[1].kfs.length);


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

        int v1=0;
        for(int i=0;i<3;i++) {

            for (int w = 0; w < 3; w++) {
                if (i<w){
                    int wtt = 100;
                    wtt = wtt +v1;
                    byte[] vv = IntAndByte.toByteArray(v1 + 1);
                    byte[] wt = IntAndByte.toByteArray(wtt);
                    for (int j = 0; j < kfs[i].kfs.length; j++) {

                        byte[] xtag = AES.encrypt(kfs[i].kfs[j].keyword.getBytes(), jxtedb.K_T);
                        jxtedb.XSet[v1][j].wj = wt;
                        jxtedb.XSet[v1][j].keyword_enc =AES.encrypt(xtag, vv);
                        for (int x = 0; x < kfs[i].kfs[j].files.length; x++) {

                            byte[] ind = IntAndByte.toByteArray(kfs[i].kfs[j].files[x]);
                            byte[] xind1 = AES.encrypt(ind, jxtedb.K_I);
                            byte[] xind2 = AES.encrypt(xind1, vv);
                            byte[] xw1 = AES.encrypt(wt, jxtedb.K_W);


                            Element xtag1 = Hash.HashToZr(pairing, xind2).add(Hash.HashToZr(pairing, xw1)).getImmutable();
                            jxtedb.XSet[v1][j].xtag.add(String.valueOf(xtag1));
                        }
                    }
                    v1++;
                }


            }
        }
        System.out.println(v1);


        for(int i=0;i<3;i++){


            for(int j=0; j<kfs[i].kfs.length; j++){
                byte[] ii = IntAndByte.toByteArray(i+1);
                byte[]  stag=AES.encrypt(kfs[i].kfs[j].keyword.getBytes(), jxtedb.K_T);
                jxtedb.TSet[i][j].keyword_enc = AES.encrypt(stag, ii);
                int cnt = 1;
                jxtedb.TSet[i][j].t = new JXTTSetTuple[kfs[i].kfs[j].files.length];
                byte[] z1 = AES.encrypt(kfs[i].kfs[j].keyword.concat(String.valueOf(0)).getBytes(), jxtedb.K_Z1);
                byte[] z2 = AES.encrypt(kfs[i].kfs[j].keyword.concat(String.valueOf(0)).getBytes(), jxtedb.K_Z2);
                for(int x=0; x<kfs[i].kfs[j].files.length; x++){
                    byte[] ind1 = IntAndByte.toByteArray(kfs[i].kfs[j].files[x]);
                    byte[] zcnt1 = AES.encrypt(kfs[i].kfs[j].keyword.concat(String.valueOf(cnt)).getBytes(), jxtedb.K_Z1);
                    byte[] zcnt2 = AES.encrypt(kfs[i].kfs[j].keyword.concat(String.valueOf(cnt)).getBytes(), jxtedb.K_Z2);
                    byte[] kew = AES.encrypt(kfs[i].kfs[j].keyword.getBytes(), jxtedb.K_enc);
                    byte[] ct = AES.encrypt(ind1, kew);
                    jxtedb.TSet[i][j].t[x] = new JXTTSetTuple();
                    jxtedb.TSet[i][j].t[x].ct = ct;
                    jxtedb.TSet[i][j].t[x].y= new ytuple[3];
                    cnt++;


                    for(int n=0;n<v1;n++){
                        int wtt=100;
                        wtt=wtt+n;
                        byte[] vv = IntAndByte.toByteArray(n+1);
                        byte[] wt = IntAndByte.toByteArray(wtt);
                        byte[] xind1 = AES.encrypt(ind1, jxtedb.K_I);
                        byte[] xind2 = AES.encrypt(xind1, vv);
                        byte[] xw1 = AES.encrypt(wt, jxtedb.K_W);

                        Element yt1 = Hash.HashToZr(pairing, xind2).sub(Hash.HashToZr(pairing,z1)).sub(Hash.HashToZr(pairing,zcnt1)).getImmutable();
                        Element yt2 = Hash.HashToZr(pairing, xw1).sub(Hash.HashToZr(pairing,z2)).sub(Hash.HashToZr(pairing,zcnt2)).getImmutable();

                        jxtedb.TSet[i][j].t[x].y[n] = new ytuple();

                        jxtedb.TSet[i][j].t[x].y[n].t=wt;
                        jxtedb.TSet[i][j].t[x].y[n].y1 = yt1;
                        jxtedb.TSet[i][j].t[x].y[n].y2 = yt2;


                    }



                }
            }


        }

        return jxtedb;

    }

    /*客户端和服务端之间的交互
    由客户机和服务器共同执行的两方协议，其中客户机的输入是要执行的查询，服务器的输入是加密的数据库EDB。
    它由单轮通信组成(即从客户机到服务器的查询消息，然后是从服务器到客户机的响应消息)。
    在协议的最后，客户端需要学习与连接查询匹配的记录索引集(跨两个表)。
     */





    public byte[] ClientGenStag1(int i1,String w1, byte[] K_T){
        byte[] tag=AES.encrypt(w1.getBytes(),K_T);
        byte[] ii = IntAndByte.toByteArray(i1);

        return AES.encrypt(tag,ii);
    }
    public byte[] ClientGenStag2(int j1,String w2, byte[] K_T){
        byte[] tag=AES.encrypt(w2.getBytes(),K_T);
        byte[] jj = IntAndByte.toByteArray(j1);

        return AES.encrypt(tag,jj);
    }
    public byte[] ClientGenStag3(int v,String w2, byte[] K_T){
        byte[] tag=AES.encrypt(w2.getBytes(),K_T);
        byte[] vv = IntAndByte.toByteArray(v);

        return AES.encrypt(tag,vv);
    }
    public JXTTSetTuple[] SearchStag1(int i1,byte[] stag1, JXTEDB jxtedb){
        for(int i=0; i<jxtedb.TSet[i1-1].length; i++)
            if(Arrays.equals(stag1, jxtedb.TSet[i1-1][i].keyword_enc))
                return jxtedb.TSet[i1-1][i].t;

        return null;
    }
    public JXTTSetTuple[] SearchStag2(int j1,byte[] stag2, JXTEDB jxtedb){

        for(int i=0; i<jxtedb.TSet[j1-1].length; i++)
            if(Arrays.equals(stag2, jxtedb.TSet[j1-1][i].keyword_enc))
                return jxtedb.TSet[j1-1][i].t;
        return null;
    }
    public int SearchWJoin(int i1, int j1){
        int v=0;

        for(int i=1;i<=3;i++)
        {
            for(int j=1;j<=3;j++){
                if(i<j){

                    v++;
                    if(i==i1&&j==j1){

                        return v;
                    }



                }

            }
        }

        return 0;
    }
    public Element[] ClientGenxjointoken1(String w1,String w2,  int cnt1, JXTEDB jxtedb){

        Element[] xjointoken1 = new Element[cnt1];


        for(int i=1; i<=cnt1; i++){

            byte[] z11 = AES.encrypt(w1.concat(String.valueOf(i)).getBytes(), jxtedb.K_Z2);
            byte[] z12 = AES.encrypt(w2.concat(String.valueOf(0)).getBytes(), jxtedb.K_Z1);
            xjointoken1[i-1]= Hash.HashToZr(pairing, z11).add(Hash.HashToZr(pairing,z12)).getImmutable();


        }

        return xjointoken1;
    }
    public Element[] ClientGenxjointoken2( String w1,String w2, int cnt2, JXTEDB jxtedb){


        Element[] xjointoken2 = new Element[cnt2];
        for(int i=1; i<=cnt2; i++){


            byte[] z21 = AES.encrypt(w1.concat(String.valueOf(0)).getBytes(), jxtedb.K_Z2);
            byte[] z22 = AES.encrypt(w2.concat(String.valueOf(i)).getBytes(), jxtedb.K_Z1);
            xjointoken2[i-1]= Hash.HashToZr(pairing, z21).add(Hash.HashToZr(pairing,z22)).getImmutable();


        }


        return xjointoken2;
    }

    /*public boolean IsInXSet(Element xtag, Vector<String> XSet){
        int flag=0;
        for(int i=0; i<XSet.size(); i++)
            if(XSet.get(i).equals(xtag.toString()))
                flag=1;
        if(flag == 1)
            return true;
        else
            return false;
    }

     */

    public Element[] ServerGenxtoken1(byte[] wtt,Element[] xjointoken1, int cnt1,JXTTSetTuple[] tuple, JXTEDB jxtedb){
        Element[] xtoken1 = new Element[cnt1];
        for(int i=0; i<cnt1; i++){
            for(int j=0;j<3;j++){

                if(Arrays.equals (tuple[i].y[j].t, wtt))
                {

                    xtoken1[i] = xjointoken1[i].add(tuple[i].y[j].y2).getImmutable();

                }
            }

        }
        System.out.println(xtoken1.length);

        return xtoken1;
    }


    public Element[] ServerGenxtoken2(byte[] wtt,Element[] xjointoken2, int cnt2,JXTTSetTuple[] tuple, JXTEDB jxtedb){
        Element[] xtoken2 = new Element[cnt2];
        for(int i=0; i<cnt2; i++){
            for(int j=0;j<3;j++){

                if(Arrays.equals (tuple[i].y[j].t, wtt))
                {

                    xtoken2[i] = xjointoken2[i].add(tuple[i].y[j].y1).getImmutable();


                }

            }

        }
        System.out.println(xtoken2.length);

        return xtoken2;
    }

    /*public Vector<byte[]> Search1(byte[] stag1,Element[] xtoken1,Element[] xtoken2, JXTTSetTuple[] tuple1, JXTEDB jxtedb){
        Vector<byte[]> ct1 = new Vector<byte[]>();

        for(int i=0; i<4; i++){
            boolean flag = true;
            Element xtag = xtoken1[i].add(xtoken2[i]).getImmutable();
            for(int j=0; j<jxtedb.XSet1.length; j++)
                if(Arrays.equals(stag1, jxtedb.XSet1[j].keyword_enc))
                    //flag =  this.IsInXSet(xtag, jxtedb.XSet1[j].xtag);
                    flag = flag && jxtedb.XSet1[j].xtag.contains(xtag.toString());

            if(flag)
                ct1.add(tuple1[i].ct);
        }

        return ct1;
    }

     */


    public Vector<byte[]> Search2(int v,byte[] stag3,int cnt1,int cnt2,Element[] xtoken1,Element[] xtoken2, JXTTSetTuple[] tuple2, JXTEDB jxtedb){
        Vector<byte[]> ct2 = new Vector<byte[]>();
        int cnt;
        if(cnt1>=cnt2)
            cnt=cnt2;
        else
            cnt=cnt1;


        for(int i=0; i<cnt; i++){
            boolean flag = true;

            Element xtag = xtoken1[i].add(xtoken2[i]).getImmutable();
            System.out.println("xtag"+xtag);
            for(int j=0; j<jxtedb.XSet[v].length; j++)
                if(Arrays.equals(stag3, jxtedb.XSet[v][j].keyword_enc))
                    //flag =  this.IsInXSet(xtag, jxtedb.XSet1[j].xtag);
                    flag = flag && jxtedb.XSet[v][j].xtag.contains(String.valueOf(xtag));


            //flag = flag && jxtedb.XSet1.contains(xtag.toString());


            if(flag)
                ct2.add(tuple2[i].ct);
        }

        return ct2;
    }


    /*public byte[][] ClientGetind1(Vector<byte[]> ct1, String w1, byte[] K_enc){
        byte[][] ind1 = new byte[ct1.size()][];
        byte[] K_encw1 = AES.encrypt(w1.getBytes(), K_enc);
        for(int i=0; i<ct1.size(); i++)
            ind1[i] = AES.decrypt(ct1.get(i), K_encw1);
        return ind1;
    }

     */
    public byte[][] ClientGetind2(Vector<byte[]> ct2, String w2, byte[] K_enc){
        byte[][] ind2 = new byte[ct2.size()][];
        byte[] K_encw2 = AES.encrypt(w2.getBytes(), K_enc);
        for(int i=0; i<ct2.size(); i++)
            ind2[i] = AES.decrypt(ct2.get(i), K_encw2);
        return ind2;
    }

    public static void main(String[] args){
        long start, end;

        jxt1.JXTProtocol jxtp = new jxt1.JXTProtocol();
        start = System.nanoTime();
        JXTEDB jxtedb = jxtp.Setup();
        end = System.nanoTime();
        System.out.println("JXTEDB generate time " + (end - start));

        String[] keywords = {"war","merged"};
        int i1=2;
        int j1=3;
        byte[] w={102};
        System.out.print("Keywords: ");
        for(int i=0; i<keywords.length; i++)
            System.out.print(keywords[i] + " ");
        System.out.print(i1 + " "+j1+" "+w);
        int v=jxtp.SearchWJoin(i1,j1);
        System.out.println("vvv"+v);

        System.out.println("\nClient is generating stag ... ");
        start = System.nanoTime();
        byte[] stag1 = jxtp.ClientGenStag1(i1,keywords[0], jxtedb.K_T);
        byte[] stag2 = jxtp.ClientGenStag2(j1,keywords[1], jxtedb.K_T);
        byte[] stag3 = jxtp.ClientGenStag3(v,keywords[0], jxtedb.K_T);

        end = System.nanoTime();
        System.out.println("Stag generate time " + (end - start));

        System.out.println("Server is searching stag in EDB ...");
        start = System.nanoTime();
        JXTTSetTuple[] tuple1 = jxtp.SearchStag1(i1,stag1, jxtedb);
        JXTTSetTuple[] tuple2 = jxtp.SearchStag2(j1,stag2, jxtedb);
        end = System.nanoTime();
        System.out.println("Stag search time " + (end - start));

        System.out.println("\nClient is generating xtoken ... ");
        start = System.nanoTime();


        int cnt1= tuple1.length;
        int cnt2= tuple2.length;


        Element[] xjointoken1 = jxtp.ClientGenxjointoken1(keywords[0],keywords[1],cnt1, jxtedb);
        Element[] xjointoken2 = jxtp.ClientGenxjointoken2(keywords[0],keywords[1],cnt2, jxtedb);


        Element[] xtoken1 = jxtp.ServerGenxtoken1(w,xjointoken1, cnt1,tuple1, jxtedb);
        Element[] xtoken2 = jxtp.ServerGenxtoken2(w,xjointoken2, cnt2,tuple2, jxtedb);

        end = System.nanoTime();
        System.out.println("xtoken generate time " + (end - start));

        System.out.println("Server is searching XSet ...");
        start = System.nanoTime();
        //Vector<byte[]> ct1 = jxtp.Search1(stag1,xtoken1,xtoken2,tuple1, jxtedb);
        Vector<byte[]> ct2 = jxtp.Search2(v-1,stag3,cnt1,cnt2,xtoken1,xtoken2,tuple2, jxtedb);
        end = System.nanoTime();
        System.out.println("XSet search time " + (end - start));

        System.out.println("Client gets inds.");
        start = System.nanoTime();
        //byte[][] ind1 = jxtp.ClientGetind1(ct1, keywords[0], jxtedb.K_enc);
        byte[][] ind2 = jxtp.ClientGetind2(ct2, keywords[1], jxtedb.K_enc);
        end = System.nanoTime();

        /*for(int i=0; i<ind1.length; i++)
            System.out.println("ind1" + (i+1) + ": " + AES.parseByte2HexStr(ind1[i]));

         */


        for(int i=0; i<ind2.length; i++)
            System.out.println("ind2" + (i+1) + ": " + IntAndByte.toInt(ind2[i]));
        end = System.nanoTime();

        System.out.println("Client gets ind time " + (end - start));
    }
}
