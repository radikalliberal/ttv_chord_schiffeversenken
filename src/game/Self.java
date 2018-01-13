package game;

import de.uniba.wiai.lspi.chord.data.ID;

public class Self extends Player {
    public Self(ID id) {
        super(id, lowerBound, nextPlayer, previousPlayer);
    }
}
