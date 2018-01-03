package game;

import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;


public class Game {

    final static int port = 40000;
	final static int chordPort = 4242;
	static int numOfNpcs = 10;
	final static int demoWait = 40;

	public static void main(String[] args) throws InterruptedException {
		de.uniba.wiai.lspi.chord.service.PropertiesLoader.loadPropertyFile();

		Scanner scanner = new Scanner(System.in);
		System.out.print("Game mode (demo or real): ");
		String gameMode = scanner.next();

		if(gameMode.equals("demo")){

		}
		if(gameMode.equals("real")){
			numOfNpcs = 1;
		}

		List<Brain> npcs = new ArrayList<>();

		try {

			for(int i = 0; i < numOfNpcs; i++) {
				URL localURL = new URL("ocrmi://game:" + (port+i) + "/") ; //Url kann beliebig sein
				URL bootstrapURL  = new URL ("ocrmi://" + util.getIp() + ":"+chordPort+"/"); // Diese Url wird vom Server vorgegeben

				if(gameMode.equals("real")){
					System.out.print("Enter bootstrap ip: ");
					String bootstrapIp = scanner.next();
					bootstrapURL  = new URL ("ocrmi://" + bootstrapIp + ":"+chordPort+"/"); // Diese Url wird vom Server vorgegeben
				}

				 Chord chord = new ChordImpl();
				 Brain b = new Brain(chord, i==109);
				 npcs.add(b);

				 // NotifyCallback bekannt machen muss geschehen bevor der Join passiert
				 chord.setCallback(b);
				 chord.join(localURL , bootstrapURL);
				 //System.out.println("Neuer Knoten unter ID: "+ chord.getID());
			}

			for(int j = 0; j < numOfNpcs; j++) {
				/* Hier schauen wir nach welche ID wir haben und für welche untere ID
				 * wir noch zuständig sind, das kann sich �ndern wenn noch jmd joint.
				 * Muss dann also nochmals ausgeführt werden.*/
				npcs.get(j).claimIds();
			}

		} catch (ServiceException e) {
			 System.out.println("Could not join DHT !");
			 System.out.println(e);
			 System.exit(0);
		} catch ( MalformedURLException | SocketException e ) {
			System.out.println(e);
			System.exit(0);
		}

		if(gameMode.equals("demo")){
			for (int k = demoWait; k > 0; k--) {
				System.out.print(k);
				Thread.sleep(1000);
			}
		}
		System.out.println("start demo");

		if(gameMode.equals("real")){
			System.out.print("Start?: ");
			String startCmd = scanner.next();
			if(startCmd.equals("yes")){
				//TODO start game
			}
		}

		try {
			ID target = util.getRandomId();
			for(int i = 0; i < numOfNpcs; i++) {
				if(npcs.get(i).lowestID()) {
					System.out.println(npcs.get(i).id + " fängt an!");
					System.out.println(npcs.get(i).id + ": Ich schiesse auf " + target);
					npcs.get(i).chord.retrieve(target); // Schuss auf zufälliges Ziel
					break;
				}
			}
		} catch (ServiceException e) {
			System.out.println(e);
			System.exit(0);
		}
	}

}
