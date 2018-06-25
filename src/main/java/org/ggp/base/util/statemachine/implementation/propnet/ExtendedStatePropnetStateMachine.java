package org.ggp.base.util.statemachine.implementation.propnet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.lucene.util.OpenBitSet;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.extendedState.ExtendedStatePropNet;
import org.ggp.base.util.propnet.architecture.extendedState.components.ExtendedStateProposition;
import org.ggp.base.util.propnet.architecture.extendedState.components.ExtendedStateTransition;
import org.ggp.base.util.propnet.factory.ExtendedStatePropNetFactory;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.query.ProverQueryBuilder;
import org.ggp.base.util.statemachine.structure.CompactAndExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

import com.google.common.collect.ImmutableList;

public class ExtendedStatePropnetStateMachine extends StateMachine {
    /** The underlying proposition network  */
    private ExtendedStatePropNet propNet;
    /** The player roles */
    private ImmutableList<ExplicitRole> roles;
    /** The initial state */
    private ExplicitMachineState initialState;

    /** The currently set move */
    private List<GdlSentence> currentMove;

    /** The currently set state */
    private OpenBitSet currentState;

    /**
	 * Total time (in milliseconds) taken to construct the propnet.
	 * If it is negative it means that the propnet didn't build in time.
	 */
	private long propnetConstructionTime = -1L;

	public ExtendedStatePropnetStateMachine(Random random){
		super(random);
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
    	long startTime = System.currentTimeMillis();
		// Create the propnet
    	try{
    		this.propNet = ExtendedStatePropNetFactory.create(description);
    	}catch(InterruptedException e){
    		GamerLogger.logError("StateMachine", "[Propnet] Propnet creation interrupted!");
    		GamerLogger.logStackTrace("StateMachine", e);
    		throw new StateMachineInitializationException(e);
    	}
    	// Compute the time taken to construct the propnet
    	this.propnetConstructionTime = System.currentTimeMillis() - startTime;
		GamerLogger.log("StateMachine", "[Propnet Creator] Propnet creation done. It took " + (this.propnetConstructionTime) + "ms.");

		ExtendedStatePropNetFactory.removeAnonymousPropositions(this.propNet);

		GamerLogger.log("StateMachine", "[Propnet Creator] Removed anonymous propositions.");

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
			this.initialState = new ExplicitMachineState(new HashSet<GdlSentence>());
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
    private ExplicitMachineState computeInitialState(){

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

		return new CompactAndExplicitMachineState(contents, basePropsTruthValue);
    }

    /**
     * This method returns the next machine state. This state contains the set of all the base propositions
     * that will become true in the next state, given the current state of the propnet, with both
     * base and input proposition marked and their value propagated.
     *
     * !REMARK: this method computes the next state but doesn't advance the propnet state. The propnet
     * will still be in the current state.
	 *
     * @return the next state.
     */
    private ExplicitMachineState computeNextState(){

    	//AGGIUNTA
    	//System.out.println("COMPUTING NEXT STATE");
    	//FINE AGGIUNTA

		return new CompactAndExplicitMachineState(new HashSet<GdlSentence>(this.propNet.getNextStateContents()), this.propNet.getNextState().clone());
    }

	/**
	 * Computes if the state is terminal. Should return the value
	 * of the terminal proposition for the state.
	 * If the state is not an extended propnet state, it is first transformed into one.
	 * Note that a new instance of state will be created and the one passed as parameter
	 * won't be modified.
	 *
	 * @state a machine state.
	 * @return true if the state is terminal, false otherwise.
	 */
	@Override
	public boolean isTerminal(ExplicitMachineState state) {
		if(!(state instanceof CompactAndExplicitMachineState)){
			state = this.stateToExtendedState(state);
		}
		return this.isTerminal((CompactAndExplicitMachineState)state);
	}

	/**
	 * Computes if the state is terminal. Should return the value
	 * of the terminal proposition for the state.
	 *
	 * @state a machine state.
	 * @return true if the state is terminal, false otherwise.
	 */
	public boolean isTerminal(CompactAndExplicitMachineState state) {
		this.markBases(state);
		return this.propNet.getTerminalProposition().getValue();
	}

	/**
	 * Computes the goals for a role in the given state.
	 * If the state is not an extended propnet state, it is first transformed into one.
	 * Should return the value of the goal proposition that
	 * is true for that role.
	 */
	@Override
	public List<Double> getAllGoalsForOneRole(ExplicitMachineState state, ExplicitRole role){
		if(!(state instanceof CompactAndExplicitMachineState)){
			state = this.stateToExtendedState(state);
		}

		return this.getOneRoleGoals((CompactAndExplicitMachineState) state, role);

	}

	/**
	 * Computes the goal for a role in the given state.
	 * Should return the value of the goal proposition that
	 * is true for that role. If there is not exactly one goal
	 * proposition true for that role, then you should throw a
	 * GoalDefinitionException because the goal is ill-defined.
	 */
	public double getGoal(CompactAndExplicitMachineState state, ExplicitRole role)
	throws GoalDefinitionException {

    	List<Double> goals = this.getOneRoleGoals(state, role);

		if(goals.size() > 1){
			GamerLogger.logError("StateMachine", "[Propnet] Got more than one true goal in state " + state + " for role " + role + ".");
			throw new GoalDefinitionException(state, role);
		}

		// If there is no true goal proposition for the role in this state throw an exception.
		if(goals.isEmpty()){
			GamerLogger.logError("StateMachine", "[Propnet] Got no true goal in state " + state + " for role " + role + ".");
			throw new GoalDefinitionException(state, role);
		}

		// Return the single goal for the given role in the given state.
		return goals.get(0);

	}

	/**
	 * Computes the goal for a role in the given state.
	 * Should return the value of the goal proposition that
	 * is true for that role.
	 */
	public List<Double> getOneRoleGoals(CompactAndExplicitMachineState state, ExplicitRole role) {
		// Mark base propositions according to state.
		this.markBases(state);

		// Get all goal propositions for the given role.
		Set<ExtendedStateProposition> goalPropsForRole = this.propNet.getGoalPropositions().get(role);

		List<Double> trueGoals = new ArrayList<Double>();

		// Check all the goal propositions that are true for the role. If there is more than one throw an exception.
		for(ExtendedStateProposition goalProp : goalPropsForRole){
			if(goalProp.getValue()){

				trueGoals.add(this.getGoalValue(goalProp));

			}
		}

		/*
		if(trueGoals.size() > 1){
			GamerLogger.logError("StateMachine", "[Propnet] Got more than one true goal in state " + state + " for role " + role + ".");
		}

		// If there is no true goal proposition for the role in this state throw an exception.
		if(trueGoals.isEmpty()){
			GamerLogger.logError("StateMachine", "[Propnet] Got no true goal in state " + state + " for role " + role + ".");
		}
		*/

		// Return the single goal for the given role in the given state.
		return trueGoals;
	}

	/**
	 * Returns the initial state. If the initial state has not been computed yet because
	 * this state machine has not been initialized, NULL will be returned.
	 */
	@Override
	public ExplicitMachineState getExplicitInitialState() {
		return this.initialState;
	}

	/**
	 * Computes the legal moves for role in state.
	 * If the state is not an extended propnet state, it is first transformed into one.
	 */
	@Override
	public List<ExplicitMove> getExplicitLegalMoves(ExplicitMachineState state, ExplicitRole role)
	throws MoveDefinitionException {

		if(!(state instanceof CompactAndExplicitMachineState)){
			state = this.stateToExtendedState(state);
		}

		return this.getLegalMoves((CompactAndExplicitMachineState) state, role);
	}

	/**
	 * Computes the legal moves for role in state.
	 */
	public List<ExplicitMove> getLegalMoves(CompactAndExplicitMachineState state, ExplicitRole role)
	throws MoveDefinitionException {
		// Mark base propositions according to state.
		this.markBases(state);

		// Retrieve all legal propositions for the given role.
		Set<ExtendedStateProposition> legalPropsForRole = this.propNet.getLegalPropositions().get(role);

		// Create the list of legal moves.
		List<ExplicitMove> legalMovesForRole = new ArrayList<ExplicitMove>();
		for(ExtendedStateProposition legalProp : legalPropsForRole){
			if(legalProp.getValue()){
				legalMovesForRole.add(getMoveFromProposition(legalProp));
			}
		}

		// If there are no legal moves for the role in this state throw an exception.
		if(legalMovesForRole.size() == 0){
			throw new MoveDefinitionException(state, role);
		}

		return legalMovesForRole;
	}

	/**
	 * Computes the next state given a state and the list of moves.
	 * If the state is not an extended propnet state, it is first transformed into one.
	 */
	@Override
	public ExplicitMachineState getExplicitNextState(ExplicitMachineState state, List<ExplicitMove> moves)
	throws TransitionDefinitionException {

		if(!(state instanceof CompactAndExplicitMachineState)){
			state = this.stateToExtendedState(state);
		}

		// Compute next state for each base proposition from the corresponding transition.
		return this.getNextState((CompactAndExplicitMachineState) state, moves);
	}

	/**
	 * Computes the next state given a state and the list of moves.
	 */
	public ExplicitMachineState getNextState(CompactAndExplicitMachineState state, List<ExplicitMove> moves)
	throws TransitionDefinitionException {
		// Mark base propositions according to state.
		this.markBases(state);

		// Mark input propositions according to the moves.
		this.markInputs(moves);

		// Compute next state for each base proposition from the corresponding transition.
		return this.computeNextState();
	}

	/* Already implemented for you */
	@Override
	public List<ExplicitRole> getExplicitRoles() {
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
	private List<GdlSentence> toDoes(List<ExplicitMove> moves){

		//AGGIUNTA
    	//System.out.println("TO DOES");
    	//System.out.println("MOVES");
    	//System.out.println(moves);
    	//FINE AGGIUNTA

		List<GdlSentence> doeses = new ArrayList<GdlSentence>(moves.size());
		Map<ExplicitRole, Integer> roleIndices = getRoleIndices();

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
	 * Takes in a Legal Proposition and returns the appropriate corresponding Move.
	 *
	 * @param p the proposition to be transformed into a move.
	 * @return the legal move corresponding to the given proposition.
	 */
	public static ExplicitMove getMoveFromProposition(ExtendedStateProposition p){
		return new ExplicitMove(p.getName().get(1));
	}

	/**
	 * Takes as input a MachineState and returns a machine state extended with the bit array
	 * representing the truth value in the state for each base proposition.
	 *
	 * @param state a machine state.
	 * @return a machine state extended with the bit array representing the truth value in
	 * the state for each base proposition.
	 */
	public CompactAndExplicitMachineState stateToExtendedState(ExplicitMachineState state){
		if(state != null){
			ExtendedStateProposition[] baseProps = this.propNet.getBasePropositionsArray();
			OpenBitSet basePropsTruthValue = new OpenBitSet(baseProps.length);
			Set<GdlSentence> contents = state.getContents();
			if(!contents.isEmpty()){
				for(int i = 0; i < baseProps.length; i++){
					if(contents.contains(baseProps[i].getName())){
						basePropsTruthValue.fastSet(i);
					}
				}
			}
			return new CompactAndExplicitMachineState(new HashSet<GdlSentence>(contents), basePropsTruthValue);
		}

		return null;
	}

	/**
	 * Helper method for parsing the value of a goal proposition
	 * @param goalProposition
	 * @return the integer value of the goal proposition
	 */
    private double getGoalValue(ExtendedStateProposition goalProposition){
		GdlRelation relation = (GdlRelation) goalProposition.getName();
		GdlConstant constant = (GdlConstant) relation.get(1);
		return Double.parseDouble(constant.toString());
	}

    /**
     * Marks the base propositions with the correct value so that the propnet state resembles the
     * given machine state. This method works only if the GdlPool is being used.
     *
     * @param state the machine state to be set in the propnet.
     */
	private void markBases(CompactAndExplicitMachineState state){

		//AGGIUNTA
    	//System.out.println("MARKING BASES");
    	//FINE AGGIUNTA

		// This will set to 1 only the propositions that must change value
		this.currentState.xor(state.getBasePropsTruthValue());

		ExtendedStateProposition[] basePropositions = this.propNet.getBasePropositionsArray();

		int indexToFlip = this.currentState.nextSetBit(0);
		while(indexToFlip != -1){
			basePropositions[indexToFlip].flipValue();
			indexToFlip = this.currentState.nextSetBit(indexToFlip+1);
		}

		this.currentState = state.getBasePropsTruthValue().clone();
	}

	/**
     * Marks the input propositions with the correct values so that the proposition corresponding to
     * a performed move are set to TRUE and all others to FALSE. This method works only if the
     * GdlPool is being used.
     *
     * !REMARK: also the INIT proposition can be considered as a special case of INPUT proposition,
     * thus when marking the input propositions to compute a subsequent state different from the
     * initial one, make sure that the INIT proposition is also set to FALSE.
     *
     * @param moves the moves to be set as performed in the propnet.
     */
	private void markInputs(List<ExplicitMove> moves){

		//AGGIUNTA
    	//System.out.println("MARKING INPUTS");
    	//FINE AGGIUNTA

		// Transform the moves into 'does' propositions (the correct order should be kept).
		List<GdlSentence> movesToDoes = this.toDoes(moves);

		// Check if the currently set move is different and we have to change it,
		// or if it's the same so we have nothing to do.
		if(!movesToDoes.equals(this.currentMove)){

			// Get all input propositions
			Map<GdlSentence, ExtendedStateProposition> inputPropositions = this.propNet.getInputPropositions();
			// First set to true the input propositions of the given move...
			for(GdlSentence gdlMove: movesToDoes){
				inputPropositions.get(gdlMove).setAndPropagateValue(true);
			}

			// Then set to false all the input propositions of the previous move
			for(int i = 0; i < this.currentMove.size(); i++){
				if(this.currentMove.get(i)!=movesToDoes.get(i)){
					inputPropositions.get(this.currentMove.get(i)).setAndPropagateValue(false);
				}
			}

			this.currentMove = movesToDoes;
		}

		// Set to false also the INIT proposition, if it exists.
		// REMARK: since at the moment the initial state is computed only once at the beginning,
		// the setting of the INIT proposition to FALSE could be done only once after computing the
		// initial state.

		ExtendedStateProposition init = this.propNet.getInitProposition();
		if(init != null){
			this.propNet.getInitProposition().setAndPropagateValue(false);
		}
	}

	/**
	 * Get method for the 'propNet' parameter.
	 *
	 * @return The proposition network.
	 */
	public ExtendedStatePropNet getPropNet(){
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