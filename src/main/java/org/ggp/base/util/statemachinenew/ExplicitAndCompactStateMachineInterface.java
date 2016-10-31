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

public interface ExplicitAndCompactStateMachineInterface {

    public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException;

    public List<Integer> getAllGoalsForOneRole(ExplicitMachineState state, ExplicitRole role) throws StateMachineException;

    public boolean isTerminal(ExplicitMachineState state) throws StateMachineException;

    public List<ExplicitRole> getRoles();

    public ExplicitMachineState getInitialState();

    public List<ExplicitMove> getLegalMoves(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException, StateMachineException;

    public ExplicitMachineState getNextState(ExplicitMachineState state, List<ExplicitMove> moves) throws TransitionDefinitionException, StateMachineException;

    public void shutdown();

    public List<Integer> getAllGoalsForOneRole(CompactMachineState state, CompactRole role) throws StateMachineException;

    public boolean isTerminal(CompactMachineState state) throws StateMachineException;

    public List<CompactMove> getLegalMoves(CompactMachineState state, CompactRole role) throws MoveDefinitionException, StateMachineException;

    public CompactMachineState getNextState(CompactMachineState state, List<CompactMove> moves) throws TransitionDefinitionException, StateMachineException;

    public ExplicitMachineState convertToExplicitMachineState(CompactMachineState state);

    public ExplicitMove convertToExplicitMove(CompactMove move);

    public ExplicitRole convertToExplicitRole(CompactRole role);

}
