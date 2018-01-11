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
import com.mbed.coap.packet.CoapPacket;
import com.mbed.coap.packet.MediaTypes;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class Game {

	enum GameMode {
		DEMO, REAL
	};

	final static int port = 40000;
	final static int chordPort = 4242;
	static int numOfNpcs = 10;
	final static int demoWait = 20;
	static GameMode mode = GameMode.DEMO;

	public static void main(String[] args)
			throws InterruptedException, IllegalStateException, IOException, CoapException {
		de.uniba.wiai.lspi.chord.service.PropertiesLoader.loadPropertyFile();

		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);

		// coap client initialisieren
		System.out.print("IP des Servers: ");
		String HOSTIP = in.next();
		CoapClient client = CoapClientBuilder.newBuilder(new InetSocketAddress(HOSTIP, 5683)).build();
		CoapPacket coapResp = client.resource("/led").payload("0", MediaTypes.CT_TEXT_PLAIN).sync().put();

		System.out.print("Game mode (demo or real): ");

		switch (in.next()) {
		case "real":
			mode = GameMode.REAL;
			numOfNpcs = 1;
			break;
		case "demo":
		default:
			mode = GameMode.DEMO;
			numOfNpcs = 10;
			break;
		}

		// im echten Spiel sind wir der einzige Npcs in unserer Liste
		List<Brain> npcs = new ArrayList<>();

		try {

			for (int i = 0; i < numOfNpcs; i++) {
				URL localURL = new URL("ocrmi://game:" + (port + i) + "/"); // Url kann beliebig sein
				System.out.println(util.getIp());
				URL bootstrapURL = new URL("ocrmi://" + util.getIp() + ":" + chordPort + "/"); // Diese Url wird vom
																								// Server vorgegeben

				if (mode == GameMode.REAL) {
					String bootstrapIp = in.next();
					bootstrapURL = new URL("ocrmi://" + bootstrapIp + ":" + chordPort + "/");
				}

				Chord chord = new ChordImpl();
				Brain b = new Brain(chord, i == 109, client);
				npcs.add(b);

				// NotifyCallback bekannt machen muss geschehen bevor der Join passiert
				chord.setCallback(b);
				chord.join(localURL, bootstrapURL);
				// System.out.println("Neuer Knoten unter ID: "+ chord.getID());
			}

			for (int j = 0; j < numOfNpcs; j++) {
				/*
				 * Hier schauen wir nach welche ID wir haben und für welche untere ID wir noch
				 * zuständig sind, das kann sich �ndern wenn noch jmd joint. Muss dann also
				 * nochmals ausgeführt werden.
				 */
				npcs.get(j).claimIds();
			}

		} catch (ServiceException e) {
			System.out.println("Could not join DHT !");
			System.out.println(e);
			System.exit(0);
		} catch (MalformedURLException | SocketException e) {
			System.out.println(e);
			System.exit(0);
		}

		if (mode == GameMode.REAL) {

			System.out.print("Start?: ");
			if (in.next().equals("yes")) {
				System.out.print("ID: " + npcs.get(0).chord.getID().toHexString());
				if (npcs.get(0).lowestID()) {
					System.out.println("We start!");

					try {
						npcs.get(0).chord.retrieve(util.getRandomId()); // Schuss auf zufälliges Ziel
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

			// close coap connection
			client.close();

		} else if (mode == GameMode.DEMO) {

			for (int k = demoWait; k > 0; k--) {
				System.out.print(k + " ");
				Thread.sleep(1000);
			}
			System.out.println("start demo");

			try {
				ID target = util.getRandomId();
				for (int i = 0; i < numOfNpcs; i++) {
					if (npcs.get(i).lowestID()) {
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
			client.close();
		}

	}

}
