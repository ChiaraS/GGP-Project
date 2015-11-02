package org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


/**
 * The root class of the Component hierarchy, which is designed to represent
 * nodes in a PropNet. The general contract of derived classes is to override
 * all methods.
 */

public abstract class DynamicComponent implements Serializable
{

	private static final long serialVersionUID = 4302696572399327601L;

	/** The inputs to the component. */
    private final Set<DynamicComponent> inputs;
    /** The outputs of the component. */
    private final Set<DynamicComponent> outputs;

	/** Index of this component in the components array used to clone the propnet */
	protected int structureIndex;

    /**
     * Creates a new Component with no inputs or outputs.
     */
    public DynamicComponent()
    {
        this.inputs = new HashSet<DynamicComponent>();
        this.outputs = new HashSet<DynamicComponent>();
        this.structureIndex = -1;
    }

    /**
     * Adds a new input.
     *
     * @param input a new input.
     */
    public void addInput(DynamicComponent input)
    {
        inputs.add(input);
    }

    public void removeInput(DynamicComponent input)
    {
    	inputs.remove(input);
    }

    public void removeOutput(DynamicComponent output)
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
    public void addOutput(DynamicComponent output)
    {
        outputs.add(output);
    }

    /**
     * Getter method.
     *
     * @return The inputs to the component.
     */
    public Set<DynamicComponent> getInputs()
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
    public DynamicComponent getSingleInput() {
        assert inputs.size() == 1;
        return inputs.iterator().next();
    }

    /**
     * Getter method.
     *
     * @return The outputs of the component.
     */
    public Set<DynamicComponent> getOutputs(){
        return outputs;
    }

    /**
     * A convenience method, to get a single output.
     * To be used only when the component is known to have
     * exactly one output.
     *
     * @return The single output to the component.
     */
    public DynamicComponent getSingleOutput(){
        assert outputs.size() == 1;
        return outputs.iterator().next();
    }

    /**
     * Sets the index of this component in the components array
     * used to clone the propnet.
     *
     * @param index the index of this component in the components
     * array.
     */
    public void setStructureIndex(int structureIndex){
    	this.structureIndex = structureIndex;
    }

    /**
     * Returns the index  of this component in the components array
     * used to clone the propnet.
     *
     * @return the index of this component in the components array.
     */
    public int getStructureIndex(){
    	return this.structureIndex;
    }

    /**
     * Returns a string representing the type of the component (i.e. AND, NOT, OR, PROPOSOTION,...)
     * giving details about it.
     *
     * @return the type of the component.
     */
    public abstract String getComponentType();

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
        for ( DynamicComponent component : getOutputs() )
        {
            sb.append("\"@" + Integer.toHexString(hashCode()) + "\"->" + "\"@" + Integer.toHexString(component.hashCode()) + "\"; ");
        }

        return sb.toString();
    }

}