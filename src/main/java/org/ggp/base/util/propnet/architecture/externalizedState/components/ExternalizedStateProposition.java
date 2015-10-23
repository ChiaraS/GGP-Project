package org.ggp.base.util.propnet.architecture.externalizedState.components;

import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.externalizedState.ExternalizedStateComponent;

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
    	BASE, INPUT, LEGAL, GOAL, TERMINAL, OTHER
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

	/**
	 * Checks if this is a base proposition.
	 *
	 * @return TRUE if this is a base proposition, FALSE otherwise.
	 */
	public boolean isBase(){
		return this.isBase;
	}

	/**
	 * Checks if this is an input proposition.
	 *
	 * @return TRUE if this is an input proposition, FALSE otherwise.
	 */
	public boolean isInput(){
		return this.isInput;
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
	public String getType() {
		return "PROPOSITION(" + this.getName() + ")";
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.ExternalizedState.ExternalizedStateComponent#toString()
	 */
	@Override
	public String toString()
	{
		return toDot("circle", value ? "red" : "white", name.toString());
	}
}
