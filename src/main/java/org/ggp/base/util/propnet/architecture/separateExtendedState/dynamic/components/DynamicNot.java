package org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.components;

import org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.DynamicComponent;

/**
 * The Not class is designed to represent logical NOT gates.
 */
@SuppressWarnings("serial")
public final class DynamicNot extends DynamicComponent{

	@Override
	public String getComponentType() {
		return "D_NOT";
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#toString()
	 */
	@Override
	public String toString(){
		return toDot("invtriangle", "grey", "NOT");
	}

}