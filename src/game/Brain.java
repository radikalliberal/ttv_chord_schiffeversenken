package game;

import java.math.BigInteger;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;

public class Brain implements NotifyCallback{
	public ID id;
	private ID lowerBound;
	public Chord chord;
	
	public Brain(Chord chordimpl) {
		this.chord = chordimpl;
	}

	@Override
	public void retrieved(ID target) {
		// TODO Auto-generated method stub
		System.out.println(this.id + ":Es gab ein retrieve für " + target.toString());
		//System.out.println("Es gab ein retrieve für " + target.toString());
		//this.broadcast(this.id, target, hit);
	}

	@Override
	public void broadcast(ID source, ID target, Boolean hit) {
		// TODO Auto-generated method stub
		System.out.println(this.id + " hat Broadcast ausgeführt für:\nSource: " + source.toString() + "\nTarget: " + target.toString() + "\n Hit: " + hit.toString());
	}
	
	public void claimIds() {
		this.id = chord.getID();
		//Untere Schranke = ID(VorherigerKnoten)+1
		this.lowerBound = ID.valueOf((this.chord.getPredecessorID().toBigInteger()).add(BigInteger.valueOf(1))); 
		
	}
	
	public boolean lowestID() {
		ID highestId = new ID(util.hexStringToByteArray("fffffffffffffffffffffffffffffffffffffffe"));
		return highestId.isInInterval(this.lowerBound, this.id);
	}
	
}
