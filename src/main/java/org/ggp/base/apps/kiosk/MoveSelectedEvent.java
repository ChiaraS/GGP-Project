package org.ggp.base.apps.kiosk;

import org.ggp.base.util.observer.Event;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;

public class MoveSelectedEvent extends Event {
    private ExplicitMove theMove;
    private boolean isFinal = false;

    public MoveSelectedEvent(ExplicitMove m) {
        theMove = m;
    }

    public MoveSelectedEvent(ExplicitMove m, boolean isFinal) {
    	theMove = m;
    	this.isFinal = isFinal;
    }

    public ExplicitMove getMove() {
        return theMove;
    }

    public boolean isFinal() {
    	return isFinal;
    }
}