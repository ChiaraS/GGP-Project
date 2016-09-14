package org.ggp.base.player.gamer.statemachine.MCTS.propnet;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.Individual;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftermove.EvoAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation.EvoAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.StandardBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.beforesimulation.EvoBeforeSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion.RandomExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.RandomPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.UCTSelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.UCTEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.decoupled.PnDecoupledTreeNodeFactory;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;


public class CTunerDuctMctsGamer extends DuctMctsGamer {

	protected double evoC;

	protected double evoValueOffset;

	protected Individual[] individuals;

	public CTunerDuctMctsGamer() {

		super();

		this.evoC = 0.2;

		this.evoValueOffset = 0.01;

		this.individuals = new Individual[9];

		this.individuals[0] = new Individual(0.1);
		this.individuals[1] = new Individual(0.2);
		this.individuals[2] = new Individual(0.3);
		this.individuals[3] = new Individual(0.4);
		this.individuals[4] = new Individual(0.5);
		this.individuals[5] = new Individual(0.6);
		this.individuals[6] = new Individual(0.7);
		this.individuals[7] = new Individual(0.8);
		this.individuals[7] = new Individual(0.9);

	}

	@Override
	public InternalPropnetMCTSManager createPropnetMCTSManager(){

		Random r = new Random();

		InternalPropnetRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
		int numRoles = this.thePropnetMachine.getInternalRoles().length;

		UCTEvaluator evaluator = new UCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue);

		SingleParameterEvolutionManager evolutionManager = new SingleParameterEvolutionManager(r, this.evoC, this.evoValueOffset, this.individuals);

		return new InternalPropnetMCTSManager(new UCTSelection(numRoles, myRole, r, this.valueOffset, evaluator),
	       		new RandomExpansion(numRoles, myRole, r), new RandomPlayout(this.thePropnetMachine),
	       		new StandardBackpropagation(numRoles, myRole), new MaximumScoreChoice(myRole, r),
	       		new EvoBeforeSimulation(evolutionManager, evaluator), new EvoAfterSimulation(evolutionManager, myRole),
	       		new EvoAfterMove(evolutionManager), new PnDecoupledTreeNodeFactory(this.thePropnetMachine),
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
