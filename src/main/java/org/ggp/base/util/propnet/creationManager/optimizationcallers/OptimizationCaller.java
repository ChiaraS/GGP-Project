package org.ggp.base.util.propnet.creationManager.optimizationcallers;

import org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.DynamicPropNet;

public interface OptimizationCaller {

	public void optimize(DynamicPropNet dynamicPropNet) throws InterruptedException;

}
