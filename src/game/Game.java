package game;

import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;


public class Game {

	public static void main(String[] args) throws InterruptedException {
		de.uniba.wiai.lspi.chord.service.PropertiesLoader.loadPropertyFile();
		URL localURL = null;
		Integer port = 40000;
		Random random_nums = new Random();
		List<Brain> npcs = new ArrayList<Brain>();
		for(int i = 0; i < 10; i++) {
			try {
				//System.out.print(protocol.toString());
				localURL = new URL("ocrmi://game:"+ ((Integer)(port+i)).toString() + "/") ; //Url kann beliebig sein
			} catch (MalformedURLException e) {
				throw new RuntimeException ( e ) ;
			}
			 URL bootstrapURL = null;
			 try {
				 bootstrapURL = new URL ("ocrmi://" + util.getIp() + ":4242/"); // Diese Url wird vom Server vorgegeben
			 } catch ( MalformedURLException | SocketException e ) {
				 throw new RuntimeException ( e ) ;
			 }
			 Chord chord = new ChordImpl();
			 Brain b = new Brain(chord, i==109);
			 npcs.add(b);
			 try {
				 // NotifyCallback bekannt machen muss geschehen bevor der Join passiert
				 chord.setCallback(b);
				 chord.join(localURL , bootstrapURL);
				 //System.out.println("Neuer Knoten unter ID: "+ chord.getID());
			 } catch (ServiceException e) {
				 throw new RuntimeException ("Could not join DHT !", e);
			 }
		}
		for(int i = 0; i < npcs.size(); i++) {
			/* Hier schauen wir nach welche ID wir haben und für welche untere ID 
			 * wir noch zuständig sind, das kann sich �ndern wenn noch jmd joint. 
			 * Muss dann also nochmals ausgeführt werden.*/
			npcs.get(i).claimIds(); 
		}
		
		Thread.sleep(40000); // Warten bis fixfingers durch ist
		
		byte[] bla = new byte[20];
		random_nums.nextBytes(bla); // erstellt zufällige Adresse
		ID target = new ID(bla); //new ID(util.hexStringToByteArray("1111111111111111111111111111111111111111")); 
		for(int i = 0; i < npcs.size(); i++) {
			if(npcs.get(i).lowestID()) {
				System.out.println(npcs.get(i).id + " fängt an!");
				try {
					System.out.println(npcs.get(i).id + ": Ich schiesse auf " + target);
					npcs.get(i).chord.retrieve(new ID(bla)); // Schuss auf zufälliges Ziel
				} catch (ServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
			
		}
	}
}
