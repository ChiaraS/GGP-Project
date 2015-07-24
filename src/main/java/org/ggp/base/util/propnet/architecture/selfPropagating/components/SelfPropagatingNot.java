package org.ggp.base.util.propnet.architecture.selfPropagating.components;

import org.ggp.base.util.propnet.architecture.selfPropagating.SelfPropagatingComponent;

/**
 * The Not class is designed to represent logical NOT gates.
 */
@SuppressWarnings("serial")
public final class SelfPropagatingNot extends SelfPropagatingComponent
{
	/**
	 * Returns the inverse of the input to the not.
	 *
	 * @see org.ggp.base.util.propnet.architecture.basic.Component#getValue()
	 */
	@Override
	public boolean getValue()
	{
		return !getSingleInput().getValue();
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.basic.Component#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("invtriangle", "grey", "NOT");
	}
}