package org.ggp.base.util.propnet.architecture.forwardInterrupting.components;

import org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent;

/**
 * The Or class is designed to represent logical OR gates.
 */
@SuppressWarnings("serial")
public final class ForwardInterruptingOr extends ForwardInterruptingComponent
{
	/**
	 * Number of inputs of this OR component that are TRUE.
	 */
	private int trueInputs;

	public ForwardInterruptingOr(){
		super();
		this.trueInputs = 0;
	}

	/**
	 * Returns true if and only if at least one of the inputs to the or is true.
	 *
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#getValue()
	 */
	@Override
	public boolean getValue()
	{
		return this.trueInputs > 0;
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#imposeConsistency()
	 */
	@Override
	public void imposeConsistency(){
		// Temporarily memorize current value
		boolean oldValue = this.getValue();
		// Reset and recompute the number of inputs that are true for this OR
		// component
		this.trueInputs = 0;
		for(ForwardInterruptingComponent c: this.getInputs()){
			if(c.getValue()){
				this.trueInputs++;
			}
		}
		// Memorize that this component is now consistent with its inputs
		this.consistent = true;
		// If the value of the component changed, inform the consistent outputs
		// that they have to change as well
		if(this.getValue() != oldValue){
			for(ForwardInterruptingComponent c: this.getOutputs()){
				c.propagateValue(this.getValue());
			}
		}
	}

	/**
	 *  @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#propagateConsistency()
	 */
	@Override
	public void propagateValue(boolean newValue){
		if(this.consistent){
			// If this method is called, and this component was consistent with its input, it means that
			// one of the inputs this component was consistent with changed value to newValue.
			boolean oldValue = this.getValue();
			if(newValue){
				this.trueInputs++;
			}else{
				this.trueInputs--;
			}
			// If the value of the component changed, inform the consistent outputs
			// that they have to change as well
			if(this.getValue() != oldValue){
				for(ForwardInterruptingComponent c: this.getOutputs()){
					c.propagateValue(this.getValue());
				}
			}
		}

	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#resetValue()
	 */
	@Override
	public void resetValue(){
		this.trueInputs = 0;
		this.consistent = false;
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("ellipse", "grey", "OR " + this.trueInputs);
	}
}