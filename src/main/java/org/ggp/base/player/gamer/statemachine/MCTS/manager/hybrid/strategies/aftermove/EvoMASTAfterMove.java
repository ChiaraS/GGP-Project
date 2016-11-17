package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GamerConfiguration;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;


public class EvoMASTAfterMove extends AfterMoveStrategy {

	private MASTAfterMove mastAfterMove;

	private EvoAfterMove evoAfterMove;

	public EvoMASTAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerConfiguration gamerConfiguration, SharedReferencesCollector sharedReferencesCollector){

		super(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);

		this.mastAfterMove = new MASTAfterMove(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);

		this.evoAfterMove = new EvoAfterMove(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.mastAfterMove.setReferences(sharedReferencesCollector);
		this.evoAfterMove.setReferences(sharedReferencesCollector);
	}

	@Override
	public void clearComponent() {
		this.mastAfterMove.clearComponent();
		this.evoAfterMove.clearComponent();
	}

	@Override
	public void setUpComponent() {
		this.mastAfterMove.setUpComponent();
		this.evoAfterMove.setUpComponent();
	}

	@Override
	public void afterMoveActions(){

		this.mastAfterMove.afterMoveActions();

		this.evoAfterMove.afterMoveActions();

	}

	@Override
	public String getComponentParameters() {
		return "(SUB_AFTER_MOVE_STRATEGY = " + this.mastAfterMove.printComponent() + ", SUB_AFTER_SIM_STRATEGY = " + this.evoAfterMove.printComponent() + ")";
	}

}
