package game;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import de.uniba.wiai.lspi.chord.data.ID;

public abstract class Player {
	public ID id;
	public ID lowerBound;

	public List<ID> hits;
	public List<ID> shots;

	public BigInteger span;
	public BigInteger fieldSize;

	public IntervalField[] fields;

	public Player nextPlayer;
	public Player previousPlayer;

	public Player(ID id, ID lowerBound, Player nextPlayer, Player previousPlayer) {
		this.id = id;
		this.nextPlayer = nextPlayer;
		this.previousPlayer = previousPlayer;

		this.hits = new ArrayList<>();
		this.shots = new ArrayList<>();

		this.lowerBound = ID.valueOf((this.previousPlayer.id.toBigInteger()).add(BigInteger.valueOf(1)));
		this.fields = new IntervalField[Game.numberOfFields];
	}

	public void update () {
		this.lowerBound = ID.valueOf((this.previousPlayer.id.toBigInteger()).add(BigInteger.valueOf(1)));

		if(this.id.compareTo(this.lowerBound) < 0){
			this.span = Game.maxID.subtract(this.lowerBound.toBigInteger()).add(this.id.toBigInteger().subtract(BigInteger.valueOf(1)));
		}else{
			this.span = this.id.toBigInteger().subtract(BigInteger.valueOf(1)).subtract(this.lowerBound.toBigInteger());
		}

		this.fieldSize = this.span.divide(BigInteger.valueOf(100));

		for (int i = 0; i < Game.numberOfFields; i++) {
			IntervalField newField = new IntervalField();

			BigInteger newStart = this.fieldSize.multiply(BigInteger.valueOf((long) i)).add(this.lowerBound.toBigInteger());
			BigInteger newEnd = newStart.add(this.fieldSize.multiply(BigInteger.valueOf((long) i+1)));

			if(newEnd.compareTo(Game.maxID) > 0){
				newEnd = newEnd.subtract(Game.maxID);
			}

			// set last field to id
			if(i >= 99){
				newEnd = this.id.toBigInteger().subtract(BigInteger.valueOf(1));
			}

			newField.start = ID.valueOf(newStart);
			newField.end = ID.valueOf(newEnd);
			newField.hasShip = false;
			newField.gotAlreadyShot = false;

			this.fields[i] = newField;
		}
	}
}

