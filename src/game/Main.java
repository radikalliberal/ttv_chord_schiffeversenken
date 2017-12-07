package game;

import java.net.MalformedURLException;

import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import game.Callbacks;

public class Main {

	public static void main(String[] args) {
		de.uniba.wiai.lspi.chord.service.PropertiesLoader.loadPropertyFile();
		String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
		URL localURL = null;
		try {
			localURL = new URL(protocol + "://localhost:8181/") ;
		} catch (MalformedURLException e) {
			throw new RuntimeException ( e ) ;
		}
		 URL bootstrapURL = null;
		 try {
			 bootstrapURL = new URL ( protocol + "://localhost:8080/") ;
		 } catch ( MalformedURLException e ) {
			 throw new RuntimeException ( e ) ;
		 }
		 Chord chord = new ChordImpl();
		 Callbacks cb = new Callbacks();
		 try {
			 // NotifyCallback bekannt machen
			 chord.setCallback(cb);
			 chord.join(localURL , bootstrapURL);
		 } catch (ServiceException e) {
			 throw new RuntimeException ("Could not join DHT !", e);
		 }

	}

}
