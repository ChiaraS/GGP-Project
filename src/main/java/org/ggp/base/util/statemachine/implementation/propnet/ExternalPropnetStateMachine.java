package org.ggp.base.util.statemachine.implementation.propnet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.util.OpenBitSet;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.extendedState.components.ExtendedStateProposition;
import org.ggp.base.util.propnet.architecture.extendedState.components.ExtendedStateTransition;
import org.ggp.base.util.propnet.architecture.externalizedState.ExternalizedStatePropNet;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateProposition;
import org.ggp.base.util.propnet.factory.ExternalizedStatePropnetFactory;
import org.ggp.base.util.propnet.state.ExternalPropnetState;
import org.ggp.base.util.statemachine.ExtendedStatePropnetMachineState;
import org.ggp.base.util.statemachine.ExternalPropnetMachineState;
import org.ggp.base.util.statemachine.ExternalPropnetMove;
import org.ggp.base.util.statemachine.ExternalPropnetRole;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.query.ProverQueryBuilder;

import com.google.common.collect.ImmutableList;

public class ExternalPropnetStateMachine extends StateMachine {

	/** The underlying proposition network  */
    private ExternalizedStatePropNet propNet;

    /** The state of the proposition network */
    private ExternalPropnetState propnetState;

    /** The player roles */
    private ImmutableList<Role> roles;
    /** The initial state */
    private ExternalPropnetMachineState initialState;



    /**
	 * Total time (in milliseconds) taken to construct the propnet.
	 * If it is negative it means that the propnet didn't build in time.
	 */
	private long propnetConstructionTime = -1L;

    /**
     * Initializes the PropNetStateMachine. You should compute the topological
     * ordering here. Additionally you may compute the initial state here, at
     * your discretion.
     *
     * @throws StateMachineInitializationException
     */
    @Override
    public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException {
    	long startTime = System.currentTimeMillis();
		// Create the propnet
    	try{
    		this.propNet = ExternalizedStatePropnetFactory.create(description);
    	}catch(InterruptedException e){
    		GamerLogger.logError("StateMachine", "[Propnet] Propnet creation interrupted!");
    		GamerLogger.logStackTrace("StateMachine", e);
    		throw new StateMachineInitializationException(e);
    	}
    	// Compute the time taken to construct the propnet
    	this.propnetConstructionTime = System.currentTimeMillis() - startTime;
		GamerLogger.log("StateMachine", "[Propnet Creator] Propnet creation done. It took " + (this.propnetConstructionTime) + "ms.");

		// Compute the roles
   		this.roles = ImmutableList.copyOf(this.propNet.getRoles());
        // If it exists, set init proposition to true without propagating, so that when making
		// the propnet consistent its value will be propagated so that the next state
		// will correspond to the initial state.
	    // REMARK: if there is not TRUE proposition in the initial state, the INIT proposition
	    // will not exist.
	    ExtendedStateProposition init = this.propNet.getInitProposition();
	    if(init != null){
	    	this.propNet.getInitProposition().setValue(true);

	    }

	    // Set that there is no joint move currently set to true
	    this.currentMove = new ArrayList<GdlSentence>();

	    // Set that the currently set state is empty, i.e. all base propositions are set to false
	    // (because they are initialized like this). Moreover, this won't change after imposing
	    // consistency, since base propositions don't need to change their value to be consistent
	    // with their input because the transitions propagate their value only in the next staep.
	    this.currentState = new OpenBitSet(this.propNet.getBasePropositionsArray().length);

		// No need to set all other inputs to false because they already are.
		// Impose consistency on the propnet (TODO REMARK: this method should also check
	    // for interrupts -> is it safe to assume it will never get stuck indefinitely?).
		this.propNet.imposeConsistency();
		// The initial state can be computed by only setting the truth value of the INIT
		// proposition to TRUE, and then computing the resulting next state paying attention that all the
		// base propositions that result being TRUE are also depending on the INIT proposition value.
		// If there is no init proposition we can just set the initial state to the state with
		// empty content.
		if(init != null){
		   	this.initialState = this.computeInitialState();
		}else{
			this.initialState = new MachineState(new HashSet<GdlSentence>());
		}
    }




    /**
     * This method returns the initial machine state. This state contains the set of all the base propositions
     * that will become true in the next state, given the current state of the propnet, with both
     * base and input proposition marked and their value propagated.
     *
     * !REMARK: this method computes the next state but doesn't advance the propnet state. The propnet
     * will still be in the current state.
	 *
     * @return the initial state.
     */
    private MachineState computeInitialState(){

    	//AGGIUNTA
    	//System.out.println("COMPUTING INIT STATE");
    	//FINE AGGIUNTA

    	Set<GdlSentence> contents = new HashSet<GdlSentence>(this.propNet.getNextStateContents());

    	// Add to the initial machine state all the base propositions that are connected to a true transition
    	// whose value also depends on the value of the INIT proposition.
    	Map<GdlSentence, ExtendedStateProposition> basePropositions = this.propNet.getBasePropositions();

    	Iterator<GdlSentence> iterator = contents.iterator();
		while(iterator.hasNext()){
			if(!((ExtendedStateTransition) basePropositions.get(iterator.next()).getSingleInput()).isDependingOnInit()){
				iterator.remove();
			}
		}

		OpenBitSet basePropsTruthValue = this.propNet.getNextState().clone();

		basePropsTruthValue.and(this.propNet.getDependOnInit());

		return new ExtendedStatePropnetMachineState(contents, basePropsTruthValue);
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

		if(trueGoalIndex >= firstGoalIndices[role.getIndex()+1]){ // No true goal for current role
			MachineState standardState = this.externalStateToState(state);
			Role standardRole = this.externalRoleToRole(role);
			GamerLogger.logError("StateMachine", "[Propnet] Got no true goal in state " + standardState + " for role " + standardRole + ".");
			throw new GoalDefinitionException(standardState, standardRole);
		}else if(trueGoalIndex < (firstGoalIndices[role.getIndex()+1]-1)){ // If it's not the last goal proposition check if there are any other true goal propositions for the role
			if(otherComponents.nextSetBit(trueGoalIndex+1) < firstGoalIndices[role.getIndex()+1]){
				MachineState standardState = this.externalStateToState(state);
				Role standardRole = this.externalRoleToRole(role);
				GamerLogger.logError("StateMachine", "[Propnet] Got more than one true goal in state " + standardState + " for role " + standardRole + ".");
				throw new GoalDefinitionException(standardState, standardRole);
			}
		}

		return this.propNet.getGoals(role.getIndex()).get(trueGoalIndex-firstGoalIndices[role.getIndex()]);

	}

	/**
	 * Returns the initial state. If the initial state has not been computed yet because
	 * this state machine has not been initialized, NULL will be returned.
	 */
	@Override
	public MachineState getInitialState() {
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
	 * If the state is not an extended propnet state, it is first transformed into one.
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

		while(trueLegalIndex < firstLegalIndices[role.getIndex()+1]){
			legalMoves.add(new ExternalPropnetMove(trueLegalIndex - firstLegalIndices[0]));
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
		return roles;
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
		Map<Role, Integer> roleIndices = getRoleIndices();

		//AGGIUNTA
		//System.out.println("ROLES");
    	//System.out.println(this.roles);

    	//System.out.println("ROLE INDICES");
    	//System.out.println(roleIndices);
    	//FINE AGGIUNTA

		// TODO: both the roles and the moves should be already in the correct order so no need
		// to retrieve the roles indices!!
		for (int i = 0; i < roles.size(); i++)
		{

			//AGGIUNTA
	    	//System.out.println("i=" + i);
	    	//FINE AGGIUNTA

			int index = roleIndices.get(roles.get(i));
			doeses.add(ProverQueryBuilder.toDoes(roles.get(i), moves.get(index)));
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
	public static Move getMoveFromProposition(ExternalizedStateProposition p){
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
			List<ExternalizedStateProposition> baseProps = this.propNet.getBasePropositions();
			OpenBitSet basePropsTruthValues = new OpenBitSet(baseProps.size());
			Set<GdlSentence> contents = state.getContents();
			if(!contents.isEmpty()){
				for(int i = 0; i < baseProps.size(); i++){
					if(contents.contains(baseProps.get(i).getName())){
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
			List<ExternalizedStateProposition> baseProps = this.propNet.getBasePropositions();
			OpenBitSet basePropsTruthValues = state.getTruthValues();
			Set<GdlSentence> contents = new HashSet<GdlSentence>();

			int setIndex = basePropsTruthValues.nextSetBit(0);
			while(setIndex != -1){
				contents.add(baseProps.get(setIndex).getName());
				setIndex = basePropsTruthValues.nextSetBit(setIndex);
			}

			return new MachineState(contents);
		}

		return null;
	}

	// TODO: IMPLEMENT
	public Role externalRoleToRole(ExternalPropnetRole role){
		if(role != null){
			return this.roles.get(role.getIndex());
		}

		return null;
	}

	// TODO: IMPLEMENT
	public ExternalPropnetRole roleToExternalRole(Role role){
		if(role != null){
			// TODO check if index is -1 -> should never happen if the role given as input is a valid role.
			return new ExternalPropnetRole(this.roles.indexOf(role));
		}

		return null;
	}

	// TODO: IMPLEMENT
	public Move externalMoveToMove(ExternalPropnetMove move){
		return getMoveFromProposition(this.propNet.getInputPropositions().get(move.getIndex()));
	}

	// TODO: IMPLEMENT
	public ExternalPropnetMove moveToExternalMove(Move move){
		List<Move> moveArray = new ArrayList<Move>();
		moveArray.add(move);
		GdlSentence moveToDoes = this.toDoes(moveArray).get(0);

		int i = 0;
		for(ExternalizedStateProposition input : this.propNet.getInputPropositions()){
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

		List<ExternalizedStateProposition> inputs = this.propNet.getInputPropositions();

		for(int i = 0; i < inputs.size(); i++){
			if(movesToDoes.contains(inputs.get(i).getName())){
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

			List<ExternalizedStateProposition> basePropositions = this.propNet.getBasePropositions();

			// Change the base propositions that have to do so.
			int indexToFlip = bitsToFlip.nextSetBit(0);
			while(indexToFlip != -1){
				basePropositions.get(indexToFlip).flipValue(this.propnetState);
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

			List<ExternalizedStateProposition> basePropositions = this.propNet.getBasePropositions();

			// Set the base proposition that have to become true.
			int indexToChange = bitsToChange.nextSetBit(0);
			while(indexToChange != -1){
				basePropositions.get(indexToChange).updateValue(true, this.propnetState);
				indexToChange = bitsToChange.nextSetBit(indexToChange+1);
			}

			// Then clear the false base propositions.
			bitsToChange = this.propnetState.getCurrentState().clone();
			bitsToChange.andNot(state.getTruthValues());

			// Clear the base proposition that have to become false.
			indexToChange = bitsToChange.nextSetBit(0);
			while(indexToChange != -1){
				basePropositions.get(indexToChange).updateValue(false, this.propnetState);
				indexToChange = bitsToChange.nextSetBit(indexToChange+1);
			}
		}
	}

	/**
     * Marks the input propositions with the correct values so that the proposition corresponding to
     * a performed move are set to TRUE and all others to FALSE.
     *
     * This method iterates over the input propositions flipping the vales that changed in the new move.
     *
     * !REMARK: also the INIT proposition can be considered as a special case of INPUT proposition,
     * thus when marking the input propositions to compute a subsequent state different from the
     * initial one, make sure that the INIT proposition is also set to FALSE.
     *
     * @param moves the moves to be set as performed in the propnet.
     *
     * TODO: instead of using a Move object just use an array of int.
     */
	private void markInputs(List<ExternalPropnetMove> moves){

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

			List<ExternalizedStateProposition> inputPropositions = this.propNet.getInputPropositions();

			// Change the input propositions that have to do so.
			int indexToFlip = bitsToFlip.nextSetBit(0);
			while(indexToFlip != -1){
				inputPropositions.get(indexToFlip).flipValue(this.propnetState);
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
     * !REMARK: also the INIT proposition can be considered as a special case of INPUT proposition,
     * thus when marking the input propositions to compute a subsequent state different from the
     * initial one, make sure that the INIT proposition is also set to FALSE.
     *
     * @param moves the moves to be set as performed in the propnet.
     *
     * TODO: instead of using a Move object just use an array of int.
     */
	private void markInputs2(List<ExternalPropnetMove> moves){

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

			List<ExternalizedStateProposition> inputPropositions = this.propNet.getInputPropositions();

			int indexToChange = bitsToChange.nextSetBit(0);
			while(indexToChange != -1){
				inputPropositions.get(indexToChange).updateValue(true, this.propnetState);
				indexToChange = bitsToChange.nextSetBit(indexToChange+1);
			}

			// Then clear the false moves
			bitsToChange = this.propnetState.getCurrentJointMove().clone();
			bitsToChange.andNot(newJointMove);

			indexToChange = bitsToChange.nextSetBit(0);
			while(indexToChange != -1){
				inputPropositions.get(indexToChange).updateValue(false, this.propnetState);
				indexToChange = bitsToChange.nextSetBit(indexToChange+1);
			}


		}
	}

	/**
	 * Get method for the 'propNet' parameter.
	 *
	 * @return The proposition network.
	 */
	public ExternalizedStatePropNet getPropNet(){
		return this.propNet;
	}

	/**
	 * Get method for the propnet construction time.
	 *
	 * @return the actual construction time of the propnet, -1 if it has not been created in time.
	 */
	public long getPropnetConstructionTime(){
		return this.propnetConstructionTime;
	}




	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		// No need to do anything
	}
}