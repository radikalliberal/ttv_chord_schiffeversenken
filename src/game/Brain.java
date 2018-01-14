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
import de.uniba.wiai.lspi.chord.service.ServiceException;
import game.IntervalField.fieldStatus;

import static game.IntervalField.fieldStatus.EMPTY;
import static game.IntervalField.fieldStatus.SHIP;

public class Brain extends Player implements NotifyCallback {


	public Chord chord;
	private CoapClient client = null;
	public boolean debug;

	private List<ID> shotsFired;
	private boolean gameover;

//	private boolean[] intervals; // our interval with 10 ships
	private int[] hist; // hits on our interval
//	private Opponent us; // ring of players
//	private int broadcastCounter;

	private int hits = 0;

	public Brain(Chord chordimpl, boolean debug, CoapClient client) throws CoapException {
		super(null);

		this.client = client;
		this.chord = chordimpl;
		this.debug = debug;
		this.shotsFired = new ArrayList<>();
		this.gameover = false;

//		this.broadcastCounter = 0;
		this.setLed(hits);
		this.hist = new int[100];
		for(int i = 0; i < 100; i++) {
			this.hist[i] = 0;
		}
	}
	
	public Brain(Chord chordimpl, boolean debug){
		super(chordimpl.getID());

		this.chord = chordimpl;
		this.debug = debug;
		this.gameover = false;
		this.shotsFired = new ArrayList<>();
		this.hist = new int[100];
		for(int i = 0; i < 100; i++) {
			this.hist[i] = 0;
		}


	}

	public void start (){
        this.claimIds();
		this.update();
		this.placeShips();

		this.printInfo();
    }

    public void printInfo(){
		StringBuilder s = new StringBuilder();
		s.append("=========================================================\n");
		s.append(this.ringString()+ "\n");
		s.append("---------------------------------------------------------\n");
		s.append(this.shipString() + "\n");
		s.append("=========================================================\n");

		System.out.printf("%s", s.toString());
	}

    public String shipString(){
		String s = "";
		for (int i = 0; i < this.fields.length; i++) {
			if(this.fields[i].status == SHIP){
				s += i+" ";
			}
		}
        return "Ships: [ " + s + "]";
	}

	public void placeShips(){
		for (int i = 0; i < Game.numberOfFields; i++) {
			this.fields[i].status = EMPTY;
		}

		Random rand = new Random();
		for (int i = 0; i < Game.numberOfShips; i++) {
			float num = rand.nextFloat() * Game.numberOfFields;
			if (this.fields[(int) num].status == SHIP) {
				i--;
			} else {
				this.fields[(int) num].status = SHIP;
			}
		}
	}


    public void startShot() {
        try {
        	ID target = this.getBestShot();
            this.chord.retrieve(target);
			System.out.println("Startshuss von " + this.id + " auf -> " + target.toString());
		} catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    public ID getBestShot() {
		Player nextPlayer = this.nextPlayer;
		int minShips = Game.numberOfShips;
		Player targetPlayer = nextPlayer;

		while(nextPlayer instanceof Opponent){
			int shipsLeft = ((Opponent) nextPlayer).shipsLeft();
			if(shipsLeft <= minShips){
				minShips = shipsLeft;
				targetPlayer = nextPlayer;
			}
			nextPlayer = nextPlayer.nextPlayer;
		}

		return ((Opponent)targetPlayer).getBestShot();
	}

	public int shipsLeft () {
		int cnt = 0;
		for (int i = 0; i < Game.numberOfFields; i++) {
			if(this.fields[i].status == SHIP){
				cnt++;
			}
		}
		return cnt;
	}

	@Override
	public void retrieved(ID target) {
		try {

			int number = shot2field(target);
			StringBuilder s = new StringBuilder();
			s.append("ANGESCHOSSEN ==================================\n");
			s.append("MY ID: ");
			s.append(this.id.toString());
			s.append("\n");
			s.append("GETROFFENES FELD: " + number +"\n");
			s.append("FELD LOWER: " + this.fields[number].start.toString() +"\n");
			s.append("SHOT ID: " + target.toString() +"\n");
			s.append("FELD UPPER: " + this.fields[number].end.toString() +"\n");
			s.append("SCHIFFE ÜBRIG: " + this.shipsLeft() +"\n");
			s.append("SCHIFFE: " + this.shipString() +"\n");
			s.append("-----------------------------------------------");
			System.out.println(s.toString());

			this.hist[this.shot2field(target)]++;
			this.chord.broadcast(target, this.hit(target));



		} catch (WrongHitId wrongHitId) {
			wrongHitId.printStackTrace();
		}

		try {
			this.setLed(++hits);
		} catch (CoapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ID new_target = this.getBestShot();
		if (!this.gameover) {
			this.shotsFired.add(new_target);
			RetrieveThread myRunnable = new RetrieveThread(this, new_target);
			new Thread(myRunnable).start();
		}

////		int field = this.id2Field(target);
////		this.hist[field] += 1;
//		//ID new_target = util.getRandomId();
//		ID new_target = this.getBestShot();
//
//		//System.out.println(this.id + ": Es gab ein retrieve für " + target.toString());
//		// Allen anderen Spielern erzählen ob es ein Hit war
//
//		if (this.intervals[field]) {
//			this.intervals[field] = false;
//			// if(!this.silent) System.out.println(this.id + ": retrieve für " +
//			// target.toString() +" HIT!!!! ARGH!!!! (Field:" + field + ") noch " + (int)(9
//			// - this.us.hits.size()) + " Schiffe");
//			if(!this.debug) {
//				System.out.println(this.us.printOpponents());
//				for(int i = 0; i < 100; i++) System.out.print(this.hist[i] + ",");
//				System.out.println();
//			}
//			this.chord.broadcast(target, true);
//			try {
//				this.setLed(++hits);
//			} catch (CoapException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		} else {
//			//System.out.println(this.id + ": retrieve für " + target.toString() +" Kein Hit (Field:" + field + ") noch " + (int)(10 - this.us.hits.size()) + " Schiffe");
//			if(this.debug) System.out.println(this.us.printOpponents());
//			this.chord.broadcast(target, false);
//		}
//
//		if (!this.gameover) {
//			this.shotsFired.add(new_target);
//			RetrieveThread myRunnable = new RetrieveThread(this, new_target);
//			new Thread(myRunnable).start();				// Schuss auf neues Ziel unserer Wahl -> jetzt gerade zufällig
//			// if(!this.silent) System.out.println(this.id + ": Schuss auf " +
//			// new_target.toString());
//		}
	}

	@Override
	public void broadcast(ID source, ID target, Boolean hit) {

		if (!this.idKnown(source)) {
			this.addOpponentToRing(new Opponent(source));
//			this.printInfo();
		}

//		this.broadcastCounter++;

		Player victim = this.getOpponent(source);

		if (victim == null) {
			System.out.println(source + " ist unbekannt ? ? ? ? ");
		} else if(victim instanceof Opponent){
			((Opponent)victim).shots.add(target);
			try {
				victim.fields[this.shot2field(target)].hit();
			} catch (WrongHitId e) {
				//e.printStackTrace();
			}
			if(hit){
//				if(this.debug) System.out.println(this.id + ": Broadcast " + this.broadcastCounter + " für " + source);
				if (!((Opponent)victim).hits.contains(target))
					((Opponent)victim).hits.add(target);
				if (((Opponent)victim).shipsLeft() == 0) {
					System.out.println(this.id + ": " + victim.id + " ist GAME OVER | Empfangene Broadcasts: ");
					//System.out.println(this.ringString());
					this.gameover = true;
					if(this.debug) {
						String s = "";
						for(int i = 0; i < 99; i++) s += this.hist[i] + ",";	
						System.out.println(s + this.hist[99]);
					}
					
					
					if (this.shotsFired.contains(target)) {
						System.out.println(this.id + ": ICH HAB GEWONNEN!!!!!");
					}
				}
			}
		}

//		if(this.debug) System.out.println(this.id + ": habe Broadcast ausgeführt für:\nSource: " + source.toString() + "\nTarget: " + target.toString() + "\nHit: " + hit.toString());
	}

	public void claimIds() {
		this.id = chord.getID();
		// Untere Schranke = ID(VorherigerKnoten)+1
//		this.lowerBound = ID.valueOf((this.chord.getPredecessorID().toBigInteger()).add(BigInteger.valueOf(1)));
//		System.out.println(this.id + ": Ich habe Predecessor " + this.chord.getPredecessorID());
//		this.intervals = this.putShips();
//		this.us = new Opponent(this.id);
		this.previousPlayer = new Opponent(this.chord.getPredecessorID());
		this.nextPlayer = this.previousPlayer;
		this.nextPlayer.previousPlayer = this;
		this.previousPlayer.nextPlayer = this;
	}

//	int id2Field(ID id) {
//		if (!id.isInInterval(this.us.prevOpponent.id,
//				ID.valueOf((this.id.toBigInteger()).add(BigInteger.valueOf(1))))) {
//			System.out.println(id.toString() + " not in Range!!!!!");
//			System.out.println((this.id.toBigInteger()).add(BigInteger.valueOf(1)));
//			return 0;
//		} else {
//			BigInteger spread;
//			if (this.lowerBound.compareTo(this.id) > 0) {
//				spread = this.lowerBound.toBigInteger().multiply(BigInteger.valueOf(-1)).add(
//						new ID(util.hexStringToByteArray("ffffffffffffffffffffffffffffffffffffffff")).toBigInteger())
//						.add(this.id.toBigInteger());
//
//			} else {
//				spread = this.id.toBigInteger().subtract(this.lowerBound.toBigInteger());
//			}
//
//			BigInteger rest = spread.mod(BigInteger.valueOf(100));
//			BigInteger fieldsize = spread.subtract(rest).divide(BigInteger.valueOf(100));
//
//			if (this.lowerBound.compareTo(this.id) > 0) {
//				if (id.compareTo(this.id) > 0) { // Sonderfall die ID ist hinter der 0 -> andere Rechnung
//					return id.toBigInteger().subtract(this.lowerBound.toBigInteger()).divide(fieldsize).intValue();
//				} else {
//					return id.toBigInteger()
//							.add(this.lowerBound.toBigInteger().multiply(BigInteger.valueOf(-1))
//							.add(new ID(util.hexStringToByteArray("ffffffffffffffffffffffffffffffffffffffff"))
//							.toBigInteger()))
//							.divide(fieldsize).intValue();
//				}
//			} else {
//				return id.toBigInteger().subtract(this.lowerBound.toBigInteger()).divide(fieldsize).intValue();
//			}
//		}
//	}
//
//	private boolean[] putShips() {
//		boolean[] b = new boolean[200];
//		for (int i = 0; i < 100; i++) {
//			b[i] = false;
//		}
//		Random rand = new Random();
//		for (int i = 0; i < 10; i++) {
//			float num = rand.nextFloat() * 100;
//			if (b[(int) num]) {
//				i--;
//			} else {
//				b[(int) num] = true;
//			}
//		}
//		// String s = "";
//		// for(int i = 0; i < 100; i++) {
//		// s += "," + Boolean.toString(b[i]) ;
//		// }
//		// System.out.println(this.id + " ships : " +s);
//		return b;
//	}
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
		return util.maxID().isInInterval(ID.valueOf(this.getLowerBound()), this.id);
	}

}
