package org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.components;

import org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.DynamicComponent;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutableComponent;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.components.ImmutableOr;

/**
 * The Or class is designed to represent logical OR gates.
 */
@SuppressWarnings("serial")
public final class DynamicOr extends DynamicComponent{

	@Override
	public String getComponentType() {
		return "D_OR";
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#toString()
	 */
	@Override
	public String toString(){
		return toDot("ellipse", "grey", "OR");
	}

	@Override
	public ImmutableComponent getImmutableClone() {
		return new ImmutableOr();
	}

}