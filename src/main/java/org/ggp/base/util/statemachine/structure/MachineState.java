package org.ggp.base.util.statemachine.structure;



public abstract class MachineState{

	@Override
	public abstract boolean equals(Object o);

	@Override
	public abstract int hashCode();

	@Override
	public abstract String toString();

	@Override
	public abstract MachineState clone();

}
