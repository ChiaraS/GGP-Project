package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure;

import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.ContinuousTunableParameter;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.TunableParameter;
import org.ggp.base.util.Interval;
import org.ggp.base.util.logging.GamerLogger;

public class ContinuousParametersManager extends ParametersManager {

    private List<ContinuousTunableParameter> continuousTunableParameters;

    public ContinuousParametersManager(GameDependentParameters gameDependentParameters, Random random,
                                       GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {
        super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

        this.continuousTunableParameters = null;

    }

    @Override
    public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		super.setReferences(sharedReferencesCollector);

		this.continuousTunableParameters = sharedReferencesCollector.getTheContinuousParametersToTune();

        if(this.continuousTunableParameters == null || this.continuousTunableParameters.size() == 0){
            GamerLogger.logError("SearchManagerCreation", "ParametersManager - Initialization with null or empty list of continuous tunable parameters!");
            throw new RuntimeException("ParametersManager - Initialization with null or empty list of continuous tunable parameters!");
        }

        this.initialParametersOrder.imposeOrder(this.continuousTunableParameters);

		this.indexOfK = -1;
		this.indexOfRef = -1;

		int i = 0;
		for(TunableParameter t : this.continuousTunableParameters){
			if(t.getName().equals("K")){
				this.indexOfK = i;
			}else if(t.getName().equals("Ref")){
				this.indexOfRef = i;
			}
			i++;
		}

    }

    @Override
    public void clearComponent() {
        super.clearComponent();
    }

    @Override
    public void setUpComponent() {
        super.setUpComponent();
    }


	/**
	 * Returns the name of the parameter at position paramIndex in the list of tunableParameters.
	 *
	 * @param paramIndex
	 * @return
	 */
	@Override
	public String getName(int paramIndex){
		return this.continuousTunableParameters.get(paramIndex).getName();
	}

	/**
	 * Returns the number of parameters being tuned.
	 *
	 * @return
	 */
	@Override
	public int getNumTunableParameters(){
		return this.continuousTunableParameters.size();
	}

	//public double[] getCurrentValuesForParam(int paramIndex){
	//	return this.continuousTunableParameters.get(paramIndex).getCurrentValues();
	//}

	/**
	 * Returns the interval of possible values for the parameter at position paramIndex in
	 * the list of tunableParameters.
	 *
	 * @param paramIndex
	 * @return
	 */
	public Interval getPossibleValuesInterval(int paramIndex){
		return this.continuousTunableParameters.get(paramIndex).getPossibleValuesInterval();
	}

	/**
	 *  Get for each parameter the interval of possible values.
	 */
	public Interval[] getPossibleValuesIntervalForAllParams(){
		Interval[] possibleValuesIntervals = new Interval[this.getNumTunableParameters()];
		for(int paramIndex = 0; paramIndex < possibleValuesIntervals.length; paramIndex++){
			possibleValuesIntervals[paramIndex] = this.getPossibleValuesInterval(paramIndex);
		}
		return possibleValuesIntervals;
	}

	public void setParametersValues(double[][] valuesPerRole){

		int paramIndex = 0;

		// If we are tuning only for my role...
		if(valuesPerRole.length == 1){
			for(ContinuousTunableParameter p : this.continuousTunableParameters){
				p.setMyRoleNewValue(this.gameDependentParameters.getMyRoleIndex(), valuesPerRole[0][paramIndex]);
				paramIndex++;
			}
		}else{ //If we are tuning for all roles...

			double[] newValues;

			for(ContinuousTunableParameter p : this.continuousTunableParameters){

				//System.out.print(c.getClass().getSimpleName() + ": [ ");

				newValues = new double[valuesPerRole.length]; // valuesPerRole.length equals the number of roles for which we are tuning

				for(int roleIndex = 0; roleIndex < newValues.length; roleIndex++){
					newValues[roleIndex] = valuesPerRole[roleIndex][paramIndex];
					//System.out.print(newValues[roleIndex] + " ");
				}

				//System.out.println("]");

				p.setAllRolesNewValues(newValues);

				paramIndex++;
			}
		}
	}

	public void setSingleParameterValues(double[] valuesPerRole, int paramIndex){
		// If we are tuning only for my role...
		if(valuesPerRole.length == 1){
			this.continuousTunableParameters.get(paramIndex).setMyRoleNewValue(this.gameDependentParameters.getMyRoleIndex(),
					valuesPerRole[0]);
		}else{ //If we are tuning for all roles...
			this.continuousTunableParameters.get(paramIndex).setAllRolesNewValues(valuesPerRole);
		}
	}

    @Override
    public String getComponentParameters(String indentation) {
		String superParams = super.getComponentParameters(indentation);

		String params = "";

		if(this.continuousTunableParameters != null){

			String tunableParametersString = "[ ";

			for(ContinuousTunableParameter p : this.continuousTunableParameters){

				tunableParametersString += indentation + "  CONTINUOUS_TUNABLE_PARAMETER = " + p.getParameters(indentation + "  ");

			}

			tunableParametersString += "\n" + indentation + "]";

			params += indentation + "CONTINUOUS_TUNABLE_PARAMETER = " + tunableParametersString;
		}else{
			params += indentation + "CONTINUOUS_TUNABLE_PARAMETER = null";
		}

		if(superParams != null){
			return  params + superParams;
		}else{
			return params;
		}
    }
}
