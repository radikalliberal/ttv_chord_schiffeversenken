package game;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.mbed.coap.client.CoapClient;
import com.mbed.coap.client.CoapClientBuilder;
import com.mbed.coap.exception.CoapException;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class Game {

	enum GameMode {
		DEMO, REAL
	};

    final static int numberOfFields = 100;
    final static int numberOfShips = 10;

	final static int port = 40003;
	final static int chordPort = 4242;
	static int numOfNpcs = 1;
	static int demoWait = 20;
	static GameMode mode = GameMode.DEMO;
	static String coapServerIp = null; // Coap-Server-IP
	static String bootstrapIp = null; // Chord-Server-IP
	
	/* Aufruf des Programms:
	 * 
	 * -chord <ip>     : Ip des Chord-Servers (demo mode macht einen Server unter der lokalen IP auf, alle andern müssen die IP explizit nennen)
	 * -coap <ip>      : IP des Coap-Servers (optional)
	 * -mode real|demo : Modi zum Debuggen und zum richtig spielen
	 * -n <int>        : Anzahl der NPC's bei Demo-Mode (optional)
	 * -t <int>        : Sekunden die gewartet wird bevor das Spiel startet damit sich die Fingertable einrichten können (optional)
	 *  
	 * java -jar game.jar -mode demo -n 10  // startet einen Server mit 10 Npc's
	 * java -jar game.jar -mode real -chord <ip> // joint den server unter <ip>
	 */

	public static void main(String[] args)
			throws InterruptedException, IllegalStateException, IOException, CoapException {
		de.uniba.wiai.lspi.chord.service.PropertiesLoader.loadPropertyFile();

		System.out.println("Game.maxID");
		System.out.println(util.maxID().toString());


		for(int i=0; i < args.length; i++) {
			if(args[i].equals("-coap")) { //Coap-Server-Flag
				coapServerIp = args[++i];
			} else if(args[i].equals("-chord")) { //Chord-Server-Flag
				bootstrapIp = args[++i];
			} else if(args[i].equals("-n")) { //Npcs-Flag Anzahl KI's
				numOfNpcs = Integer.parseInt(args[++i]);
			} else if(args[i].equals("-t")) { // Wartezeit
				demoWait = Integer.parseInt(args[++i]);
			} else if(args[i].equals("-mode")) { //Mode-Flag
				switch (args[++i]) {
				case "real":
					mode = GameMode.REAL;
					numOfNpcs = 1;
					break;
				case "demo":
				default:
					mode = GameMode.DEMO;
					break;
				}
			}
		}

		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);
		CoapClient client = null;
		// coap client initialisieren
		//System.out.print("IP Coap-Server: ");
		//String HOSTIP = in.next();
		if(coapServerIp != null) {
			client = CoapClientBuilder.newBuilder(new InetSocketAddress(coapServerIp, 5683)).build();
		}
		//CoapPacket coapResp = client.resource("/led").payload("0", MediaTypes.CT_TEXT_PLAIN).sync().put();

		//System.out.print("Game mode (demo or real): ");

		/*
		switch (in.next()) {
		case "real":
			mode = GameMode.REAL;
			numOfNpcs = 1;
			break;
		case "demo":
		default:
			mode = GameMode.DEMO;
			numOfNpcs = 1;
			break;
		}*/

		// im echten Spiel sind wir der einzige Npcs in unserer Liste
		List<Brain> npcs = new ArrayList<>();

		try {

			for (int i = 0; i < numOfNpcs; i++) {
				URL localURL = new URL("ocrmi://game:" + (port + i) + "/"); // Url kann beliebig sein
				System.out.println(util.getIp());
				URL bootstrapURL = new URL("ocrmi://" + util.getIp() + ":" + chordPort + "/"); // Diese Url wird vom
																								// Server vorgegeben
				if (mode == GameMode.REAL) {
					bootstrapURL = new URL("ocrmi://" + bootstrapIp + ":" + chordPort + "/");
				}

				Chord chord = new ChordImpl();
				Brain b = null;
				if(client != null){
					b = new Brain(chord, false, client);
				} else {
					b = new Brain(chord, true); // kein coap
				}
				
				npcs.add(b);

				// NotifyCallback bekannt machen muss geschehen bevor der Join passiert
				chord.setCallback(b);
				if(i==0 && mode == GameMode.DEMO) {
					chord.create(bootstrapURL);
				} else {
					chord.join(localURL, bootstrapURL);
				}
			}
			
		} catch (ServiceException e) {
			System.out.println("Could not join DHT !");
			e.printStackTrace();
			System.exit(0);
		} catch (MalformedURLException | SocketException e) {
			e.printStackTrace();
			System.exit(0);
		}

		if (mode == GameMode.REAL) {
			Brain us = npcs.get(0);

			System.out.print("All Players joined the Server?: ");
			if (in.next().equals("yes")) {
				System.out.println("ID: " + us.id.toHexString());
				us.start();
				if (us.lowestID()) {
					System.out.println("We start! write \"go\" to start");
					while(!in.next().equals("go"));
					us.startShot();
				} else {
					System.out.println("We dont start! We wait until first shot hits us.");
				}
			} else {
				System.out.println("exit game.");
				System.exit(0);
			}

			// close coap connection
			

		} else if (mode == GameMode.DEMO) {

			for (int k = demoWait; k > 0; k--) {
				System.out.print(k + " ");
				Thread.sleep(1000);
			}

			System.out.println("start demo");

			for (int j = 0; j < numOfNpcs; j++) {
				/*
				 * Hier schauen wir nach welche ID wir haben und für welche untere ID wir noch
				 * zuständig sind, das kann sich �ndern wenn noch jmd joint. Muss dann also
				 * nochmals ausgeführt werden.
				 */
				npcs.get(j).start();
			}


			for (int i = 0; i < numOfNpcs; i++) {
				if (npcs.get(i).lowestID()) {
					System.out.println(npcs.get(i).id + " faengt an!");
					npcs.get(i).startShot();
					break;
				}
			}

			// close coap connection
			if(client != null) client.close();
		}

	}

}
