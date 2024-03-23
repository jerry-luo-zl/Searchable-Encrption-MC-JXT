package util;

public class IntAndByte {
	
	public static byte[] toByteArray(int iSource) {
		byte[] bytes = new byte[4];
	    bytes[0] = (byte) (iSource >> 24);
	    bytes[1] = (byte) (iSource >> 16);
	    bytes[2] = (byte) (iSource >> 8);
	    bytes[3] = (byte) iSource;
	    return bytes;
	}

	public static int toInt(byte[] bRefArr) {
	    int iOutcome = 0;
	    byte bLoop;
	  
	    for (int i = 0; i < bRefArr.length; i++) {
	        bLoop = bRefArr[i];
	        iOutcome += (bLoop & 0xFF) << (8 * (bRefArr.length-i-1));
	    }
	    return iOutcome;
	}
	
	public static void main(String[] args){
		int a = 5;
		byte[] b = IntAndByte.toByteArray(a);
		System.out.println(IntAndByte.toInt(b));
	}
}
