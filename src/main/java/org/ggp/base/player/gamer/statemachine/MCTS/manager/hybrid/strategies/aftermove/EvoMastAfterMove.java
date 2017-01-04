package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;


public class EvoMastAfterMove extends AfterMoveStrategy {

	private MastAfterMove mastAfterMove;

	private EvoAfterMove evoAfterMove;

	public EvoMastAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector){

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.mastAfterMove = new MastAfterMove(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.evoAfterMove = new EvoAfterMove(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

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
	public String getComponentParameters(String indentation) {
		return indentation + "AFTER_MOVE_STRATEGY_1 = " + this.mastAfterMove.printComponent(indentation + "  ") + indentation + "AFTER_MOVE_STRATEGY_2 = " + this.evoAfterMove.printComponent(indentation + "  ");
	}

}
