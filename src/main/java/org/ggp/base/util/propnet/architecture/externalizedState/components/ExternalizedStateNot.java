package org.ggp.base.util.propnet.architecture.externalizedState.components;

import org.ggp.base.util.propnet.architecture.externalizedState.ExternalizedStateComponent;
import org.ggp.base.util.propnet.state.ExternalPropnetState;

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
	public void updateValue(boolean newInputValue, ExternalPropnetState propnetState){

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
}