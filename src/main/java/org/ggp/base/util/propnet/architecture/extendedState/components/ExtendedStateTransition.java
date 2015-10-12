package org.ggp.base.util.propnet.architecture.extendedState.components;

import org.ggp.base.util.propnet.architecture.extendedState.ExtendedStateComponent;

/**
 * The Transition class is designed to represent pass-through gates.
 */
@SuppressWarnings("serial")
public final class ExtendedStateTransition extends ExtendedStateComponent
{
	/**
	 * TRUE if this transition's value also depends on the value of the INIT proposition,
	 * FALSE otherwise.
	 *
	 * This is needed to compute the base propositions that are TRUE in the initial state.
	 * Only the ones whose next value depends on a transition that is TRUE because of the INIT
	 * proposition must be included in the initial state.
	 * Since we set by default the values of all components to FALSE, it might happen, when first
	 * imposing consistency on the propnet, that some transitions become true not because of the
	 * INIT proposition but because of other propositions. In this case they do not have to be
	 * included in the initial state.
	 * A simple example where this can happen is the following:
	 * (<= (next b) (not (true b))) ---> when translating this into the propnet, the base proposition
	 * 'b' will have its value initialized to FALSE, when imposing consistency for the first time,
	 * the transition connected to 'b' will have a TRUE value, so when looking for all the base
	 * proposition that will be true in the next state (i.e. the initial state) also this one will
	 * be included, even if it is TRUE just by chance and not because of the INIT proposition.
	 *
	 * ATTENTION: at the moment this value is initialized during propnet creation in the same
	 * moment when (and if) the transition is connected (directly or indirectly) to the INIT
	 * proposition. Not sure if this interferes somehow with the rest of the propnet creation
	 * code.
	 */
	private boolean dependingOnInit;

	/**
	 * Setter for the 'dependsOnInit' variable.
	 *
	 * @param dependsOnInit the value to be assigned to the 'dependsOnInit' variable.
	 */
	public void setDependingOnInit(boolean dependingOnInit){
		this.dependingOnInit = dependingOnInit;
	}

	/**
	 * Getter for the 'dependsOnInit' variable.
	 *
	 * @return TRUE if the value of this transition depends on the value of the INIT proposition,
	 * false otherwise.
	 */
	public boolean isDependingOnInit(){
		return this.dependingOnInit;
	}

	/**
	 * Constructor that initializes to FALSE the fact that the value of this transition
	 * depends on the INIT proposition.
	 */
	public ExtendedStateTransition(){
		this(false);
	}

	/**
	 * Constructor that initializes the fact that the value of this transition
	 * depends on the INIT proposition according to the given value.
	 */
	public ExtendedStateTransition(boolean dependingOnInit){
		super();
		this.dependingOnInit = dependingOnInit;
	}

	/**
	 * Returns the value of the input to the transition.
	 *
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#getValue()
	 */
	@Override
	public boolean getValue()
	{
		return getSingleInput().getValue();
	}

	/**
	 * A transition is always consistent with its input since its value is always recomputed from its single input,
	 * so this method does nothing but changing to TRUE the attribute that states that this component is consistent.
	 *
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#imposeConsistency()
	 */
	@Override
	public void imposeConsistency(){
		this.consistent = true;
	}

	/**
	 * This method does nothing since a transition is supposed to propagate its value only in the next step.
	 *  @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#propagateConsistency()
	 */
	@Override
	public void propagateValue(boolean newValue){

		// If the thread calling this method has been interrupted we must stop the execution and throw an
		// InterruptedException otherwise we risk having the PropnetStateMachine trying to impose consistency
		// forever if it gets stuck in a loop of continuously flipping values.
		//ConcurrencyUtils.checkForInterruption();

	}

	/**
	 * A transition is always consistent with its input since its value is always recomputed from its single input,
	 * so this method does nothing but changing to FALSE the attribute that states that this component is consistent.
	 *
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#resetValue()
	 */
	@Override
	public void resetValue(){
		this.consistent = false;
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("box", "grey", "TRANSITION " + this.getValue());
	}
}