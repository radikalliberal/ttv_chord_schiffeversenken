package game;

import de.uniba.wiai.lspi.chord.data.ID;

import static game.IntervalField.fieldStatus.*;

public class IntervalField {

    enum fieldStatus {UNKNOWN, EMPTY, SHIP, HIT, MISS}

    // start and end are not part of the actual field
    public ID start;
    public ID end;
    fieldStatus status;
    boolean gotAlreadyShot;
    int hitCounter;

    public IntervalField(ID start, ID end) {
        this.start = start;
        this.end = end;
        this.status = UNKNOWN;
        this.hitCounter = 0;
    }

    public boolean hit() {
        this.hitCounter++;
        if(this.status == SHIP){
        	this.status = HIT;
            return true;
        } else {
        	this.status = MISS;
            return false;
        }
    }
}
