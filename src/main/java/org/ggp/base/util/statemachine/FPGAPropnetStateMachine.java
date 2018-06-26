package org.ggp.base.util.statemachine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.ggp.base.util.Pair;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.placeholders.FPGAPropnetLibrary;
import org.ggp.base.util.placeholders.FpgaInternalMove;
import org.ggp.base.util.placeholders.FpgaInternalState;
import org.ggp.base.util.statemachine.abstractsm.ExplicitAndFpgaStateMachineInterface;
import org.ggp.base.util.statemachine.abstractsm.FpgaStateMachineInterface;
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
import org.ggp.base.util.statemachine.structure.fpga.FpgaMachineState;
import org.ggp.base.util.statemachine.structure.fpga.FpgaMove;
import org.ggp.base.util.statemachine.structure.fpga.FpgaRole;

import com.google.common.collect.ImmutableList;

public class FPGAPropnetStateMachine extends StateMachine implements FpgaStateMachineInterface, ExplicitAndFpgaStateMachineInterface{

	/**
	 * Once the functions of the FPGA library are called on a state we memorize the returned output in the
	 * following variables. This is because this class has multiple functions that only return part of such
	 * output and they are normally called in sequence on the same state. It would be a waste to call the
	 * library every time one of such methods is called, and then throw away part of the output.
	 */
	private FpgaInternalState lastVisitedState;

	/**
	 * Tells whether lastVisitedState is terminal.
	 */
	private boolean terminal;

	/**
	 * Gives the goals for each role in the lastVisitedState.
	 * Note that this is set to null whenever the lastVisitedState is non-terminal.
	 */
	private List<Double> goals;



	private FPGAPropnetLibrary fpgaPropnetInterface;

    /** The player roles */
    protected List<CompactRole> roles;
    /** The initial state */
    protected FpgaMachineState initialState;


	public FPGAPropnetStateMachine(Random random, FPGAPropnetLibrary fpgaPropnetInterface){
		super(random);
		this.fpgaPropnetInterface = fpgaPropnetInterface;
	}

    /**
     * Initializes the PropNetStateMachine. You should compute the topological
     * ordering here. Additionally you may compute the initial state here, at
     * your discretion.
     *
     * @throws StateMachineInitializationException
     */
    @Override
    public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException {

    	if(this.fpgaPropnetInterface == null){
    		GamerLogger.log("StateMachine", "[FPGAPropnet] State machine initialized with FPGAPropnetInterface set to null. Impossible to reason on the game!");
    		throw new StateMachineInitializationException("Null parameter passed during initialization of the state machine: cannot reason on the game with null FPGAPropnetInterface.");
    	}else{

    		int numRoles = this.fpgaPropnetInterface.fucntion1().getLegalMovesPerRole().length;

    		this.roles = new ArrayList<CompactRole>(numRoles);
    		for(int i = 0; i < numRoles; i++){
    			this.roles.add(new CompactRole(i));
    		}
    		this.initialState = new CompactMachineState(this.fpgaPropnetInterface.function2().clone());
    	}
    }

	/**
	 * Computes if the state is terminal.
	 * Since the state is not an FpgaMachineState, it is first transformed
	 * into one.
	 *
	 * NOTE that this method has been added only for compatibility with other state
	 * machines, however its performance will be much slower than the corresponding
	 * method for the FpgaMachineState since the state will always have
	 * to be translated first into an FpgaMachineState.
	 *
	 * @state a machine state.
	 * @return true if the state is terminal, false otherwise.
	 */
	@Override
	public boolean isTerminal(ExplicitMachineState state) {
		return this.isTerminal(this.convertToFpgaMachineState(state));
	}

	/**
	 * Computes if the state is terminal. Assume that if function1 returns no legal moves for any of the players
	 * than the state is terminal.
	 *
	 * @state a machine state.
	 * @return true if the state is terminal, false otherwise.
	 */
	@Override
	public boolean isTerminal(FpgaMachineState state) {
		if(!state.getStateRepresentation().equals(this.lastVisitedState)) {
			this.getStateInfo(state.getStateRepresentation());
		}

		return this.terminal;
	}

	@Override
	public List<Double> getAllGoalsForOneRole(ExplicitMachineState state, ExplicitRole role) {
		return this.getAllGoalsForOneRole(this.convertToFpgaMachineState(state), this.convertToFpgaRole(role));
	}

	@Override
	public List<Integer> getAllGoalsForOneRole(FpgaMachineState state, FpgaRole role) {
		// This state machine cannot get goals

		GamerLogger.logError("StateMachine", "[FPGAPropnet] State machine initialized with FPGAPropnetInterface set to null. Impossible to reason on the game!");
		throw new StateMachineInitializationException("Null parameter passed during initialization of the state machine: cannot reason on the game with null FPGAPropnetInterface.");
	}

	@Override
	public ExplicitMachineState getExplicitInitialState() {
		return this.convertToExplicitMachineState(this.initialState);
	}

	@Override
	public FpgaMachineState getFpgaInitialState() {
		return this.initialState;
	}

	@Override
	public List<ExplicitMove> getExplicitLegalMoves(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException, StateMachineException {
		List<ExplicitMove> moves = new ArrayList<ExplicitMove>();
		CompactRole externalRole = this.convertToCompactRole(role);
		for(CompactMove m : this.getCompactLegalMoves(this.convertToCompactMachineState(state), externalRole)){
			moves.add(this.convertToExplicitMove(m));
		}
		return moves;
	}

	@Override
	public List<CompactMove> getCompactLegalMoves(CompactMachineState state, CompactRole role)
			throws MoveDefinitionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExplicitMachineState getExplicitNextState(ExplicitMachineState state, List<ExplicitMove> moves)
			throws TransitionDefinitionException, StateMachineException {
		return this.convertToExplicitMachineState(this.getCompactNextState(this.convertToCompactMachineState(state), this.movesToInternalMoves(moves)));
	}

	@Override
	public FpgaMachineState getFpgaNextState(FpgaMachineState state, List<FpgaMove> moves) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ExplicitRole> getExplicitRoles() {
		List<ExplicitRole> roles = new ArrayList<ExplicitRole>();
		//ExplicitRole[] rolesArray = this.fpgaPropnetInterface.getRoles();
		//for(CompactRole r : this.roles){
		//	roles.add(rolesArray[r.getIndex()]);
		//}  TODO
		return ImmutableList.copyOf(roles);
	}

	@Override
	public List<CompactRole> getCompactRoles() {
		return this.roles;
	}

	/*** Methods to convert propnet states/moves/roles to GDL states/moves/roles and vice versa ***/

	/**
	 * For the FPGA propnet we cannot convert states, so we just return null.
	 * We also log an error to signal that this method has been called.
	 */
	@Override
	public ExplicitMachineState convertToExplicitMachineState(FpgaMachineState state) {
		GamerLogger.logError("StateMachine", "[FPGAPropnet] Impossible to convert FpgaMachineState to ExplicitMachineState.");
		return new ExplicitMachineState(new HashSet<GdlSentence>());
	}

	@Override
	public ExplicitMove convertToExplicitMove(FpgaMove move) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExplicitRole convertToExplicitRole(CompactRole role) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompactMachineState convertToCompactMachineState(ExplicitMachineState state) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FpgaMove convertToFpgaMove(ExplicitMove move) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompactRole convertToCompactRole(ExplicitRole role) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CompactMove> movesToInternalMoves(List<ExplicitMove> moves) {
		// TODO  Auto-generated method stub
		return null;
	}

	private void getStateInfo(FpgaInternalState state) {
		List<Pair<FpgaInternalState,List<FpgaInternalMove>>> stateInfo = this.fpgaPropnetInterface.getNextStates(state);
		this.terminal = stateInfo == null; // TODO: check! If there are no next states will this be null? Empty?
	}

	private void getStateGoals(FpgaInternalState state) {
		List<Pair<FpgaInternalState,List<FpgaInternalMove>>> stateInfo = this.fpgaPropnetInterface.getNextStates(state);
		this.terminal = stateInfo == null; // TODO: check! If there are no next states will this be null? Empty?
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}


}
