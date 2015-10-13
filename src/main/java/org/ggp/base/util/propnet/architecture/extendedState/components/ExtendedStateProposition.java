package org.ggp.base.util.propnet.architecture.extendedState.components;

import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.extendedState.ExtendedStateComponent;

/**
 * The Proposition class is designed to represent named latches.
 */
@SuppressWarnings("serial")
public final class ExtendedStateProposition extends ExtendedStateComponent
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
	public ExtendedStateProposition(GdlSentence name)
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
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#getValue()
	 */
	@Override
	public boolean getValue()
	{
		return value;
	}

	/**
	 * Setter method without propagation. This method sets the value of this proposition to the new value,
	 * without propagating it to is outputs.
	 *
	 * !REMARK: only use this method if the propnet is not consistent otherwise it will be in a non-consistent
	 * state without knowing it. If you want to use this method on a consistent propnet, first use the method
	 * ExtendedStatePropnet.resetValues() and after using this setValue() method use also
	 * ExtendedStatePropnet.imposeConsistency() to restore the propnet consistency.
	 *
	 * @param value The new value of the Proposition.
	 */
	public void setValue(boolean value){
		this.value = value;
	}

	/**
	 * Setter method with propagation. This method sets the value of this proposition to the new value,
	 * propagating it if different from the previous value.
	 *
	 * @param value The new value of the Proposition.
	 */
	public void setAndPropagateValue(boolean value){
		// If the value doesn't change, do nothing
		// If it changes, propagate it. And if this proposition has inputs and is consistent with its inputs,
		// then a change of value makes it inconsistent.
		if(value != this.value){
			this.flipValue();
		}
	}

	/**
	 * This method flips the current proposition value, propagating the change to its outputs.
	 * Note that if this is not a base proposition, it will become inconsistent with it inputs!
	 */
	public void flipValue(){
		this.value = !this.value;
		// If it is not a base proposition and it has inputs, memorize that this proposition became
		// inconsistent before propagating, so if the value gets propagated again to this proposition
		// it won't be updated again.
		if(this.isConsistent() && this.hasInput() && !this.isBase()){
			this.consistent = false;
		}
		for(ExtendedStateComponent c : this.getOutputs()){
			c.propagateValue(this.value);
		}
	}

	/**
	 * Checks if this is a base proposition.
	 *
	 * @return TRUE if this is a base proposition, FALSE otherwise.
	 */
	public boolean isBase(){
		return (this.getInputs().size() == 1 && this.getSingleInput() instanceof ExtendedStateTransition);
	}

	/**
	 * Checks if this proposition has any input.
	 *
	 * !REMARK: a proposition that has no inputs is not necessarily an input proposition (e.g. the init proposition
	 * has no inputs but is not an input proposition), thus the use of this method is not sufficient to detect
	 * an input proposition.
	 *
	 * @return TRUE if this proposition has at least one input, FALSE otherwise.
	 */
	public boolean hasInput(){
		return (this.getInputs().size() != 0);
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#imposeConsistency()
	 */
	@Override
	public void imposeConsistency(){
		// If this proposition has an input...
		if(this.getInputs().size() == 1){
			ExtendedStateComponent component = this.getSingleInput();
			//...and it is not a transition...
			if(! (component instanceof ExtendedStateTransition)){
				boolean oldValue = this.value;
				this.value = component.getValue();
				this.consistent = true;
				if(this.value != oldValue){
					for(ExtendedStateComponent c: this.getOutputs()){
						c.propagateValue(this.getValue());
					}
				}
			}
		}else{
			// This case includes also propositions with more than one input
			// (officially they should not exist).
			// TODO: manage the case when the proposition has more than one input
			// (i.e. throw exception).
			this.consistent = true;
		}
	}

	/**
	 * DO NOT USE THIS METHOD TO MANUALLY CHANGE THE VALUE OF THIS PROPOSITION. TO DO THAT USE THE SETTER METHOD!
	 *
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#propagateConsistency()
	 */
	@Override
	public void propagateValue(boolean newValue){

		//ConcurrencyUtils.checkForInterruption();

		// If this method is called, and this component was consistent with its input, it means that
		// the single input this component was consistent with changed value to newValue.
		// Assume that if this method is called then newValue is different from the current value
		if(this.consistent){
			this.value = newValue;
			for(ExtendedStateComponent c: this.getOutputs()){
				c.propagateValue(this.getValue());
			}
		}

	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#resetValue()
	 */
	@Override
	public void resetValue(){
		this.value = false;
		this.consistent = false;
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExtendedState.ExtendedStateComponent#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("circle", value ? "red" : "white", name.toString() + this.getValue());
	}
}