package org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.components;

import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutableComponent;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;

/**
 * The And class is designed to represent logical AND gates.
 */
@SuppressWarnings("serial")
public final class ImmutableAnd extends ImmutableComponent{

	@Override
	public String getComponentType() {
		return "I_AND";
	}

	/**
	 *  @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#propagateConsistency()
	 */
	@Override
	public void updateValue(boolean newInputValue, ImmutableSeparatePropnetState propnetState){

		//ConcurrencyUtils.checkForInterruption();

		// If this method is called, and this component was consistent with its input, it means that
		// one of the inputs this component was consistent with changed value to newValue.
		boolean oldGateValue = propnetState.getGateValue(this.stateIndex);
		if(newInputValue){
			propnetState.incrementTrueInputs(this.stateIndex);
		}else{
			propnetState.decrementTrueInputs(this.stateIndex);
		}

		// If the value of the component changed, inform the outputs that, if they want to keep being
		// consistent, they have to change as well
		boolean newGateValue = propnetState.getGateValue(this.stateIndex);
		if(newGateValue != oldGateValue){
			for(ImmutableComponent o : this.getOutputs()){
				o.updateValue(newGateValue, propnetState);
			}
		}
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#toString()
	 */
	@Override
	public String toString(){
		return toDot("invhouse", "grey", "AND");
	}

	@Override
	public boolean getValue(ImmutableSeparatePropnetState propnetState) {
		return propnetState.getGateValue(this.stateIndex);
	}

	@Override
	public void imposeConsistency(ImmutableSeparatePropnetState propnetState) {
		// Temporarily memorize current value
		boolean oldGateValue = this.getValue(propnetState);
		// Compute the number of inputs that are true for this AND component
		int trueInputs = 0;
		for(ImmutableComponent i : this.getInputs()){
			if(i.getValue(propnetState)){
				trueInputs++;
			}
		}

		propnetState.incrementTrueInputs(this.stateIndex, trueInputs);

		// Memorize that this component is now consistent with its inputs
		this.isConsistent = true;

		boolean currentGateValue = this.getValue(propnetState);

		// If the value of the component changed, inform the consistent outputs
		// that they have to change as well
		if(currentGateValue != oldGateValue){
			for(ImmutableComponent o : this.getOutputs()){
				o.propagateConsistency(currentGateValue, propnetState);
			}
		}
	}

	@Override
	public void propagateConsistency(boolean newInputValue, ImmutableSeparatePropnetState propnetState) {
		if(this.isConsistent){
			//ConcurrencyUtils.checkForInterruption();

			// If this method is called, and this component was consistent with its input, it means that
			// one of the inputs this component was consistent with changed value to newValue.
			boolean oldGateValue = propnetState.getGateValue(this.stateIndex);
			if(newInputValue){
				propnetState.incrementTrueInputs(this.stateIndex);
			}else{
				propnetState.decrementTrueInputs(this.stateIndex);
			}

			// If the value of the component changed, inform the outputs that, if they want to keep being
			// consistent, they have to change as well
			boolean newGateValue = propnetState.getGateValue(this.stateIndex);
			if(newGateValue != oldGateValue){
				for(ImmutableComponent o : this.getOutputs()){
					o.propagateConsistency(newGateValue, propnetState);
				}
			}
		}
	}

	@Override
	public void resetValue(ImmutableSeparatePropnetState propnetState) {
		this.isConsistent = false;
		boolean oldValue = propnetState.getGateValue(this.stateIndex);
		propnetState.resetTrueInputsAnd(this.stateIndex, this.getInputs().length);
		if(oldValue){
			for(ImmutableComponent o : this.getOutputs()){
				o.propagateConsistency(false, propnetState);
			}
		}
	}
}
