package game;

import java.net.*;
import java.util.Collections;
import java.util.Enumeration;

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
	
	public static String getIp() throws SocketException {
	    Enumeration<NetworkInterface> nets = java.net.NetworkInterface.getNetworkInterfaces();
	    for (NetworkInterface netint : Collections.list(nets)) {
	        for (InetAddress addr : Collections.list(netint.getInetAddresses())) {
	            if(!addr.isLinkLocalAddress() && !addr.isLoopbackAddress()) {
	               // change to Inet6Address if you prefer ip6
	                if (addr instanceof java.net.Inet4Address) { 
	                    return addr.getHostAddress();    
	                }
	            }
	        }
	    }
	    return null;
	}
}
