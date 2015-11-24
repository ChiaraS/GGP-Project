package org.ggp.base.util.propnet.architecture.externalizedState.components;

import org.ggp.base.util.propnet.architecture.externalizedState.ExternalizedStateComponent;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;

/**
 * The Not class is designed to represent logical NOT gates.
 */
@SuppressWarnings("serial")
public final class ExternalizedStateNot extends ExternalizedStateComponent
{
	@Override
	public String getComponentType() {
		return "NOT";
	}

	/**
	 *  @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#propagateConsistency()
	 */
	@Override
	public void updateValue(boolean newInputValue, ImmutableSeparatePropnetState propnetState){

		//ConcurrencyUtils.checkForInterruption();

		// If this method is called it means that the value of the single input of this NOT component
		// has flipped, thus also the value of this component must flip and be propagated to the
		// outputs of this component.
		propnetState.flipOtherValue(this.index);
		for(ExternalizedStateComponent c: this.getOutputs()){
			c.updateValue(!newInputValue, propnetState);
		}
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("invtriangle", "grey", "NOT ");
	}

	@Override
	public boolean getValue(ImmutableSeparatePropnetState propnetState) {
		return propnetState.getOtherValue(this.index);
	}

	@Override
	public void imposeConsistency(ImmutableSeparatePropnetState propnetState) {
		if(this.getInputs().size() == 0){
			throw new IllegalStateException("Detected a NOT component with no inputs in the propnet!");
		}else if(this.getInputs().size() > 1){
			throw new IllegalStateException("Detected a NOT component with more than one input in the propnet!");
		}else{
			boolean inputValue = this.getSingleInput().getValue(propnetState);
			if(!inputValue != this.getValue(propnetState)){ // This value to be consistent must be the negation of its input
				propnetState.flipOtherValue(this.index);
				this.isConsistent = true;
				for(ExternalizedStateComponent c : this.getOutputs()){
					c.propagateConsistency(!inputValue, propnetState);
				}
			}else{
				this.isConsistent = true;
			}
		}
	}

	@Override
	public void propagateConsistency(boolean newInputValue, ImmutableSeparatePropnetState propnetState) {
		if(this.isConsistent){
			//ConcurrencyUtils.checkForInterruption();

			// If this method is called it means that the value of the single input of this NOT component
			// has flipped, thus also the value of this component must flip and be propagated to the
			// outputs of this component.
			propnetState.flipOtherValue(this.index);
			for(ExternalizedStateComponent c: this.getOutputs()){
				c.propagateConsistency(!newInputValue, propnetState);
			}
		}
	}
}