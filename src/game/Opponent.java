package game;

import java.math.BigInteger;

import de.uniba.wiai.lspi.chord.data.ID;

public abstract class Opponent extends Player{
	
	public Opponent nextOponent;
	public Opponent prevOponent;
	
	public ID estimateBorder() {
		return ID.valueOf((this.prevOponent.id.toBigInteger()).add(BigInteger.valueOf(1)));
	}
	
	public ID estimateBestShot() {
		return null;
	}
	
	public float hitPercentage() {
		return Float.NaN;
	}
	
	

}
