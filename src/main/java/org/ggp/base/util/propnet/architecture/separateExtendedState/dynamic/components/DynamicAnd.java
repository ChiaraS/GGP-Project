package org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.components;

import org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.DynamicComponent;

/**
 * The And class is designed to represent logical AND gates.
 */
@SuppressWarnings("serial")
public final class DynamicAnd extends DynamicComponent{

	@Override
	public String getComponentType() {
		return "D_AND";
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#toString()
	 */
	@Override
	public String toString(){
		return toDot("invhouse", "grey", "AND");
	}

}
