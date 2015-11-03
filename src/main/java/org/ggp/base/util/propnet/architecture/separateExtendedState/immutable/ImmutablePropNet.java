package org.ggp.base.util.propnet.architecture.separateExtendedState.immutable;

import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.components.ImmutableProposition;
import org.ggp.base.util.statemachine.Role;

public class ImmutablePropNet {

	/********************************** Parameters **********************************/

	/** Ordered references to every component in the PropNet. */
	private final ImmutableComponent[] components;

	/** A helper list of all of the roles. */
	private final Role[] roles;

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

	/********************************** Constructor *********************************/

	public ImmutablePropNet(ImmutableComponent[] components, Role[] roles, ImmutableProposition[] basePropositions, ImmutableProposition[] inputPropositions, int[][] goalValues){
		this.components = components;
		this.roles = roles;
		this.basePropositions = basePropositions;
		this.inputPropositions = inputPropositions;
		this.goalValues = goalValues;

	}

	/****************************** Setters and getters *****************************/

	/**
	 * Getter method.
	 *
	 * @return ordered list of roles.
	 */
	public Role[] getRoles(){
	    return roles;
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
	 * @return References to every InputProposition in the PropNet, grouped by role
	 * and ordered.
	 */
	public ImmutableProposition[] getInputPropositions(){
		return this.inputPropositions;
	}

	/**
	 * Getter method.
	 *
	 * @return The goal values corresponding to each goal proposition, divided by role
	 * 		   and in the same order as the goalsPerRole propositions.
	 */
	public int[][] getGoalValues(){
		return this.goalValues;
	}

}
