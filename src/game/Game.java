package game;

import java.io.IOException;
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

	final static int port = 40003;
	final static int chordPort = 4242;
	static int numOfNpcs = 1;
	static int demoWait = 40;
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
		
		// Programmarguemnte abgreifen
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

		// coap client initialisieren
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);
		CoapClient client = null;
		if(coapServerIp != null) {
			client = CoapClientBuilder.newBuilder(new InetSocketAddress(coapServerIp, 5683)).build();
		}

		// im echten Spiel sind wir der einzige Npcs in unserer Liste
		List<Brain> npcs = new ArrayList<>();

		try {

			for (int i = 0; i < numOfNpcs; i++) {
				// URLs für die Npcs zusammensetzen
				URL localURL = new URL("ocrmi://game:" + (port + i) + "/"); // Url kann beliebig sein
				System.out.println(util.getIp());
				URL bootstrapURL = new URL("ocrmi://" + util.getIp() + ":" + chordPort + "/"); // Diese Url wird vom Server vorgegeben

				if (mode == GameMode.REAL) {
					bootstrapURL = new URL("ocrmi://" + bootstrapIp + ":" + chordPort + "/");
				}

				// Chord initialisieren
				Chord chord = new ChordImpl();
				Brain b = null;
				if(client == null){
					b = new Brain(chord, false, client);
				} else {
					b = new Brain(chord, true); // kein coap
				}
				
				npcs.add(b);

				// NotifyCallback bekannt machen muss geschehen bevor der Join passiert
				chord.setCallback(b);
				// erster Chordnode erstellt das Netzwerk, alle weiteren joinen über diesen
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
			// Spieler muss Start bestätigen, wenn Netzwerk bereit
			System.out.print("All Players joined the Server?: ");
			if (in.next().equals("yes")) {
				System.out.println("ID: " + npcs.get(0).chord.getID().toHexString());
				npcs.get(0).claimIds();
				// wenn wir die größte ID im Interval haben, fangen wir an
				if (npcs.get(0).lowestID()) {
					System.out.println("We start! write \"go\" to start");
					while(!in.next().equals("go"));
					try {
						npcs.get(0).chord.retrieve(npcs.get(0).getRandomId()); // Schuss auf zufälliges Ziel
					} catch (ServiceException e) {
						System.out.println(e);
						System.exit(0);
					}
				} else {
					System.out.println("We dont start! We wait until first shot hits us.");
				}
			} else {
				System.out.println("exit game.");
				System.exit(0);
			}


		} else if (mode == GameMode.DEMO) {
			// Swarte demoWait in Sekunden bis sich die Fingertable eingerichtet haben
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
				npcs.get(j).claimIds();
			}

			try {
				for (int i = 0; i < numOfNpcs; i++) {
					if (npcs.get(i).lowestID()) {
						ID target = npcs.get(i).getRandomId();
						System.out.println(npcs.get(i).id + " faengt an!");
						System.out.println(npcs.get(i).id + ": Ich schiesse auf " + target);
						npcs.get(i).chord.retrieve(target); // Schuss auf zufälliges Ziel
						break;
					}
				}
			} catch (ServiceException e) {
				System.out.println(e);
				System.exit(0);
			}
			// close coap connection
			if(client != null) client.close();
		}

	}

}
