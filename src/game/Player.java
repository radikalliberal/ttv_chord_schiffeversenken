package game;

import java.util.List;

import de.uniba.wiai.lspi.chord.data.ID;

public abstract class Player {

	public ID id; // Spieler ID
	public ID lowerBound; // Spieler untere Grenze zum nächsten Spieler
	public List<ID> hits; // Treffer auf diesen Spieler
	public List<ID> shots; // Misses auf diesen Spieler

}
