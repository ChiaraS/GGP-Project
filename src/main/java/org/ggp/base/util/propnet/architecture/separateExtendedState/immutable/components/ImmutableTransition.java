package org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.components;

import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutableComponent;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;

/**
 * The Transition class is designed to represent pass-through gates.
 */
@SuppressWarnings("serial")
public final class ImmutableTransition extends ImmutableComponent{

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
	private final boolean dependingOnInit;

	/**
	 * Constructor that initializes the fact that the value of this transition depends
	 * on the INIT proposition according to the given value.
	 */
	public ImmutableTransition(boolean dependingOnInit) {
		super();
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

	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.util.propnet.architecture.externalizedState.ImmutableComponent#getType()
	 */
	@Override
	public String getComponentType() {
		return "I_TRANSITION";
	}

	@Override
	public void updateValue(boolean newInputValue, ImmutableSeparatePropnetState propnetState) {
		propnetState.flipTransitionValue(this.stateIndex);

		// If the thread calling this method has been interrupted we must stop the execution and throw an
		// InterruptedException otherwise we risk having the PropnetStateMachine trying to impose consistency
		// forever if it gets stuck in a loop of continuously flipping values.
		//ConcurrencyUtils.checkForInterruption();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ggp.base.util.propnet.architecture.externalizedState.ImmutableComponent#toString()
	 */
	@Override
	public String toString(){
		return toDot("box", "grey", "TRANSITION");
	}

	@Override
	public boolean getValue(ImmutableSeparatePropnetState propnetState) {
		return propnetState.getTransitionValue(this.stateIndex);
	}

	@Override
	public void imposeConsistency(ImmutableSeparatePropnetState propnetState) {
		if(this.getInputs().length == 1){
			if(this.getSingleInput().getValue(propnetState) != this.getValue(propnetState)){
				propnetState.flipTransitionValue(this.stateIndex);
				this.isConsistent = true;
			}else{
				this.isConsistent = true;
			}
		}else{
			if(this.getInputs().length == 0)
				throw new IllegalStateException("Detected transition with no inputs in the propnet!");
			else
				throw new IllegalStateException("Detected transition with more than one input in the propnet!");
		}
	}

	@Override
	public void propagateConsistency(boolean newInputValue, ImmutableSeparatePropnetState propnetState) {
		if(this.isConsistent){
			propnetState.flipTransitionValue(this.stateIndex);

			// If the thread calling this method has been interrupted we must stop the execution and throw an
			// InterruptedException otherwise we risk having the PropnetStateMachine trying to impose consistency
			// forever if it gets stuck in a loop of continuously flipping values.
			//ConcurrencyUtils.checkForInterruption();
		}

	}

}