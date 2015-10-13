package org.ggp.base.util.propnet.architecture.extendedState;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The root class of the Component hierarchy, which is designed to represent
 * nodes in a PropNet. The general contract of derived classes is to override
 * all methods.
 */

public abstract class ExtendedStateComponent implements Serializable
{

    /**
	 *
	 */
	private static final long serialVersionUID = -3451961734636633811L;

	/** The inputs to the component. */
    private final Set<ExtendedStateComponent> inputs;
    /** The outputs of the component. */
    private final Set<ExtendedStateComponent> outputs;

    /**
     * TRUE if the value of the component has already been correctly initialized and
     * thus it is consistent with the value of its inputs, FALSE otherwise.
     */
    protected boolean consistent;

    /**
     * Creates a new Component with no inputs or outputs.
     */
    public ExtendedStateComponent()
    {
        this.inputs = new HashSet<ExtendedStateComponent>();
        this.outputs = new HashSet<ExtendedStateComponent>();
        this.consistent = false;
    }

    /**
     * Adds a new input.
     *
     * @param input
     *            A new input.
     */
    public void addInput(ExtendedStateComponent input)
    {
        inputs.add(input);
    }

    public void removeInput(ExtendedStateComponent input)
    {
    	inputs.remove(input);
    }

    public void removeOutput(ExtendedStateComponent output)
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
    public void addOutput(ExtendedStateComponent output)
    {
        outputs.add(output);
    }

    /**
     * Getter method.
     *
     * @return The inputs to the component.
     */
    public Set<ExtendedStateComponent> getInputs()
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
    public ExtendedStateComponent getSingleInput() {
        assert inputs.size() == 1;
        return inputs.iterator().next();
    }

    /**
     * Getter method.
     *
     * @return The outputs of the component.
     */
    public Set<ExtendedStateComponent> getOutputs()
    {
        return outputs;
    }

    /**
     * A convenience method, to get a single output.
     * To be used only when the component is known to have
     * exactly one output.
     *
     * @return The single output to the component.
     */
    public ExtendedStateComponent getSingleOutput() {
        assert outputs.size() == 1;
        return outputs.iterator().next();
    }

    /**
     * Returns the value of the Component.
     *
     * @return The value of the Component.
     */
    public abstract boolean getValue();

    /**
     * This method makes sure that the value of the component is consistent with its
     * inputs, and if such value changes it informs its outputs that they have to
     * recompute their value if the were consistent with this input and want to keep
     * being consistent.
     */
    public abstract void imposeConsistency();

    /**
     * This method must be used to tell to this component that one of is inputs changed
     * its value to newValue, thus this component has to modify its value accordingly.
     *
     * !REMARK: use this method only when the input really changes its value. This method
     * assumes that, whatever value is received (true or false), the previous value was
     * exactly the opposite (e.g. if the component that receives the newValue=FALSE is
     * and AND component, it will assume that the previous value was TRUE and so it has
     * to decrement the number of trueInputs by 1).
     *
     * @param newValue
     */
    public abstract void propagateValue(boolean newValue);

    /**
     * Checks if the value of this component is consistent with the value of its inputs.
     *
     * @return TRUE if the value of this component is consistent with the value of its
     * inputs, FALSE otherwise.
     */
    public boolean isConsistent(){
    	return this.consistent;
    }

    /**
     * This method resets the value of the component to a default value, probably removing
     * the consistency with its inputs.
     *
     * Note that the use of the resetValue() method on even only one of the components of the propnet, makes the whole
	 * propnet inconsistent without the propnet realizing it, thus when using this method, even if only on one component,
	 * before using again the propnet reset the value of all other components and re-impose the consistency over the
	 * whole propnet.
	 *
     */
    public abstract void resetValue();

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
        for ( ExtendedStateComponent component : getOutputs() )
        {
            sb.append("\"@" + Integer.toHexString(hashCode()) + "\"->" + "\"@" + Integer.toHexString(component.hashCode()) + "\"; ");
        }

        return sb.toString();
    }

}