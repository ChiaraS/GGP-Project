package org.ggp.base.player.gamer.statemachine.MCTS.manager.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;

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
 * after simulation action in one of the previous steps and the exponential is not consistent,
 * the exponential must be computed first. Otherwise, the exponential is returned as it is.
 * For (ii), we always need the exponential of the value that the weight had BEFORE any increment
 * takes place during the current step. Therefore, if the exponential is inconsistent because
 * the last update of the weight happened in an earlier step, we update it before returning it.
 * Otherwise, the reason why the exponential is inconsistent is that the weight has already been
 * incremented in the current step. We know that before the increment could be computed we
 * must have accessed this instance and updated the exponential if it wasn't consistent. So we know
 * that the current exponential is consistent with the value of the weight before the increment. Thus,
 * we return that exponential.
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
 * the simulation with all playouts will be used to update the weights as they were in the previous step. This
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


	public void initialize(int numRoles){
		this.weightsPerMove = new ArrayList<Map<Move,PpaInfo>>();
		for(int roleIndex = 0; roleIndex < numRoles; roleIndex++){
			this.weightsPerMove.add(new HashMap<Move,PpaInfo>());
		}
	}

	/**
	 * THIS FUNCTION SHOULD BE USED WHEN SELECTING MOVES DURING THE PLAYOUT BECAUSE IT ALWAYS
	 * RECOMPUTES THE EXPONENTIAL IF IT IS NOT CONSISTENT WITH THE WEIGHT.
	 * This method gets a role and one of its moves and returns the exponential of the weight
	 * corresponding to the move of the role. If the exponential is not consistent with the
	 * weight, it gets recomputed.
	 *
	 * @param role
	 * @param move
	 * @return
	 */
	public double getExponentialForSelection(int role, Move move, int currentTimeStep){

		// Check if there already is an entry for the move. If not, add it with a
		// weight of 0 and exponential of 1.
		PpaInfo result = this.weightsPerMove.get(role).get(move);
		if(result == null){
			result = new PpaInfo(0.0, 1.0,true, currentTimeStep);
			this.weightsPerMove.get(role).put(move, result);
		}else if(!result.isConsistent()){
			// If the entry exists, we need to check if the exponential has already been computed.
			// If not, compute it.
			result.updateExp();
		}
		return result.getExp();

	}

	/**
	 * THIS FUNCTION SHOULD BE USED WHEN ADAPTING THE POLICY DURING AFTER SIMULATION ACTIONS
	 * BECAUSE IT RECOMPUTES THE EXPONENTIAL THAT IS NOT CONSISTENT WITH THE WEIGHT ONLY IF
	 * THE WEIGHT HASN'T BEEN INCREMENTED YET DURING THE CURRENT ADAPTATION OF THE POLICY.
	 * This method gets a role and one of its moves and returns the exponential of the weight
	 * corresponding to the move of the role. If the exponential is not consistent with the
	 * value that the weight had before any increment took place during the current game step,
	 * it gets recomputed.
	 *
	 * @param role
	 * @param move
	 * @param currentTimeStep
	 * @return
	 */
	public double getExponentialForPolicyAdaptation(int role, Move move, int currentTimeStep){

		// Check if there already is an entry for the move. If not, add it with a
		// weight of 0 and exponential of 1.
		PpaInfo result = this.weightsPerMove.get(role).get(move);
		if(result == null){
			result = new PpaInfo(0.0, 1.0,true, currentTimeStep);
			this.weightsPerMove.get(role).put(move, result);
		}else if(!result.isConsistent()){
			// If the entry exists, we need to check if the exponential has already been computed.
			// If not, check if it is not consistent because the weight was increased in a previous step.
			// If so, update it.
			if(result.getLastIncrementStep() != currentTimeStep){
				result.updateExp();
			}
			// If it's not consistent because the weight has already been incremented at least once
			// in the current game step, then do not modify it. Use it as is, because it is consistent
			// with the value the weight had during the playout for this time step.
		}
		return result.getExp();

	}


	/**
	 * This method updates the weight of the given move of the given role with the
	 * given increment. The increment is added to the original value of the weight.
	 * Moreover, PpaInfo record that the exponential is now inconsistent with the weight
	 * and that the weight was incremented last in this time step.
	 * @param role
	 * @param move
	 * @param newValue
	 */
	public void incrementWeight(int role, Move move, double increment, int currentStep){

		// Check if there already is an entry for the move. If not, add it with a
		// weight of 0 and set the exponential to 1.
		PpaInfo result = this.weightsPerMove.get(role).get(move);
		if(result == null){
			result = new PpaInfo(0.0, 1.0, true, currentStep);
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
			if(!result.isConsistent() && result.getLastIncrementStep() != currentStep){
				result.updateExp();
			}
		}

		result.incrementWeight(currentStep, increment);

	}

	public String getMinimalInfo(){
		String weightsPerMoveString = "[ ";

		for(Map<Move,PpaInfo> ppaWeights : this.weightsPerMove){
			weightsPerMoveString += ppaWeights.size() + " entries, ";
		}

		weightsPerMoveString += "]";

		return weightsPerMoveString;
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
			double weight;
			double exp;
			for(int roleIndex = 0; roleIndex < this.weightsPerMove.size(); roleIndex++){
				toLog += ("ROLE=;" + roleIndex + ";\n");
				for(Entry<Move,PpaInfo> moveWeight : this.weightsPerMove.get(roleIndex).entrySet()){
					toLog += ("MOVE=;" + moveWeight.getKey() + moveWeight.getValue() + ";\n");
				}
			}
		}

		toLog += "\n";

		System.out.println(toLog);

		System.out.println();

	}

	public static void main(String[] args){

		TEST!!!


		PpaWeights w = new PpaWeights();
		w.initialize(3);
		w.printWeights();
		w.getWeightAndExponential(0, new CompactMove(0));
		w.printWeights();
		w.getWeightAndExponential(0, new CompactMove(2));
		w.printWeights();
		w.getWeightAndExponential(1, new CompactMove(0));
		w.printWeights();
		w.getWeightAndExponential(1, new CompactMove(1));
		w.printWeights();
		w.getWeightAndExponential(2, new CompactMove(2));
		w.printWeights();

		w.incrementWeight(0, new CompactMove(1), 1);
		w.printWeights();
		w.incrementWeight(1, new CompactMove(1), -1);
		w.printWeights();
		w.incrementWeight(2, new CompactMove(1), 2);
		w.printWeights();

		w.getWeightAndExponential(0, new CompactMove(1));
		w.printWeights();
		w.getWeightAndExponential(1, new CompactMove(1));
		w.printWeights();
		w.getWeightAndExponential(2, new CompactMove(1));
		w.printWeights();
	}




}
