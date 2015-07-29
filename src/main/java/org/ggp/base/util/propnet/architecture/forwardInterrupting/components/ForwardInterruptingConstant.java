package org.ggp.base.util.propnet.architecture.forwardInterrupting.components;

import org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent;

/**
 * The Constant class is designed to represent nodes with fixed logical values.
 */
@SuppressWarnings("serial")
public final class ForwardInterruptingConstant extends ForwardInterruptingComponent
{
	/** The value of the constant. */
	private final boolean value;

	/**
	 * Creates a new Constant with value <tt>value</tt>.
	 *
	 * @param value
	 *            The value of the Constant.
	 */
	public ForwardInterruptingConstant(boolean value)
	{
		this.value = value;
	}

	/**
	 * Returns the value that the constant was initialized to.
	 *
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#getValue()
	 */
	@Override
	public boolean getValue()
	{
		return value;
	}

	/**
	 * A constant is always consistent since it has no inputs, so this method does nothing
	 * but changing to TRUE the attribute that states that this component is consistent.
	 *
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#imposeConsistency()
	 */
	@Override
	public void imposeConsistency(){
		this.consistent = true;
	}

	/**
	 * This method on a constant should never be used, since a constant is supposed not to have inputs
	 * that can change its value.
	 *
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#propagateConsistency()
	 */
	@Override
	public void propagateValue(boolean newValue){
	}

	/**
	 * A constant is always consistent since it has no inputs, so this method does nothing
	 * but changing to FALSE the attribute that states that this component is consistent.
	 *
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#resetValue()
	 */
	@Override
	public void resetValue(){
		this.consistent = false;
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("doublecircle", "grey", Boolean.toString(value).toUpperCase());
	}
}