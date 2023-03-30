package util;

public class IntAndByte {
	
	//��Intת��ΪbyteArr
	public static byte[] toByteArray(int iSource) {
	    byte[] bLocalArr = new byte[1];
	    for (int i = 0; i < 1; i++) {
	        bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);
	    }
	    return bLocalArr;
	}

	// ��byte����bRefArrתΪһ������,�ֽ�����ĵ�λ�����͵ĵ��ֽ�λ
	public static int toInt(byte[] bRefArr) {
	    int iOutcome = 0;
	    byte bLoop;

	    for (int i = 0; i < bRefArr.length; i++) {
	        bLoop = bRefArr[i];
	        iOutcome += (bLoop & 0xFF) << (8 * i);
	    }
	    return iOutcome;
	}
	
	public static void main(String[] args){
		int a = 5;
		byte[] b = IntAndByte.toByteArray(a);
		System.out.println(IntAndByte.toInt(b));
	}
}
