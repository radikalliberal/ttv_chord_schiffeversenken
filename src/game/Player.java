package game;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;

import de.uniba.wiai.lspi.chord.data.ID;

public abstract class Player {
	public ID id;

	public BigInteger span;
	public BigInteger fieldSize;

	public IntervalField[] fields;

	public Player nextPlayer;
	public Player previousPlayer;

	public Player(ID id) {
		this.id = id;
		this.fields = new IntervalField[Game.numberOfFields];
		nextPlayer = this;
		previousPlayer = this;
	}

	public BigInteger getLowerBound() {
		return this.previousPlayer.id.toBigInteger();
	}

	public BigInteger getUpperBound(){
		BigInteger upperBound = this.id.toBigInteger().add(BigInteger.ONE);
		if (ID.valueOf(upperBound).compareTo(util.maxID()) > 0){
			upperBound = BigInteger.ZERO;
		}

		return upperBound;
	}

	// update span and field bounds
	public void update(){
		BigInteger lowerBound = this.getLowerBound();
		BigInteger upperBound = this.getUpperBound();

		// prüfen ob über 0 gegangen wird
		if(upperBound.compareTo(lowerBound) < 0){
			// falls ja, ziehe maxId ab
			this.span = util.maxAdress().toBigInteger().subtract(lowerBound).add(upperBound);
		}else{
			// falls nein, berechne Bereich durch Subtrahieren der der Untergrenze von der Obergrenze
			this.span = upperBound.subtract(lowerBound);
		}

		this.span.subtract(BigInteger.ONE); // lowerBound not part of interval

		this.fieldSize = this.span.divide(BigInteger.valueOf(Game.numberOfFields));

		for (int i = 0; i < Game.numberOfFields; i++) {
			// ids from start+1 and end-1 are actual valid ids of field
			BigInteger fieldStart = lowerBound.add(this.fieldSize.multiply(BigInteger.valueOf(i)));
			BigInteger fieldEnd = fieldStart.add(this.fieldSize).add(BigInteger.ONE);

			if(ID.valueOf(fieldEnd).compareTo(util.maxAdress()) > 0){
				fieldEnd = fieldEnd.subtract(util.maxAdress().toBigInteger());
			}

			if(i >= 99){
				fieldEnd = upperBound;
			}

			this.fields[i] = new IntervalField(ID.valueOf(fieldStart), ID.valueOf(fieldEnd));
		}
	}

	void addOpponentToRing(Player opponent) {
		addPlayerToRing(this, opponent);
	}

	private void addPlayerToRing(Player p1, Player p2) {
		if(p2.id.isInInterval(p1.id, p1.nextPlayer.id)) {
			Player nextPlayer = p1.nextPlayer;
			p1.nextPlayer = p2;
			p2.nextPlayer = nextPlayer;
			p2.previousPlayer = p1;
			nextPlayer.previousPlayer = p2;
		} else {
			if(p1.equals(this.previousPlayer)) {
				return;
			} else {
				addPlayerToRing( p1.nextPlayer, p2);
			}
		}
	}

	boolean idKnown(ID new_id) {
		return _idKnown(this.nextPlayer, new_id);
	}

	private boolean _idKnown(Player o, ID new_id) {
		if(new_id.equals(o.id)) {
			return true;
		}
		if(o.id == this.id) {
			return false;
		} else {
			return _idKnown(o.nextPlayer, new_id);
		}
	}

	Player getOpponent(ID source) {
		return _getOpponent(this, source);
	}

	private Player _getOpponent(Player o,ID source) {
		if(o.id.equals(source)) return o;
		if(o.equals(this.previousPlayer)) return null;
		else return _getOpponent(o.nextPlayer, source);
	}

	public String ringString(){
		StringBuilder s = new StringBuilder();
		ArrayList<ID> knownIds = new ArrayList<>();
		knownIds.add(this.id);

		Player next = this.nextPlayer;
		while(!next.id.equals(this.id)){
			knownIds.add(next.id);
			next = next.nextPlayer;
		}

		knownIds.sort(new Comparator<ID>() {
			@Override
			public int compare(ID o1, ID o2) {
				return o1.compareTo(o2);
			}
		});

		for(ID id : knownIds){
			String pre = "";
			if(id.equals(this.id)){
				pre = "->";
			}
			if(id.equals(this.nextPlayer.id)){
				pre = "next";
			}
			if(id.equals(this.previousPlayer.id)){
				pre = "prev";
			}
			if(id.equals(this.nextPlayer.id) && id.equals(this.previousPlayer.id)){
				pre = "P/N";
			}

			s.append(String.format("%5s %s \n", pre, id.toString()).toString());
		}

		return s.toString();
	}
}

