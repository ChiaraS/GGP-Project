package org.ggp.base.util.propnet.architecture.selfPropagating.components;

import org.ggp.base.util.propnet.architecture.selfPropagating.ForwardInterruptingComponent;

/**
 * The Transition class is designed to represent pass-through gates.
 */
@SuppressWarnings("serial")
public final class ForwardInterruptingTransition extends ForwardInterruptingComponent
{
	/**
	 * Returns the value of the input to the transition.
	 *
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#getValue()
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
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#imposeConsistency()
	 */
	@Override
	public void imposeConsistency(){
		this.consistent = true;
	}

	/**
	 * This method does nothing since a transition is supposed to propagate its value only in the next step.
	 *  @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#propagateConsistency()
	 */
	@Override
	public void propagateValue(boolean newValue){

	}

	/**
	 * A transition is always consistent with its input since its value is always recomputed from its single input,
	 * so this method does nothing but changing to FALSE the attribute that states that this component is consistent.
	 *
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#resetValue()
	 */
	@Override
	public void resetValue(){
		this.consistent = false;
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("box", "grey", "TRANSITION " + this.getValue());
	}
}