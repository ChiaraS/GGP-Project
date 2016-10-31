package org.ggp.base.util.statemachinenew;

import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

public interface CompactStateMachineInterface {

    public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException;

    public List<Integer> getAllGoalsForOneRole(CompactMachineState state, CompactRole role) throws StateMachineException;

    public boolean isTerminal(CompactMachineState state) throws StateMachineException;

    public List<CompactRole> getRoles();

    public CompactMachineState getInitialState();

    public List<CompactMove> getLegalMoves(CompactMachineState state, CompactRole role) throws MoveDefinitionException, StateMachineException;

    public CompactMachineState getNextState(CompactMachineState state, List<CompactMove> moves) throws TransitionDefinitionException, StateMachineException;

    public void shutdown();

    public ExplicitMachineState convertToExplicitMachineState(CompactMachineState state);

    public ExplicitMove convertToExplicitMove(CompactMove move);

    public ExplicitRole convertToExplicitRole(CompactRole role);

}
