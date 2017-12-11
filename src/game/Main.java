package game;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;


public class Main {

	public static void main(String[] args) throws InterruptedException {
		de.uniba.wiai.lspi.chord.service.PropertiesLoader.loadPropertyFile();
		String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
		URL localURL = null;
		Integer port = 4000;
		Random random_nums = new Random();
		List<Brain> npcs = new ArrayList<Brain>();
		for(int i = 0; i < 20; i++) {
			try {
				System.out.print(protocol.toString());
				localURL = new URL("ocrmi://mynode:"+ ((Integer)(port+i)).toString() + "/") ; //Url kann beliebig sein
			} catch (MalformedURLException e) {
				throw new RuntimeException ( e ) ;
			}
			 URL bootstrapURL = null;
			 try {
				 bootstrapURL = new URL ("ocrmi://192.168.2.102:4242/"); // Diese Url wird vom Server vorgegeben
			 } catch ( MalformedURLException e ) {
				 throw new RuntimeException ( e ) ;
			 }
			 Chord chord = new ChordImpl();
			 Brain b = new Brain(chord);
			 npcs.add(b);
			 try {
				 // NotifyCallback bekannt machen muss geschehen bevor der Join passiert
				 chord.setCallback(b);
				 chord.join(localURL , bootstrapURL);
				 System.out.println("Neuer Knoten unter ID: "+ chord.getID());
			 } catch (ServiceException e) {
				 throw new RuntimeException ("Could not join DHT !", e);
			 }
		}
		for(int i = 0; i < npcs.size(); i++) {
			/* Hier schauen wir nach welche ID wir haben und für welche untere ID 
			 * wir noch zuständig sind, das kann sich ändern wenn noch jmd joint. 
			 * Muss dann also nochmals ausgeführt werden.*/
			npcs.get(i).claimIds(); 
		}
		byte[] bla = new byte[20];
		random_nums.nextBytes(bla); // erstellt zufällige Adresse
		//chord.broadcast(new ID(bla), true);
		ID target = new ID(bla);
		try {
			System.out.println(npcs.get(0).id + ":Ich schiesse auf " + target);
			npcs.get(0).chord.retrieve(target); // 1. NPC macht Schuss auf ein zufälliges Opfer
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
