package game;

import java.util.List;

import de.uniba.wiai.lspi.chord.data.ID;

public abstract class Player {
	
	public Player nextOpponent;
	public Player prevOpponent;

	public ID id;
	public ID lowerBound;
	public List<ID> hits;
	public List<ID> shot;
	
	
}
