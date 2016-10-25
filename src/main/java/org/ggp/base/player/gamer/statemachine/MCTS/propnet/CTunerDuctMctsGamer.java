package org.ggp.base.player.gamer.statemachine.MCTS.propnet;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.Individual;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftermove.PnEvoAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation.PnEvoAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.PnStandardBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.beforesimulation.PnEvoBeforeSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion.PnRandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice.PnMaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.PnRandomPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.PnUCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.PnUCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.decoupled.PnDecoupledTreeNodeFactory;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;


public class CTunerDuctMctsGamer extends DuctMctsGamer {

	protected double evoC;

	protected double evoValueOffset;

	protected double[] individualsValues;

	public CTunerDuctMctsGamer() {

		super();

		this.evoC = 0.2;

		this.evoValueOffset = 0.01;

		this.individualsValues = new double[9];

		this.individualsValues[0] = 0.1;
		this.individualsValues[1] = 0.2;
		this.individualsValues[2] = 0.3;
		this.individualsValues[3] = 0.4;
		this.individualsValues[4] = 0.5;
		this.individualsValues[5] = 0.6;
		this.individualsValues[6] = 0.7;
		this.individualsValues[7] = 0.8;
		this.individualsValues[8] = 0.9;

	}

	@Override
	public InternalPropnetMCTSManager createPropnetMCTSManager(){

		Random r = new Random();

		InternalPropnetRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
		int numRoles = this.thePropnetMachine.getInternalRoles().length;

		PnUCTEvaluator evaluator = new PnUCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue);

		Individual[] individuals = new Individual[this.individualsValues.length];

		for(int i = 0; i < this.individualsValues.length; i++){
			individuals[i] = new Individual(this.individualsValues[i]);
		}

		SingleParameterEvolutionManager evolutionManager = new SingleParameterEvolutionManager(r, this.evoC, this.evoValueOffset, individuals);

		return new InternalPropnetMCTSManager(new PnUCTSelection(numRoles, myRole, r, this.valueOffset, evaluator),
	       		new PnRandomExpansion(numRoles, myRole, r), new PnRandomPlayout(this.thePropnetMachine),
	       		new PnStandardBackpropagation(numRoles, myRole), new PnMaximumScoreChoice(myRole, r),
	       		new PnEvoBeforeSimulation(evolutionManager, evaluator), new PnEvoAfterSimulation(evolutionManager, myRole),
	       		new PnEvoAfterMove(evolutionManager), new PnDecoupledTreeNodeFactory(this.thePropnetMachine),
	       		this.thePropnetMachine,	this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);
	}


/*
	@Override
	public ProverMCTSManager createProverMCTSManager(){

		Random r = new Random();

		Role myRole = this.getRole();
		int numRoles = this.getStateMachine().getRoles().size();

		int myRoleIndex = this.getStateMachine().getRoleIndices().get(this.getRole());

		return new ProverMCTSManager(new ProverUCTSelection(numRoles, myRole, r, this.valueOffset, new ProverUCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue)),
	       		new ProverRandomExpansion(numRoles, myRole, r), new ProverRandomPlayout(this.getStateMachine()),
	       		new ProverStandardBackpropagation(numRoles, myRole), new ProverMaximumScoreChoice(myRoleIndex, r),
	       		null, null, new ProverDecoupledTreeNodeFactory(this.getStateMachine()), this.getStateMachine(),
	       		this.gameStepOffset, this.maxSearchDepth);

	}
*/
}
