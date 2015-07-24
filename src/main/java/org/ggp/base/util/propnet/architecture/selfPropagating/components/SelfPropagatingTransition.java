package org.ggp.base.util.propnet.architecture.selfPropagating.components;

import org.ggp.base.util.propnet.architecture.selfPropagating.SelfPropagatingComponent;

/**
 * The Transition class is designed to represent pass-through gates.
 */
@SuppressWarnings("serial")
public final class SelfPropagatingTransition extends SelfPropagatingComponent
{
	/**
	 * Returns the value of the input to the transition.
	 *
	 * @see org.ggp.base.util.propnet.architecture.basic.Component#getValue()
	 */
	@Override
	public boolean getValue()
	{
		return getSingleInput().getValue();
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.basic.Component#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("box", "grey", "TRANSITION");
	}
}