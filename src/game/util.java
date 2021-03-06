package game;

import de.uniba.wiai.lspi.chord.data.ID;

import java.math.BigInteger;
import java.net.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Random;

public class util {

	public static Random random_nums = new Random();

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static ID maxID () {
		byte[] tmp = new byte[20];
		tmp = util.hexStringToByteArray("fffffffffffffffffffffffffffffffffffffffe");
		return new ID(tmp);
	}

	public static ID maxAdress () {
		byte[] tmp = new byte[20];
		tmp = util.hexStringToByteArray("ffffffffffffffffffffffffffffffffffffffff");
		return new ID(tmp);
	}

	public static String getIp() throws SocketException {
		Enumeration<NetworkInterface> nets = java.net.NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			for (InetAddress addr : Collections.list(netint.getInetAddresses())) {
				if (!addr.isLinkLocalAddress() && !addr.isLoopbackAddress()) {
					// change to Inet6Address if you prefer ip6
					if (addr instanceof java.net.Inet4Address) {
						return addr.getHostAddress();
					}
				}
			}
		}
		return null;
	}

	public static ID getRandomId() {
		//TODO nicht ID aus eigenem Interval zurückgeben
		byte[] tmp = new byte[20];
		util.random_nums.nextBytes(tmp);
		return new ID(tmp);
	}
}
