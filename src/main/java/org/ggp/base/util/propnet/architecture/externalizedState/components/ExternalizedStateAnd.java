package org.ggp.base.util.propnet.architecture.externalizedState.components;

import org.ggp.base.util.propnet.architecture.externalizedState.ExternalizedStateComponent;
import org.ggp.base.util.propnet.state.ExternalPropnetState;

/**
 * The And class is designed to represent logical AND gates.
 */
@SuppressWarnings("serial")
public final class ExternalizedStateAnd extends ExternalizedStateComponent{

	@Override
	public String getComponentType() {
		return "AND";
	}

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

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("invhouse", "grey", "AND ");
	}

	@Override
	public boolean getValue(ExternalPropnetState propnetState) {
		return propnetState.getGateValue(this.index);
	}

	@Override
	public void imposeConsistency(ExternalPropnetState propnetState) {
		// Temporarily memorize current value
		boolean oldGateValue = this.getValue(propnetState);
		// Compute the number of inputs that are true for this AND component
		int trueInputs = 0;
		for(ExternalizedStateComponent c: this.getInputs()){
			if(c.getValue(propnetState)){
				trueInputs++;
			}
		}

		propnetState.incrementTrueInputs(this.index, trueInputs);

		// Memorize that this component is now consistent with its inputs
		this.isConsistent = true;

		boolean currentGateValue = this.getValue(propnetState);

		// If the value of the component changed, inform the consistent outputs
		// that they have to change as well
		if(currentGateValue != oldGateValue){
			for(ExternalizedStateComponent c: this.getOutputs()){
				c.propagateConsistency(currentGateValue, propnetState);
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
					c.updateValue(newGateValue, propnetState);
				}
			}
		}
	}
}
