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

	private OpenBitSet basePropThruthValue;

	/**
	 *
	 */
	public ExtendedStatePropnetMachineState() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param contents
	 */
	public ExtendedStatePropnetMachineState(Set<GdlSentence> contents) {
		super(contents);
		// TODO Auto-generated constructor stub
	}

}
