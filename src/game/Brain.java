package game;

import java.math.BigInteger;
import java.util.Random;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;

public class Brain extends Player implements NotifyCallback{
	public Chord chord;
	public boolean silent;
	private boolean[] intervals;
	private Opponent us;
	
	public Brain(Chord chordimpl, boolean silent) {
		this.chord = chordimpl;
		this.silent = silent;
	}

	@Override
	public void retrieved(ID target) {
		// TODO Auto-generated method stub
		if(!this.silent) System.out.println(this.id + ": Es gab ein retrieve für " + target.toString());
		
		//System.out.println("Es gab ein retrieve für " + target.toString());
		this.chord.broadcast(target, this.checkhit(target));
		
		
	}

	private Boolean checkhit(ID target) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void broadcast(ID source, ID target, Boolean hit) {
		// TODO Auto-generated method stub
		if(!this.idKnown(source)) {
			this.renewLinkedList((Opponent) this.nextOpponent, new Opponent(source));
		}
		
		// Todo: Hit/Miss registrieren für Opponent
		
		
		
		if(!this.silent) System.out.println(this.id + ": habe Broadcast ausgeführt für:\nSource: " + source.toString() + "\nTarget: " + target.toString() + "\n Hit: " + hit.toString());
	}
	
	public void claimIds() {
		this.id = chord.getID();
		//Untere Schranke = ID(VorherigerKnoten)+1
		this.lowerBound = ID.valueOf((this.chord.getPredecessorID().toBigInteger()).add(BigInteger.valueOf(1))); 
		if(!this.silent) {
			System.out.println(this.id + ": Ich habe Predecessor " + this.chord.getPredecessorID());
		}
		this.intervals = this.putShips();
		this.prevOpponent = new Opponent(this.chord.getPredecessorID());
		this.us = new Opponent(this.id);
		this.nextOpponent = this.us;
		
	}
	
	private boolean[] putShips() {
		boolean[] b = new boolean[200];
		for(int i = 0; i < 100; i++) {
			b[i] = false;
		}
		Random rand = new Random(); 
		for(int i = 0; i < 10; i++) {
			float num = rand.nextFloat()*100;
			if(b[(int) num]) {
				i--;
			} else {
				b[(int) num] = true;
			}
		}
		return b;
	}

	private void renewLinkedList(Opponent o, Opponent newOpponent) {
		if(newOpponent.id.isInInterval(o.id, o.nextOpponent.id)) {
			Opponent nexto = o.nextOpponent;
			o.nextOpponent = newOpponent;
			newOpponent.nextOpponent = nexto;
			nexto.prevOpponent = newOpponent;
		} else {
			if(o.equals(us)) {
				return;
			} else {
				renewLinkedList( o.nextOpponent, newOpponent);
			}
		}
	}
	
	private boolean idKnown(ID new_id) {
		return _idKnown(this.nextOpponent, new_id);
	}
	
	boolean _idKnown(Opponent o, ID new_id) {
		if(new_id.equals(o.id)) {
			return true;
		} 
		if(o.id == this.id) {
			return false;
		} else {
			return _idKnown((Opponent) o.nextOpponent, new_id);
		}
	}
	
	public boolean lowestID() {
		ID highestId = new ID(util.hexStringToByteArray("fffffffffffffffffffffffffffffffffffffffe"));
		return highestId.isInInterval(this.lowerBound, this.id);
	}
	
}
