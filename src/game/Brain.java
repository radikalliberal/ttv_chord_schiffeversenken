package game;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;

public class Brain implements NotifyCallback {
	
	private ID id;
	private Chord chord;
	
	public Brain(Chord chordimpl) {
		this.chord = chordimpl;
	}
	
	

	@Override
	public void retrieved(ID target) {
		// TODO: Kram machen wenn retrieve ausgeführt wurde
		System.out.println("Es gab ein retrieve für " + target.toString());
	}

	@Override
	public void broadcast(ID source, ID target, Boolean hit) {
		// TODO: Kram machen wenn Broadcast ausgeführt wurde
		System.out.println(this.id + " hat Broadcast ausgeführt für:\nSource: " + source.toString() + "\nTarget: " + target.toString() + "\n Hit: " + hit.toString());
		
	}

	public void setId(ID node_id) {
		this.id = node_id;
		// TODO Auto-generated method stub
		
	}
	
	public ID getId() {
		if(this.id != null) { 
			return this.id;
		} else {
			throw new NullPointerException();
		}
	}



}
