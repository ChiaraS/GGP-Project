package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.ParametersManager;

/**
 * ATTENTION! NOT ALL IMPLEMENTATIONS OF THIS CLASS PROVIDE A WAY TO CONSIDER A BIAS
 * FOR THE PARAMETERS EVEN IF ALL IMPLEMENTATIONS ALLOW TO SPECIFY SUCH BIAS IN THE
 * SETTINGS. If you want to consider the bias, make sure that the class is implemented
 * to do so.
 *
 * ATTENTION! NOT ALL IMPLEMENTATIONS OF THIS CLASS CAN CORRECTLY HANDLE THE MEMORIZATION
 * AND RE-USE OF THE BEST COMBINATION OF PARAMETERS FOUND IN PREVIOUS GAMES, EVEN IF ALL
 * IMPLEMENTATIONS ALLOW TO SPECIFY THAT WE WANT TO MEMORIZE AND RE-USE THE BEST COMBINA-
 * TION. If you want to re-use the best combination make sure that the class is implemented
 * to do so correctly.
 *
 * @author c.sironi
 *
 */
public abstract class ParametersTuner extends SearchManagerComponent{

	/**
	 * True if the tuner is still being used to tune parameters, false otherwise.
	 * This parameter is needed when we only have a limited simulations budget to
	 * tune parameters. In this case we need to know when we are done tuning (i.e.
	 * the tuning budget expired) so that the AfterSimulationStrategy will also
	 * stop updating the statistics of the parameters.
	 */
	protected boolean tuning;

	/**
	 * If true, parameters will be tuned for all the roles, otherwise only for the playing role.
	 */
	protected boolean tuneAllRoles;

	/**
	 * When parameters are tuned only for the playing role (i.e. tuneAllRoles=false), this parameter
	 * controls how the other roles are modeled. If true, the parameters for the other roles are randomized,
	 * otherwise they are kept fixed to the default values.
	 */
	protected boolean randomOpponents;

	/**
	 * True if the tuner must memorize the best combinations found after the end of the first game
	 * that has been won and then re-use them for all subsequent games.
	 */
	protected boolean reuseBestCombos;

	protected boolean reuseStats;

	/**
	 * Names of the classes being considered, i.e. names of the parameters being tuned.
	 */
	//protected String[] classesNames;

	/**
	 * Size of the classes of unit actions of the combinatorial problem.
	 *
	 * Each parameter being tuned corresponds to one class. The size of a class is the number of
	 * different values that the parameter can assume. Each unit move for a class corresponds to
	 * the assignment of one of the available values to the parameter. A combinatorial move is
	 * thus an assignment of a value to each of the parameters being tuned.
	 *
	 * Note that in this class we represent each unit move as the index in the list of possible
	 * values of the value that the move assigns to the parameter corresponding to the class.
	 * A combinatorial move is thus a list of indices, each of them indicating the index of the
	 * selected value for the corresponding parameter.
	 *
	 * Note that since we represent each unit move as an index we don't need to know he exact values
	 * that each unit move assigns to the corresponding parameter. It's sufficient to know how many
	 * parameters there are per class to know how many indices we have to deal with.
	 *
	 * Note that we either tune all the parameters only for our role or for all the roles. Tuning
	 * different parameters for different roles is not permitted.
	 *
	 */
	//protected int[] classesLength;

	/**
	 * Needed only for logging. Given this values we could avoid keeping as parameter the classesLenght,
	 * because the length of each class can be deduced from here.
	 */
	//protected String[][] classesValues;

	/**
	 * For each class, for each unit move in the class, this array specifies the penalty.
	 * NOTE: the penalty values must be specified for either all or none of the classes,
	 * otherwise an exception must be thrown.
	 * If there is no penalty specified in the gamers settings for any of the classes,
	 * then this pointer will be null.
	 */
	//protected double[][] unitMovesPenalty;

	public ParametersTuner(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.tuning = true;

		this.tuneAllRoles = gamerSettings.getBooleanPropertyValue("ParametersTuner.tuneAllRoles");

		if(!this.tuneAllRoles && gamerSettings.specifiesProperty("ParametersTuner.randomOpponents")) {
			this.randomOpponents = gamerSettings.getBooleanPropertyValue("ParametersTuner.randomOpponents");
		}else {
			this.randomOpponents = false;
		}

		this.reuseBestCombos = gamerSettings.getBooleanPropertyValue("ParametersTuner.reuseBestCombos");

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector){
		// Do nothing
	}

	/*
	public ParametersTuner(ParametersTuner toCopy) {
		super(toCopy);

		this.tuneAllRoles = toCopy.isTuningAllRoles();

		this.classesNames = null;

		this.classesLength = null;

		this.classesValues = null;

		this.unitMovesPenalty = null;

	}*/

	/*
	public void setClassesAndPenalty(String[] classesNames, int[] classesLength, String[][] classesValues, double[][] unitMovesPenalty) {
		this.classesNames = classesNames;
		this.classesLength = classesLength;
		this.classesValues = classesValues;
		this.unitMovesPenalty = unitMovesPenalty;
	}
	*/

	@Override
	public void clearComponent() {
		this.tuning = false;
	}

	@Override
	public void setUpComponent() {
		this.tuning = true;
	}

	@Override
	public String getComponentParameters(String indentation) {

		String params = indentation + "TUNE_ALL_ROLES = " + this.tuneAllRoles +
				indentation + "RANDOM-OPPONENTS = " + this.randomOpponents +
				indentation + "REUSE_BEST_COMBOS = " + this.reuseBestCombos +
				indentation + "REUSE_STATS = " + this.reuseStats +
				indentation + "tuning = " + this.tuning;

		/*
		if(this.classesLength != null){
			String classesLengthString = "[ ";

			for(int i = 0; i < this.classesLength.length; i++){

				classesLengthString += this.classesLength[i] + " ";

			}

			classesLengthString += "]";

			params += indentation + "CLASSES_LENGTH = " + classesLengthString;
		}else{
			params += indentation + "CLASSES_LENGTH = null";
		}

		if(this.unitMovesPenalty != null){
			String unitMovesPenaltyString = "[ ";

			for(int i = 0; i < this.unitMovesPenalty.length; i++){

				unitMovesPenaltyString += this.unitMovesPenalty[i] + " ";

			}

			unitMovesPenaltyString += "]";

			params += indentation + "UNIT_MOVES_PENALTY = " + unitMovesPenaltyString;
		}else{
			params += indentation + "UNIT_MOVES_PENALTY = null";
		}
		*/

		return params;

	}

	/*
	 *  TODO: for all ParametersTuners change the code so that if there is a structure keeping track
	 *  of the next parameter combination to check, this structure advances to the next combination before
	 *  executing setNextCombinations() and not after executing updateStatistics(). This is needed because
	 *  it should be allowed to call updateStatistics() multiple times with multiple goals for the same
	 *  combination without advancing to the next combination. updateStatistics() can be called multiple
	 *  times when using multiple playouts or when using a batch of simulations to evaluate the same combinations.
	 */
	public abstract void setNextCombinations();

	public abstract void setBestCombinations();

	public abstract void updateStatistics(double[] goals);

	public abstract void logStats();

    /**
     * This method keeps factor*oldStatistic statistics. Factor should be in the interval [0,1].
     *
     * @param factor
     */
    public abstract void decreaseStatistics(double factor);

	public boolean isTuningAllRoles(){
		return this.tuneAllRoles;
	}

	protected void stopTuning(){
		this.tuning = false;
	}

	public boolean isTuning(){
		return this.tuning;
	}

	public abstract boolean isMemorizingBestCombo();

	public abstract void memorizeBestCombinations();

	protected String getGlobalParamsOrder(){
		String globalParamsOrder = "[ ";
		for(int paramIndex = 0; paramIndex < this.getParametersManager().getNumTunableParameters(); paramIndex++){
			globalParamsOrder += (this.getParametersManager().getName(paramIndex) + " ");
		}
		globalParamsOrder += "]";

		return globalParamsOrder;
	}

	public abstract ParametersManager getParametersManager();

}
