package org.ggp.base.util.propnet.architecture.extendedState.components;

import org.ggp.base.util.propnet.architecture.extendedState.ExtendedStateComponent;

/**
 * The Not class is designed to represent logical NOT gates.
 */
@SuppressWarnings("serial")
public final class ExtendedStateNot extends ExtendedStateComponent
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


	public ExtendedStateNot(){
		this.value = false;
	}

	/**
	 * Returns the value of this component.
	 *
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#getValue()
	 */
	@Override
	public boolean getValue()
	{
		return this.value;
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#imposeConsistency()
	 */
	@Override
	public void imposeConsistency(){
		ExtendedStateComponent component = this.getSingleInput();
		boolean oldValue = this.value;
		// Change the value of this NOT component to be the negation of the value of its input.
		this.value = !component.getValue();
		this.consistent = true;

		// If the value of this NOT changed, propagate it to its outputs.
		if(this.value != oldValue){
			for(ExtendedStateComponent c: this.getOutputs()){
				c.propagateValue(this.getValue());
			}
		}
	}

	/**
	 *  @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#propagateConsistency()
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
				for(ExtendedStateComponent c: this.getOutputs()){
					c.propagateValue(this.value);
				}
			}

		}

	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#resetValue()
	 */
	@Override
	public void resetValue(){
		this.value = false;
		this.consistent = false;
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("invtriangle", "grey", "NOT " + this.getValue());
	}
}