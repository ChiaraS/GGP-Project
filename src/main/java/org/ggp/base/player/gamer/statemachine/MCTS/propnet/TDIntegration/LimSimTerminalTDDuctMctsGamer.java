package org.ggp.base.player.gamer.statemachine.MCTS.propnet.TDIntegration;


/**
 * Gamer like TerminalTDDuctMctsGamer, but instead of performing search within the given timeout
 * it always performs a given fixed number of iterations.
 *
 * NOTE that this player will ignore any time limit and ALWAYS finish the set number of iterations.
 *
 * @author C.Sironi
 *
 */
public class LimSimTerminalTDDuctMctsGamer extends TerminalTDDuctMctsGamer {

	private int numExpectedIterations;

	public LimSimTerminalTDDuctMctsGamer() {

		this.numExpectedIterations = 1000;

		this.metagameSearch = false;

	}
/*
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

		GlobalExtremeValues globalExtremeValues = new GlobalExtremeValues(numRoles, this.defaultGlobalMinValue, this.defaultGlobalMaxValue);

		TDBackpropagation backpropagation = new TerminalTDBackpropagation(theMachine, numRoles, globalExtremeValues, this.qPlayout, this.lambda, this.gamma);

		return new HybridMCTSManager(new UCTSelection(numRoles, myRoleIndex, r, this.valueOffset, new TDUCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, globalExtremeValues, numRoles, myRoleIndex)),
	       		new NoExpansion(), new StandardPlayout(theMachine, new RandomJointMoveSelector(theMachine)),
	       		backpropagation, new MaximumScoreChoice(myRoleIndex, r), null, new TDAfterSimulation(backpropagation), null,
	       		new TDDecoupledTreeNodeFactory(theMachine), theMachine,
	       		this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable, this.numExpectedIterations);
	}
	*/

}
