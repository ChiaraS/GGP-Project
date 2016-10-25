package org.ggp.base.util.statemachine.implementation.propnet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingPropNet;
import org.ggp.base.util.propnet.architecture.forwardInterrupting.components.ForwardInterruptingProposition;
import org.ggp.base.util.propnet.architecture.forwardInterrupting.components.ForwardInterruptingTransition;
import org.ggp.base.util.propnet.factory.ForwardInterruptingPropNetFactory;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.query.ProverQueryBuilder;
import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;

import com.google.common.collect.ImmutableList;


public class FwdInterrPropnetStateMachine extends StateMachine {
    /** The underlying proposition network  */
    private ForwardInterruptingPropNet propNet;
    /** The player roles */
    private ImmutableList<ProverRole> roles;
    /** The initial state */
    private ProverMachineState initialState;

    /** The currently set move */
    private List<GdlSentence> currentMove;

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
    		this.propNet = ForwardInterruptingPropNetFactory.create(description);
    	}catch(InterruptedException e){
    		GamerLogger.logError("StateMachine", "[Propnet] Propnet creation interrupted!");
    		GamerLogger.logStackTrace("StateMachine", e);
    		throw new StateMachineInitializationException(e);
    	}
    	// Compute the time taken to construct the propnet
    	this.propnetConstructionTime = System.currentTimeMillis() - startTime;
		GamerLogger.log("StateMachine", "[Propnet Creator] Propnet creation done. It took " + (this.propnetConstructionTime) + "ms.");


		System.out.println("Propnet has: " + propNet.getSize() + " COMPONENTS, " + propNet.getNumPropositions() + " PROPOSITIONS, " + propNet.getNumConstants() + " CONSTANTS, " + propNet.getNumLinks() + " LINKS.");
		System.out.println("Propnet has: " + propNet.getNumAnds() + " ANDS, " + propNet.getNumOrs() + " ORS, " + propNet.getNumNots() + " NOTS.");
		System.out.println("Propnet has: " + propNet.getNumBases() + " BASES, " + propNet.getNumTransitions() + " TRANSITIONS.");
		System.out.println("Propnet has: " + propNet.getNumInputs() + " INPUTS, " + propNet.getNumLegals() + " LEGALS.");
		System.out.println("Propnet has: " + propNet.getNumGoals() + " GOALS.");
		//System.out.println("Propnet has: " + propNet.getNumOthers() + " OTHER PROPOSITIONS.");
		//System.out.println("Propnet has: " + propNet.getNumInits() + " INITS, " + propNet.getNumTerminals() + " TERMINALS.");






		GamerLogger.log("StateMachine", "Removing unreachable bases and inputs.");
		startTime = System.currentTimeMillis();
		Collection<ForwardInterruptingProposition> baseProps = this.propNet.getBasePropositions().values();

		Set<ForwardInterruptingProposition> basesTrueByInit = new HashSet<ForwardInterruptingProposition>();

		for(ForwardInterruptingProposition p : baseProps){
			if(((ForwardInterruptingTransition)p.getSingleInput()).isDependingOnInit()){
				basesTrueByInit.add(p);
			}
		}

		try {
			ForwardInterruptingPropNetFactory.removeUnreachableBasesAndInputs(this.propNet, basesTrueByInit);
			//System.out.println("Done removing unreachable bases and inputs; took " + (System.currentTimeMillis() - startTime) + "ms, propnet has " + propNet.getComponents().size() + " components and " + propNet.getNumLinks() + " links");
			//System.out.println("Propnet has " +propNet.getNumAnds()+" ands; "+propNet.getNumOrs()+" ors; "+propNet.getNumNots()+" nots");
			//System.out.println("Propnet has " +propNet.getNumBases() + " bases; "+propNet.getNumTransitions()+" transitions; "+propNet.getNumInputs()+" inputs");

			System.out.println("Done removing unreachable bases and inputs; took " + (System.currentTimeMillis() - startTime) + "ms.");
			System.out.println("Propnet has: " + propNet.getSize() + " COMPONENTS, " + propNet.getNumPropositions() + " PROPOSITIONS, " + propNet.getNumConstants() + " CONSTANTS, " + propNet.getNumLinks() + " LINKS.");
			System.out.println("Propnet has: " + propNet.getNumAnds() + " ANDS, " + propNet.getNumOrs() + " ORS, " + propNet.getNumNots() + " NOTS.");
			System.out.println("Propnet has: " + propNet.getNumBases() + " BASES, " + propNet.getNumTransitions() + " TRANSITIONS.");
			System.out.println("Propnet has: " + propNet.getNumInputs() + " INPUTS, " + propNet.getNumLegals() + " LEGALS.");
			System.out.println("Propnet has: " + propNet.getNumGoals() + " GOALS.");


		} catch (InterruptedException e) {
			GamerLogger.logError("StateMachine", "[Propnet] Removal of unreachable bases and inputs interrupted!");
    		GamerLogger.logStackTrace("StateMachine", e);
    		throw new StateMachineInitializationException(e);
		}








		// Compute the roles
   		this.roles = ImmutableList.copyOf(this.propNet.getRoles());
        // If it exists, set init proposition to true without propagating, so that when making
		// the propnet consistent its value will be propagated so that the next state
		// will correspond to the initial state.
	    // REMARK: if there is not TRUE proposition in the initial state, the INIT proposition
	    // will not exist.
	    ForwardInterruptingProposition init = this.propNet.getInitProposition();
	    if(init != null){
	    	this.propNet.getInitProposition().setValue(true);

	    }

	    // Set that there is no joint move currently set to true
	    this.currentMove = new ArrayList<GdlSentence>();

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
			this.initialState = new ProverMachineState(new HashSet<GdlSentence>());
		}
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
    private ProverMachineState computeInitialState(){

    	//AGGIUNTA
    	//System.out.println("COMPUTING INIT STATE");
    	//FINE AGGIUNTA

    	Set<GdlSentence> contents = new HashSet<GdlSentence>();

    	// Add to the initial machine state all the base propositions that are connected to a true transition
    	// whose value also depends on the value of the INIT proposition.
		for (ForwardInterruptingProposition p : this.propNet.getBasePropositions().values()){
			// Get the transition (We can be sure that when getting the single input of a base proposition we get a
			// transition, right?)
			ForwardInterruptingTransition transition = ((ForwardInterruptingTransition) p.getSingleInput());
			if (transition.getValue() && transition.isDependingOnInit()){
				contents.add(p.getName());
			}
		}

		return new ProverMachineState(contents);
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
    private ProverMachineState computeNextState(){

    	//AGGIUNTA
    	//System.out.println("COMPUTING NEXT STATE");
    	//FINE AGGIUNTA

    	Set<GdlSentence> contents = new HashSet<GdlSentence>();

    	// For all the base propositions that are true, add the corresponding proposition to the
    	// next machine state.
		for (ForwardInterruptingProposition p : this.propNet.getBasePropositions().values()){
			if (p.getSingleInput().getValue()){
				contents.add(p.getName());
			}
		}

		return new ProverMachineState(contents);
    }

	/**
	 * Computes if the state is terminal. Should return the value
	 * of the terminal proposition for the state.
	 */
	@Override
	public boolean isTerminal(ProverMachineState state) {
		this.markBases(state);
		return this.propNet.getTerminalProposition().getValue();
	}

	/**
	 * Computes the goals for a role in the current state.
	 * Should return the value of the goal propositions that
	 * are true for that role.
	 */
	@Override
	public List<Integer> getOneRoleGoals(ProverMachineState state, ProverRole role) {
		// Mark base propositions according to state.
		this.markBases(state);

		// Get all goal propositions for the given role.
		Set<ForwardInterruptingProposition> goalPropsForRole = this.propNet.getGoalPropositions().get(role);

		List<Integer> trueGoals = new ArrayList<Integer>();

		// Check all the goal propositions that are true for the role. If there is more than one throw an exception.
		for(ForwardInterruptingProposition goalProp : goalPropsForRole){
			if(goalProp.getValue()){
				trueGoals.add(this.getGoalValue(goalProp));
			}
		}

		if(trueGoals.size() > 1){
			GamerLogger.logError("StateMachine", "[Propnet] Got more than one true goal in state " + state + " for role " + role + ".");
		}

		// If there is no true goal proposition for the role in this state throw an exception.
		if(trueGoals.isEmpty()){
			GamerLogger.logError("StateMachine", "[Propnet] Got no true goal in state " + state + " for role " + role + ".");
		}

		// Return the single goal for the given role in the given state.
		return trueGoals;
	}

	/**
	 * Returns the initial state. If the initial state has not been computed yet because
	 * this state machine has not been initialized, NULL will be returned.
	 */
	@Override
	public ProverMachineState getInitialState() {
		return this.initialState;
	}

	/**
	 * Computes the legal moves for role in state.
	 */
	@Override
	public List<ProverMove> getLegalMoves(ProverMachineState state, ProverRole role)
	throws MoveDefinitionException {
		// Mark base propositions according to state.
		this.markBases(state);

		// Retrieve all legal propositions for the given role.
		Set<ForwardInterruptingProposition> legalPropsForRole = this.propNet.getLegalPropositions().get(role);

		// Create the list of legal moves.
		List<ProverMove> legalMovesForRole = new ArrayList<ProverMove>();
		for(ForwardInterruptingProposition legalProp : legalPropsForRole){
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
	 */
	@Override
	public ProverMachineState getNextState(ProverMachineState state, List<ProverMove> moves)
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
	public List<ProverRole> getRoles() {
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
	private List<GdlSentence> toDoes(List<ProverMove> moves){

		//AGGIUNTA
    	//System.out.println("TO DOES");
    	//System.out.println("MOVES");
    	//System.out.println(moves);
    	//FINE AGGIUNTA

		List<GdlSentence> doeses = new ArrayList<GdlSentence>(moves.size());
		Map<ProverRole, Integer> roleIndices = getRoleIndices();

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
	public static ProverMove getMoveFromProposition(ForwardInterruptingProposition p){
		return new ProverMove(p.getName().get(1));
	}

	/**
	 * Helper method for parsing the value of a goal proposition
	 * @param goalProposition
	 * @return the integer value of the goal proposition
	 */
    private int getGoalValue(ForwardInterruptingProposition goalProposition){
		GdlRelation relation = (GdlRelation) goalProposition.getName();
		GdlConstant constant = (GdlConstant) relation.get(1);
		return Integer.parseInt(constant.toString());
	}

    /**
     * Marks the base propositions with the correct value so that the propnet state resembles the
     * given machine state. This method works only if the GdlPool is being used.
     *
     * @param state the machine state to be set in the propnet.
     */
	private void markBases(ProverMachineState state){

		//AGGIUNTA
    	//System.out.println("MARKING BASES");
    	//FINE AGGIUNTA

		Set<GdlSentence> contents = state.getContents();
		for(ForwardInterruptingProposition base : this.propNet.getBasePropositions().values()){
			base.setAndPropagateValue(contents.contains(base.getName()));
		}
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
	private void markInputs(List<ProverMove> moves){

		//AGGIUNTA
    	//System.out.println("MARKING INPUTS");
    	//FINE AGGIUNTA

		// Transform the moves into 'does' propositions (the correct order should be kept).
		List<GdlSentence> movesToDoes = this.toDoes(moves);

		// Check if the currently set move is different and we have to change it,
		// or if it's the same so we have nothing to do.
		if(!movesToDoes.equals(this.currentMove)){

			// Get all input propositions
			Map<GdlSentence, ForwardInterruptingProposition> inputPropositions = this.propNet.getInputPropositions();
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

		ForwardInterruptingProposition init = this.propNet.getInitProposition();
		if(init != null){
			this.propNet.getInitProposition().setAndPropagateValue(false);
		}
	}

	/**
	 * Get method for the 'propNet' parameter.
	 *
	 * @return The proposition network.
	 */
	public ForwardInterruptingPropNet getPropNet(){
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