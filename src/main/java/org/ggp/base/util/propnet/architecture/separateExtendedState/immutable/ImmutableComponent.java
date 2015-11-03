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

	/** The inputs to the component. */
    private ImmutableComponent[] inputs;
    /** The outputs of the component. */
    private ImmutableComponent[] outputs;

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
    public ImmutableComponent(){
    	this.inputs = new ImmutableComponent[0];
    	this.outputs = new ImmutableComponent[0];
        this.stateIndex = -1;
        this.isConsistent = false;
    }

    /**
     * Sets the inputs of this component.
     *
     * @param inputs the inputs of this component.
     */
    public void setInputs(ImmutableComponent[] inputs){
    	this.inputs = inputs;
    }

    /**
     * Gets the inputs of this component.
     *
     * @return the inputs of this component.
     */
    public ImmutableComponent[] getInputs(){
    	return this.inputs;
    }

    /**
     * Gets the single input of this component.
     *
     * @return the single input of this component.
     */
    public ImmutableComponent getSingleInput(){
    	assert this.inputs.length == 1;
        return this.inputs[0];
    }

    /**
     * Sets the outputs of this component.
     *
     * @param outputs the outputs of this component.
     */
    public void setOutputs(ImmutableComponent[] outputs){
    	this.outputs = outputs;
    }

    /**
     * Gets the outputs of this component.
     *
     * @return the outputs of this component.
     */
    public ImmutableComponent[] getOutputs(){
    	return this.outputs;
    }

    /**
     * Gets the single output of this component.
     *
     * @return the single output of this component.
     */
    public ImmutableComponent getSingleOutput(){
    	assert this.outputs.length == 1;
        return this.outputs[0];
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
        for ( ImmutableComponent o : this.outputs ){
            sb.append("\"@" + Integer.toHexString(hashCode()) + "\"->" + "\"@" + Integer.toHexString(o.hashCode()) + "\"; ");
        }

        return sb.toString();
    }

    public abstract void imposeConsistency(ExternalPropnetState propnetState);

    public abstract void propagateConsistency(boolean newInputValue, ExternalPropnetState propnetState);

}