package org.ggp.base.util.propnet.architecture.separateExtendedState.immutable;

import java.io.Serializable;

import org.ggp.base.util.propnet.state.ExternalPropnetState;


/**
 * The root class of the Component hierarchy, which is designed to represent
 * nodes in a PropNet. The general contract of derived classes is to override
 * all methods.
 */

public abstract class ImmutableComponent implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 85773622157362907L;

	/** The array of all the components in the propnet */
	protected final ImmutableComponent[] components;

	/** The inputs to the component. */
    //private final ImmutableComponent[] inputs;
    /** The outputs of the component. */
    //private final ImmutableComponent[] outputs;

	/** Indices of all the inputs of this component in the components array */
	protected final int[] inputsIndices;

	/** Indices of all the inputs of this component in the components array */
	protected final int[] outputsIndices;

	/** Index of this component in the components array */
	protected final int structureIndex;

    /**
     * The index of the component in the propnet state that contains
     * the truth values of every component.
     */
    protected int stateIndex;

    /**
     * Parameter needed when computing consistent truth values for all the
     * components in the propNet. If TRUE it means that the component's value
     * is consistent with the current value of its inputs (if any) and thus
     * if an input component signals that its value has changed this component
     * must take care of updating as well and inform its outputs in case its
     * value also changed.
     *
     * TODO: the value of this parameter doesn't depend on the component itself
     * but on its value in the propnetState, so it should be better to remove it
     * from here and store it somewhere else.
     */
    protected boolean isConsistent;

    /**
     * Creates a new Component with the given structure index, inputs and outputs
     * indices and setting the given array of components.
     */
    public ImmutableComponent(ImmutableComponent[] components, int structureIndex, int[] inputsIndices, int[] outputsIndices){
    	this.components = components;
    	this.structureIndex = structureIndex;
    	this.inputsIndices = inputsIndices;
    	this.outputsIndices = outputsIndices;
        this.stateIndex = -1;
        this.isConsistent = false;
    }

    /**
     * Returns the index that this component has in the array of
     * propnet components.
     *
     * @return the index in the propnet state where this
     * component can find its truth value.
     */
    public int getStrictureIndex(){
    	return this.structureIndex;
    }

    /**
     * Sets the index that the truth value of this component
     * has in the propnet state.
     *
     * @param index the index in the propnet state where this
     * component can find its truth value.
     */
    public void setStateIndex(int stateIndex){
    	this.stateIndex = stateIndex;
    }

    /**
     * Returns the index that the truth value of this component
     * has in the propnet state.
     *
     * @return the index in the propnet state where this
     * component can find its truth value.
     */
    public int getStateIndex(){
    	return this.stateIndex;
    }

    /**
     * Returns a string representing the type of the component (i.e. AND, NOT, OR, PROPOSOTION,...)
     * giving details about it.
     *
     * @return the type of the component.
     */
    public abstract String getComponentType();

    public abstract boolean getValue(ExternalPropnetState propnetState);

    public abstract void updateValue(boolean newInputValue, ExternalPropnetState propnetState);

    /**
     * Returns a representation of the Component in .dot format.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public abstract String toString();

    /**
     * Returns a configurable representation of the Component in .dot format.
     *
     * @param shape
     *            The value to use as the <tt>shape</tt> attribute.
     * @param fillcolor
     *            The value to use as the <tt>fillcolor</tt> attribute.
     * @param label
     *            The value to use as the <tt>label</tt> attribute.
     * @return A representation of the Component in .dot format.
     */
    protected String toDot(String shape, String fillcolor, String label)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("\"@" + Integer.toHexString(hashCode()) + "\"[shape=" + shape + ", style= filled, fillcolor=" + fillcolor + ", label=\"" + label + "\"]; ");
        for ( int i : this.outputsIndices ){
            sb.append("\"@" + Integer.toHexString(hashCode()) + "\"->" + "\"@" + Integer.toHexString(this.components[i].hashCode()) + "\"; ");
        }

        return sb.toString();
    }

    public abstract void imposeConsistency(ExternalPropnetState propnetState);

    public abstract void propagateConsistency(boolean newInputValue, ExternalPropnetState propnetState);

}