package org.ggp.base.util.propnet.architecture.selfPropagating.components;

import org.ggp.base.util.propnet.architecture.selfPropagating.SelfPropagatingComponent;

/**
 * The And class is designed to represent logical AND gates.
 */
@SuppressWarnings("serial")
public final class SelfPropagatingAnd extends SelfPropagatingComponent
{

	/**
	 * Number of inputs of this AND component that are TRUE.
	 */
	private int trueInputs;

	/**
	 * Returns true if and only if every input to the and is true.
	 *
	 * @see org.ggp.base.util.propnet.architecture.basic.Component#getValue()
	 */
	@Override
	public boolean getValue()
	{
		if(this.getInputs() != null){
			return this.trueInputs == this.getInputs().size();
		}

		return true;
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.basic.Component#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("invhouse", "grey", "AND " + this.trueInputs);
	}

}
