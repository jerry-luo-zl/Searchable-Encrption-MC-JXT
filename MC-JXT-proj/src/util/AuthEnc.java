package util;

import java.security.InvalidKeyException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.plugins.tiff.ExifGPSTagSet;

	

public class AuthEnc {
	
	private static final int IV_LENGTH = 16;
    private static final int MAC_LENGTH = 32;
    
	public byte[] EncAndMac(byte[] key, byte[] mkey, byte[] plaintext) {

        // Encrypt the plaintext
        byte[] ciphertext = AES.encrypt(plaintext, key);
        
    
        // Calculate the MAC
        
        byte[] macValue = null;
        try {
        	Mac mac = Mac.getInstance("HmacSHA256");
        	SecretKeySpec macKey = new SecretKeySpec(mkey, 0, mkey.length, "HmacSHA256");
        	mac.init(macKey);
        	macValue = mac.doFinal(ciphertext);
        } catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {	
			e.printStackTrace();
		}
        
        // Concatenate the IV, ciphertext, and MAC
        byte[] result = new byte[ciphertext.length + MAC_LENGTH];
        System.arraycopy(ciphertext, 0, result, 0, ciphertext.length);
        System.arraycopy(macValue, 0, result, ciphertext.length, MAC_LENGTH);

        return result;
    }
	
	
	public byte[] DecAndVerify(byte[] cipherText, byte[] key, byte[] mkey) {
		byte[] encrypted = Arrays.copyOfRange(cipherText, 0, cipherText.length - MAC_LENGTH);
		byte[] macBytes = Arrays.copyOfRange(cipherText, cipherText.length - MAC_LENGTH, cipherText.length);
		
		byte[] calculatedMacBytes = null;
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec macKey = new SecretKeySpec(mkey, "HmacSHA256");
			mac.init(macKey);
			calculatedMacBytes = mac.doFinal(encrypted);
		
			if (!Arrays.equals(macBytes, calculatedMacBytes)) {
				System.out.print("MAC check failed");
				return null;
			}
			
			byte[] decrypted = AES.decrypt(encrypted, key);
			return decrypted;
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
}