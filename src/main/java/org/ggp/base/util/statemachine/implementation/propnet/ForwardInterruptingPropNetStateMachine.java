package org.ggp.base.util.statemachine.implementation.propnet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.selfPropagating.ForwardInterruptingPropNet;
import org.ggp.base.util.propnet.architecture.selfPropagating.components.ForwardInterruptingProposition;
import org.ggp.base.util.propnet.factory.ForwardInterruptingPropNetFactory;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.query.ProverQueryBuilder;


@SuppressWarnings("unused")
public class ForwardInterruptingPropNetStateMachine extends StateMachine {
    /** The underlying proposition network  */
    private ForwardInterruptingPropNet propNet;
    /** The player roles */
    private List<Role> roles;
    /** The initial state */
    private MachineState initialState;

    /**
     * Initializes the PropNetStateMachine. You should compute the topological
     * ordering here. Additionally you may compute the initial state here, at
     * your discretion.
     */
    @Override
    public void initialize(List<Gdl> description) {
        try {
			this.propNet = ForwardInterruptingPropNetFactory.create(description);
	        this.roles = propNet.getRoles();
	        // Set init proposition to true without propagating, so that when making
	        // the propnet consistent its value will be propagated so that the next state
	        // will correspond to the initial state.
	        this.propNet.getInitProposition().setValue(true);
	        // No need to set all other inputs to false because they already are.
	        // Impose consistency on the propnet.
	        this.propNet.imposeConsistency();
	        this.initialState = this.computeInitialState();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
    }

    /**
     * The initial state can be computed by only setting the truth value of the INIT
     * proposition to TRUE, and then computing the resulting state.
     * This method assumes that the INIT proposition has already been set to TRUE and
     * all other input propositions are set to FALSE. This method can make this assumption
     * cause it must be called only once during the initialization of the propnet state
     * machine, where all the propositions are already set to FALSE and the INIT proposition
     * has already been set to TRUE (without propagation).
	 *
     * @return the initial state.
     */
    private MachineState computeInitialState(){
    	Set<GdlSentence> contents = new HashSet<GdlSentence>();
		for (ForwardInterruptingProposition p : this.propNet.getBasePropositions().values()){
			if (p.getSingleInput().getValue()){
				contents.add(p.getName());
			}
		}
		return new MachineState(contents);
    }

	/**
	 * Computes if the state is terminal. Should return the value
	 * of the terminal proposition for the state.
	 */
	@Override
	public boolean isTerminal(MachineState state) {
		this.markBases(state);
		return this.propNet.getTerminalProposition().getValue();
	}

	/**
	 * Computes the goal for a role in the current state.
	 * Should return the value of the goal proposition that
	 * is true for that role. If there is not exactly one goal
	 * proposition true for that role, then you should throw a
	 * GoalDefinitionException because the goal is ill-defined.
	 */
	@Override
	public int getGoal(MachineState state, Role role)
	throws GoalDefinitionException {
		// Mark base propositions according to state.
		this.markBases(state);

		// Get all goal propositions for the given role.
		Set<ForwardInterruptingProposition> goalPropsForRole = this.propNet.getGoalPropositions().get(role);

		ForwardInterruptingProposition trueGoal = null;

		// Check all the goal propositions that are true for the role. If there is more than one throw an exception.
		for(ForwardInterruptingProposition goalProp : goalPropsForRole){
			if(goalProp.getValue()){
				if(trueGoal != null){
					GamerLogger.logError("StateMachine", "[Propnet] Got more than one true goal in state " + state + " for role " + role + ".");
					throw new GoalDefinitionException(state, role);
				}else{
					trueGoal = goalProp;
				}
			}
		}

		// If there is no true goal proposition for the role in this state throw an exception.
		if(trueGoal == null){
			GamerLogger.logError("StateMachine", "[Propnet] Got no true goal in state " + state + " for role " + role + ".");
			throw new GoalDefinitionException(state, role);
		}

		// Return the single goal for the given role in the given state.
		return this.getGoalValue(trueGoal);
	}

	/**
	 * Returns the initial state. If the initial state has not been computed yet because
	 * this state machine has not been initialized, NULL will be returned.
	 */
	@Override
	public MachineState getInitialState() {
		return this.initialState;
	}

	/**
	 * Computes the legal moves for role in state.
	 */
	@Override
	public List<Move> getLegalMoves(MachineState state, Role role)
	throws MoveDefinitionException {
		// Mark base propositions according to state.
		this.markBases(state);

		// Retrieve all legal propositions for the given role.
		Set<ForwardInterruptingProposition> legalPropsForRole = this.propNet.getLegalPropositions().get(role);

		// Create the list of legal moves.
		List<Move> legalMovesForRole = new ArrayList<Move>();
		for(ForwardInterruptingProposition legalProp : legalPropsForRole){
			if(legalProp.getValue()){
				legalMovesForRole.add(this.getMoveFromProposition(legalProp));
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
	public MachineState getNextState(MachineState state, List<Move> moves)
	throws TransitionDefinitionException {
		// Mark base propositions according to state.
		this.markBases(state);

		// Mark input propositions according to the moves
		this.markInputs(moves);

		// TODO
		return null;
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
		List<GdlSentence> doeses = new ArrayList<GdlSentence>(moves.size());
		Map<Role, Integer> roleIndices = getRoleIndices();

		// TODO: both the roles and the moves should be already in the correct order so no need
		// to retrieve the roles indices!!
		for (int i = 0; i < roles.size(); i++)
		{
			int index = roleIndices.get(roles.get(i));
			doeses.add(ProverQueryBuilder.toDoes(roles.get(i), moves.get(index)));
		}
		return doeses;
	}

	/**
	 * Takes in a Legal Proposition and returns the appropriate corresponding Move.
	 *
	 * @param p the proposition to be transformed into a move.
	 * @return the legal move corresponding to the given proposition.
	 */
	public static Move getMoveFromProposition(ForwardInterruptingProposition p){
		return new Move(p.getName().get(1));
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
	private void markBases(MachineState state){
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
     * @param moves the moves to be set as performed in the propnet.
     */
	private void markInputs(List<Move> moves){
		// Transform the moves into 'does' propositions (the correct order should be kept).
		List<GdlSentence> movesToDoes = this.toDoes(moves);

		for(ForwardInterruptingProposition input : this.propNet.getInputPropositions().values()){
			input.setAndPropagateValue(movesToDoes.contains(input.getName()));
		}

	}
}