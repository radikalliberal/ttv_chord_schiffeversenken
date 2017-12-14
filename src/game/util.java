package game;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class util {

	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	final static ID getHashKey(Key entry) throws NoSuchAlgorithmException {

		if (entry == null) {
			throw new IllegalArgumentException(
					"Parameter entry must not be null!");
		}
		if (entry.getBytes() == null || entry.getBytes().length == 0) {
			throw new IllegalArgumentException(
					"Byte representation of Parameter must not be null or have length 0!");
		}

		byte[] testBytes = entry.getBytes();
		return createID(testBytes);
	}

	private static final ID createID(byte[] testBytes) throws NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
		synchronized (messageDigest) {
			messageDigest.reset();
			messageDigest.update(testBytes);
			return new ID(messageDigest.digest());
		}
	}
}
