/**
 *
 */
package org.ggp.base.util.statemachine.implementation.propnet;

import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutablePropNet;
import org.ggp.base.util.propnet.creationManager.PropNetManagerRunner;
import org.ggp.base.util.propnet.creationManager.SeparateInternalPropnetManager;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;

/**
 * @author c.sironi
 *
 */
public class SelfInitSeparateInternalPropNetStateMachine extends
		SeparateInternalPropnetStateMachine {

	/**
	 * @param propNet
	 * @param propnetState
	 */
	public SelfInitSeparateInternalPropNetStateMachine(
			ImmutablePropNet propNet, ImmutableSeparatePropnetState propnetState) {
		super(propNet, propnetState);
	}

	public SelfInitSeparateInternalPropNetStateMachine() {
		super(null, null);
	}



    @Override
    public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException {
    	if(this.propNet == null || this.propnetState == null){

    		SeparateInternalPropnetManager manager =  new SeparateInternalPropnetManager(description, timeout);

    		PropNetManagerRunner.runPropNetManager(manager, timeout - System.currentTimeMillis());

    		this.propNet = manager.getImmutablePropnet();
    		this.propnetState = manager.getInitialPropnetState();

    	}

    	super.initialize(description, timeout);
    }


}
