package util;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Hash {

    private static Random random = new Random();
    public static void setSeed(long seed) {
        random.setSeed(seed);
    }

    public static long hash64(long x, long seed) {
        x += seed;
        x = (x ^ (x >>> 33)) * 0xff51afd7ed558ccdL;
        x = (x ^ (x >>> 33)) * 0xc4ceb9fe1a85ec53L;
        x = x ^ (x >>> 33);
        return x;
    }

    public static long randomSeed() {
        return random.nextLong();
    }


    public static int reduce(int hash, int n) {
        return (int) (((hash & 0xffffffffL) * n) >>> 32);
    }

    public static byte[]  Get_SHA_256(byte[] passwordToHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("Sha-256");
            md.update(passwordToHash);
            byte[] bytes = md.digest();

            return bytes;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static byte[]  Get_MD5(byte[] passwordToHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(passwordToHash);
            byte[] bytes = md.digest();

            return bytes;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[]  SHA_256_Int(long passwordToHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(longToBytes(passwordToHash));
            byte[] bytes = md.digest();
            return bytes;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ByteBuffer buffer = ByteBuffer.allocate(16);

    public static byte[] longToBytes(long x) {
        buffer.clear();
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static Element HashToZr(Pairing pairing, byte[] m){
        Element result = pairing.getZr().newElementFromHash(m, 0, m.length);
        return result;
    }

    public static Element HashToG1(Pairing pairing, byte[] m){
        Element result = pairing.getG1().newElementFromHash(m, 0, m.length);
        return result;
    }

}
