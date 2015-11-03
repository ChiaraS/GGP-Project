package org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.components;

import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutableComponent;
import org.ggp.base.util.propnet.state.ExternalPropnetState;
import org.ggp.base.util.propnet.utils.PROP_TYPE;

/**
 * The Proposition class is designed to represent named latches.
 */
@SuppressWarnings("serial")
public final class ImmutableProposition extends ImmutableComponent{

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
	public ImmutableProposition(GdlSentence name, PROP_TYPE propType) {
		super();
		this.name = name;
		this.propType = propType;
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
			propnetState.flipBaseValue(this.stateIndex);
			break;
		case INPUT:
			propnetState.flipInputValue(this.stateIndex);
			break;
		default:
			propnetState.flipOtherValue(this.stateIndex);
			break;
		}

		// Since the value of this proposition changed, the new value must be propagated to its outputs.
		for(ImmutableComponent o : this.getOutputs()){
			o.updateValue(newInputValue, propnetState);
		}
	}

	@Override
	public String getComponentType() {
		return this.propType +  " I_PROPOSITION(" + this.getName() + ")";
	}

	public void setPropositionType(PROP_TYPE propType){
		this.propType = propType;
	}

	public PROP_TYPE getPropositionType(){
		return this.propType;
	}

	/**
	 * @see org.ggp.base.util.propnet.architecture.Immutable.ImmutableComponent#toString()
	 */
	@Override
	public String toString(){
		return toDot("circle", "white", name.toString());
	}

	@Override
	public boolean getValue(ExternalPropnetState propnetState) {
		switch(propType){
		case BASE:
			return propnetState.getBaseValue(this.stateIndex);
		case INPUT:
			return propnetState.getInputValue(this.stateIndex);
		default:
			return propnetState.getOtherValue(this.stateIndex);
		}
	}

	@Override
	public void imposeConsistency(ExternalPropnetState propnetState) {
		switch(propType){
		case BASE:
		case INPUT:
			this.isConsistent = true;
			break;
		default:
			if(this.getInputs().length == 0){
				this.isConsistent = true;
			}else if(this.getInputs().length == 1){
				// Note that if we are here we are sure it is not a base, so it must
				// change its value according to the value of its input.
				boolean currentPropValue = this.getValue(propnetState);
				if(this.getSingleInput().getValue(propnetState) != currentPropValue){
					propnetState.flipOtherValue(this.stateIndex);
					this.isConsistent = true;
					for(ImmutableComponent o : this.getOutputs()){
						o.propagateConsistency(!currentPropValue, propnetState);
					}
				}else{
					this.isConsistent = true;
				}
			}else if(this.getInputs().length > 1){
				throw new IllegalStateException("Detected a propNet proposition with more than one input!");
			}
			break;
		}
	}

	@Override
	public void propagateConsistency(boolean newInputValue, ExternalPropnetState propnetState) {
		if(this.isConsistent){
			//ConcurrencyUtils.checkForInterruption();

			// If this method is called, and this component was consistent with its input, it means that
			// the single input this component was consistent with changed value to newValue.
			// Assume that if this method is called then newValue is different from the current value,
			// so it must be flipped.
			switch(propType){
			case BASE:
			case INPUT:
				throw new IllegalStateException("A base or input proposition should have no inputs that can call the propagateConsistency() method!");
			default:
				propnetState.flipOtherValue(this.stateIndex);
				break;
			}

			// Since the value of this proposition changed, the new value must be propagated to its outputs.
			for(ImmutableComponent o : this.getOutputs()){
				o.propagateConsistency(newInputValue, propnetState);
			}
		}
	}
}
