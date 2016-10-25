package org.ggp.base.apps.kiosk;

import org.ggp.base.util.observer.Event;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;

public class MoveSelectedEvent extends Event {
    private ProverMove theMove;
    private boolean isFinal = false;

    public MoveSelectedEvent(ProverMove m) {
        theMove = m;
    }

    public MoveSelectedEvent(ProverMove m, boolean isFinal) {
    	theMove = m;
    	this.isFinal = isFinal;
    }

    public ProverMove getMove() {
        return theMove;
    }

    public boolean isFinal() {
    	return isFinal;
    }
}