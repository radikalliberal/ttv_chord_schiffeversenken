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

	public Chord chord;
	public boolean debug;
	private boolean[] intervals; // our interval with 10 ships
	private int[] hist; // hits on our interval
	private Opponent us; // ring of players
	private int broadcastCounter;
	private List<ID> shotsTaken;
	private boolean gameover;
	private CoapClient client;
	private int hits = 0;

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

	public ID getBestShot() {
		ID target = ;

		Opponent enemy = this.us.prevOpponent;
		int mostHits = -1;
		Opponent targetEnemy = this.us.prevOpponent;

		while(!enemy.equals(this.us)){
			if(enemy.getHitCount() > mostHits){
				targetEnemy = enemy;
				mostHits = enemy.getHitCount();
			}
			enemy = enemy.prev();
		}

		return targetEnemy.getGoodShotId();
	}

	@Override
	public void retrieved(ID target) {
		// TODO Auto-generated method stub

		int field = this.id2Field(target);
		this.hist[field] += 1;
		//ID new_target = util.getRandomId();
		ID new_target = this.getBestShot();

		//System.out.println(this.id + ": Es gab ein retrieve für " + target.toString());
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			//System.out.println(this.id + ": retrieve für " + target.toString() +" Kein Hit (Field:" + field + ") noch " + (int)(10 - this.us.hits.size()) + " Schiffe");			
			if(this.debug) System.out.println(this.us.printOpponents());
			this.chord.broadcast(target, false);
		}

		if (!this.gameover) {
			this.shotsTaken.add(new_target);
			RetrieveThread myRunnable = new RetrieveThread(this, new_target);
			new Thread(myRunnable).start();				// Schuss auf neues Ziel unserer Wahl -> jetzt gerade zufällig
			// if(!this.silent) System.out.println(this.id + ": Schuss auf " +
			// new_target.toString());
		}
	}

	@Override
	public void broadcast(ID source, ID target, Boolean hit) {
		if (!this.us.idKnown(source)) {
			//if(!this.silent) System.out.println("AHA ein neuer Knoten:" + source);
			this.us.renewLinkedList(new Opponent(source));
		}
		if ((this.broadcastCounter % 10) == 0) {
			// if(!this.silent) System.out.println(this.broadcastCounter + " OpponentList: "
			// + this.us.printOpponents());
			// if(!this.silent) System.out.println(this.broadcastCounter + " broadcasts: " +
			// this.us.printNumberOfOpponents());
		}

		this.broadcastCounter++;

		Opponent o = this.us.getOpponent(source);
		if (o == null) {
			System.out.println(source + " ist unbekannt ? ? ? ? ");
			System.out.println(this.us.printOpponents());
		} else {
			o.shots.add(target);
			if (hit) {
				if(this.debug) System.out.println(this.id + ": Broadcast " + this.broadcastCounter + " für " + source);
				if (!o.hits.contains(target))
					o.hits.add(target);
				if (o.hits.size() == 10) {
					System.out.println(this.id + ": " + o.id + " ist GAME OVER | Empfangene Broadcasts: " + this.broadcastCounter);
					System.out.println(this.us.printOpponents());
					this.gameover = true;
					for(int i = 0; i < 100; i++) System.out.print(this.hist[i] + ",");
					System.out.println();
					
					if (this.shotsTaken.contains(target)) {
						System.out.println(this.id + ": ICH HAB GEWONNEN!!!!!");
					}
				}
			}
		}

		if(this.debug) System.out.println(this.id + ": habe Broadcast ausgeführt für:\nSource: " + source.toString() + "\nTarget: " + target.toString() + "\nHit: " + hit.toString());
	}

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
		// String s = "";
		// for(int i = 0; i < 100; i++) {
		// s += "," + Boolean.toString(b[i]) ;
		// }
		// System.out.println(this.id + " ships : " +s);
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

	public boolean lowestID() {
		return ID.valueOf(Game.maxID).isInInterval(this.lowerBound, this.id);
	}

}
