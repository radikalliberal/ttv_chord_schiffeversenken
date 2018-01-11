package game;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.ServiceException;

public class RetrieveThread implements Runnable{
		   
	   private Brain brain;
	   private ID target;
	
	   public RetrieveThread(Brain b, ID t){
	      this.brain = b;
	      this.target = t;
	   }
	
	   public void run(){
	      try {
			this.brain.chord.retrieve(this.target);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   }
}


