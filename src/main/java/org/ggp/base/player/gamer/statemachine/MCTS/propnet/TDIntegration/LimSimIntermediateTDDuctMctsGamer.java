package org.ggp.base.player.gamer.statemachine.MCTS.propnet.TDIntegration;


public class LimSimIntermediateTDDuctMctsGamer extends IntermediateTDDuctMctsGamer {

	private int numExpectedIterations;

	public LimSimIntermediateTDDuctMctsGamer() {

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

		TDBackpropagation backpropagation = new IntermediateTDBackpropagation(theMachine, numRoles, globalExtremeValues, this.qPlayout, this.lambda, this.gamma);

		return new HybridMCTSManager(new UCTSelection(numRoles, myRoleIndex, r, this.valueOffset, new TDUCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, globalExtremeValues, numRoles, myRoleIndex)),
	       		new NoExpansion(), new GoalsMemorizingStandardPlayout(theMachine, new RandomJointMoveSelector(theMachine)),
	       		backpropagation, new MaximumScoreChoice(myRoleIndex, r), null, new TDAfterSimulation(backpropagation), null,
	       		new TDDecoupledTreeNodeFactory(theMachine), theMachine,
	       		this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable, this.numExpectedIterations);
	}
*/
}
