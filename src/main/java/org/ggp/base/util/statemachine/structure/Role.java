package org.ggp.base.util.statemachine.structure;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class Role implements Serializable{

	@Override
	public abstract boolean equals(Object o);

	@Override
	public abstract int hashCode();

	@Override
	public abstract String toString();


}
