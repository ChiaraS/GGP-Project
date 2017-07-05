package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

public class MultiplePlayout extends PlayoutStrategy {

	private PlayoutStrategy subPlayoutStrategy;

	/**
	 * If the MAST score of my move in the list of joint moves that led to the state that was just
	 * added to the tree is higher than this threshold, multiple playouts will be performed for the
	 * state.
	 */
	private double scoreThreshold;

	/**
	 * Number of playouts to be performed on the given state when the MAST score is above the threshold.
	 */
	private int numPlayouts;

	private Map<Move,MoveStats> mastStatistics;

	public MultiplePlayout(GameDependentParameters gameDependentParameters,	Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		String[] subPlayoutStrategyDetails = gamerSettings.getIDPropertyValue("PlayoutStrategy" + id + ".subPlayoutStrategyType");

		try {
			this.subPlayoutStrategy = (PlayoutStrategy) SearchManagerComponent.getConstructorForMultiInstanceSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.PLAYOUT_STRATEGIES.getConcreteClasses(), subPlayoutStrategyDetails[0])).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, subPlayoutStrategyDetails[1]);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating sub-playoutStrategy " + gamerSettings.getPropertyValue("PlayoutStrategy.subPlayoutStrategyType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

		this.scoreThreshold = gamerSettings.getDoublePropertyValue("PlayoutStrategy" + id + ".scoreThreshold");

		this.numPlayouts = gamerSettings.getIntPropertyValue("PlayoutStrategy" + id + ".numPlayouts");

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		this.subPlayoutStrategy.setReferences(sharedReferencesCollector);
		this.mastStatistics = sharedReferencesCollector.getMastStatistics();

	}

	@Override
	public void clearComponent() {

		this.subPlayoutStrategy.clearComponent();

	}

	@Override
	public void setUpComponent() {

		this.subPlayoutStrategy.setUpComponent();

	}

	@Override
	public SimulationResult singlePlayout(MachineState state, int maxDepth) {
		return this.subPlayoutStrategy.singlePlayout(state, maxDepth);
	}

	@Override
	public SimulationResult[] playout(List<Move> jointMove, MachineState state, int maxDepth) {

		// Get the MAST score of my move that led to this state
		MoveStats myMoveStats = this.mastStatistics.get(jointMove.get(this.gameDependentParameters.getMyRoleIndex()));

		// If there is no MAST score, return the result of a single playout
		if(myMoveStats == null){
			return this.subPlayoutStrategy.playout(jointMove, state, maxDepth);
		}

		// Compute the average MAST score
		double myMoveAvgScore = myMoveStats.getScoreSum() / ((double) myMoveStats.getVisits());

		// If the score is below the threshold, perform a single playout
		if(myMoveAvgScore < this.scoreThreshold){
			return this.subPlayoutStrategy.playout(jointMove, state, maxDepth);
		}

		// If the score is above the threshold, perform multiple playouts
		SimulationResult[] results = new SimulationResult[this.numPlayouts];
		for(int repetition = 0; repetition < results.length; repetition++){
			results[repetition] = this.subPlayoutStrategy.singlePlayout(state, maxDepth);
		}

		return results;
	}

	@Override
	public List<Move> getJointMove(MachineState state) {
		return this.subPlayoutStrategy.getJointMove(state);
	}

	@Override
	public Move getMoveForRole(MachineState state, int roleIndex) {
		return this.subPlayoutStrategy.getMoveForRole(state, roleIndex);
	}



	@Override
	public String getComponentParameters(String indentation) {
		return indentation + "SUB_PLAYOUT_STRATEGY = " + this.subPlayoutStrategy.printComponent(indentation + "  ") +
				indentation + "SCORE_THRESHOLD = " + this.scoreThreshold +
				indentation + "NUM_PLAYOUTS = " + this.numPlayouts +
				indentation + "mast_statistics = " + (this.mastStatistics == null ? "null" : this.mastStatistics.size()+"entries");
	}

}
