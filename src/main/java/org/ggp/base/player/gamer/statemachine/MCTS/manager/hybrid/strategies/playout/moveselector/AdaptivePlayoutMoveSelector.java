package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.moveselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.decoupled.DecoupledMctsNode;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;

public class AdaptivePlayoutMoveSelector extends MoveSelector {

	private List<Map<Move, Double>> weightsPerMove;

	private double temperature = 1; // TODO: make this parameter assignable from the config file

	/**
	 * First play urgency for a move never explored before.
	 * Note that this must be in the range [0,100] because MAST doesn't
	 * bother normalizing the move values (it's not necessary).
	 */
	//private double mastFpu;

	public AdaptivePlayoutMoveSelector(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);


		//this.mastFpu = gamerSettings.getDoublePropertyValue("MoveSelector.mastFpu");

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.weightsPerMove = sharedReferencesCollector.getWeightsPerMove();
	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	/**
	 * This method returns a joint move according to the MAST strategy.
	 * For each role it gets the list of all its legal moves in the state and picks the one with highest MAST expected score.
	 * @throws StateMachineException
	 */
/*	@Override
	public List<Move> getJointMove(MctsNode node, MachineState state) throws MoveDefinitionException, StateMachineException {

		List<Move> jointMove = new ArrayList<Move>();

		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){
			jointMove.add(this.getMoveForRole(node, state, i));
		}

		//System.out.println(Arrays.toString(jointMove.toArray()));

		return jointMove;

	}
*/
	/**
	 * This method returns a move according to the AdaprtivePlayout strategy.
	 * For the given role it gets the list of all its legal moves in the state
	 * and picks one according to the distribution generated using the weights
	 * of the move.
	 *
	 * @throws MoveDefinitionException, StateMachineException
	 */
	@Override
	public Move getMoveForRole(MctsNode node, MachineState state, int roleIndex) throws MoveDefinitionException, StateMachineException {

		List<Move> legalMoves;

		if(node != null && node instanceof DecoupledMctsNode) {
			legalMoves = ((DecoupledMctsNode)node).getLegalMovesForRole(roleIndex);
		}else {
			Role role = this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex);
			legalMoves = this.gameDependentParameters.getTheMachine().getLegalMoves(state, role);
		}

		//String toLog = "";
		//for(int index = 0; index < this.weightsPerMove.size(); index++){
		//	toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(index)) + ";\n");
		//	for(Entry<Move, Double> moveWeight : this.weightsPerMove.get(index).entrySet()){
		//		double weight = moveWeight.getValue();
		//		toLog += ("MOVE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitMove(moveWeight.getKey()) +
		//		";WEIGHT=;" + weight + ";\n");
		//	}
		//}
		//System.out.println(toLog);

		// Pick the move according to the distribution computed with the weights.
		return this.getMoveFromDistribution(roleIndex, legalMoves);
	}

	/**
	 * @param moves
	 * @return
	 */
	private Move getMoveFromDistribution(int roleIndex, List<Move> moves) {

		EnumeratedDistribution<Integer> distribution;
		List<Pair<Integer,Double>> probabilities;

		probabilities = new ArrayList<Pair<Integer,Double>>();

		Map<Move,Double> weightsForRole = this.weightsPerMove.get(roleIndex);

		double[] exponentialPerMove = new double[moves.size()];

		double probabilitySum = 0;
		for(int i = 0; i < moves.size(); i++){

			Double weightForMove = weightsForRole.get(moves.get(i));
			if(weightForMove != null){

				//System.out.println();
				//System.out.println(weightForMove);
				//System.out.println();


				exponentialPerMove[i] = Math.exp(this.temperature*weightForMove);
				probabilitySum += exponentialPerMove[i];
			}else{
				exponentialPerMove[i] = Math.exp(this.temperature*0.0);
				probabilitySum += exponentialPerMove[i];
			}

		}

		if(probabilitySum == 0){ // If all weights are 0, return random move
			return moves.get(this.getRandom().nextInt(moves.size()));
		}

		for(int i = 0; i < moves.size(); i++){

			//System.out.println(exponentialPerMove[i]);
			//System.out.println(probabilitySum);
			//System.out.println();

			probabilities.add(new Pair<Integer,Double>(i,exponentialPerMove[i]/probabilitySum));
		}




		try{
		distribution = new EnumeratedDistribution<Integer>(probabilities);
		}catch(Exception e){
			e.printStackTrace();
			String toLog = "";
			for(int index = 0; index < this.weightsPerMove.size(); index++){
				toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(index)) + ";\n");
				for(Entry<Move, Double> moveWeight : this.weightsPerMove.get(roleIndex).entrySet()){
					double weight = moveWeight.getValue();
					toLog += ("MOVE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitMove(moveWeight.getKey()) +
					";WEIGHT=;" + weight + ";\n");
				}
			}
			System.out.println(toLog);
			distribution = null;
		}

		return moves.get(distribution.sample());

	}

	@Override
	public String getComponentParameters(String indentation) {
		String params = indentation + "TEMPERATURE = " + this.temperature;

		if(this.weightsPerMove != null){
			String weightsPerMoveString = "[ ";

			for(Map<Move, Double> roleWeightsPerMove : this.weightsPerMove){
				weightsPerMoveString += roleWeightsPerMove.size() + " entries, ";
			}

			weightsPerMoveString += "]";

			params += indentation + "weights_per_move = " + weightsPerMoveString;
		}else{
			params += indentation + "weights_per_move = null";
		}

		return params;

	}

}
