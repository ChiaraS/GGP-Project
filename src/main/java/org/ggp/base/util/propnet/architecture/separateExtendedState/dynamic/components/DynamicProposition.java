package org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.components;

import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.DynamicComponent;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutableComponent;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.components.ImmutableProposition;
import org.ggp.base.util.propnet.utils.PROP_TYPE;

/**
 * The Proposition class is designed to represent named latches.
 */
@SuppressWarnings("serial")
public final class DynamicProposition extends DynamicComponent{

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
	public DynamicProposition(GdlSentence name){
		super();
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
	public String getComponentType() {
		return this.propType +  " D_PROPOSITION(" + this.getName() + ")";
	}

	public void setPropositionType(PROP_TYPE propType){
		this.propType = propType;
	}

	public PROP_TYPE getPropositionType(){
		return this.propType;
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.Dynamic.DynamicComponent#toString()
	 */
	@Override
	public String toString(){
		return toDot("circle", "white", name.toString());
	}

	@Override
	public ImmutableComponent getImmutableClone() {
		return new ImmutableProposition(this.name, this.propType);
	}

}
