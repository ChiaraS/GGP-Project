package org.ggp.base.util.propnet.architecture.externalizedState.components;

import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.externalizedState.ExternalizedStateComponent;
import org.ggp.base.util.propnet.state.ExternalPropnetState;

/**
 * The Proposition class is designed to represent named latches.
 */
@SuppressWarnings("serial")
public final class ExternalizedStateProposition extends ExternalizedStateComponent{

	/**
	 * Enumeration of all possible types of propositions.
	 *
	 * @author C.Sironi
	 *
	 */
	public enum PROP_TYPE{
    	BASE, INPUT, LEGAL, GOAL, TERMINAL, INIT, OTHER
    }

	/** The name of the Proposition. */
	private GdlSentence name;

	/** The type of the proposition */
	private PROP_TYPE propType;

	/**
	 * Creates a new Proposition with name <tt>name</tt>.
	 *
	 * @param name
	 *            The name of the Proposition.
	 */
	public ExternalizedStateProposition(GdlSentence name){
		this.name = name;
	}

	/**
	 * Getter method.
	 *
	 * @return The name of the Proposition.
	 */
	public GdlSentence getName(){
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
    public void setName(GdlSentence newName){
        name = newName;
    }

	@Override
	public void updateValue(boolean newInputValue, ExternalPropnetState propnetState){

		//ConcurrencyUtils.checkForInterruption();

		// If this method is called, and this component was consistent with its input, it means that
		// the single input this component was consistent with changed value to newValue.
		// Assume that if this method is called then newValue is different from the current value,
		// so it must be flipped.
		switch(propType){
		case BASE:
			propnetState.flipBaseValue(this.index);
			break;
		case INPUT:
			propnetState.flipInputValue(this.index);
			break;
		default:
			propnetState.flipOtherValue(this.index);
			break;
		}

		// Since the value of this proposition changed, the new value must be propagated to its outputs.
		for(ExternalizedStateComponent c: this.getOutputs()){
			c.updateValue(newInputValue, propnetState);
		}
	}

	/**
	 * Checks if this is a base proposition.
	 *
	 * @return TRUE if this is a base proposition, FALSE otherwise.
	 */
	public boolean isBase(){
		return this.propType == PROP_TYPE.BASE;
	}

	/**
	 * Checks if this is an input proposition.
	 *
	 * @return TRUE if this is an input proposition, FALSE otherwise.
	 */
	public boolean isInput(){
		return this.propType == PROP_TYPE.INPUT;
	}

	/**
	 * Checks if this proposition has any input.
	 *
	 * !REMARK: a proposition that has no inputs is not necessarily an input proposition (e.g. the INIT proposition
	 * has no inputs but is not an input proposition), thus the use of this method is not sufficient to detect
	 * an input proposition.
	 *
	 * @return TRUE if this proposition has at least one input, FALSE otherwise.
	 */
	public boolean hasInput(){
		return (this.getInputs().size() != 0);
	}

	@Override
	public String getComponentType() {
		return "PROPOSITION(" + this.getName() + ")";
	}

	public void setPropositionType(PROP_TYPE propType){
		this.propType = propType;
	}

	public PROP_TYPE getPropositionType(){
		return this.propType;
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExternalizedState.ExternalizedStateComponent#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("circle", "white", name.toString());
	}
}
