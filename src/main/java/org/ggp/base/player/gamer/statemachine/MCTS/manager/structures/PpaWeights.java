package org.ggp.base.player.gamer.statemachine.MCTS.manager.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.util.Pair;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.Move;

/**
 * This class keeps track of the weights and the corresponding exponential
 * for the playout policy adaptation strategy. Whenever the value of a weight
 * is changed, the corresponding exponential is removed, but not recomputed
 * immediately. The next exponential value is computed only the first time it
 * is needed (if ever).
 *
 * More precisely, a PpaInfo for a move can be accessed (i) when selecting a move during the
 * playout or (ii) when updating the weight during the after simulation action.
 * For (i), we need the exponential of the current weight. If the weight has changed during the
 * after simulation action in one of the previous iterations and the exponential is not consistent,
 * the exponential must be computed first. Otherwise, the exponential is returned as it is.
 * For (ii), we always need the exponential of the value that the weight had BEFORE any increment
 * takes place during the current iteration. Therefore, if the exponential is inconsistent because
 * the last update of the weight happened in an earlier iteration, we update it before returning it.
 * Otherwise, the reason why the exponential is inconsistent is that the weight has already been
 * incremented in the current iteration. We know that before the increment could be computed we
 * must have accessed this instance and updated the exponential if it wasn't consistent (because we
 * need the sum of ALL exponentials of all weights of all moves in the state). So we know that the
 * current exponential is consistent with the value of the weight before the increment. Thus, we
 * return that exponential.
 *
 * Note that all these checks wouldn't be necessary if we only used the moves in the playout to update the policy.
 * In that case, all the moves would be visited when selecting actions in each node in the playout, so their
 * exponential would be consistent with their weight by the time adaptation of the policy is performed. However,
 * we also update the policy using the actions in the simulation visited during selection, before the playout,
 * and their siblings. For these actions, we don't know if we already have an entry in the weights map and we
 * don't know if their exponential has already been made consistent with the current weight.
 *
 * Note that this setup has a particular effect if we are performing multiple playouts in the same simulation.
 * First of all, for all playouts the same policy (i.e. the same weights) will be used to select actions. Then,
 * the simulation with all playouts will be used to update the weights as they were in the previous iteration. This
 * means that we do not use the weights to select moves in the first playout of the series of playouts for the
 * current simulation, then update the weights with the result of this playout, use the new weights to select
 * the moves in the next playout of the series, and so on. We first use the current weights to select moves in
 * all the playouts for the simulation. Then update the weights once with all the playouts.
 *
 * @author C.Sironi
 *
 */
public class PpaWeights {

	/**
	 * List with one map per role. Each map maps a move to the
	 * corresponding weight for the playout policy adaptation
	 * strategy and to the exponential of the weight.
	 */
	private List<Map<Move,PpaInfo>> weightsPerMove;


	public void setUp(int numRoles){
		this.weightsPerMove = new ArrayList<Map<Move,PpaInfo>>();
		for(int roleIndex = 0; roleIndex < numRoles; roleIndex++){
			this.weightsPerMove.add(new HashMap<Move,PpaInfo>());
		}
	}

	public void clear(){
		this.weightsPerMove.clear();
	}

	/**
	 * THIS FUNCTION SHOULD BE USED WHEN SELECTING MOVES DURING THE PLAYOUT BECAUSE IT ALWAYS
	 * RECOMPUTES THE EXPONENTIAL IF IT IS NOT CONSISTENT WITH THE WEIGHT.
	 * This method gets a role and one of its moves and returns the PPA info corresponding to
	 * the move of the role. If the exponential is not consistent with the weight, it gets
	 * recomputed before the PPA info is returned.
	 *
	 * @param role
	 * @param move
	 * @return
	 */
	public PpaInfo getPpaInfoForSelection(int role, Move move){

		// Check if there already is an entry for the move. If not, add it with a
		// weight of 0 and exponential of 1.
		PpaInfo result = this.weightsPerMove.get(role).get(move);
		if(result == null){
			// No increment took place yet, so we set the last increment iteration to -1.
			// Anyway it has no influence because the exponential is consistent. The moment
			// the exponential becomes inconsistent is because of an increment, therefore
			// the value of the last increment iteration gets updated correctly too.
			result = new PpaInfo(0.0, 1.0,true, -1);
			this.weightsPerMove.get(role).put(move, result);
		}else if(!result.isConsistent()){
			// If the entry exists, we need to check if the exponential has already been computed.
			// If not, compute it.
			result.updateExp();
		}
		return result;

	}

	/**
	 * THIS FUNCTION SHOULD BE USED WHEN ADAPTING THE POLICY DURING AFTER SIMULATION ACTIONS
	 * BECAUSE IT RECOMPUTES THE EXPONENTIAL THAT IS NOT CONSISTENT WITH THE WEIGHT ONLY IF
	 * THE WEIGHT HASN'T BEEN INCREMENTED YET DURING THE CURRENT ADAPTATION OF THE POLICY.
	 * This method gets a role and one of its moves and returns the PPA info corresponding
	 * to the move of the role. If the exponential is not consistent with the value that the
	 * weight had before any increment took place during the current iteration, it gets recomputed
	 * before returning the PPA info.
	 *
	 * NOTE: here, the currentIteration is already set to count the current iteration as completed,
	 * so it does not match the value that this variable had during the simulation that just ended.
	 * However, the currentIteration is used locally only to the after simulation strategy to keep
	 * track of when an increment took place so that we can know whether the exponential has to be
	 * updated or not. Therefore, this system still works, because every time the after simulation
	 * strategy is executed, the currentIteration will have a higher value that the previous time
	 * the after simulation strategy was executed. The currentIteration has no effect on the selection
	 * of the moves during playout, because there each inconsistent exponential will always be updated
	 * when needed.
	 *
	 * @param role
	 * @param move
	 * @param currentIteration
	 * @return
	 */
	public PpaInfo getPpaInfoForPolicyAdaptation(int role, Move move, int currentIteration){

		// Check if there already is an entry for the move. If not, add it with a
		// weight of 0 and exponential of 1.
		PpaInfo result = this.weightsPerMove.get(role).get(move);
		if(result == null){
			result = new PpaInfo(0.0, 1.0,true, -1);
			this.weightsPerMove.get(role).put(move, result);
		}else if(!result.isConsistent()){
			// If the entry exists, we need to check if the exponential has already been computed.
			// If not, check if it is not consistent because the weight was increased in a previous iteration.
			// If so, update it.
			if(result.getLastWeightUpdateIteration() != currentIteration){
				result.updateExp();
			}
			// If it's not consistent because the weight has already been incremented at least once
			// in the current game iteration, then do not modify it. Use it as is, because it is consistent
			// with the value the weight had during the playout for this iteration.
		}
		return result;

	}


	/**
	 * This method updates the weight of the given move of the given role with the
	 * given increment. The increment is added to the original value of the weight.
	 * Moreover, PpaInfo record that the exponential is now inconsistent with the weight
	 * and that the weight was incremented last in this iteration.
	 * @param role
	 * @param move
	 * @param newValue
	 */
	/*public void incrementWeight(int role, Move move, double increment, int currentIteration){

		// Check if there already is an entry for the move. If not, add it with a
		// weight of 0 and set the exponential to 1.
		PpaInfo result = this.weightsPerMove.get(role).get(move);
		if(result == null){
			result = new PpaInfo(0.0, 1.0, true, currentIteration);
			this.weightsPerMove.get(role).put(move, result);
		}else{
			// Note that it could be that the weight we are increasing belongs to the move that was
			// selected during the simulation. Therefore we didn't need to compute its exponential to
			// calculate the increment. This means that the exponential might not be consistent with
			// the weight and, if we increment the weight we lose the information we need to compute
			// its exponential, in case we need to compute its increment in a node where the move was
			// one of the siblings. In practice, this never happens if the move was chosen in the playout.
			// The fact that we don't need the exponential when incrementing the weight of a move that
			// was in the playout, means that we already had to compute the right exponential when
			// selecting this move in the playout. However, if the move was chosen during MCTS selection,
			// we did not use the policy given by the weights to chose it, therefore we did not update their
			// exponentials either.
			// Here, we check if the exponential needs to be updated.
			if(!result.isConsistent() && result.getLastIncrementIteration() != currentIteration){
				result.updateExp();
			}
		}

		result.incrementWeight(currentIteration, increment);

	}*/

	public String getMinimalInfo(){

		if(this.weightsPerMove != null){
			String weightsPerMoveString = "[ ";

			for(Map<Move,PpaInfo> ppaWeights : this.weightsPerMove){
				weightsPerMoveString += ppaWeights.size() + " entries, ";
			}

			weightsPerMoveString += "]";

			return weightsPerMoveString;
		}else{
			return "null";
		}

	}

	/**
	 * This method prints the weights on the console. Used only for debugging.
	 */
	public void printPpaWeights(){

		String toLog = "";

		if(this.weightsPerMove == null){
			for(int roleIndex = 0; roleIndex < this.weightsPerMove.size(); roleIndex++){
				toLog += ("ROLE=;" + roleIndex + ";\n");
				toLog += "null;\n";
			}
		}else{
			for(int roleIndex = 0; roleIndex < this.weightsPerMove.size(); roleIndex++){
				toLog += ("ROLE=;" + roleIndex + ";\n");
				for(Entry<Move,PpaInfo> moveWeight : this.weightsPerMove.get(roleIndex).entrySet()){
					toLog += ("MOVE=;" + moveWeight.getKey() + ";" + moveWeight.getValue() + "\n");
				}
			}
		}

		toLog += "\n";

		System.out.println(toLog);

		System.out.println();

	}

	public List<Pair<Integer,Double>> getPlayoutProbabilities(int roleIndex, List<Move> roleMoves, double temperature){

		//System.out.println("select");
		//this.printPpaWeights();
		//System.out.println();

		List<Pair<Integer,Double>> probabilities;

		probabilities = new ArrayList<Pair<Integer,Double>>();

		double[] legalMovesForWinnerExponential = new double[roleMoves.size()];

		PpaInfo currentInfo;

		// Iterate over all legal moves to compute the sum of the exponential of their probabilities
		double exponentialSum = 0;
		for(int j = 0; j < roleMoves.size(); j++){
			currentInfo = this.getPpaInfoForSelection(roleIndex, roleMoves.get(j));
			legalMovesForWinnerExponential[j] = Math.pow(currentInfo.getExp(), temperature); // Adding the temperature to the exponential
			exponentialSum += legalMovesForWinnerExponential[j];
		}

		if(exponentialSum == 0){ // If the sum is 0 (should never happen) return null.
			return null;
		}

		// Iterate over all the exponentials and create the probability distribution
		for(int j = 0; j < legalMovesForWinnerExponential.length; j++){

			probabilities.add(new Pair<Integer,Double>(j,legalMovesForWinnerExponential[j]/exponentialSum));

		}

		//this.printPpaWeights();
		//System.out.println();
		//System.out.println("--------------------------------");
		//System.out.println();


		return probabilities;

	}

	public void adaptPolicy(int winnerIndex, List<Move> legalMovesForWinner, Move selectedMoveForWinner,
			double alpha, int currentIteration){

		//System.out.println("adapt");
		//this.printPpaWeights();
		//System.out.println();

		// If we only have one move for the role, we don't need to adapt its weight,
		// because it will always be selected in this state independently of the policy
		// and because if we do increment the weight we break the rule that the sum of
		// weights for the moves of a role in a state has to be always 0.
		if(legalMovesForWinner.size() != 1){// || !this.gameDependentParameters.getTheMachine().convertToExplicitMove(legalMoves.get(0)).getContents().toString().equals("noop")){

			PpaInfo[] legalMovesForWinnerInfo = new PpaInfo[legalMovesForWinner.size()];

			// Iterate over all legal moves to compute the sum of the exponential of their probabilities
			double exponentialSum = 0;
			//System.out.println(exponentialSum);
			for(int j = 0; j < legalMovesForWinner.size(); j++){
				legalMovesForWinnerInfo[j] = this.getPpaInfoForPolicyAdaptation(winnerIndex, legalMovesForWinner.get(j), currentIteration);
				exponentialSum += legalMovesForWinnerInfo[j].getExp();
				//System.out.println(exponentialSum);
			}


			// Iterate over all legal moves and decrease their weight proportionally to their expoenetial.
			// For the selected move also increase the weight by alpha.
			for(int j = 0; j < legalMovesForWinner.size(); j++){

				if(selectedMoveForWinner.equals(legalMovesForWinner.get(j))){
					legalMovesForWinnerInfo[j].incrementWeight(currentIteration, alpha - alpha * (legalMovesForWinnerInfo[j].getExp()/exponentialSum));
					//System.out.println("detected1");
				}else{
					if(exponentialSum > 0){ // Should always be positive
						legalMovesForWinnerInfo[j].incrementWeight(currentIteration, -alpha * (legalMovesForWinnerInfo[j].getExp()/exponentialSum));
					}else{
						GamerLogger.logError("AfterSimulationStrategy", "AdaptivePlayoutAfterSimulation - Found non-positive sum of exponentials when adapting the playout policy!");
						throw new RuntimeException("Found non-positive sum of exponentials when adapting the playout policy.");
					}
				}
			}

		}

		//this.printPpaWeights();
		//System.out.println();
		//System.out.println("--------------------------------");
		//System.out.println();

		//for(Map<Move, Double> weightOfPlayer : this.weightsPerMove){
		//	double sum = 0;
		//	for(Entry<Move,Double> weight : weightOfPlayer.entrySet()){
		//		sum += weight.getValue();
		//	}
		//	System.out.println(sum);
		//}
		//System.out.println();
	}

	// Note that decaying the weights still ensures that their sum is 0 (or close to it because of approximation)!
	public void decayWeights(double decayFactor, int currentIteration){

		if(decayFactor == 0.0){ // If we want to throw away everything, we just clear all the stats. No need to iterate.
			for(int roleIndex = 0; roleIndex < this.weightsPerMove.size(); roleIndex++){
				this.weightsPerMove.get(roleIndex).clear();
			}
		}else if(decayFactor != 1.0){ // If the decay factor is 1.0 we keep everything without modifying anything.
			// Decrease the weight. If the weight is now 0, remove the entry form the map. In this way, we can get
			// rid of weights for moves that will not be legal anymore in the game and are only occupying space.
			// If they are not used anymore, over time the decay will make their weight 0 and thus we will remove
			// them to free space. If the weight becomes zero for a coincidence and the move can still be legal,
			// its weight will be re-added the next time the move is visited.
			Iterator<Entry<Move,PpaInfo>> iterator;
			PpaInfo theInfo;
			for(int roleIndex = 0; roleIndex < this.weightsPerMove.size(); roleIndex++){
				iterator = this.weightsPerMove.get(roleIndex).entrySet().iterator();
				while(iterator.hasNext()){
					theInfo = iterator.next().getValue();
					theInfo.decayWeight(decayFactor, currentIteration);
					if(theInfo.getWeight() == 0){
						iterator.remove();
					}
				}
			}
		}
	}

	public List<Map<Move,PpaInfo>> getWeightsPerMove(){
		return this.weightsPerMove;
	}

	public static void main(String[] args){

		PpaWeights w = new PpaWeights();
		w.setUp(3);

		// Simulate selection at step 0
		w.printPpaWeights();
		//w.getExponentialForSelection(0, new CompactMove(0), 0);
		w.printPpaWeights();
		//w.getExponentialForSelection(0, new CompactMove(2), 0);
		w.printPpaWeights();
		//w.getExponentialForSelection(1, new CompactMove(0), 0);
		w.printPpaWeights();
		//w.getExponentialForSelection(1, new CompactMove(1), 0);
		w.printPpaWeights();
		//w.getExponentialForSelection(2, new CompactMove(2), 0);
		w.printPpaWeights();

		// Simulate adaptation at step 0
		//w.getExponentialForPolicyAdaptation(0, new CompactMove(1), 0);
		w.printPpaWeights();
		//w.getExponentialForPolicyAdaptation(1, new CompactMove(1), 0);
		w.printPpaWeights();
		//w.getExponentialForPolicyAdaptation(2, new CompactMove(1), 0);
		w.printPpaWeights();


		//w.incrementWeight(0, new CompactMove(1), 1, 0);
		w.printPpaWeights();
		//w.incrementWeight(1, new CompactMove(1), -1, 0);
		w.printPpaWeights();
		//w.incrementWeight(2, new CompactMove(1), 2, 0);
		w.printPpaWeights();
		//w.incrementWeight(2, new CompactMove(0), -1, 0);
		w.printPpaWeights();

		// Simulate selection at step 1
		//w.getExponentialForSelection(0, new CompactMove(2), 1);
		w.printPpaWeights();
		//w.getExponentialForSelection(1, new CompactMove(1), 1);
		w.printPpaWeights();

		// Simulate adaptation at step 1
		//w.getExponentialForPolicyAdaptation(2, new CompactMove(0), 1);
		w.printPpaWeights();
		//w.getExponentialForPolicyAdaptation(2, new CompactMove(0), 1);
		w.printPpaWeights();

		//w.incrementWeight(0, new CompactMove(1), 1, 1);
		w.printPpaWeights();
		//w.incrementWeight(0, new CompactMove(1), -1, 1);
		w.printPpaWeights();
		//w.incrementWeight(2, new CompactMove(0), -1, 1);
		w.printPpaWeights();

	}

}
