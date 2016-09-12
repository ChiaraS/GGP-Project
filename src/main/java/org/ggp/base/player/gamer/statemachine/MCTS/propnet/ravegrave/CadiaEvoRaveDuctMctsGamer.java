package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.EvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftermove.EvoGRAVEAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation.EvoGRAVEAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.GRAVEBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.beforesimulation.EvoGRAVEBeforeSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion.NoExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.GRAVEPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.GRAVESelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.CADIABetaComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.GRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.treestructure.AMAFDecoupled.PnAMAFDecoupledTreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.ProverMCTSManager;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetMove;
import org.ggp.base.util.statemachine.inernalPropnetStructure.InternalPropnetRole;

public class CadiaEvoRaveDuctMctsGamer extends CadiaRaveDuctMctsGamer {

	protected double evoC;

	protected double evoValueOffset;

	public CadiaEvoRaveDuctMctsGamer() {

		super();

		this.evoC = 0.2;

		this.evoValueOffset = 0.01;

	}

	@Override
	public InternalPropnetMCTSManager createPropnetMCTSManager() {

		Random r = new Random();

		InternalPropnetRole myRole = this.thePropnetMachine.roleToInternalRole(this.getRole());
		int numRoles = this.thePropnetMachine.getInternalRoles().length;

		List<List<InternalPropnetMove>> allJointMoves = new ArrayList<List<InternalPropnetMove>>();

		GRAVESelection graveSelection = new GRAVESelection(numRoles, myRole, r, this.valueOffset, this.minAMAFVisits, new GRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, this.betaComputer, this.defaultExploration));

		GRAVEPlayout gravePlayout = new GRAVEPlayout(this.thePropnetMachine, allJointMoves);

		EvolutionManager evolutionManager = new EvolutionManager(r, this.evoC, this.evoValueOffset);

		return new InternalPropnetMCTSManager(graveSelection, new NoExpansion() /*new RandomExpansion(numRoles, myRole, r)*/,
				gravePlayout, new GRAVEBackpropagation(numRoles, myRole, allJointMoves), new MaximumScoreChoice(myRole, r),
				new EvoGRAVEBeforeSimulation(evolutionManager, ((CADIABetaComputer)this.betaComputer)),
				new EvoGRAVEAfterSimulation(graveSelection, gravePlayout, evolutionManager, myRole),
				new EvoGRAVEAfterMove(evolutionManager), new PnAMAFDecoupledTreeNodeFactory(this.thePropnetMachine),
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
