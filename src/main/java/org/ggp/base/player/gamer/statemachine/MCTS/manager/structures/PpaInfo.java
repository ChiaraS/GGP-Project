package org.ggp.base.player.gamer.statemachine.MCTS.manager.structures;

public class PpaInfo {

	// FOR DEBUGGING

	//private List<Double> updates;

	/////////////////////////

	private double weight;

	private double exp;

	private int visits;

	/**
	 * True if the exponential is the exponential of the current weight,
	 * false if it's still the exponential of the previous weight and needs
	 * to be recomputed before using it to select moves during the playout
	 * or before starting to update the weight after a simulation.
	 */
	private boolean consistent;

	/**
	 * Memorizes the last iteration in which the weight has been updated.
	 * If it has been updated in this iteration, it means that we are in the "AfterSimulation"
	 * phase, and the exponential of this weight has already been updated to the correct one
	 * for this iteration, because the update has happened at the latest the first time this PpaInfo
	 * was accessed to compute the update value during AfterSimulationAcion.
	 */
	private int lastWeightUpdateIteration;

	// If the exponential is consistent, use it.
	// If it's not consistent there are two options:
	// - it's not consistent from an update in a previous iteration -> needs to be recomputed
	// - it's not consistent because it's been updated in the current iteration -> do nothing, we still need to use the old exponential

	public PpaInfo(double weight, double exp, boolean consistent, int updateIteration) {
		this.weight = weight;
		this.exp = exp;
		this.consistent = consistent;
		this.lastWeightUpdateIteration = updateIteration;
		this.visits = 0;


		//this.updates = new ArrayList<Double>();

	}

	public double getWeight() {
		return weight;
	}

	//public void setWeight(double weight) {
	//	this.weight = weight;
	//}

	public double getExp() {
		//System.out.println(this.exp);
		return this.exp;
	}

	//public void setExp(double exp) {
	//	this.exp = exp;
	//}

	public int getVisits() {
		return this.visits;
	}

	public boolean isConsistent() {
		return consistent;
	}

	//public void setConsistent(boolean consistent) {
	//	this.consistent = consistent;
	//}

	public int getLastWeightUpdateIteration() {
		return this.lastWeightUpdateIteration;
	}

	//public void setIncrementIteration(int incrementIteration) {
	//	this.incrementIteration = incrementIteration;
	//}

	public void updateExp(){

		//if(Math.exp(this.weight) == Double.NaN ||
			//	Math.exp(this.weight) == Double.POSITIVE_INFINITY){
			//System.out.println("Illegal exponential detected!");
			//System.out.println("Old weight = " + this.weight);
			//System.out.println("Old exponential = " + this.exp);
			//System.out.println("New exponnetial = " + (Math.exp(this.weight)));
		//}

		this.exp = Math.exp(this.weight);
		this.consistent = true;
	}

	// Assume that the weight is incremented only once every time a move is visited, therefore
	// every time the weight is incremented we increase the number of visits.
	public void incrementWeight(int incrementIteration, double increment){

		//System.out.println("Given increment;" + increment);

		//System.out.print(increment + " ");

		//this.updates.add(increment);

/*
		if((this.weight + increment) == Double.NaN ||
				(this.weight + increment) == Double.POSITIVE_INFINITY ||
				(this.weight + increment) == Double.NEGATIVE_INFINITY ){
			System.out.println("Illegal weight detected!");
			System.out.println("Old weight = " + this.weight);
			System.out.println("Increment = " + increment);
			System.out.println("New weight = " + (this.weight + increment));
		}
*/
		this.weight += increment;
		this.lastWeightUpdateIteration = incrementIteration;
		this.consistent = false;
		this.visits++;
	}

	public void decayWeight(double decayFactor, int decayIteration){

		if(decayFactor == 0.0){
			this.weight = 0.0;
			this.exp = 1.0;
			this.visits = 0;
			this.consistent = true;
			this.lastWeightUpdateIteration = -1;
		}else if(decayFactor != 1.0){
			this.weight *= decayFactor;
			this.lastWeightUpdateIteration = decayIteration;
			this.consistent = false;
			this.visits = (int) Math.round(((double)this.visits)*decayFactor); // If the weight decays, the number of visits is decreased proportionally to indicate that this weight is less accurate
		}

	}

	/*
	 * THE FOLLOWING METHODS HAVE BEEN ADDED WITH THE INTENT TO REMOVE THE USE OF THE PpaWeights CLASS.
	 * NPPA IS ALREADY USING THESE METHODS, WHILE PPA (I.E ADAPTIVE PLAYOUT) IS NOT. HOWEVER, PPA CAN BE
	 * REPLACED BY USING THE NPPA CLASSES WITH MAX N-GRAM LENGTH SET TO 1.
	 */

	/**
	 * THIS FUNCTION SHOULD BE USED WHEN SELECTING MOVES DURING THE PLAYOUT BECAUSE IT ALWAYS
	 * RECOMPUTES THE EXPONENTIAL IF IT IS NOT CONSISTENT WITH THE WEIGHT.
	 * This method returns the exponential of the weight. If the exponential is not consistent
	 * with the weight, it gets recomputed first.
	 */
	public double getExpForSelection(){

		if(!this.consistent){
			this.updateExp();
		}

		return this.exp;

	}

	/**
	 * THIS FUNCTION SHOULD BE USED WHEN ADAPTING THE POLICY DURING AFTER SIMULATION ACTIONS
	 * BECAUSE IT RECOMPUTES THE EXPONENTIAL THAT IS NOT CONSISTENT WITH THE WEIGHT ONLY IF
	 * THE WEIGHT HASN'T BEEN INCREMENTED YET DURING THE CURRENT ADAPTATION OF THE POLICY.
	 * This method returns the exponential of the weight. If the exponential is not consistent with
	 * the value that the weight had before any increment took place during the current iteration,
	 * it gets recomputed first.
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
	 * @param currentIteration
	 * @return
	 */
	public double getExpForPolicyAdaptation(int currentIteration){

		if(!this.consistent){
			// If the exponential is not consistent because the weight was increased
			// in a previous iteration, update it. If the weight was increased in this
			// iteration do not modify the exponential.
			if(this.lastWeightUpdateIteration != currentIteration){
				this.updateExp();
			}
		}

		return this.exp;

	}


	@Override
	public String toString(){
		return "WEIGHT=;" + this.weight + ";EXP=;" + this.exp + ";VISITS=;" + this.visits + ";CONSISTENT=;" + this.consistent +
				";LAST_WEIGHT_UPDATE_ITERATION=;" + this.lastWeightUpdateIteration + ";";
	}


	/*public String printUpdates(){
		String s = "[ ";
		for(Double d : this.updates){
			s += d + " ";
		}
		s += "]";

		return s;
	}*/

}

