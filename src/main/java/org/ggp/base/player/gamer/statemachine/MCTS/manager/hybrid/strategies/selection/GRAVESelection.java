package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.OnlineTunableComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GamerConfiguration;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.GRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MCTSNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MCTSJointMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.amafdecoupled.AMAFNode;

public class GRAVESelection extends MoveValueSelection implements OnlineTunableComponent{

	/**
	 * Minimum number of visits that the node must have to be allowed to use its own AMAF statistics.
	 */
	private int[] minAMAFVisits;

	private int initialMinAMAFVisits;

	private double[] valuesForMinAMAFVisits;

	public GRAVESelection(GameDependentParameters gameDependentParameters, Random random,
			GamerConfiguration gamerConfiguration, SharedReferencesCollector sharedReferencesCollector, double valueOffset,
			int initialMinAMAFVisits, GRAVEEvaluator moveEvaluator) {

		super(gameDependentParameters, random, gamerConfiguration, sharedReferencesCollector, valueOffset, moveEvaluator);

		sharedReferencesCollector.setGraveSelection(this);

		this.minAMAFVisits = null;

		this.initialMinAMAFVisits = initialMinAMAFVisits;

	}

	@Override
	public void clearComponent(){

		super.clearComponent();
		this.minAMAFVisits = null;

	}

	@Override
	public void setUpComponent(){

		super.setUpComponent();

		this.minAMAFVisits = new int[this.gameDependentParameters.getNumRoles()];

		for(int i = 0; i < this.gameDependentParameters.getNumRoles(); i++){
			this.minAMAFVisits[i] = this.initialMinAMAFVisits;
		}

	}

	@Override
	public MCTSJointMove select(MCTSNode currentNode) {

		//System.out.println("GRAVE selection");

		if(currentNode instanceof AMAFNode){

			//System.out.println("tot node visits: " + currentNode.getTotVisits());

			// For each role we must check if we have to change the reference to the closest AMAF statistics
			// that have a number of visits higher than the corresponding minAMAFVisits threshold for the role.

			for(int i = 0; i < this.minAMAFVisits.length; i++){
				// For each role first we check if there is no reference to any AMAF statistics, and if so we set
				// the reference to the statistics of the current node (note that this will happen only the first
				// time selection is performed during a simulation, so the current node will be the root).
				// Otherwise, if there is a reference to the closest AMAF statistics for the role, we check if the
				// current node has enough visits (according to the threshold for the role (i.e. minAMAFVisits[roleIndex]))
				// to have its statistics substituted to the currently set ones.
				// This will make sure that if no stats have visits higher than the threshold at least
				// the root stats will be used rather than ignoring amaf values.
				if((((GRAVEEvaluator)this.moveEvaluator).getClosestAmafStats().get(i)) == null || currentNode.getTotVisits() >= this.minAMAFVisits[i]){
					// i.e.: if(ClosestAmafStatsForRole == null || VisitsOfCurrentNode >= ThresholdForRole)

					//if((((GRAVEEvaluator)this.moveEvaluator).getClosestAmafStats()) == null){
					//	System.out.print("Null reference: ");
					//}
					//System.out.println("change");
					((GRAVEEvaluator)this.moveEvaluator).setClosestAmafStats(i, ((AMAFNode)currentNode).getAmafStats());
				}
			}

			return super.select(currentNode);

		}else{
			throw new RuntimeException("GRAVESelection-select(): detected a node not implementing interface AMAFNode.");
		}
	}

	public void resetClosestAmafStats(){

		for(int i = 0; i < ((GRAVEEvaluator)this.moveEvaluator).getClosestAmafStats().size(); i++){
			((GRAVEEvaluator)this.moveEvaluator).setClosestAmafStats(i, null);
		}

	}

	@Override
	public String getComponentParameters(){
		String params = super.getComponentParameters();

		String roleParams = "[ ";

		for(int i = 0; i <this.minAMAFVisits.length; i++){

			roleParams += this.minAMAFVisits[i] + " ";

		}

		roleParams += "]";

		if(params == null){
			return "MIN_AMAF_VISITS = " + roleParams;
		}else{
			return params + ", MIN_AMAF_VISITS = " + roleParams;
		}
	}

	@Override
	public void setNewValues(double[] newValues) {

		// We are tuning only the value of myRole
		if(newValues.length == 1){
			this.minAMAFVisits[this.gameDependentParameters.getMyRoleIndex()] = (int) newValues[0];

			//System.out.println("C = " + this.c[this.gameDependentParameters.getMyRoleIndex()]);

		}else{ // We are tuning all constants
			for(int i = 0; i <this.minAMAFVisits.length; i++){
				this.minAMAFVisits[i] = (int) newValues[i];
			}
		}

	}

	@Override
	public String printOnlineTunableComponent() {

		return "(ONLINE_TUNABLE_COMPONENT = " + this.printComponent() + ")";

	}

	@Override
	public double[] getPossibleValues() {
		return this.valuesForMinAMAFVisits;
	}
}
