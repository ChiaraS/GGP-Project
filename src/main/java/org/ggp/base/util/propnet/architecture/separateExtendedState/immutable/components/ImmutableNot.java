package org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.components;

import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutableComponent;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;

/**
 * The Not class is designed to represent logical NOT gates.
 */
@SuppressWarnings("serial")
public final class ImmutableNot extends ImmutableComponent{

	@Override
	public String getComponentType() {
		return "I_NOT";
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
		propnetState.flipOtherValue(this.stateIndex);
		for(ImmutableComponent o : this.getOutputs()){
			o.updateValue(!newInputValue, propnetState);
		}
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#toString()
	 */
	@Override
	public String toString(){
		return toDot("invtriangle", "grey", "NOT");
	}

	@Override
	public boolean getValue(ImmutableSeparatePropnetState propnetState){
		return propnetState.getOtherValue(this.stateIndex);
	}

	@Override
	public void imposeConsistency(ImmutableSeparatePropnetState propnetState) {
		if(this.getInputs().length != 1){
			throw new IllegalStateException("Wrong number of inputs for NOT component: it has " + this.getInputs().length + " inputs!");
		}else{
			boolean inputValue = this.getSingleInput().getValue(propnetState);
			if(!inputValue != this.getValue(propnetState)){ // This value to be consistent must be the negation of its input
				propnetState.flipOtherValue(this.stateIndex);
				this.isConsistent = true;
				for(ImmutableComponent o : this.getOutputs()){
					o.propagateConsistency(!inputValue, propnetState);
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
			propnetState.flipOtherValue(this.stateIndex);
			for(ImmutableComponent o : this.getOutputs()){
				o.propagateConsistency(!newInputValue, propnetState);
			}
		}
	}

	@Override
	public void resetValue(ImmutableSeparatePropnetState propnetState) {
		this.isConsistent = false;
		if(propnetState.getOtherValue(this.stateIndex)){
			propnetState.flipOtherValue(this.stateIndex);
			for(ImmutableComponent o : this.getOutputs()){
				o.propagateConsistency(false, propnetState);
			}
		}

	}
}