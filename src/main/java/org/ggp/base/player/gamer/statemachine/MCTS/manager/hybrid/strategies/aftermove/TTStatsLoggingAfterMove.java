package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsTranspositionTable;
import org.ggp.base.util.logging.GamerLogger;

import csironi.ggp.course.experiments.propnet.SingleValueLongStats;

public class TTStatsLoggingAfterMove extends AfterMoveStrategy {

	private MctsTranspositionTable transpositionTable;

	public TTStatsLoggingAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		//this.paramStatsDecreaseFactor = gamerSettings.getDoublePropertyValue("AfterMoveStrategy" + id + ".paramStatsDecreaseFactor");

		//this.log = gamerSettings.getBooleanPropertyValue("AfterMoveStrategy" + id + ".log");
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.transpositionTable = sharedReferencesCollector.getTranspositionTable();
	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "TTBranchingLogs", "Step;#Samples;Min;Max;Median;SD;SEM;Avg;CI;");
		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "TTEntropyLogs", "Step;#Samples;Min;Max;Median;SD;SEM;Avg;CI;");
	}

	@Override
	public void afterMoveActions() {

		SingleValueLongStats ttBranchingStats = new SingleValueLongStats();

		SingleValueLongStats ttEntropyStats = new SingleValueLongStats();

		for(MctsNode node : this.transpositionTable.getTranspositionTable().values()) {

			//TODO

		}



	}

	@Override
	public String getComponentParameters(String indentation) {
		// Here we only print the name
		return indentation + "TRANSPOSITION_TABLE = " + this.transpositionTable.getClass().getSimpleName();
	}

}
