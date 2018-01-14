package game;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mbed.coap.client.CoapClient;
import com.mbed.coap.exception.CoapException;
import com.mbed.coap.packet.MediaTypes;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;

public class Brain extends Player implements NotifyCallback {

	public Chord chord;           // chord adapter
	public boolean debug;
	private boolean[] intervals;  // unser Interval, true = Schiff
	private int[] hist;           // Schuss Counter auf unsere Felder
	private Opponent us;          // Alle bekannten SPieler in einer Ringstruktur
	private int broadcastCounter;
	private List<ID> shotsTaken;  // unsere abgefeuerten Schüsse
	private boolean gameover;
	private CoapClient client;
	private int hits = 0;         // Counter für LED Anzeige

	Opponent rememberedOpponent = null;  // Bisher angegriffener Spieler

	enum fieldStatus {UNKONW, HIT, MISS} // mögliche Feldstatuse

	// simple Klasse zum Abbilden eines gegnerischen Intervalfeldes
	class IntervalField {
		ID start;
		ID end;
		fieldStatus status;

		public IntervalField(ID start, ID end) {
			this.start = start;
			this.end = end;
			this.status = fieldStatus.UNKONW;
		}
	}

	public Brain(Chord chordimpl, boolean debug, CoapClient client) throws CoapException {
		this.chord = chordimpl;
		this.debug = debug;
		this.broadcastCounter = 0;
		this.gameover = false;
		this.shotsTaken = new ArrayList<ID>();
		this.client = client;
		this.setLed(hits);
		this.hist = new int[100];
		for(int i = 0; i < 100; i++) {
			this.hist[i] = 0;
		}
	}
	
	public Brain(Chord chordimpl, boolean debug){
		this.chord = chordimpl;
		this.debug = debug;
		this.broadcastCounter = 0;
		this.gameover = false;
		this.shotsTaken = new ArrayList<ID>();
		this.client = null;
		this.hist = new int[100];
		for(int i = 0; i < 100; i++) {
			this.hist[i] = 0;
		}
	}

	// Liefert zufällige ID auserhalb unseres Intervalls zurück
	public ID getRandomId(){
		byte[] tmp = new byte[20];
		util.random_nums.nextBytes(tmp);
		ID target = new ID(tmp);

		ID upper = ID.valueOf(this.id.toBigInteger().add(BigInteger.ONE));
		ID lower = this.us.prevOpponent.id;

		while(target.isInInterval(lower, upper)){
			util.random_nums.nextBytes(tmp);
			target = new ID(tmp);
		}

		return target;
	}

	@Override
	public void retrieved(ID target) {
		// Mappe ID auf unser Feld
		int field = this.id2Field(target);
		this.hist[field] += 1;

		// Eruiere beste Ziel ID
		ID new_target = this.getBestKnownShot();

		// Allen anderen Spielern erzählen ob es ein Hit war
		if (this.intervals[field]) {
			this.intervals[field] = false;
			// if(!this.silent) System.out.println(this.id + ": retrieve für " +
			// target.toString() +" HIT!!!! ARGH!!!! (Field:" + field + ") noch " + (int)(9
			// - this.us.hits.size()) + " Schiffe");
			if(!this.debug) {
				System.out.println(this.us.printOpponents());
				for(int i = 0; i < 100; i++) System.out.print(this.hist[i] + ",");
				System.out.println();
			}
			this.chord.broadcast(target, true);
			try {
				this.setLed(++hits);
			} catch (CoapException e) {
				e.printStackTrace();
			}

		} else {
			//System.out.println(this.id + ": retrieve für " + target.toString() +" Kein Hit (Field:" + field + ") noch " + (int)(10 - this.us.hits.size()) + " Schiffe");			
			if(this.debug) System.out.println(this.us.printOpponents());
			this.chord.broadcast(target, false);
		}

		// wenn das Spiel noch nicht vorbei ist, feuer auf bestes Ziel
		if (!this.gameover) {
			this.shotsTaken.add(new_target);
			RetrieveThread myRunnable = new RetrieveThread(this, new_target);
			new Thread(myRunnable).start();
		}
	}

	// Liefert die angenommene mittlere ID des nächsten Intervallfeldes eines  Gegners, von dem wir noch nichts wissen (Status == unknown)
	public ID getBestKnownShot () {
		Opponent player = this.us.nextOpponent;
		int minShips = 10;
		Opponent target_player = player;

		// suche Spieler mit den meisten zerstörten Schiffen
		while(!player.id.equals(this.us.id)){
			if(player.shipsLeft() <= minShips){
				minShips = player.shipsLeft();
				target_player = player;
			}
			player = player.nextOpponent;
		}

		// erinnere Spieler, falls mehrere Spieler gleich viele zerstörte Schiffe haben, da wir bei diesem bereits viele Felder kennen
		if(rememberedOpponent == null){
			rememberedOpponent = target_player;
		}
		if(rememberedOpponent.shipsLeft() <= target_player.shipsLeft()){
			target_player = rememberedOpponent;
		} else {
			rememberedOpponent = target_player;
		}

		// schätze Intervallfelder anhand der bekannten ID des Spielers vor ihm
		IntervalField[] fields = new IntervalField[100];
		BigInteger lowerBound = target_player.lowerIntervalBorder();
		BigInteger upperBound = target_player.upperIntervalBorder();

		// prüfen ob über 0 gegangen wird
		BigInteger estimatedSpan = upperBound.compareTo(lowerBound) < 0 ? util.maxID().toBigInteger().subtract(lowerBound).add(upperBound) : upperBound.subtract(lowerBound);

		estimatedSpan.subtract(BigInteger.ONE); // lowerBound not part of interval

		BigInteger fieldSize = estimatedSpan.divide(BigInteger.valueOf(100));

		for (int i = 0; i < 100; i++) {
			// ids from fieldStart+1 and fieldEnd-1 are actual valid ids of field, but with borders not included function isInIntveral is easier to use
			BigInteger fieldStart = lowerBound.add(fieldSize.multiply(BigInteger.valueOf(i)));
			BigInteger fieldEnd = fieldStart.add(fieldSize).add(BigInteger.ONE);

			if(ID.valueOf(fieldEnd).compareTo(util.maxID()) > 0){
				fieldEnd = fieldEnd.subtract(util.maxID().toBigInteger());
			}

			// letztes Feld kann größer als fieldSize sein
			if(i >= 99){
				fieldEnd = upperBound;
			}

			fields[i] = new IntervalField(ID.valueOf(fieldStart), ID.valueOf(fieldEnd));
		}

		// map hits and misses to fields = known fields of opponent
		for (int i = 0; i < target_player.shots.size(); i++) {
			for (int j = 0; j < 100; j++) {
				if(target_player.shots.get(i).isInInterval(fields[j].start, fields[j].end)){
					fields[j].status = fieldStatus.MISS;
				}
			}
		}

		for (int k = 0; k < target_player.hits.size(); k++) {
			for (int l = 0; l < 100; l++) {
				if(target_player.hits.get(k).isInInterval(fields[l].start, fields[l].end)){
					fields[l].status = fieldStatus.HIT;
				}
			}
		}

		// finde nächtes unbekanntes feld
		int targetFieldNumber;
		for (targetFieldNumber = 0; targetFieldNumber < 100; targetFieldNumber++) {
			if(fields[targetFieldNumber].status == fieldStatus.UNKONW){
				break;
			}
		}

		// get estimated center of target field
		BigInteger tmp = fields[targetFieldNumber].start.toBigInteger().add(fieldSize.divide(BigInteger.valueOf(2)));
		if(ID.valueOf(tmp).compareTo(util.maxID()) > 0){
			tmp = tmp.subtract(util.maxID().toBigInteger());
		}

		// best target id = center of first unknown field of target_opponent
		return ID.valueOf(tmp);
	}

	@Override
	public void broadcast(ID source, ID target, Boolean hit) {
		// wenn broadcast unbekannter herkunft, füge neuen Spieler in unseren Ring ein
		if (!this.us.idKnown(source)) {
			//if(!this.silent) System.out.println("AHA ein neuer Knoten:" + source);
			this.us.renewLinkedList(new Opponent(source));
		}

		this.broadcastCounter++;

		Opponent o = this.us.getOpponent(source);
		if (o == null) {
			System.out.println(source + " ist unbekannt ? ? ? ? ");
			System.out.println(this.us.printOpponents());
		} else {
			// füge Schüsse zum Spieler hinzu
			o.shots.add(target);
			if (hit) {
				if(this.debug) System.out.println(this.id + ": Broadcast " + this.broadcastCounter + " für " + source);
				if (!o.hits.contains(target))
					o.hits.add(target);

				// falls Spieler 10 Treffer hat, ist das Spiel vorbei
				if (o.hits.size() == 10) {
					System.out.println(this.id + ": " + o.id + " ist GAME OVER | Empfangene Broadcasts: " + this.broadcastCounter);
					System.out.println(this.us.printOpponents());
					this.gameover = true;
					for(int i = 0; i < 100; i++) System.out.print(this.hist[i] + ",");
					System.out.println();

					// falls wir den letzten Treffer abgegeben haben, sind wir der Gewinner
					if (this.shotsTaken.contains(target)) {
						System.out.println(this.id + ": ICH HAB GEWONNEN!!!!!");
					}
				}
			}
		}

		if(this.debug) System.out.println(this.id + ": habe Broadcast ausgeführt für:\nSource: " + source.toString() + "\nTarget: " + target.toString() + "\nHit: " + hit.toString());
	}

	// IDs beziehen und Spieler Ring aufbauen
	public void claimIds() {
		this.id = chord.getID();
		// Untere Schranke = ID(VorherigerKnoten)+1
		this.lowerBound = ID.valueOf((this.chord.getPredecessorID().toBigInteger()).add(BigInteger.valueOf(1)));
		System.out.println(this.id + ": Ich habe Predecessor " + this.chord.getPredecessorID());
		this.intervals = this.putShips();
		this.us = new Opponent(this.id);
		this.us.prevOpponent = new Opponent(this.chord.getPredecessorID());
		this.us.nextOpponent = this.us.prevOpponent;
		this.us.prevOpponent.prevOpponent = this.us;
		this.us.prevOpponent.nextOpponent = this.us;
	}

	// ermittelt Intervallfeld aus ID in inserem Intervall
	int id2Field(ID id) {
		if (!id.isInInterval(this.us.prevOpponent.id,
				ID.valueOf((this.id.toBigInteger()).add(BigInteger.valueOf(1))))) {
			System.out.println(id.toString() + " not in Range!!!!!");
			System.out.println((this.id.toBigInteger()).add(BigInteger.valueOf(1)));
			return 0;
		} else {
			BigInteger spread;
			if (this.lowerBound.compareTo(this.id) > 0) {
				spread = this.lowerBound.toBigInteger().multiply(BigInteger.valueOf(-1)).add(
						new ID(util.hexStringToByteArray("ffffffffffffffffffffffffffffffffffffffff")).toBigInteger())
						.add(this.id.toBigInteger());

			} else {
				spread = this.id.toBigInteger().subtract(this.lowerBound.toBigInteger());
			}

			BigInteger rest = spread.mod(BigInteger.valueOf(100));
			BigInteger fieldsize = spread.subtract(rest).divide(BigInteger.valueOf(100));

			if (this.lowerBound.compareTo(this.id) > 0) {
				if (id.compareTo(this.id) > 0) { // Sonderfall die ID ist hinter der 0 -> andere Rechnung
					return id.toBigInteger().subtract(this.lowerBound.toBigInteger()).divide(fieldsize).intValue();
				} else {
					return id.toBigInteger()
							.add(this.lowerBound.toBigInteger().multiply(BigInteger.valueOf(-1))
									.add(new ID(util.hexStringToByteArray("ffffffffffffffffffffffffffffffffffffffff"))
											.toBigInteger()))
							.divide(fieldsize).intValue();
				}
			} else {
				return id.toBigInteger().subtract(this.lowerBound.toBigInteger()).divide(fieldsize).intValue();
			}
		}
	}

	// platziere Schiffe nach dem Zufall
	private boolean[] putShips() {
		boolean[] b = new boolean[200];
		for (int i = 0; i < 100; i++) {
			b[i] = false;
		}
		Random rand = new Random();
		for (int i = 0; i < 10; i++) {
			float num = rand.nextFloat() * 100;
			if (b[(int) num]) {
				i--;
			} else {
				b[(int) num] = true;
			}
		}
		return b;
	}

	/*
	private ID field2ID(int field, Opponent o) {
		
		//hier aufpassen, wenn o der erste Knoten ist. 
		if(o.prevOpponent.id.compareTo(o.id)))
		o.lowerBound.toBigInteger()
		
		
		id = ID.valueOf(BigInteger())
		return id;
		
	}*/

	// coap resource led
	private void setLed(int hits) throws CoapException {
		
		if(this.client == null) return;
		
		switch(hits){
		case 0: 
			client.resource("/led").payload("0", MediaTypes.CT_TEXT_PLAIN).sync().put();
			client.resource("/led").payload("g", MediaTypes.CT_TEXT_PLAIN).sync().put();
		break;
		case 9:
			client.resource("/led").payload("0", MediaTypes.CT_TEXT_PLAIN).sync().put();
			client.resource("/led").payload("b", MediaTypes.CT_TEXT_PLAIN).sync().put();
		break;
		case 5:
			client.resource("/led").payload("0", MediaTypes.CT_TEXT_PLAIN).sync().put();
			client.resource("/led").payload("b", MediaTypes.CT_TEXT_PLAIN).sync().put();
			client.resource("/led").payload("g", MediaTypes.CT_TEXT_PLAIN).sync().put();
			
		case 10: 
			client.resource("/led").payload("0", MediaTypes.CT_TEXT_PLAIN).sync().put();
			client.resource("/led").payload("r", MediaTypes.CT_TEXT_PLAIN).sync().put();
		break;
		default: break;
		}
	}

	// Besitzen wir die größte ID?
	public boolean lowestID() {
		ID highestId = new ID(util.hexStringToByteArray("fffffffffffffffffffffffffffffffffffffffe"));
		return highestId.isInInterval(this.lowerBound, this.id);
	}

}
