package org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.components;

import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutableComponent;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;

/**
 * The Constant class is designed to represent nodes with fixed logical values.
 */
@SuppressWarnings("serial")
public final class ImmutableConstant extends ImmutableComponent{

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
	public ImmutableConstant(boolean value) {
		super();
		this.value = value;
	}

	public boolean getValue(){
		return this.value;
	}

	@Override
	public String getComponentType(){
		String s;
		if(this.value){
			s = "TRUE ";
		}else{
			s = "FALSE ";
		}
		return s + "I_CONSTANT";
	}

	/**
	 * This method on a constant should never be used, since a constant is supposed not to have inputs
	 * that can change its value.
	 *
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#propagateConsistency()
	 */
	@Override
	public void updateValue(boolean newInputValue, ImmutableSeparatePropnetState propnetState) {
		//ConcurrencyUtils.checkForInterruption();
		throw new IllegalStateException("A costant proposition should have no inputs that can call the updateValue() method!");
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.Immutable.ImmutableComponent#toString()
	 */
	@Override
	public String toString(){
		return toDot("doublecircle", "grey", Boolean.toString(value).toUpperCase());
	}

	/**
	 * Returns the value that the constant was initialized to.
	 *
	 * @see org.ggp.base.util.propnet.architecture.externalizedState.ImmutableComponent#getValue(org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState)
	 */
	@Override
	public boolean getValue(ImmutableSeparatePropnetState propnetState){
		return this.value;
	}

	@Override
	public void imposeConsistency(ImmutableSeparatePropnetState propnetState) {
		this.isConsistent = true;
	}

	@Override
	public void propagateConsistency(boolean newInputValue, ImmutableSeparatePropnetState propnetState) {
		throw new IllegalStateException("A costant proposition should have no inputs that can call the propagateConsistency() method!");
	}

	@Override
	public void resetValue(ImmutableSeparatePropnetState propnetState) {
		throw new IllegalStateException("A costant proposition should have no inputs and thus be in no cycles without necessity of resetting its value!");
	}

}