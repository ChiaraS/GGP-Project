package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.td;

public class GlobalExtremeValues {

	/**
	 * Minimum value for a state-action pair seen so far during the whole search.
	 */
	private double globalMinValue;

	/**
	 * Maximum value for a state-action pair seen so far during the whole search.
	 */
	private double globalMaxValue;

	/**
	 * Default value to be used when the globalMinValue has not been set yet or has the same value as
	 * globalMaxValue.
	 *
	 * NOTE: if globalMinValue has not been set then also globalMaxValue has not been set yet,
	 * otherwise they are both set (they could still be set to the same value).
	 */
	private double defaultGlobalMinValue;

	/**
	 * Default value to be used when the globalMaxValue has not been set yet or has the same value as
	 * globalMinValue.
	 *
	 * NOTE: if globalMaxValue has not been set then also globalMinValue has not been set yet,
	 * otherwise they are both set (they could still be set to the same value).
	 *
	 * NOTE2: it's also rare that the default values will be used. No selection is performed during the
	 * first simulation of the game and after the first simulation of the entire game both globalMaxValue
	 * and globalMinValue will be set. Only in case these two values are the same then the default values
	 * will be used, but this should be rare. And once the two parameters are set to different values then
	 * they can only diverge and never be equal again.
	 */
	private double defaultGlobalMaxValue;

	public GlobalExtremeValues(double defaultGlobalMinValue, double defaultGlobalMaxValue) {

		this.globalMinValue = Double.MAX_VALUE;
		this.globalMaxValue = -Double.MAX_VALUE;

		this.defaultGlobalMinValue = defaultGlobalMinValue;
		this.defaultGlobalMaxValue = defaultGlobalMaxValue;

	}

	public void setGlobalMinValue(double globalMinValue){
		this.globalMinValue = globalMinValue;
	}

	public void setGlobalMaxValue(double globalMaxValue){
		this.globalMaxValue = globalMaxValue;
	}

	public double getGlobalMinValue(){
		return this.globalMinValue;
	}

	public double getGlobalMaxValue(){
		return this.globalMaxValue;
	}

	public double getDefaultGlobalMinValue(){
		return this.defaultGlobalMinValue;
	}

	public double getDefaultGlobalMaxValue(){
		return this.defaultGlobalMaxValue;
	}

}
