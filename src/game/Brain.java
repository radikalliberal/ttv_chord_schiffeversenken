package game;

import java.math.BigInteger;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;

public class Brain extends Player implements NotifyCallback{
	public Chord chord;
	public boolean silent;
	
	public Brain(Chord chordimpl, boolean silent) {
		this.chord = chordimpl;
		this.silent = silent;
	}

	@Override
	public void retrieved(ID target) {
		// TODO Auto-generated method stub
		if(!this.silent) System.out.println(this.id + ": Es gab ein retrieve für " + target.toString());
		//System.out.println("Es gab ein retrieve für " + target.toString());
		this.chord.broadcast(target, true);
	}

	@Override
	public void broadcast(ID source, ID target, Boolean hit) {
		// TODO Auto-generated method stub
		if(!this.silent) System.out.println(this.id + ": habe Broadcast ausgeführt für:\nSource: " + source.toString() + "\nTarget: " + target.toString() + "\n Hit: " + hit.toString());
	}
	
	public void claimIds() {
		this.id = chord.getID();
		//Untere Schranke = ID(VorherigerKnoten)+1
		this.lowerBound = ID.valueOf((this.chord.getPredecessorID().toBigInteger()).add(BigInteger.valueOf(1))); 
		if(!this.silent) {
			System.out.println(this.id + ": Ich habe Predecessor " + this.chord.getPredecessorID());
		}
		
	}
	
	public boolean lowestID() {
		ID highestId = new ID(util.hexStringToByteArray("fffffffffffffffffffffffffffffffffffffffe"));
		return highestId.isInInterval(this.lowerBound, this.id);
	}
	
}
