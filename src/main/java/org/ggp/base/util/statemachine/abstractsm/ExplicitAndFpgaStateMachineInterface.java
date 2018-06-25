package org.ggp.base.util.statemachine.abstractsm;

import java.util.List;

import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;
import org.ggp.base.util.statemachine.structure.fpga.FpgaMachineState;
import org.ggp.base.util.statemachine.structure.fpga.FpgaMove;
import org.ggp.base.util.statemachine.structure.fpga.FpgaRole;

import csironi.ggp.course.utils.MyPair;

public interface ExplicitAndFpgaStateMachineInterface extends AbstractStateMachineInterface{

	// Methods common to all state machines, but with different types of inputs that extend general MachineState, Move and Role classes

	// EXPLICIT

    public List<Double> getAllGoalsForOneRole(ExplicitMachineState state, ExplicitRole role) throws StateMachineException;

    public boolean isTerminal(ExplicitMachineState state) throws StateMachineException;

    //FPGA

    public List<Double> getAllGoalsForOneRole(FpgaMachineState state, FpgaRole role) throws StateMachineException;

    public boolean isTerminal(FpgaMachineState state) throws StateMachineException;



    // Methods that return specific types of MachineStates, Moves and Roles

    // EXPLICIT

    public List<ExplicitRole> getExplicitRoles();

    public ExplicitMachineState getExplicitInitialState();

    public List<ExplicitMove> getExplicitLegalMoves(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException, StateMachineException;

    public ExplicitMachineState getExplicitNextState(ExplicitMachineState state, List<ExplicitMove> moves) throws TransitionDefinitionException, StateMachineException;

    // FPGA

    public List<FpgaRole> getFpgaRoles();

    public FpgaMachineState getFpgaInitialState();

    public List<FpgaMove> getFpgaLegalMoves(FpgaMachineState state, FpgaRole role) throws MoveDefinitionException, StateMachineException;

    public FpgaMachineState getFpgaNextState(FpgaMachineState state, List<FpgaMove> moves) throws TransitionDefinitionException, StateMachineException;


    // Methods that translate states, moves and roles to the Explicit representation

    public ExplicitMachineState convertToExplicitMachineState(FpgaMachineState state);

    public ExplicitMove convertToExplicitMove(FpgaMove move);

    public ExplicitRole convertToExplicitRole(FpgaRole role);

    // Methods that perform playout and playout choices using the reasoner underlying the state machine

	// EXPLICIT

	public MyPair<double[], Double> fastPlayouts(ExplicitMachineState state, int numSimulationsPerPlayout, int maxDepth) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException, StateMachineException;

	//public List<ExplicitMove> getJointMove(List<List<ExplicitMove>> legalMovesPerRole, ExplicitMachineState state)throws StateMachineException, MoveDefinitionException;

	public ExplicitMove getMoveForRole(List<ExplicitMove> legalMoves, ExplicitMachineState state, ExplicitRole role) throws StateMachineException, MoveDefinitionException;

	// FPGA

	public MyPair<double[], Double> fastPlayouts(FpgaMachineState state, int numSimulationsPerPlayout, int maxDepth) throws TransitionDefinitionException, MoveDefinitionException, StateMachineException, GoalDefinitionException;

	//public List<FpgaMove> getJointMove(List<List<FpgaMove>> legalMovesPerRole, FpgaMachineState state) throws MoveDefinitionException;

	public FpgaMove getMoveForRole(List<FpgaMove> legalMoves, FpgaMachineState state, FpgaRole role) throws MoveDefinitionException;


}
