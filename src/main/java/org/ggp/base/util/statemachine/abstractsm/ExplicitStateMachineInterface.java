package org.ggp.base.util.statemachine.abstractsm;

import java.util.List;

import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

import csironi.ggp.course.utils.MyPair;

public interface ExplicitStateMachineInterface extends AbstractStateMachineInterface{

	// Methods common to all state machines, but with different types of inputs that extend general MachineState, Move and Role classes

    public List<Double> getAllGoalsForOneRole(ExplicitMachineState state, ExplicitRole role) throws StateMachineException;

    public boolean isTerminal(ExplicitMachineState state) throws StateMachineException;

    // Methods that return specific types of MachineStates, Moves and Roles

    public List<ExplicitRole> getExplicitRoles();

    public ExplicitMachineState getExplicitInitialState();

    public List<ExplicitMove> getExplicitLegalMoves(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException, StateMachineException;

    public ExplicitMachineState getExplicitNextState(ExplicitMachineState state, List<ExplicitMove> moves) throws TransitionDefinitionException, StateMachineException;

    // Methods that perform playout and playout choices using the reasoner underlying the state machine

    public MyPair<double[], Double> fastPlayouts(ExplicitMachineState state, int numSimulationsPerPlayout, int maxDepth) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException, StateMachineException;

    //public List<ExplicitMove> getJointMove(List<List<ExplicitMove>> legalMovesPerRole, ExplicitMachineState state) throws StateMachineException, MoveDefinitionException;

	public ExplicitMove getMoveForRole(List<ExplicitMove> legalMoves, ExplicitMachineState state, ExplicitRole role) throws StateMachineException, MoveDefinitionException;

}
