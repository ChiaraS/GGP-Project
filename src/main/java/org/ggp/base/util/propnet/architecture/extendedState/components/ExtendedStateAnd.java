package org.ggp.base.util.propnet.architecture.extendedState.components;

import org.ggp.base.util.propnet.architecture.extendedState.ExtendedStateComponent;

/**
 * The And class is designed to represent logical AND gates.
 */
@SuppressWarnings("serial")
public final class ExtendedStateAnd extends ExtendedStateComponent
{

	/**
	 * Number of inputs of this AND component that are TRUE.
	 */
	private int trueInputs;

	/**
	 * Returns true if and only if every input to the and is true.
	 *
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#getValue()
	 */
	@Override
	public boolean getValue()
	{
		if(this.getInputs() != null){
			return this.trueInputs == this.getInputs().size();
		}

		return true;
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#imposeConsistency()
	 */
	@Override
	public void imposeConsistency(){
		// Temporarily memorize current value
		boolean oldValue = this.getValue();
		// Reset and recompute the number of inputs that are true for this AND
		// component
		this.trueInputs = 0;
		for(ExtendedStateComponent c: this.getInputs()){
			if(c.getValue()){
				this.trueInputs++;
			}
		}
		// Memorize that this component is now consistent with its inputs
		this.consistent = true;
		// If the value of the component changed, inform the consistent outputs
		// that they have to change as well
		if(this.getValue() != oldValue){
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
			// If this method is called, and this component was consistent with its input, it means that
			// one of the inputs this component was consistent with changed value to newValue.
			boolean oldValue = this.getValue();
			if(newValue){
				this.trueInputs++;
			}else{
				this.trueInputs--;
			}
			// If the value of the component changed, inform the outputs that, if they want to keep being
			// consistent, they have to change as well
			if(this.getValue() != oldValue){
				for(ExtendedStateComponent c: this.getOutputs()){
					c.propagateValue(this.getValue());
				}
			}
		}

	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#resetValue()
	 */
	@Override
	public void resetValue(){
		this.trueInputs = 0;
		this.consistent = false;
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("invhouse", "grey", "AND " + this.trueInputs);
	}

}
