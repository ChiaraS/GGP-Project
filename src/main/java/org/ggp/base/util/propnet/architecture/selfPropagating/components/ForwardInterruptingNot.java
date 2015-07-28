package org.ggp.base.util.propnet.architecture.selfPropagating.components;

import org.ggp.base.util.propnet.architecture.selfPropagating.ForwardInterruptingComponent;

/**
 * The Not class is designed to represent logical NOT gates.
 */
@SuppressWarnings("serial")
public final class ForwardInterruptingNot extends ForwardInterruptingComponent
{
	/**
	 * Returns the inverse of the input to the not.
	 *
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#getValue()
	 */
	@Override
	public boolean getValue()
	{
		return !getSingleInput().getValue();
	}

	/**
	 * A NOT is always consistent with its input since its value is always recomputed from its single input,
	 * so this method does nothing but changing to FALSE the attribute that states that this component is consistent.
	 *
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#imposeConsistency()
	 */
	@Override
	public void imposeConsistency(){
		this.consistent = true;
	}

	/**
	 *  @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#propagateConsistency()
	 */
	@Override
	public void propagateValue(boolean newValue){
		if(this.isConsistent()){
			// If this method is called, and this component was consistent with its input, it means that the
			// input this component was consistent with changed value to newValue, so automatically this
			// component changed value to !newValue and has to inform its outputs about it.
			for(ForwardInterruptingComponent c: this.getOutputs()){
				c.propagateValue(!newValue);
			}
		}

	}

	/**
	 * A NOT is always consistent with its input since its value is always recomputed from its single input,
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
		return toDot("invtriangle", "grey", "NOT " + this.getValue());
	}
}