package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.EvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.MemorizedStandardPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.GRAVESelection;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class EvoGRAVEAfterSimulation extends GRAVEAfterSimulation {

	private EvolutionManager evolutionManager;

	private InternalPropnetRole myRole;

	public EvoGRAVEAfterSimulation(GRAVESelection graveSelection,
			MemorizedStandardPlayout gravePlayout, EvolutionManager evolutionManager, InternalPropnetRole myRole) {
		super(graveSelection, gravePlayout);

		this.evolutionManager = evolutionManager;

		this.myRole = myRole;

	}

	@Override
	public void afterSimulationActions(int[] goals) {

		super.afterSimulationActions(goals);

		this.evolutionManager.updateFitness(goals[this.myRole.getIndex()]);

	}

	@Override
	public String getStrategyParameters() {
		String params = super.getStrategyParameters();

		if(params == null){
			return this.evolutionManager.printEvolutionManager();
		}else{
			return params + "," + this.evolutionManager.printEvolutionManager();
		}
	}

}
