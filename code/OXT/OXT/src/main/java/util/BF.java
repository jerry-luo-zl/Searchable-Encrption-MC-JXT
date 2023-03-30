package util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BF {

    public static byte[] create(int n){
        byte[] bits = new byte[getIndex(n*29) + 1];
        return bits;
    }

    public static byte[] F(int k, byte[] message){

        byte[] a = new byte[1];
        a[0] = (byte)k;
        String s =  new String(message)+k;
        return Hash.Get_SHA_256(s.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] add_element(int k, byte[] message, byte[] bits){
        int i=0 ,c = 0,len = 8*bits.length;
        while(i<k){
            byte[] a = F(c+1, message);
            for(int j=0;j<8; j++){
                int b_index = (bytesToInt(a,0+4*j))%len;
                if(b_index<0)
                    b_index = b_index+len;
                add(bits,b_index);
                i++;
                if(i==k)
                    break;
            }
            c++;
        }

        return bits;
    }

    public static boolean Test_BF(int k, byte[] message, byte[] bits){
        int i=0 ,c = 0,len = 8*bits.length;
        while(i<k){
            byte[] a = F(c+1, message);
            for(int j=0;j<8; j++){
                int b_index = bytesToInt(a,0+4*j)%len;
                if(b_index<0)
                    b_index = b_index+len;
                if(contains(bits,b_index) != true){
                    return false;
                }
                i++;
                if(i==k)
                    break;
            }
            c++;
        }

        return true;
    }
    public static void add(byte[] bits, int num){
        bits[getIndex(num)] |= 1 << getPosition(num);
    }

    public static boolean contains(byte[] bits, int num){
        return (bits[getIndex(num)] & 1 << getPosition(num)) != 0;
    }

    public static int getIndex(int num){
        return num >> 3;
    }

    public static int getPosition(int num){
        return num & 0x07;
    }



    public static void main(String[] args){
        BF bf = new BF();
        byte b[] = bf.create(100);
        String[] list = {"a","b","c","d"};
        for(int i=0;i<list.length;i++){
            bf.add_element(1,Hash.Get_MD5((list[i]).getBytes(StandardCharsets.UTF_8)),b);
        }

        boolean tag = bf.Test_BF(1,Hash.Get_MD5(("b").getBytes(StandardCharsets.UTF_8)), b);
        System.out.println(tag);
    }


    private static ByteBuffer buffer = ByteBuffer.allocate(4);


    public static int bytesToInt(byte[] bytes,int offset) {
        buffer.clear();
        buffer.put(bytes, offset, 4);
        buffer.flip();//need flip
        return buffer.getInt();
    }
}