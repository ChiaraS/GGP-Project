package org.ggp.base.util.propnet.architecture.externalizedState;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The root class of the Component hierarchy, which is designed to represent
 * nodes in a PropNet. The general contract of derived classes is to override
 * all methods.
 */

public abstract class ExternalizedStateComponent implements Serializable
{

    /** The serial version ID. */
	private static final long serialVersionUID = -4826572855490717037L;

	/** The inputs to the component. */
    private final Set<ExternalizedStateComponent> inputs;
    /** The outputs of the component. */
    private final Set<ExternalizedStateComponent> outputs;

    /**
     * The index of the component in the propnet state that contains
     * the truth values of every component.
     */
    private int index;

    /**
     * Creates a new Component with no inputs or outputs.
     */
    public ExternalizedStateComponent()
    {
        this.inputs = new HashSet<ExternalizedStateComponent>();
        this.outputs = new HashSet<ExternalizedStateComponent>();
        this.index = -1;
    }

    /**
     * Adds a new input.
     *
     * @param input a new input.
     */
    public void addInput(ExternalizedStateComponent input)
    {
        inputs.add(input);
    }

    public void removeInput(ExternalizedStateComponent input)
    {
    	inputs.remove(input);
    }

    public void removeOutput(ExternalizedStateComponent output)
    {
    	outputs.remove(output);
    }

    public void removeAllInputs()
    {
		inputs.clear();
	}

	public void removeAllOutputs()
	{
		outputs.clear();
	}

    /**
     * Adds a new output.
     *
     * @param output
     *            A new output.
     */
    public void addOutput(ExternalizedStateComponent output)
    {
        outputs.add(output);
    }

    /**
     * Getter method.
     *
     * @return The inputs to the component.
     */
    public Set<ExternalizedStateComponent> getInputs()
    {
        return inputs;
    }

    /**
     * A convenience method, to get a single input.
     * To be used only when the component is known to have
     * exactly one input.
     *
     * @return The single input to the component.
     */
    public ExternalizedStateComponent getSingleInput() {
        assert inputs.size() == 1;
        return inputs.iterator().next();
    }

    /**
     * Getter method.
     *
     * @return The outputs of the component.
     */
    public Set<ExternalizedStateComponent> getOutputs(){
        return outputs;
    }

    /**
     * A convenience method, to get a single output.
     * To be used only when the component is known to have
     * exactly one output.
     *
     * @return The single output to the component.
     */
    public ExternalizedStateComponent getSingleOutput(){
        assert outputs.size() == 1;
        return outputs.iterator().next();
    }

    /**
     * Sets the index that the truth value of this component
     * has in the propnet state.
     *
     * @param index the index in the propnet state where this
     * component can find its truth value.
     */
    public void setIndex(int index){
    	this.index = index;
    }

    /**
     * Returns the index that the truth value of this component
     * has in the propnet state.
     *
     * @return the index in the propnet state where this
     * component can find its truth value.
     */
    public int getIndex(){
    	return this.index;
    }

    /**
     * Returns a string representing the type of the component (i.e. AND, NOT, OR, PROPOSOTION,...)
     * giving details about it.
     *
     * @return the type of the component.
     */
    public abstract String getType();

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
        for ( ExternalizedStateComponent component : getOutputs() )
        {
            sb.append("\"@" + Integer.toHexString(hashCode()) + "\"->" + "\"@" + Integer.toHexString(component.hashCode()) + "\"; ");
        }

        return sb.toString();
    }

}