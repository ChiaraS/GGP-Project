package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.OnlineTunableComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.GraveEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.amafdecoupled.AmafNode;

public class GraveSelection extends MoveValueSelection implements OnlineTunableComponent{

	/**
	 * Minimum number of visits that the node must have to be allowed to use its own AMAF statistics.
	 */
	private int[] minAmafVisits;

	private int initialMinAmafVisits;

	private double[] valuesForMinAmafVisits;

	public GraveSelection(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.minAmafVisits = null;

		this.initialMinAmafVisits = Integer.parseInt(gamerSettings.getPropertyValue("SelectionStrategy.initialMinAmafVisits"));

		// If this component must be tuned online, then we should add its reference to the sharedReferencesCollector
		String toTuneString = gamerSettings.getPropertyValue("SelectionStrategy.tune");
		boolean toTune = Boolean.parseBoolean(toTuneString);
		if(toTune){
			// If we have to tune the component then we look in the setting for all the values that we must use
			// Note: the format for these values in the file must be the following:
			// BetaComputer.valuesForK=v1;v2;...;vn
			// The values are listed separated by ; with no spaces
			String[] values = gamerSettings.getPropertyMultiValue("SelectionStrategy.valuesForMinAmafVisits");
			this.valuesForMinAmafVisits = new double[values.length];
			for(int i = 0; i < values.length; i++){
				this.valuesForMinAmafVisits[i] = Integer.parseInt(values[i]);
			}
			sharedReferencesCollector.setTheComponentToTune(this);
		}else{
			this.valuesForMinAmafVisits = null;
		}

		sharedReferencesCollector.setGraveSelection(this);

	}

	@Override
	public void clearComponent(){

		super.clearComponent();
		this.minAmafVisits = null;

	}

	@Override
	public void setUpComponent(){

		super.setUpComponent();

		this.minAmafVisits = new int[this.gameDependentParameters.getNumRoles()];

		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){
			this.minAmafVisits[i] = this.initialMinAmafVisits;
		}

	}

	@Override
	public MctsJointMove select(MctsNode currentNode) {

		if(currentNode instanceof AmafNode){

			//System.out.println("tot node visits: " + currentNode.getTotVisits());

			// For each role we must check if we have to change the reference to the closest AMAF statistics
			// that have a number of visits higher than the corresponding minAmafVisits threshold for the role.

			for(int i = 0; i < this.minAmafVisits.length; i++){
				// For each role first we check if there is no reference to any AMAF statistics, and if so we set
				// the reference to the statistics of the current node (note that this will happen only the first
				// time selection is performed during a simulation, so the current node will be the root).
				// Otherwise, if there is a reference to the closest AMAF statistics for the role, we check if the
				// current node has enough visits (according to the threshold for the role (i.e. minAmafVisits[roleIndex]))
				// to have its statistics substituted to the currently set ones.
				// This will make sure that if no stats have visits higher than the threshold at least
				// the root stats will be used rather than ignoring amaf values.
				if((((GraveEvaluator)this.moveEvaluator).getClosestAmafStats().get(i)) == null || currentNode.getTotVisits() >= this.minAmafVisits[i]){
					// i.e.: if(ClosestAmafStatsForRole == null || VisitsOfCurrentNode >= ThresholdForRole)

					//if((((GraveEvaluator)this.moveEvaluator).getClosestAmafStats()) == null){
					//	System.out.print("Null reference: ");
					//}
					//System.out.println("change");
					((GraveEvaluator)this.moveEvaluator).setClosestAmafStats(i, ((AmafNode)currentNode).getAmafStats());
				}
			}

			return super.select(currentNode);

		}else{
			throw new RuntimeException("GraveSelection-select(): detected a node not implementing interface AmafNode.");
		}
	}

	public void resetClosestAmafStats(){

		for(int i = 0; i < ((GraveEvaluator)this.moveEvaluator).getClosestAmafStats().size(); i++){
			((GraveEvaluator)this.moveEvaluator).setClosestAmafStats(i, null);
		}

	}

	@Override
	public String getComponentParameters(String indentation){

		String params = indentation + "INITIAL_MIN_AMAF_VSITS = " + this.initialMinAmafVisits;

		if(this.valuesForMinAmafVisits != null){

			String valuesForMinAmafVisitsString = "[ ";

			for(int i = 0; i < this.valuesForMinAmafVisits.length; i++){

				valuesForMinAmafVisitsString += this.valuesForMinAmafVisits[i] + " ";

			}

			valuesForMinAmafVisitsString += "]";

			params += indentation + "VALUES_FOR_TUNING_MIN_AMAF_VISITS = " + valuesForMinAmafVisitsString;
		}else{
			params += indentation + "VALUES_FOR_TUNING_MIN_AMAF_VISITS = null";
		}

		if(this.minAmafVisits != null){

			String minAmafVisitsString = "[ ";

			for(int i = 0; i < this.minAmafVisits.length; i++){

				minAmafVisitsString += this.minAmafVisits[i] + " ";

			}

			minAmafVisitsString += "]";

			params += indentation + "min_amaf_visits = " + minAmafVisitsString;
		}else{
			params += indentation + "min_amaf_visits = null";
		}

		String superParams = super.getComponentParameters(indentation);

		if(superParams == null){
			return params;
		}else{
			return superParams + params;
		}
	}

	@Override
	public void setNewValues(double[] newValues) {

		// We are tuning only the value of myRole
		if(newValues.length == 1){
			this.minAmafVisits[this.gameDependentParameters.getMyRoleIndex()] = (int) newValues[0];

			//System.out.println("C = " + this.c[this.gameDependentParameters.getMyRoleIndex()]);

		}else{ // We are tuning all constants
			for(int i = 0; i <this.minAmafVisits.length; i++){
				this.minAmafVisits[i] = (int) newValues[i];
			}
		}

	}

	@Override
	public String printOnlineTunableComponent(String indentation) {

		return this.printComponent(indentation);

	}

	@Override
	public double[] getPossibleValues() {
		return this.valuesForMinAmafVisits;
	}
}
