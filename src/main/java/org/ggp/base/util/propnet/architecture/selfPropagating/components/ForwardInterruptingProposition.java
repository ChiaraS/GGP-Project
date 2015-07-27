package org.ggp.base.util.propnet.architecture.selfPropagating.components;

import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.selfPropagating.ForwardInterruptingComponent;

/**
 * The Proposition class is designed to represent named latches.
 */
@SuppressWarnings("serial")
public final class ForwardInterruptingProposition extends ForwardInterruptingComponent
{
	/** The name of the Proposition. */
	private GdlSentence name;
	/** The value of the Proposition. */
	private boolean value;

	/**
	 * Creates a new Proposition with name <tt>name</tt>.
	 *
	 * @param name
	 *            The name of the Proposition.
	 */
	public ForwardInterruptingProposition(GdlSentence name)
	{
		this.name = name;
		this.value = false;
	}

	/**
	 * Getter method.
	 *
	 * @return The name of the Proposition.
	 */
	public GdlSentence getName()
	{
		return name;
	}

    /**
     * Setter method.
     *
     * This should only be rarely used; the name of a proposition
     * is usually constant over its entire lifetime.
     *
     * @return The name of the Proposition.
     */
    public void setName(GdlSentence newName)
    {
        name = newName;
    }

	/**
	 * Returns the current value of the Proposition.
	 *
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#getValue()
	 */
	@Override
	public boolean getValue()
	{
		return value;
	}

	/**
	 * Setter method.
	 *
	 * @param value
	 *            The new value of the Proposition.
	 */
	public void setValue(boolean value)
	{
		if(value != this.value){
			this.value = value;
			for(ForwardInterruptingComponent c : this.getOutputs()){
				//!!!!!!!!!!!!!!!!!!????????????????????c.propagate(value);
			}
		}

	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#imposeConsistency()
	 */
	@Override
	public void imposeConsistency(){
		if(this.getInputs().size() == 1){
			ForwardInterruptingComponent component = this.getSingleInput();
			if(! (component instanceof ForwardInterruptingTransition)){
				boolean oldValue = this.value;
				this.value = component.getValue();
				this.consistent = true;
				if(this.value != oldValue){
					for(ForwardInterruptingComponent c: this.getOutputs()){
						if(c.isConsistent()){
							c.propagateConsistency(this.getValue());
						}
					}
				}
			}
		}else{
			this.consistent = true;
		}
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#propagateConsistency()
	 */
	@Override
	public void propagateConsistency(boolean newValue){
		// Assume that if this method is called then newValue is different from the current value
		this.value = newValue;
		for(ForwardInterruptingComponent c: this.getOutputs()){
			if(c.isConsistent()){
				c.propagateConsistency(this.getValue());
			}
		}
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.forwardInterrupting.ForwardInterruptingComponent#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("circle", value ? "red" : "white", name.toString() + this.getValue());
	}
}