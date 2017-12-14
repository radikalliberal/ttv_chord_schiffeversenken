package game;

import java.math.BigInteger;

import de.uniba.wiai.lspi.chord.data.ID;

public class Opponent extends Player{
	
	public Opponent(ID id) {
		this.id = id;
	}
	
	public ID estimateBorder() {
		return ID.valueOf((this.prevOpponent.id.toBigInteger()).add(BigInteger.valueOf(1)));
	}
	
	public ID estimateBestShot() {
		return null;
	}
	
	public float hitPercentage() {
		return Float.NaN;
	}
	
	

}
