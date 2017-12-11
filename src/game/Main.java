package game;

import java.net.MalformedURLException;
import java.util.Random;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

import game.Brain;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		de.uniba.wiai.lspi.chord.service.PropertiesLoader.loadPropertyFile();
		String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
		URL localURL = null;
		Integer port = 4000;
		Random random_nums = new Random();
		for(int i = 0; i < 20; i++) {
			try {
				System.out.print(protocol.toString());
				localURL = new URL("ocrmi://192.168.2.106:"+ ((Integer)(port+i)).toString() + "/") ;
			} catch (MalformedURLException e) {
				throw new RuntimeException ( e ) ;
			}
			 URL bootstrapURL = null;
			 try {
				 bootstrapURL = new URL ("ocrmi://192.168.2.102:4242/") ;
			 } catch ( MalformedURLException e ) {
				 throw new RuntimeException ( e ) ;
			 }
			 Chord chord = new ChordImpl();
			 Brain ai = new Brain(chord);
			 
			 try {
				 // NotifyCallback bekannt machen
				 chord.setCallback(ai);
				 chord.join(localURL , bootstrapURL);
				 System.out.println("Meine ID: "+ chord.getID());
				 ai.setId(chord.getID());
				 if(i > 12) {
					 byte[] bla = new byte[20];
					 random_nums.nextBytes(bla); // erstellt zufï¿½llige Adresse
					 //chord.broadcast(new ID(bla), true);
					 chord.retrieve(new ID(bla)); //Schuss auf ein zufälliges Opfer
				 }
			 } catch (ServiceException e) {
				 throw new RuntimeException ("Could not join DHT !", e);
			 }
			 Thread.sleep(500);
		}

	}

}
