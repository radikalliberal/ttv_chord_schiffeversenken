package game;

import java.math.BigInteger;
import java.util.ArrayList;

import de.uniba.wiai.lspi.chord.data.ID;

public class Opponent extends Player{
	
	public Opponent nextOpponent;
	public Opponent prevOpponent;

	public Opponent(ID id) {
		this.id = id;
		this.hits = new ArrayList<ID>();
		this.shots = new ArrayList<ID>();
	}
	
	public ID estimateBorder() {
		return ID.valueOf((this.prevOpponent.id.toBigInteger()).add(BigInteger.valueOf(1)));
	}
	
	public ID estimateBestShot() { // Das ist vielleicht eine methode die lieber in einer KI-Klasse  beheimatet sein sollte
		return null;
	}
	
	public float hitPercentage() {
		return Float.NaN;
	}
	
	void renewLinkedList(Opponent newOpponent) {
		_renewLinkedList(this, newOpponent);
	}

	public int getHitCount() {
		return this.hits.size();
	}

	public ID getGoodShotId() {
		this.update();

		BigInteger tmp = this.lowerBound.toBigInteger();
		for (int i = 0; i < 100; i++) {
			if()
		}

	}

	public void update () {
		this.lowerBound = this.prevOpponent.id;
		this.span = this.id.toBigInteger().subtract(BigInteger.valueOf(1)).subtract(this.estimateBorder().toBigInteger());
		this.fieldSize = this.span.divide(BigInteger.valueOf(100));
	}

	private void _renewLinkedList(Opponent o, Opponent newOpponent) {
		if(newOpponent.id.isInInterval(o.id, o.nextOpponent.id)) {
			Opponent nexto = o.nextOpponent;
			o.nextOpponent = newOpponent;
			newOpponent.nextOpponent = nexto;
			newOpponent.prevOpponent = o;
			nexto.prevOpponent = newOpponent;
		} else {
			if(o.equals(this.prevOpponent)) {
				return;
			} else {
				_renewLinkedList( o.nextOpponent, newOpponent);
			}
		}
	}
	
	String printOpponents() {
		return this._printOpponents(this, "");
	}
	
	private String _printOpponents(Opponent o, String unfinishedOp) {
		if(o.equals(this.prevOpponent)) return unfinishedOp;
		unfinishedOp += o.nextOpponent.id.toString() + "| Hits: " + o.nextOpponent.hits.size() + ", Misses: " +(o.nextOpponent.shots.size()-o.nextOpponent.hits.size()) + "\n";
		return _printOpponents(o.nextOpponent, unfinishedOp);
	}
	
	int printNumberOfOpponents() {
		return _printNumberOfOpponents(this, 1);
	}
	
	private int _printNumberOfOpponents(Opponent o, int i) {
		if(o.equals(this.prevOpponent)) return i;
		return _printNumberOfOpponents(o.nextOpponent, i+1);
	}
	
	boolean idKnown(ID new_id) {
		return _idKnown(this.nextOpponent, new_id);
	}
	
	private boolean _idKnown(Opponent o, ID new_id) {
		if(new_id.equals(o.id)) {
			return true;
		} 
		if(o.id == this.id) {
			return false;
		} else {
			return _idKnown(o.nextOpponent, new_id);
		}
	}

	Opponent getOpponent(ID source) {
		return _getOpponent(this, source);
	}
	
	private Opponent _getOpponent(Opponent o,ID source) {
		if(o.id.equals(source)) return o;
		if(o.equals(this.prevOpponent)) return null;
		else return _getOpponent(o.nextOpponent, source);
	}
}


