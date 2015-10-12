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
public class BitPropnetMachineState extends MachineState {

	private OpenBitSet basePropThruthValue;

	/**
	 *
	 */
	public BitPropnetMachineState() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param contents
	 */
	public BitPropnetMachineState(Set<GdlSentence> contents) {
		super(contents);
		// TODO Auto-generated constructor stub
	}

}
