package org.ggp.base.util.propnet.architecture.externalizedState.components;

import org.ggp.base.util.propnet.architecture.externalizedState.ExternalizedStateComponent;
import org.ggp.base.util.propnet.state.ExternalPropnetState;

/**
 * The Or class is designed to represent logical OR gates.
 */
@SuppressWarnings("serial")
public final class ExternalizedStateOr extends ExternalizedStateComponent
{
	/**
	 *  @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#propagateConsistency()
	 */
	@Override
	public void updateValue(boolean newInputValue, ExternalPropnetState propnetState){

		//ConcurrencyUtils.checkForInterruption();

		// If this method is called, and this component was consistent with its input, it means that
		// one of the inputs this component was consistent with changed value to newValue.
		boolean oldGateValue = propnetState.getGateValue(this.index);
		if(newInputValue){
			propnetState.incrementTrueInputs(this.index);
		}else{
			propnetState.decrementTrueInputs(this.index);
		}

		// If the value of the component changed, inform the outputs that, if they want to keep being
		// consistent, they have to change as well
		boolean newGateValue = propnetState.getGateValue(this.index);
		if(newGateValue != oldGateValue){
			for(ExternalizedStateComponent c: this.getOutputs()){
				c.updateValue(newGateValue, propnetState);
			}
		}

	}

	@Override
	public String getComponentType() {
		return "OR";
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("ellipse", "grey", "OR ");
	}

	@Override
	public boolean getValue(ExternalPropnetState propnetState) {
		return propnetState.getGateValue(this.index);
	}

	@Override
	public void imposeConsistency(ExternalPropnetState propnetState) {
		// Temporarily memorize current value
		boolean oldGateValue = this.getValue(propnetState);
		// Compute the number of inputs that are true for this OR component
		int trueInputs = 0;
		for(ExternalizedStateComponent c: this.getInputs()){
			if(c.getValue(propnetState)){
				trueInputs++;
			}
		}

		// Increment the counter for the gate
		propnetState.incrementTrueInputs(this.index, trueInputs);
		// Memorize that this component is now consistent with its inputs
		this.isConsistent = true;

		boolean newGateValue = this.getValue(propnetState);
		// If the value of the component changed, inform the consistent outputs
		// that they have to change as well
		if(newGateValue != oldGateValue){
			for(ExternalizedStateComponent c: this.getOutputs()){
				c.propagateConsistency(newGateValue, propnetState);
			}
		}

	}

	@Override
	public void propagateConsistency(boolean newInputValue, ExternalPropnetState propnetState) {
		if(this.isConsistent){
			//ConcurrencyUtils.checkForInterruption();

			// If this method is called, and this component was consistent with its input, it means that
			// one of the inputs this component was consistent with changed value to newValue.
			boolean oldGateValue = propnetState.getGateValue(this.index);
			if(newInputValue){
				propnetState.incrementTrueInputs(this.index);
			}else{
				propnetState.decrementTrueInputs(this.index);
			}

			// If the value of the component changed, inform the outputs that, if they want to keep being
			// consistent, they have to change as well
			boolean newGateValue = propnetState.getGateValue(this.index);
			if(newGateValue != oldGateValue){
				for(ExternalizedStateComponent c: this.getOutputs()){
					c.propagateConsistency(newGateValue, propnetState);
				}
			}
		}

	}

}