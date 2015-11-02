package org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.components;

import org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.DynamicComponent;


/**
 * The Constant class is designed to represent nodes with fixed logical values.
 */
@SuppressWarnings("serial")
public final class DynamicConstant extends DynamicComponent{
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
	public DynamicConstant(boolean value){
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
		return s + "D_CONSTANT";
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.Dynamic.DynamicComponent#toString()
	 */
	@Override
	public String toString(){
		return toDot("doublecircle", "grey", Boolean.toString(value).toUpperCase());
	}

}