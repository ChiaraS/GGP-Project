package org.ggp.base.util.statemachinenew;

import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

public interface ExplicitStateMachineInterface {

    public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException;

    public List<Integer> getAllGoalsForOneRole(ExplicitMachineState state, ExplicitRole role) throws StateMachineException;

    public boolean isTerminal(ExplicitMachineState state) throws StateMachineException;

    public List<ExplicitRole> getRoles();

    public ExplicitMachineState getInitialState();

    public List<ExplicitMove> getLegalMoves(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException, StateMachineException;

    public ExplicitMachineState getNextState(ExplicitMachineState state, List<ExplicitMove> moves) throws TransitionDefinitionException, StateMachineException;

    public void shutdown();

}
