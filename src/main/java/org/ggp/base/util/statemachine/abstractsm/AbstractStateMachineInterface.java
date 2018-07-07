package org.ggp.base.util.statemachine.abstractsm;

import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;

public interface AbstractStateMachineInterface {

	// Methods common to all state machines

    public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException;

    public void shutdown();

    public void doPerMoveWork();

    public String getName();

    public StateMachine getActualStateMachine();

}
