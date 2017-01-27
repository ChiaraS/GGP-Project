package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;

public class TunerMastAfterMove extends AfterMoveStrategy {

	private MastAfterMove mastAfterMove;

	private TunerAfterMove tunerAfterMove;

	public TunerMastAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id){

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.mastAfterMove = new MastAfterMove(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, "");

		this.tunerAfterMove = new TunerAfterMove(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, "");

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.mastAfterMove.setReferences(sharedReferencesCollector);
		this.tunerAfterMove.setReferences(sharedReferencesCollector);
	}

	@Override
	public void clearComponent() {
		this.mastAfterMove.clearComponent();
		this.tunerAfterMove.clearComponent();
	}

	@Override
	public void setUpComponent() {
		this.mastAfterMove.setUpComponent();
		this.tunerAfterMove.setUpComponent();
	}

	@Override
	public void afterMoveActions(){
		this.mastAfterMove.afterMoveActions();
		this.tunerAfterMove.afterMoveActions();
	}

	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "AFTER_MOVE_STRATEGY_1 = " + this.mastAfterMove.printComponent(indentation + "  ") + indentation + "AFTER_MOVE_STRATEGY_2 = " + this.tunerAfterMove.printComponent(indentation + "  ");
	}

}
