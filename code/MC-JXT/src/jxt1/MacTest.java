package jxt1;
import java.security.GeneralSecurityException;

import util.*;

public class MacTest {
	
	public static void main(String[] args) throws GeneralSecurityException {
		AuthEnc authEnc = new AuthEnc();
		byte[] key = IntAndByte.toByteArray(1);
		byte[] mkey = IntAndByte.toByteArray(2);
		byte[] plain = "12345".getBytes();
		
		byte[] res = authEnc.EncAndMac(key, mkey, plain);
		
		byte[] dec = authEnc.DecAndVerify(res, key, mkey);
		System.out.print(new String(dec));
	}
}
