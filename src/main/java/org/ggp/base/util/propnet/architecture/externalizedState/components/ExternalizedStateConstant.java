package org.ggp.base.util.propnet.architecture.externalizedState.components;

import org.ggp.base.util.propnet.architecture.externalizedState.ExternalizedStateComponent;
import org.ggp.base.util.propnet.state.ExternalPropnetState;

/**
 * The Constant class is designed to represent nodes with fixed logical values.
 */
@SuppressWarnings("serial")
public final class ExternalizedStateConstant extends ExternalizedStateComponent
{
	/**
	 * The value of the constant.
	 * Needed to distinguish if it is a TRUE or FALSE constant.
	 * Cannot be changed.
	 */
	private final boolean value;

	/**
	 * Creates a new Constant with value <tt>value</tt>.
	 *
	 * @param value
	 *            The value of the Constant.
	 */
	public ExternalizedStateConstant(boolean value){
		this.value = value;
	}

	/**
	 * Returns the value that the constant was initialized to.
	 *
	 * @see org.ggp.base.util.propnet.architecture.ExternalizedState.ExternalizedStateComponent#getValue()
	 */
	public boolean getValue()
	{
		return value;
	}

	@Override
	public String getComponentType() {
		String s;
		if(this.value){
			s = "TRUE ";
		}else{
			s = "FALSE ";
		}
		return s + "CONSTANT";
	}

	/**
	 * This method on a constant should never be used, since a constant is supposed not to have inputs
	 * that can change its value.
	 *
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#propagateConsistency()
	 */
	@Override
	public void updateValue(boolean newInputValue, ExternalPropnetState propnetState) {
		//ConcurrencyUtils.checkForInterruption();
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExternalizedState.ExternalizedStateComponent#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("doublecircle", "grey", Boolean.toString(value).toUpperCase());
	}

}