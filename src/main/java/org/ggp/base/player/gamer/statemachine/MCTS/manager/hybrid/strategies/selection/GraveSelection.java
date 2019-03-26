package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.GraveEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parameters.TunableParameter;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.amafdecoupled.AmafNode;
import org.ggp.base.util.statemachine.structure.MachineState;

public class GraveSelection extends MoveValueSelection {

	/**
	 * Minimum number of visits that the node must have to be allowed to use its own AMAF statistics.
	 */
	private TunableParameter ref;

	public GraveSelection(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);

		this.ref = this.createTunableParameter("SelectionStrategy", "Ref", gamerSettings, sharedReferencesCollector);

		/*
		// Get default value for ref (this is the value used for the roles for which we are not tuning the parameter)
		int fixedRef = gamerSettings.getIntPropertyValue("SelectionStrategy.fixedRef");

		if(gamerSettings.getBooleanPropertyValue("SelectionStrategy.tuneRef")){
			// If we have to tune the parameter then we look in the setting for all the values that we must use
			// Note: the format for these values in the file must be the following:
			// SelectionStrategy.valuesForRef=v1;v2;...;vn
			// The values are listed separated by ; with no spaces.
			// We also need to look if a specific order is specified for tuning the parameters. If so, we must get from the
			// settings the unique index that this parameter has in the ordering for the tunable parameters.
			// Moreover, we need to look if a specific order is specified for tuning the parameters. If so, we must get from the
			// settings the unique index that this parameter has in the ordering for the tunable parameters
			int[] possibleValues = gamerSettings.getIntPropertyMultiValue("SelectionStrategy.valuesForRef");
			double[] possibleValuesPenalty = null;
			if(gamerSettings.specifiesProperty("SelectionStrategy.possibleValuesPenaltyForRef")){
				possibleValuesPenalty =  gamerSettings.getDoublePropertyMultiValue("SelectionStrategy.possibleValuesPenaltyForRef");
			}
			int tuningOrderIndex = -1;
			if(gamerSettings.specifiesProperty("SelectionStrategy.tuningOrderIndexRef")){
				tuningOrderIndex =  gamerSettings.getIntPropertyValue("SelectionStrategy.tuningOrderIndexRef");
			}

			this.ref = new IntTunableParameter("Ref", fixedRef, possibleValues, possibleValuesPenalty, tuningOrderIndex);

			// If the parameter must be tuned online, then we should add its reference to the sharedReferencesCollector
			sharedReferencesCollector.addParameterToTune(this.ref);

		}else{
			this.ref = new IntTunableParameter("Ref", fixedRef);
		}*/

		sharedReferencesCollector.setGraveSelection(this);

	}

	@Override
	public void clearComponent(){

		super.clearComponent();
		this.ref.clearParameter();

	}

	@Override
	public void setUpComponent(){

		super.setUpComponent();

		this.ref.setUpParameter(this.gameDependentParameters.getNumRoles());

	}

	@Override
	public MctsJointMove select(MctsNode currentNode, MachineState state) {

		if(currentNode instanceof AmafNode){

			return super.select(currentNode, state);

		}else{
			throw new RuntimeException("GraveSelection-select(): detected a node not implementing interface AmafNode.");
		}
	}

	@Override
	public MctsMove selectPerRole(MctsNode currentNode, MachineState state, int roleIndex) {

		if(currentNode instanceof AmafNode){

			return super.selectPerRole(currentNode, state, roleIndex);

		}else{
			throw new RuntimeException("GraveSelection-selectPerRole(): detected a node not implementing interface AmafNode.");
		}
	}

	@Override
	public void preSelectionActions(MctsNode currentNode) {
		super.preSelectionActions(currentNode);

		if(currentNode instanceof AmafNode){

			//System.out.println("tot node visits: " + currentNode.getTotVisits());

			// For each role we must check if we have to change the reference to the closest AMAF statistics
			// that have a number of visits higher than the corresponding Ref threshold for the role.

			for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){
				// For each role first we check if there is no reference to any AMAF statistics, and if so we set
				// the reference to the statistics of the current node (note that this will happen only the first
				// time selection is performed during a simulation, so the current node will be the root).
				// Otherwise, if there is a reference to the closest AMAF statistics for the role, we check if the
				// current node has enough visits (according to the threshold for the role (i.e. ref[roleIndex]))
				// to have its statistics substituted to the currently set ones.
				// This will make sure that if no stats have visits higher than the threshold at least
				// the root stats will be used rather than ignoring amaf values.
				double refValuePerRole = this.ref.getValuePerRole(i);
				//System.out.println("Role=" + i + "Ref=" + refValuePerRole);
				if((((GraveEvaluator)this.moveEvaluator).getClosestAmafStats().get(i)) == null || currentNode.getTotVisits()[i] >= refValuePerRole){
					// i.e.: if(ClosestAmafStatsForRole == null || VisitsOfCurrentNode >= ThresholdForRole)

					//if((((GraveEvaluator)this.moveEvaluator).getClosestAmafStats()) == null){
					//	System.out.print("Null reference: ");
					//}
					//System.out.println("change");
					((GraveEvaluator)this.moveEvaluator).setClosestAmafStats(i, ((AmafNode)currentNode).getAmafStats());
				}
			}

		}else{
			throw new RuntimeException("GraveSelection-preSelectionActions(): detected a node not implementing interface AmafNode.");
		}

	}

	public void resetClosestAmafStats(){

		for(int i = 0; i < ((GraveEvaluator)this.moveEvaluator).getClosestAmafStats().size(); i++){
			((GraveEvaluator)this.moveEvaluator).setClosestAmafStats(i, null);
		}

	}

	@Override
	public String getComponentParameters(String indentation){

		String params = indentation + "MIN_AMAF_VSITS = " + this.ref.getParameters(indentation + "  ");

		String superParams = super.getComponentParameters(indentation);

		if(superParams == null){
			return params;
		}else{
			return superParams + params;
		}
	}

}
