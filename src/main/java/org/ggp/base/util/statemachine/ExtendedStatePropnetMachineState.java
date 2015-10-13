/**
 *
 */
package org.ggp.base.util.statemachine;

import java.util.Set;

import org.apache.lucene.util.OpenBitSet;
import org.ggp.base.util.gdl.grammar.GdlSentence;

/**
 * @author C.Sironi
 *
 */
public class ExtendedStatePropnetMachineState extends MachineState {

	private OpenBitSet basePropsTruthValue;

	/**
	 *
	 */
	public ExtendedStatePropnetMachineState() {
		super();
		this.basePropsTruthValue = null;
	}

	/**
	 * @param contents
	 */
	public ExtendedStatePropnetMachineState(Set<GdlSentence> contents) {
		super(contents);
		this.basePropsTruthValue = null;
	}

	/**
	 * @param contents
	 */
	public ExtendedStatePropnetMachineState(Set<GdlSentence> contents, OpenBitSet basePropsTruthValue) {
		super(contents);
		this.basePropsTruthValue = basePropsTruthValue;
	}

	public OpenBitSet getBasePropsTruthValue(){
		return this.basePropsTruthValue;
	}

	@Override
	public boolean equals(Object o)
    {
        if ((o != null) && (o instanceof MachineState)){
        	if(o instanceof ExtendedStatePropnetMachineState){
        		ExtendedStatePropnetMachineState state = (ExtendedStatePropnetMachineState) o;
        		return state.getBasePropsTruthValue().equals(this.getBasePropsTruthValue());
        	}else{
        		MachineState state = (MachineState) o;
        		return state.getContents().equals(this.getContents());
        	}
        }

        return false;
    }

}
