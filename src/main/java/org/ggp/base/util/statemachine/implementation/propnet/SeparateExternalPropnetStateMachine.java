package org.ggp.base.util.statemachine.implementation.propnet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.lucene.util.OpenBitSet;
import org.ggp.base.util.concurrency.ConcurrencyUtils;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutablePropNet;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.components.ImmutableProposition;
import org.ggp.base.util.propnet.state.ExternalPropnetState;
import org.ggp.base.util.statemachine.ExternalPropnetMachineState;
import org.ggp.base.util.statemachine.ExternalPropnetMove;
import org.ggp.base.util.statemachine.ExternalPropnetRole;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.query.ProverQueryBuilder;

import com.google.common.collect.ImmutableList;

public class SeparateExternalPropnetStateMachine extends StateMachine {

	/** The underlying proposition network  */
    private ImmutablePropNet propNet;

    /** The state of the proposition network */
    private ExternalPropnetState propnetState;

    /** The player roles */
    private ExternalPropnetRole[] roles;
    /** The initial state */
    private ExternalPropnetMachineState initialState;

	public SeparateExternalPropnetStateMachine(ImmutablePropNet propNet, ExternalPropnetState propnetState){
		this.propNet = propNet;
		this.propnetState = propnetState;
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
    	if(this.propNet != null && this.propnetState != null){
    		this.roles = new ExternalPropnetRole[this.propNet.getRoles().length];
    		for(int i = 0; i < this.roles.length; i++){
    			this.roles[i] = new ExternalPropnetRole(i);
    		}
    		this.initialState = new ExternalPropnetMachineState(this.propnetState.getCurrentState().clone());
    	}else{
    		GamerLogger.log("StateMachine", "[ExternalPropnet] State machine initialized with at least one among the propnet structure and the propnet state set to null. Impossible to reason on the game!");
    		throw new StateMachineInitializationException("Null parameter passed during instantiaton of the state mahcine: cannot reason on the game with null propnet or null propnet state.");
    	}
    }

	/**
	 * Computes if the state is terminal. Should return the value of the terminal
	 * proposition for the state.
	 * Since the state is not an ExternalPropnetMachineState, it is first transformed
	 * into one.
	 *
	 * NOTE that this method has been added only for compatibility with other state
	 * machines, however its performance will be much slower than the corresponding
	 * method for the ExternalPropnetMachineState since the state will always have
	 * to be translated first into an ExternalPropnetMachineState.
	 *
	 * @state a machine state.
	 * @return true if the state is terminal, false otherwise.
	 */
	@Override
	public boolean isTerminal(MachineState state) {
		return this.isTerminal(this.stateToExternalState(state));
	}

	/**
	 * Computes if the state is terminal. Should return the value
	 * of the terminal proposition for the state.
	 *
	 * @state a machine state.
	 * @return true if the state is terminal, false otherwise.
	 */
	public boolean isTerminal(ExternalPropnetMachineState state) {
		this.markBases(state);
		return this.propnetState.isTerminal();
	}

	/**
	 * Computes the goal for a role in the given state.
	 * Since the state is not an ExternalPropnetMachineState,
	 * it is first transformed into one.
	 *
	 * Should return the value of the goal proposition that
	 * is true for that role. If there is not exactly one goal
	 * proposition true for that role, then you should throw a
	 * GoalDefinitionException because the goal is ill-defined.
	 *
	 * NOTE that this method has been added only for compatibility with other state
	 * machines, however its performance will be much slower than the corresponding
	 * method for the ExternalPropnetMachineState since the state will always have
	 * to be translated first into an ExternalPropnetMachineState.
	 *
	 * @param state
	 * @param role
	 */
	@Override
	public int getGoal(MachineState state, Role role) throws GoalDefinitionException {
		return this.getGoal(this.stateToExternalState(state), this.roleToExternalRole(role));
	}

	/**
	 * Computes the goal for a role in the given state.
	 * Should return the value of the goal proposition that
	 * is true for that role. If there is not exactly one goal
	 * proposition true for that role, then you should throw a
	 * GoalDefinitionException because the goal is ill-defined.
	 */
	public int getGoal(ExternalPropnetMachineState state, ExternalPropnetRole role) throws GoalDefinitionException {

		// Mark base propositions according to state.
		this.markBases(state);

		// Get all the otehrProposition values (the goals are included there).
		OpenBitSet otherComponents = this.propnetState.getOtherComponents();
		// Get for every role the index that its first goal propositions has in
		// the otherComponents array. Remember all goal proposition for the same
		// role are one after the other.
		int[] firstGoalIndices = this.propnetState.getFirstGoalIndices();

		int trueGoalIndex = otherComponents.nextSetBit(firstGoalIndices[role.getIndex()]);

		if(trueGoalIndex >= firstGoalIndices[role.getIndex()+1] || trueGoalIndex == -1){ // No true goal for current role
			MachineState standardState = this.externalStateToState(state);
			Role standardRole = this.externalRoleToRole(role);
			GamerLogger.logError("StateMachine", "[Propnet] Got no true goal in state " + standardState + " for role " + standardRole + ".");
			throw new GoalDefinitionException(standardState, standardRole);
		}else if(trueGoalIndex < (firstGoalIndices[role.getIndex()+1]-1)){ // If it's not the last goal proposition check if there are any other true goal propositions for the role
			int nextTrueGoalIndex =	otherComponents.nextSetBit(trueGoalIndex+1);
			if(nextTrueGoalIndex < firstGoalIndices[role.getIndex()+1] && nextTrueGoalIndex != -1){
				MachineState standardState = this.externalStateToState(state);
				Role standardRole = this.externalRoleToRole(role);
				GamerLogger.logError("StateMachine", "[Propnet] Got more than one true goal in state " + standardState + " for role " + standardRole + ".");
				throw new GoalDefinitionException(standardState, standardRole);
			}
		}

		return this.propNet.getGoalValues()[role.getIndex()][trueGoalIndex-firstGoalIndices[role.getIndex()]];

	}

	/**
	 * Returns the initial state. If the initial state has not been computed yet because
	 * this state machine has not been initialized, NULL will be returned.
	 */
	@Override
	public MachineState getInitialState() {

		/*
		if(this.initialState == null){
			System.out.println("Null initial state.");
		}else{
			OpenBitSet state = this.initialState.getTruthValues();
			System.out.print("[ ");
			for(int i = 0; i < state.size(); i++){

				if(state.fastGet(i)){
					System.out.print("1");
				}else{
					System.out.print("0");
				}

			}
			System.out.println(" ]");
		}
		*/


		return this.externalStateToState(this.initialState);
	}

	/**
	 * Returns the initial state. If the initial state has not been computed yet because
	 * this state machine has not been initialized, NULL will be returned.
	 */
	public ExternalPropnetMachineState getPropnetInitialState() {
		return this.initialState;
	}

	/**
	 * Computes the legal moves for role in state.
	 * Since the state is not an internal propnet state, it is first transformed into one.
	 */
	@Override
	public List<Move> getLegalMoves(MachineState state, Role role)throws MoveDefinitionException {
		List<Move> moves = new ArrayList<Move>();
		ExternalPropnetRole externalRole = this.roleToExternalRole(role);
		for(ExternalPropnetMove m : this.getLegalMoves(this.stateToExternalState(state), externalRole)){
			moves.add(this.externalMoveToMove(m));
		}
		return moves;
	}

	/**
	 * Computes the legal moves for role in state.
	 */
	public List<ExternalPropnetMove> getLegalMoves(ExternalPropnetMachineState state, ExternalPropnetRole role)throws MoveDefinitionException {
		// Mark base propositions according to state.
		this.markBases(state);

		List<ExternalPropnetMove> legalMoves = new ArrayList<ExternalPropnetMove>();

		// Get all the otehrProposition values (the legal propositions are included there).
		OpenBitSet otherComponents = this.propnetState.getOtherComponents();
		// Get for every role the index that its first legal propositions has in
		// the otherComponents array. Remember all legal proposition for the same
		// role are one after the other.
		int[] firstLegalIndices = this.propnetState.getFirstLegalIndices();

		int trueLegalIndex = otherComponents.nextSetBit(firstLegalIndices[role.getIndex()]);

		while(trueLegalIndex < firstLegalIndices[role.getIndex()+1] && trueLegalIndex != -1){
			legalMoves.add(new ExternalPropnetMove(trueLegalIndex - firstLegalIndices[0]));
			trueLegalIndex = otherComponents.nextSetBit(trueLegalIndex+1);
		}

		// If there are no legal moves for the role in this state throw an exception.
		if(legalMoves.size() == 0){
			throw new MoveDefinitionException(this.externalStateToState(state), this.externalRoleToRole(role));
		}

		return legalMoves;
	}

	/**
	 * Computes the next state given a state and the list of moves.
	 * If the state is not an extended propnet state, it is first transformed into one.
	 */
	@Override
	public MachineState getNextState(MachineState state, List<Move> moves)throws TransitionDefinitionException {
		return this.externalStateToState(this.getNextState(this.stateToExternalState(state), this.moveToExternalMove(moves)));
	}

	/**
	 * Computes the next state given a state and the list of moves.
	 */
	public ExternalPropnetMachineState getNextState(ExternalPropnetMachineState state, List<ExternalPropnetMove> moves) throws TransitionDefinitionException {
		// Mark base propositions according to state.
		this.markBases(state);

		// Mark input propositions according to the moves.
		this.markInputs(moves);

		// Compute next state for each base proposition from the corresponding transition.
		return new ExternalPropnetMachineState(this.propnetState.getNextState().clone());
	}

	/* Already implemented for you */
	@Override
	public List<Role> getRoles() {
		List<Role> roles = new ArrayList<Role>();
		Role[] rolesArray = this.propNet.getRoles();
		for(ExternalPropnetRole r : this.roles){
			roles.add(rolesArray[r.getIndex()]);
		}
		return ImmutableList.copyOf(roles);
	}

	/* Helper methods */

	/**
	 * The Input propositions are indexed by (does ?player ?action).
	 *
	 * This translates a list of Moves (backed by a sentence that is simply ?action)
	 * into GdlSentences that can be used to get Propositions from inputPropositions.
	 * and accordingly set their values etc.  This is a naive implementation when coupled with
	 * setting input values, feel free to change this for a more efficient implementation.
	 *
	 * @param moves the moves to be translated into 'does' propositions.
	 * @return a list with the 'does' propositions corresponding to the given joint move.
	 */
	private List<GdlSentence> toDoes(List<Move> moves){

		//AGGIUNTA
    	//System.out.println("TO DOES");
    	//System.out.println("MOVES");
    	//System.out.println(moves);
    	//FINE AGGIUNTA

		List<GdlSentence> doeses = new ArrayList<GdlSentence>(moves.size());

		//AGGIUNTA
		//System.out.println("ROLES");
    	//System.out.println(this.roles);

    	//System.out.println("ROLE INDICES");
    	//System.out.println(roleIndices);
    	//FINE AGGIUNTA

		// TODO: both the roles and the moves should be already in the correct order so no need
		// to retrieve the roles indices!!
		for (int i = 0; i < this.roles.length; i++){

			//AGGIUNTA
	    	//System.out.println("i=" + i);
	    	//FINE AGGIUNTA

			doeses.add(ProverQueryBuilder.toDoes(this.externalRoleToRole(this.roles[i]), moves.get(i)));
		}

		//AGGIUNTA
		//System.out.println("MOVES");
		//System.out.println(moves);
		//System.out.println("DOESES");
		//System.out.println(doeses);
		//FINE AGGIUNTA

		return doeses;
	}

	/**
	 * Takes in an Input Proposition and returns the appropriate corresponding Move.
	 *
	 * This method should work both for input and legal propositions.
	 *
	 * @param p the proposition to be transformed into a move.
	 * @return the move corresponding to the given proposition.
	 */
	public static Move getMoveFromProposition(ImmutableProposition p){
		return new Move(p.getName().get(1));
	}

	/**
	 * Takes as input a MachineState and returns a machine state extended with the bit array
	 * representing the truth value in the state for each base proposition.
	 *
	 * @param state a machine state.
	 * @return a machine state extended with the bit array representing the truth value in
	 * the state for each base proposition.
	 */
	// TODO IMPLEMENT
	public ExternalPropnetMachineState stateToExternalState(MachineState state){
		if(state != null){
			ImmutableProposition[] baseProps = this.propNet.getBasePropositions();
			OpenBitSet basePropsTruthValues = new OpenBitSet(baseProps.length);
			Set<GdlSentence> contents = state.getContents();
			if(!contents.isEmpty()){
				for(int i = 0; i < baseProps.length; i++){
					if(contents.contains(baseProps[i].getName())){
						basePropsTruthValues.fastSet(i);
					}
				}
			}
			return new ExternalPropnetMachineState(basePropsTruthValues);
		}

		return null;
	}

	// TODO: IMPLEMENT
	public MachineState externalStateToState(ExternalPropnetMachineState state){
		if(state != null){
			ImmutableProposition[] baseProps = this.propNet.getBasePropositions();
			OpenBitSet basePropsTruthValues = state.getTruthValues();
			Set<GdlSentence> contents = new HashSet<GdlSentence>();

			int setIndex = basePropsTruthValues.nextSetBit(0);
			while(setIndex != -1){
				contents.add(baseProps[setIndex].getName());
				setIndex = basePropsTruthValues.nextSetBit(setIndex+1);
			}

			return new MachineState(contents);
		}

		return null;
	}

	// TODO: IMPLEMENT
	public Role externalRoleToRole(ExternalPropnetRole role){
		if(role != null){
			return this.propNet.getRoles()[role.getIndex()];
		}

		return null;
	}

	// TODO: IMPLEMENT
	public ExternalPropnetRole roleToExternalRole(Role role){
		if(role != null){
			// TODO check if index is -1 -> should never happen if the role given as input is a valid role.
			Role[] roles = this.propNet.getRoles();
			for(int i = 0; i < roles.length; i++){
				if(role.equals(roles[i])){
					return new ExternalPropnetRole(i);
				}
			}
		}

		return null;
	}

	// TODO: IMPLEMENT
	public Move externalMoveToMove(ExternalPropnetMove move){
		return getMoveFromProposition(this.propNet.getInputPropositions()[move.getIndex()]);
	}

	// TODO: IMPLEMENT
	public ExternalPropnetMove moveToExternalMove(Move move){
		List<Move> moveArray = new ArrayList<Move>();
		moveArray.add(move);
		GdlSentence moveToDoes = this.toDoes(moveArray).get(0);

		int i = 0;
		for(ImmutableProposition input : this.propNet.getInputPropositions()){
			if(input.equals(moveToDoes)){
				break;
			}
			i++;
		}
		return new ExternalPropnetMove(i);
	}

	/**
	 * Useful when we need to translate a joint move. Faster than translating the moves one by one.
	 *
	 * @param move
	 * @param roleIndex
	 * @return
	 */
	// TODO: IMPLEMENT
	public List<ExternalPropnetMove> moveToExternalMove(List<Move> moves){

		List<ExternalPropnetMove> transformedMoves = new ArrayList<ExternalPropnetMove>();
		List<GdlSentence> movesToDoes = this.toDoes(moves);

		ImmutableProposition[] inputs = this.propNet.getInputPropositions();

		for(int i = 0; i < inputs.length; i++){
			if(movesToDoes.contains(inputs[i].getName())){
				transformedMoves.add(new ExternalPropnetMove(i));
			}
		}

		return transformedMoves;
	}

    /**
     * Marks the base propositions with the correct value so that the propnet state resembles the
     * given machine state.
     *
     * This method iterates over the base propositions and flips the values of the ones that change
     * in the new state wrt the current state.
     *
     * @param state the machine state to be set in the propnet.
     */
	private void markBases(ExternalPropnetMachineState state){

		// Clone the currently set state to avoid modifying it here.
		OpenBitSet bitsToFlip = this.propnetState.getCurrentState().clone();

		// If the new state is different from the currently set one, change it and update the propnet.
		if(!(bitsToFlip.equals(state.getTruthValues()))){

			// Get only the bits that have to change.
			bitsToFlip.xor(state.getTruthValues());

			ImmutableProposition[] basePropositions = this.propNet.getBasePropositions();

			// Change the base propositions that have to do so.
			int indexToFlip = bitsToFlip.nextSetBit(0);
			while(indexToFlip != -1){
				basePropositions[indexToFlip].updateValue(state.getTruthValues().get(indexToFlip), this.propnetState);
				indexToFlip = bitsToFlip.nextSetBit(indexToFlip+1);
			}
		}
	}

    /**
     * Marks the base propositions with the correct value so that the propnet state resembles the
     * given machine state.
     *
     * This method first sets the base propositions that became true in the new state, then sets
     * the ones that became false.
     *
     * @param state the machine state to be set in the propnet.
     */
	private void markBases2(ExternalPropnetMachineState state){

		// Clone the currently set state to avoid modifying it here.
		OpenBitSet bitsToChange = state.getTruthValues().clone();

		// If the new state is different from the currently set one, change it and update the propnet.
		if(!(bitsToChange.equals(this.propnetState.getCurrentState()))){

			// First set the true base propositions.
			bitsToChange.andNot(this.propnetState.getCurrentState());

			ImmutableProposition[] basePropositions = this.propNet.getBasePropositions();

			// Set the base proposition that have to become true.
			int indexToChange = bitsToChange.nextSetBit(0);
			while(indexToChange != -1){
				basePropositions[indexToChange].updateValue(true, this.propnetState);
				indexToChange = bitsToChange.nextSetBit(indexToChange+1);
			}

			// Then clear the false base propositions.
			bitsToChange = this.propnetState.getCurrentState().clone();
			bitsToChange.andNot(state.getTruthValues());

			// Clear the base proposition that have to become false.
			indexToChange = bitsToChange.nextSetBit(0);
			while(indexToChange != -1){
				basePropositions[indexToChange].updateValue(false, this.propnetState);
				indexToChange = bitsToChange.nextSetBit(indexToChange+1);
			}
		}
	}

	/**
     * Marks the input propositions with the correct values so that the proposition corresponding to
     * a performed move are set to TRUE and all others to FALSE.
     *
     * This method iterates over the input propositions flipping the values that changed in the new move.
     *
     * @param moves the moves to be set as performed in the propnet.
     *
     * TODO: instead of using a Move object just use an array of int.
     */
	private void markInputs2(List<ExternalPropnetMove> moves){

		// Clone the currently set move to avoid modifying it here.
		OpenBitSet bitsToFlip = this.propnetState.getCurrentJointMove().clone();

		// Translate the indexes into a bitset.
		OpenBitSet newJointMove = new OpenBitSet(bitsToFlip.capacity());
		for(ExternalPropnetMove move : moves){
			newJointMove.fastSet(move.getIndex());
		}

		// If it's a different move, update the current move.
		if(!(bitsToFlip.equals(newJointMove))){

			// Get only the bits that have to change.
			bitsToFlip.xor(newJointMove);

			ImmutableProposition[] inputPropositions = this.propNet.getInputPropositions();

			// Change the input propositions that have to do so.
			int indexToFlip = bitsToFlip.nextSetBit(0);
			while(indexToFlip != -1){
				inputPropositions[indexToFlip].updateValue(newJointMove.get(indexToFlip), this.propnetState);
				indexToFlip = bitsToFlip.nextSetBit(indexToFlip+1);
			}
		}
	}

	/**
     * Marks the input propositions with the correct values so that the proposition corresponding to
     * a performed move are set to TRUE and all others to FALSE.
     *
     * First it sets the input propositions that became true and then the ones that became false.
     *
     *
     * @param moves the moves to be set as performed in the propnet.
     *
     * TODO: instead of using a Move object just use an array of int.
     */
	private void markInputs(List<ExternalPropnetMove> moves){

		// Get the currently set move
		OpenBitSet currentJointMove = this.propnetState.getCurrentJointMove();

		// Translate the indexes into a bitset
		OpenBitSet newJointMove = new OpenBitSet(currentJointMove.capacity());

		for(ExternalPropnetMove move : moves){
			newJointMove.fastSet(move.getIndex());
		}

		// If it's a different move, update the current move
		if(!(newJointMove.equals(currentJointMove))){

			// First set the true moves
			OpenBitSet bitsToChange = newJointMove.clone();
			bitsToChange.andNot(currentJointMove);

			ImmutableProposition[] inputPropositions = this.propNet.getInputPropositions();

			int indexToChange = bitsToChange.nextSetBit(0);
			while(indexToChange != -1){
				inputPropositions[indexToChange].updateValue(true, this.propnetState);
				indexToChange = bitsToChange.nextSetBit(indexToChange+1);
			}

			// Then clear the false moves
			bitsToChange = this.propnetState.getCurrentJointMove().clone();
			bitsToChange.andNot(newJointMove);

			indexToChange = bitsToChange.nextSetBit(0);
			while(indexToChange != -1){
				inputPropositions[indexToChange].updateValue(false, this.propnetState);
				indexToChange = bitsToChange.nextSetBit(indexToChange+1);
			}
		}
	}

	/**
	 * Get method for the 'propNet' parameter.
	 *
	 * @return The proposition network.
	 */
	public ImmutablePropNet getPropNet(){
		return this.propNet;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		// No need to do anything
	}


	/***************** Extra methods to replace the ones offered by the StateMahcine *****************/

    /**
     * Returns a terminal state derived from repeatedly making random joint moves
     * until reaching the end of the game.
     *
     * @param theDepth an integer array, the 0th element of which will be set to
     * the number of state changes that were made to reach a terminal state.
     *
     * @throws TransitionDefinitionException indicates an error in either the
     * game description or the StateMachine implementation.
     * @throws MoveDefinitionException if a role has no legal moves. This indicates
     * an error in either the game description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to completely perform a
     * playout of the game because of an error that occurred in the state machine and
     * couldn't be handled.
     */
    public ExternalPropnetMachineState performDepthCharge(ExternalPropnetMachineState state, final int[] theDepth) throws TransitionDefinitionException, MoveDefinitionException, StateMachineException {
        int nDepth = 0;
        while(!isTerminal(state)) {
            nDepth++;
            state = getNextState(state, getRandomJointMove(state));
        }
        if(theDepth != null)
            theDepth[0] = nDepth;
        return state;
    }

    /**
     * Like performDepthCharge() method, but this one checks after visiting each node
     * if it has been interrupted. If so it makes sure that the array theDepth contains
     * the currently reached depth and returns null as terminal state.
     * Moreover, when any other exception is thrown while visiting the nodes, the number
     * of nodes visited so far (nDepth) is returned and the exception is not re-thrown,
     * since this method is only used to check the amount of nodes that the state machine
     * can visit in a certain amount of time. Also in this case null will be returned as
     * terminal state.
     *
     * @param state the state from where to start the simulation.
     * @param theDepth an integer array, the 0th element of which will be set to
     * the number of state changes that were made until the current visited state.
     *
     */
    public ExternalPropnetMachineState interruptiblePerformDepthCharge(ExternalPropnetMachineState state, final int[] theDepth) /*throws TransitionDefinitionException, MoveDefinitionException, StateMachineException*/ {
        int nDepth = 0;
        try {
	        while(!isTerminal(state)) {

	            state = getNextState(state, getRandomJointMove(state));

	            nDepth++;

				ConcurrencyUtils.checkForInterruption();
			}
        } catch (InterruptedException | StateMachineException | TransitionDefinitionException | MoveDefinitionException e) {
			// This method can return a consistent result even if it has not completed execution
			// so the InterruptedException is not re-thrown
			if(theDepth != null)
	            theDepth[0] = nDepth;
	        return null;
		}
        if(theDepth != null)
            theDepth[0] = nDepth;
        return state;
    }

    /**
     * Returns a random joint move from among all the possible joint moves in
     * the given state.
     *
     * @throws MoveDefinitionException if a role has no legal moves. This indicates
     * an error in either the game description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to compute the random
     * joint move in the given state because of an error that occurred in the state
     * machine and couldn't be handled.
     */
    public List<ExternalPropnetMove> getRandomJointMove(ExternalPropnetMachineState state) throws MoveDefinitionException, StateMachineException{
        List<ExternalPropnetMove> random = new ArrayList<ExternalPropnetMove>();
        for(ExternalPropnetRole role : this.roles) {
            random.add(getRandomMove(state, role));
        }

        return random;
    }

    /**
     * Returns a random move from among the possible legal moves for the
     * given role in the given state.
     *
     * @throws MoveDefinitionException if the role has no legal moves. This indicates
     * an error in either the game description or the StateMachine implementation.
     * @throws StateMachineException if it was not possible to compute a
     * random move for the given role in the given state because of an
     * error that occurred in the state machine and couldn't be handled.
     */
    public ExternalPropnetMove getRandomMove(ExternalPropnetMachineState state, ExternalPropnetRole role) throws MoveDefinitionException, StateMachineException
    {
        List<ExternalPropnetMove> legals = getLegalMoves(state, role);
        return legals.get(new Random().nextInt(legals.size()));
    }

}
