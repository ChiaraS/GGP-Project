package org.ggp.base.util.statemachine.abstractsm;

import java.util.List;
import java.util.Vector;

import org.ggp.base.util.Pair;
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

public interface FpgaStateMachineInterface extends AbstractStateMachineInterface {

	// Methods common to all state machines, but with different types of inputs that extend general MachineState, Move and Role classes

    public List<Integer> getAllGoalsForOneRole(FpgaMachineState state, FpgaRole role) throws StateMachineException;

    public boolean isTerminal(FpgaMachineState state) throws StateMachineException;

    // Methods that return specific types of MachineStates, Moves and Roles

    public List<FpgaRole> getFpgaRoles();

    public FpgaMachineState getFpgaInitialState();

    public List<FpgaMove> getFpgaLegalMoves(FpgaMachineState state, FpgaRole role) throws MoveDefinitionException, StateMachineException;

    public FpgaMachineState getFpgaNextState(FpgaMachineState state, List<FpgaMove> moves) throws TransitionDefinitionException, StateMachineException;

    public Vector<Pair<FpgaMachineState,Vector<FpgaMove>>> getAllFpgaJointMovesAndNextStates(FpgaMachineState state);


    public ExplicitMachineState convertToExplicitMachineState(FpgaMachineState state);

    public ExplicitMove convertToExplicitMove(FpgaMove move);

    public ExplicitRole convertToExplicitRole(FpgaRole role);

    // Methods that perform playout and playout choices using the reasoner underlying the state machine

	public MyPair<double[], Double> fastPlayouts(FpgaMachineState state, int numSimulationsPerPlayout, int maxDepth);

	public List<FpgaMove> getJointMove(List<List<FpgaMove>> legalMovesPerRole, FpgaMachineState state);

	public FpgaMove getMoveForRole(List<FpgaMove> legalMoves, FpgaMachineState state, FpgaRole role);

}
