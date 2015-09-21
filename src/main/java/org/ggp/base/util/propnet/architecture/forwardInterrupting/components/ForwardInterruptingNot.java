package org.ggp.base.util.propnet.architecture.forwardInterrupting.components;

import org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent;

/**
 * The Not class is designed to represent logical NOT gates.
 */
@SuppressWarnings("serial")
public final class ForwardInterruptingNot extends ForwardInterruptingComponent
{
	/**
	 * Memorizes the value of the component. This memorization is needed for how the
	 * implementation of the propnet consistency has been done.
	 * This implementation assumes that a component is consistent if its value is consistent
	 * with the one of its direct input. But if such input is a NOT, when the input of the NOT
	 * changes also the value of NOT changes, but if NOT has not yet been set to be consistent
	 * with its input it won't inform its consistent outputs that it changed value.
	 */
	private boolean value;


	public ForwardInterruptingNot(){
		this.value = false;
	}

	/**
	 * Returns the value of this component.
	 *
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#getValue()
	 */
	@Override
	public boolean getValue()
	{
		return this.value;
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#imposeConsistency()
	 */
	@Override
	public void imposeConsistency(){
		ForwardInterruptingComponent component = this.getSingleInput();
		boolean oldValue = this.value;
		// Change the value of this NOT component to be the negation of the value of its input.
		this.value = !component.getValue();
		this.consistent = true;

		// If the value of this NOT changed, propagate it to its outputs.
		if(this.value != oldValue){
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

		//ConcurrencyUtils.checkForInterruption();

		if(this.consistent){
			// If the new value of the input of this NOT is the same as the value of this NOT component, it means that
			// this component must negate it, changing its value, and thus it has to inform all its outputs about this
			// change.
			if(this.value == newValue){
				this.value = !newValue;
				for(ForwardInterruptingComponent c: this.getOutputs()){
					c.propagateValue(this.value);
				}
			}

		}

	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#resetValue()
	 */
	@Override
	public void resetValue(){
		this.value = false;
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