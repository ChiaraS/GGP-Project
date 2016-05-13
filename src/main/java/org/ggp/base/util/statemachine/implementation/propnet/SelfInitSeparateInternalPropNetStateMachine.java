/**
 *
 */
package org.ggp.base.util.statemachine.implementation.propnet;

import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.propnet.architecture.separateExtendedState.immutable.ImmutablePropNet;
import org.ggp.base.util.propnet.creationManager.PropNetManagerRunner;
import org.ggp.base.util.propnet.creationManager.SeparateInternalPropnetManager;
import org.ggp.base.util.propnet.creationManager.optimizationcallers.OptimizationCaller;
import org.ggp.base.util.propnet.state.ImmutableSeparatePropnetState;
import org.ggp.base.util.statemachine.exceptions.StateMachineInitializationException;

/**
 * @author c.sironi
 *
 */
public class SelfInitSeparateInternalPropNetStateMachine extends
		SeparateInternalPropnetStateMachine {

	private SeparateInternalPropnetManager manager;
	private OptimizationCaller[] optimizations;

	/**
	 * @param propNet
	 * @param propnetState
	 */
	public SelfInitSeparateInternalPropNetStateMachine(
			ImmutablePropNet propNet, ImmutableSeparatePropnetState propnetState) {
		super(propNet, propnetState);
		this.optimizations = null;
		this.manager = null;
	}

	public SelfInitSeparateInternalPropNetStateMachine() {
		super(null, null);
		this.optimizations = null;
		this.manager = null;
	}

	public SelfInitSeparateInternalPropNetStateMachine(OptimizationCaller[] optimizations) {
		super(null, null);

		this.optimizations = optimizations;
		this.manager = null;
	}



    @Override
    public void initialize(List<Gdl> description, long timeout) throws StateMachineInitializationException {
    	if(this.propNet == null || this.propnetState == null){

    		this.manager = new SeparateInternalPropnetManager(description, timeout, this.optimizations);

    		PropNetManagerRunner.runPropNetManager(this.manager, timeout - System.currentTimeMillis());

    		this.propNet = manager.getImmutablePropnet();
    		this.propnetState = manager.getInitialPropnetState();

    		//DynamicPropNet dm = manager.getDynamicPropnet();
    		//if(dm != null){
    		//	System.out.println(dm);
    		//}

    	}

    	super.initialize(description, timeout);
    }

    public long getTotalInitTime(){

    	if(this.manager != null){
    		return this.manager.getTotalInitTime();
    	}else{
    		return 0L;
    	}

    }

    public long getPropnetConstructionTime(){

    	if(this.manager != null){
    		return this.manager.getPropnetConstructionTime();
    	}else{
    		return 0L;
    	}

    }

}
