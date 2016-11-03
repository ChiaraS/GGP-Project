package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import java.util.Random;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.Individual;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.evolution.SingleParameterEvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.HybridMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove.EvoAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation.EvoAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation.EvoGRAVEAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation.GRAVEAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.GRAVEBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesimualtion.EvoBeforeSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.expansion.NoExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.movechoice.MaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.GRAVEPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.GRAVESelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.GRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.InternalPropnetMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftermove.PnEvoAfterMove;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation.PnEvoAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation.PnEvoGRAVEAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.aftersimulation.PnGRAVEAfterSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.backpropagation.PnGRAVEBackpropagation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.beforesimulation.PnEvoBeforeSimulation;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.expansion.PnNoExpansion;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.movechoice.PnMaximumScoreChoice;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.PnGRAVEPlayout;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.PnGRAVESelection;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.selection.evaluators.GRAVE.PnGRAVEEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.prover.ProverMCTSManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.amafdecoupled.AMAFDecoupledTreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.amafdecoupled.PnAMAFDecoupledTreeNodeFactory;
import org.ggp.base.util.statemachine.abstractsm.AbstractStateMachine;
import org.ggp.base.util.statemachine.abstractsm.CompactStateMachine;
import org.ggp.base.util.statemachine.abstractsm.ExplicitStateMachine;
import org.ggp.base.util.statemachine.structure.compact.CompactRole;

public class CadiaCTunerRaveDuctMctsGamer extends CadiaRaveDuctMctsGamer {

	protected double evoC;

	protected double evoValueOffset;

	protected double[] individualsValues;

	public CadiaCTunerRaveDuctMctsGamer() {
		super();

		this.evoC = 0.05;

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
	public InternalPropnetMCTSManager createPropnetMCTSManager() {

		Random r = new Random();

		CompactRole myRole = this.thePropnetMachine.convertToCompactRole(this.getRole());
		int numRoles = this.thePropnetMachine.getCompactRoles().size();

		PnGRAVEEvaluator evaluator = new PnGRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, this.betaComputer, this.defaultExploration);

		PnGRAVESelection graveSelection = new PnGRAVESelection(numRoles, myRole, r, this.valueOffset, this.minAMAFVisits, evaluator);

		Individual[] individuals = new Individual[this.individualsValues.length];

		for(int i = 0; i < this.individualsValues.length; i++){
			individuals[i] = new Individual(this.individualsValues[i]);
		}

		SingleParameterEvolutionManager evolutionManager = new SingleParameterEvolutionManager(r, this.evoC, this.evoValueOffset, individuals);

		return new InternalPropnetMCTSManager(graveSelection, new PnNoExpansion() /*new RandomExpansion(numRoles, myRole, r)*/,
				new PnGRAVEPlayout(this.thePropnetMachine), new PnGRAVEBackpropagation(numRoles, myRole), new PnMaximumScoreChoice(myRole, r),
				new PnEvoBeforeSimulation(evolutionManager, evaluator),
				new PnEvoGRAVEAfterSimulation(new PnGRAVEAfterSimulation(graveSelection), new PnEvoAfterSimulation(evolutionManager, myRole)),
				new PnEvoAfterMove(evolutionManager), new PnAMAFDecoupledTreeNodeFactory(this.thePropnetMachine),
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

	@Override
	public HybridMCTSManager createHybridMCTSManager() {

		Random r = new Random();

		int myRoleIndex;
		int numRoles;

		AbstractStateMachine theMachine;

		if(this.thePropnetMachine != null){
			theMachine = new CompactStateMachine(this.thePropnetMachine);
			myRoleIndex = this.thePropnetMachine.convertToCompactRole(this.getRole()).getIndex();
			numRoles = this.thePropnetMachine.getCompactRoles().size();
		}else{
			theMachine = new ExplicitStateMachine(this.getStateMachine());
			numRoles = this.getStateMachine().getExplicitRoles().size();
			myRoleIndex = this.getStateMachine().getRoleIndices().get(this.getRole());
		}

		GRAVEEvaluator evaluator = new GRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, this.betaComputer, this.defaultExploration, numRoles);

		GRAVESelection graveSelection = new GRAVESelection(numRoles, myRoleIndex, r, this.valueOffset, this.minAMAFVisits, evaluator);

		Individual[] individuals = new Individual[this.individualsValues.length];

		for(int i = 0; i < this.individualsValues.length; i++){
			individuals[i] = new Individual(this.individualsValues[i]);
		}

		SingleParameterEvolutionManager evolutionManager = new SingleParameterEvolutionManager(r, this.evoC, this.evoValueOffset, individuals);

		return new HybridMCTSManager(graveSelection, new NoExpansion() /*new RandomExpansion(numRoles, myRole, r)*/,
				new GRAVEPlayout(theMachine), new GRAVEBackpropagation(numRoles, myRoleIndex), new MaximumScoreChoice(myRoleIndex, r),
				new EvoBeforeSimulation(evolutionManager, evaluator),
				new EvoGRAVEAfterSimulation(new GRAVEAfterSimulation(graveSelection), new EvoAfterSimulation(evolutionManager, myRoleIndex)),
				new EvoAfterMove(evolutionManager), new AMAFDecoupledTreeNodeFactory(theMachine),
				theMachine, this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);

	}


}
