package org.ggp.base.player.gamer.statemachine.MCTS.manager.structures;


public class PpaInfo {

	private double weight;

	private double exp;

	/**
	 * True if the exponential is the exponential of the current weight,
	 * false if it's still the exponential of the previous weight and needs
	 * to be recomputed before using it to select moves during the playout
	 * or before starting to update the weight after a simulation.
	 */
	private boolean consistent;

	/**
	 * Memorizes the last step in which the weight has been updated.
	 * If it has been updated in this step, it means that we are in the "AfterSimulation"
	 * phase, and the exponential of this weight has already been updated to the correct one
	 * for this step, because the update has happened at the latest the first time this PpaInfo
	 * was accessed to compute the update value during AfterSimulationAcion.
	 */
	private int lastIncrementStep;

	// If the exponential is consistent, use it.
	// If it's not consistent there are two options:
	// - it's not consistent from an update in a previous step -> needs to be recomputed
	// - it's not consistent because it's been updated in the current step -> do nothing, we still need to use the old exponential

	public PpaInfo(double weight, double exp, boolean consistent, int incrementStep) {
		this.weight = weight;
		this.exp = exp;
		this.consistent = consistent;
		this.lastIncrementStep = incrementStep;
	}

	public double getWeight() {
		return weight;
	}

	//public void setWeight(double weight) {
	//	this.weight = weight;
	//}

	public double getExp() {
		return exp;
	}

	//public void setExp(double exp) {
	//	this.exp = exp;
	//}

	public boolean isConsistent() {
		return consistent;
	}

	//public void setConsistent(boolean consistent) {
	//	this.consistent = consistent;
	//}

	public int getLastIncrementStep() {
		return this.lastIncrementStep;
	}

	//public void setIncrementStep(int incrementStep) {
	//	this.incrementStep = incrementStep;
	//}

	public void updateExp(){
		this.exp = Math.exp(this.weight);
		this.consistent = true;
	}

	public void incrementWeight(int incrementStep, double increment){
		this.weight += increment;
		this.lastIncrementStep = incrementStep;
		this.consistent = false;
	}

	@Override
	public String toString(){
		return "WEIGHT=;" + this.weight + ";EXP=;" + this.exp + ";CONSISTENT=;" + this.consistent +
				";LAST_INRCEMENT_STEP=;" + this.lastIncrementStep + ";";
	}
}
