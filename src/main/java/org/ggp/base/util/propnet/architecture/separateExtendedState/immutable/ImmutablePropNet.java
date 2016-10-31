package org.ggp.base.util.propnet.architecture.separateExtendedState.immutable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.components.ImmutableProposition;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

public class ImmutablePropNet implements Serializable{

	/********************************** Parameters **********************************/

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** Ordered references to every component in the PropNet. */
	private final ImmutableComponent[] components;

	/** A helper list of all of the roles. */
	private final ExplicitRole[] roles;

	/**
	 * References to every BaseProposition in the PropNet.
	 * Corresponds to the current state in the ExternalPropnetState class.
	 * This list and the current and next state in the ExternalPropnetState class all
	 * have the elements in the same order.
	 */
	private final ImmutableProposition[] basePropositions;

	/**
	 * References to every InputProposition in the PropNet, ordered by role.
	 * Roles are in the same order as in the roles list.
	 * The order is the same as the values of the currentJointMove in the ExternalPropnetState class.
	 */
	private final ImmutableProposition[] inputPropositions;

	/**
	 * List of all the goals that corresponds to a goal proposition, grouped by role
	 * and listed in the same order as the role propositions values in the ExternalPropnetState class.
	 */
	private final int[][] goalValues;

	/**
	 * This set keeps track of the GDL propositions corresponding to base propositions that have
	 * been removed because always true. These are needed when converting an internal representation
	 * of a game state to the standard representation of GGP-Base.
	 */
	private final Set<GdlSentence> alwaysTrueBases;

	/********************************** Constructor *********************************/

	public ImmutablePropNet(ImmutableComponent[] components, ExplicitRole[] roles, ImmutableProposition[] basePropositions, ImmutableProposition[] inputPropositions, int[][] goalValues, Set<GdlSentence> alwaysTrueBases){
		this.components = components;
		this.roles = roles;
		this.basePropositions = basePropositions;
		this.inputPropositions = inputPropositions;
		this.goalValues = goalValues;
		this.alwaysTrueBases = alwaysTrueBases;
	}

	/****************************** Setters and getters *****************************/

	/**
	 * Getter method.
	 *
	 * @return ordered array of components.
	 */
	public ImmutableComponent[] getComponents(){
	    return this.components;
	}

	/**
	 * Getter method.
	 *
	 * @return ordered array of roles.
	 */
	public ExplicitRole[] getRoles(){
	    return this.roles;
	}

	/**
	 * Getter method.
	 *
	 * @return references to every BaseProposition in the PropNet in the correct order.
	 */
	public ImmutableProposition[] getBasePropositions(){
		return this.basePropositions;
	}

	/**
	 * Getter method.
	 *
	 * @return references to every InputProposition in the PropNet, grouped by role
	 * and ordered.
	 */
	public ImmutableProposition[] getInputPropositions(){
		return this.inputPropositions;
	}

	/**
	 * Getter method.
	 *
	 * @return the goal values corresponding to each goal proposition, divided by role
	 * 		   and in the same order as the goalsPerRole propositions.
	 */
	public int[][] getGoalValues(){
		return this.goalValues;
	}

	/**
	 * Getter method.
	 *
	 * @return a copy of the list with all the Gdl base propositions that are true in
	 * every state and thus have been removed from the propnet.
	 */
	public Set<GdlSentence> getAlwaysTrueBases(){
		return new HashSet<GdlSentence>(this.alwaysTrueBases);
	}

}
