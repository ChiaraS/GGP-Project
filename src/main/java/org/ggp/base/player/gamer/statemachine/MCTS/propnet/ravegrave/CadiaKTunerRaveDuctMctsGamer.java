package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.Individual;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftermove.EvoAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation.EvoAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation.EvoGRAVEAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation.GRAVEAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.GRAVEBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.beforesimulation.EvoBeforeSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion.NoExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.GRAVEPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.GRAVESelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.GRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.AMAFDecoupled.PnAMAFDecoupledTreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.ProverMCTSManager;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class CadiaKTunerRaveDuctMctsGamer extends CadiaRaveDuctMctsGamer {

	protected double evoC;

	protected double evoValueOffset;

	protected Individual[] individuals;

	public CadiaKTunerRaveDuctMctsGamer() {

		super();

		this.evoC = 0.2;

		this.evoValueOffset = 0.01;

		this.individuals = new Individual[8];

		this.individuals[0] = new Individual(10);
		this.individuals[1] = new Individual(50);
		this.individuals[2] = new Individual(100);
		this.individuals[3] = new Individual(250);
		this.individuals[4] = new Individual(500);
		this.individuals[5] = new Individual(750);
		this.individuals[6] = new Individual(1000);
		this.individuals[7] = new Individual(2000);

	}

	@Override
	public InternalPropnetMCTSManager createPropnetMCTSManager() {

		Random r = new Random();

		InternalPropnetRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
		int numRoles = this.thePropnetMachine.getInternalRoles().length;

		List<List<InternalPropnetMove>> allJointMoves = new ArrayList<List<InternalPropnetMove>>();

		GRAVESelection graveSelection = new GRAVESelection(numRoles, myRole, r, this.valueOffset, this.minAMAFVisits, new GRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, this.betaComputer, this.defaultExploration));

		GRAVEPlayout gravePlayout = new GRAVEPlayout(this.thePropnetMachine, allJointMoves);

		SingleParameterEvolutionManager evolutionManager = new SingleParameterEvolutionManager(r, this.evoC, this.evoValueOffset, this.individuals);

		return new InternalPropnetMCTSManager(graveSelection, new NoExpansion() /*new RandomExpansion(numRoles, myRole, r)*/,
				gravePlayout, new GRAVEBackpropagation(numRoles, myRole, allJointMoves), new MaximumScoreChoice(myRole, r),
				new EvoBeforeSimulation(evolutionManager, this.betaComputer),
				new EvoGRAVEAfterSimulation(new GRAVEAfterSimulation(graveSelection, gravePlayout), new EvoAfterSimulation(evolutionManager, myRole)),
				new EvoAfterMove(evolutionManager), new PnAMAFDecoupledTreeNodeFactory(this.thePropnetMachine),
				this.thePropnetMachine, this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);

	}

	@Override
	public ProverMCTSManager createProverMCTSManager(){

		/*
		Random r = new Random();

		Role myRole = this.getRole();
		int numRoles = this.getStateMachine().getRoles().size();

		int myRoleIndex = this.getStateMachine().getRoleIndices().get(this.getRole());

		List<List<Move>> allJointMoves = new ArrayList<List<Move>>();

		ProverGRAVESelection graveSelection = new ProverGRAVESelection(numRoles, myRole, r, this.valueOffset, this.minAMAFVisits, new ProverGRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, this.betaComputer, this.defaultExploration));

		ProverGRAVEPlayout gravePlayout = new ProverGRAVEPlayout(this.getStateMachine(), allJointMoves);

		return new ProverMCTSManager(graveSelection, new ProverNoExpansion() /*new RandomExpansion(numRoles, myRole, r)*//*,
				gravePlayout, new ProverGRAVEBackpropagation(numRoles, myRole, allJointMoves),
				new ProverMaximumScoreChoice(myRoleIndex, r), new ProverGRAVEAfterSimulation(graveSelection, gravePlayout),
				null, new ProverAMAFDecoupledTreeNodeFactory(this.getStateMachine()), this.getStateMachine(),
	       		this.gameStepOffset, this.maxSearchDepth);

	    */

		return null;
	}

}
