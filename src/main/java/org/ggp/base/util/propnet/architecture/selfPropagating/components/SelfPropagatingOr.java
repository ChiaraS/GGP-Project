package org.ggp.base.util.propnet.architecture.selfPropagating.components;

import org.ggp.base.util.propnet.architecture.selfPropagating.SelfPropagatingComponent;

/**
 * The Or class is designed to represent logical OR gates.
 */
@SuppressWarnings("serial")
public final class SelfPropagatingOr extends SelfPropagatingComponent
{
	/**
	 * Number of inputs of this OR component that are TRUE.
	 */
	private int trueInputs;

	/**
	 * Returns true if and only if at least one of the inputs to the or is true.
	 *
	 * @see org.ggp.base.util.propnet.architecture.basic.Component#getValue()
	 */
	@Override
	public boolean getValue()
	{
		return this.trueInputs > 0;
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.basic.Component#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("ellipse", "grey", "OR " + this.trueInputs);
	}
}