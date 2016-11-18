package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;


public class PhMastAfterMove extends AfterMoveStrategy {

	private MastAfterMove mastAfterMove;

	private ProgressiveHistoryAfterMove phAfterMove;

	public PhMastAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.mastAfterMove = new MastAfterMove(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.phAfterMove = new ProgressiveHistoryAfterMove(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.mastAfterMove.setReferences(sharedReferencesCollector);
		this.phAfterMove.setReferences(sharedReferencesCollector);
	}

	@Override
	public void clearComponent() {
		this.mastAfterMove.clearComponent();
		this.phAfterMove.clearComponent();
	}

	@Override
	public void setUpComponent() {
		this.mastAfterMove.setUpComponent();
		this.phAfterMove.setUpComponent();
	}

	@Override
	public String getComponentParameters() {
		return "AFTER_MOVE_1 = " + this.mastAfterMove.printComponent() + ", AFTER_MOVE_2 = " + this.phAfterMove.printComponent();
	}

	@Override
	public void afterMoveActions() {

		this.mastAfterMove.afterMoveActions();

		this.phAfterMove.afterMoveActions();

	}

}
