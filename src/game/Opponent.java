package game;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import de.uniba.wiai.lspi.chord.data.ID;

import static game.IntervalField.fieldStatus.*;

public class Opponent extends Player {

	public List<ID> hits;
	public List<ID> shots;

	public Opponent(ID id) {
		super(id);

		this.hits = new ArrayList<>();
		this.shots = new ArrayList<>();
	}

	@Override
	public void update() {
		super.update();
	 	//map hits and shots to fields
		for (int j = 0; j < Game.numberOfFields; j++) {
			for (int k = 0; k < this.shots.size(); k++) {
				if (this.shots.get(k).isInInterval(this.fields[j].start, this.fields[j].end)){
					this.fields[j].status = MISS;
				}
			}
			for (int l = 0; l < this.hits.size(); l++) {
				if (this.hits.get(l).isInInterval(this.fields[j].start, this.fields[j].end)){
					this.fields[j].status = HIT;
				}
			}

		}
	}

	public ID getBestShot(){
		this.update();

		// gehe von 0 bis 100 die Felder durch und wähle das nächste Feld mit Status unbekannt.
		int i = 0;
		for (i = 0; i < Game.numberOfFields; i++) {
			if(this.fields[i].status == UNKNOWN){
				break;
			}
		}

//		System.out.println("ertes unbekanntes feld bei: "+ i);
//		System.out.println(this.fields[i].start.toString());
//		System.out.println(this.fields[i].end.toString());
//		System.out.println("fieldsize");
//		System.out.println(this.fieldSize.toString(16));
		BigInteger tmp = this.fields[i].start.toBigInteger().add(this.fieldSize.divide(BigInteger.valueOf(2)));
		if(ID.valueOf(tmp).compareTo(util.maxID()) > 0){
			tmp = tmp.subtract(util.maxID().toBigInteger());
		}

		return ID.valueOf(tmp);
	}

	public int shipsLeft () {
		this.update();

		return Game.numberOfShips - this.hits.size();
	}

//	public ID estimateBorder() {
//		return ID.valueOf((this.prevOpponent.id.toBigInteger()).add(BigInteger.valueOf(1)));
//	}
	
//	public ID estimateBestShot() { // Das ist vielleicht eine methode die lieber in einer KI-Klasse  beheimatet sein sollte
//		return null;
//	}
	
//	public float hitPercentage() {
//		return Float.NaN;
//	}
	


//	public int getHitCount() {
//		return this.hits.size();
//	}
//
//	public ID getGoodShotId() {
//		this.update();
//
//		BigInteger tmp = this.lowerBound.toBigInteger();
//		for (int i = 0; i < 100; i++) {
//			if()
//		}
//
//	}

//	public void update () {
//		this.lowerBound = this.prevOpponent.id;
//		this.span = this.id.toBigInteger().subtract(BigInteger.valueOf(1)).subtract(this.estimateBorder().toBigInteger());
//		this.fieldSize = this.span.divide(BigInteger.valueOf(100));
//	}

//	private void _renewLinkedList(Opponent o, Opponent newOpponent) {
//		if(newOpponent.id.isInInterval(o.id, o.nextOpponent.id)) {
//			Opponent nexto = o.nextOpponent;
//			o.nextOpponent = newOpponent;
//			newOpponent.nextOpponent = nexto;
//			newOpponent.prevOpponent = o;
//			nexto.prevOpponent = newOpponent;
//		} else {
//			if(o.equals(this.prevOpponent)) {
//				return;
//			} else {
//				_renewLinkedList( o.nextOpponent, newOpponent);
//			}
//		}
//	}
	
//	String printOpponents() {
//		return this._printOpponents(this, "");
//	}
	
//	private String _printOpponents(Opponent o, String unfinishedOp) {
//		if(o.equals(this.prevOpponent)) return unfinishedOp;
//		unfinishedOp += o.nextOpponent.id.toString() + "| Hits: " + o.nextOpponent.hits.size() + ", Misses: " +(o.nextOpponent.shots.size()-o.nextOpponent.hits.size()) + "\n";
//		return _printOpponents(o.nextOpponent, unfinishedOp);
//	}
//
//	int printNumberOfOpponents() {
//		return _printNumberOfOpponents(this, 1);
//	}
	
//	private int _printNumberOfOpponents(Opponent o, int i) {
//		if(o.equals(this.prevOpponent)) return i;
//		return _printNumberOfOpponents(o.nextOpponent, i+1);
//	}
	
//	boolean idKnown(ID new_id) {
//		return _idKnown(this.nextOpponent, new_id);
//	}
//
//	private boolean _idKnown(Opponent o, ID new_id) {
//		if(new_id.equals(o.id)) {
//			return true;
//		}
//		if(o.id == this.id) {
//			return false;
//		} else {
//			return _idKnown(o.nextOpponent, new_id);
//		}
//	}

//	Opponent getOpponent(ID source) {
//		return _getOpponent(this, source);
//	}
//
//	private Opponent _getOpponent(Opponent o,ID source) {
//		if(o.id.equals(source)) return o;
//		if(o.equals(this.prevOpponent)) return null;
//		else return _getOpponent(o.nextOpponent, source);
//	}
}


