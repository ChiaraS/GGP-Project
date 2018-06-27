package org.ggp.base.util.statemachine.abstractsm;

import java.util.List;

import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

import csironi.ggp.course.utils.MyPair;

public interface ExplicitAndCompactStateMachineInterface extends AbstractStateMachineInterface{

	// Methods common to all state machines, but with different types of inputs that extend general MachineState, Move and Role classes

	// EXPLICIT

    public List<Double> getAllGoalsForOneRole(ExplicitMachineState state, ExplicitRole role) throws StateMachineException;

    public boolean isTerminal(ExplicitMachineState state) throws StateMachineException;

    //COMPACT

    public List<Double> getAllGoalsForOneRole(CompactMachineState state, CompactRole role) throws StateMachineException;

    public boolean isTerminal(CompactMachineState state) throws StateMachineException;



    // Methods that return specific types of MachineStates, Moves and Roles

    // EXPLICIT

    public List<ExplicitRole> getExplicitRoles();

    public ExplicitMachineState getExplicitInitialState();

    public List<ExplicitMove> getExplicitLegalMoves(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException, StateMachineException;

    public ExplicitMachineState getExplicitNextState(ExplicitMachineState state, List<ExplicitMove> moves) throws TransitionDefinitionException, StateMachineException;

    // COMPACT

    public List<CompactRole> getCompactRoles();

    public CompactMachineState getCompactInitialState();

    public List<CompactMove> getCompactLegalMoves(CompactMachineState state, CompactRole role) throws MoveDefinitionException, StateMachineException;

    public CompactMachineState getCompactNextState(CompactMachineState state, List<CompactMove> moves) throws TransitionDefinitionException, StateMachineException;


    // Methods that translate states, moves and roles to the Explicit representation

    public ExplicitMachineState convertToExplicitMachineState(CompactMachineState state);

    public ExplicitMove convertToExplicitMove(CompactMove move);

    public ExplicitRole convertToExplicitRole(CompactRole role);

    public CompactMachineState convertToCompactMachineState(ExplicitMachineState state);

    public CompactMove convertToCompactMove(ExplicitMove move);

    public CompactRole convertToCompactRole(ExplicitRole role);

    // Methods that perform playout and playout choices using the reasoner underlying the state machine

	// EXPLICIT

	public MyPair<double[], Double> fastPlayouts(ExplicitMachineState state, int numSimulationsPerPlayout, int maxDepth) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException, StateMachineException;

	//public List<ExplicitMove> getJointMove(List<List<ExplicitMove>> legalMovesPerRole, ExplicitMachineState state)throws StateMachineException, MoveDefinitionException;

	public ExplicitMove getMoveForRole(List<ExplicitMove> legalMoves, ExplicitMachineState state, ExplicitRole role) throws StateMachineException, MoveDefinitionException;

	// COMPACT

	public MyPair<double[], Double> fastPlayouts(CompactMachineState state, int numSimulationsPerPlayout, int maxDepth) throws TransitionDefinitionException, MoveDefinitionException, StateMachineException, GoalDefinitionException;

	//public List<CompactMove> getJointMove(List<List<CompactMove>> legalMovesPerRole, CompactMachineState state) throws MoveDefinitionException;

	public CompactMove getMoveForRole(List<CompactMove> legalMoves, CompactMachineState state, CompactRole role) throws MoveDefinitionException;
}
