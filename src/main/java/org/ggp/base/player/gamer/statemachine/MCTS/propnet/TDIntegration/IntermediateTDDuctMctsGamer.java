package org.ggp.base.player.gamer.statemachine.MCTS.propnet.TDIntegration;


public class IntermediateTDDuctMctsGamer extends TDDuctMctsGamer {
/*
	@Override
	public InternalPropnetMCTSManager createPropnetMCTSManager() {

		Random r = new Random();

		CompactRole myRole = this.thePropnetMachine.convertToCompactRole(this.getRole());
		int numRoles = this.thePropnetMachine.getCompactRoles().size();

		PnTDBackpropagation backpropagation = new PnIntermediateTDBackpropagation(this.thePropnetMachine, numRoles, this.qPlayout, this.lambda, this.gamma);

		return new InternalPropnetMCTSManager(new PnUCTSelection(numRoles, myRole, r, this.valueOffset, new PnUCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue)),
	       		new PnNoExpansion(), new PnGoalsMemorizingStandardPlayout(this.thePropnetMachine, new PnRandomJointMoveSelector(this.thePropnetMachine)),
	       		backpropagation, new PnMaximumScoreChoice(myRole, r), null, new PnTDAfterSimulation(backpropagation), null,
	       		new PnDecoupledTreeNodeFactory(this.thePropnetMachine), this.thePropnetMachine,
	       		this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);
	}

	@Override
	public ProverMCTSManager createProverMCTSManager() {
		// TODO Auto-generated method stub
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

		GlobalExtremeValues globalExtremeValues = new GlobalExtremeValues(numRoles, this.defaultGlobalMinValue, this.defaultGlobalMaxValue);

		TDBackpropagation backpropagation = new IntermediateTDBackpropagation(theMachine, numRoles, globalExtremeValues, this.qPlayout, this.lambda, this.gamma);

		return new HybridMCTSManager(new UCTSelection(numRoles, myRoleIndex, r, this.valueOffset, new TDUCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, globalExtremeValues, numRoles, myRoleIndex)),
	       		new NoExpansion(), new GoalsMemorizingStandardPlayout(theMachine, new RandomJointMoveSelector(theMachine)),
	       		backpropagation, new MaximumScoreChoice(myRoleIndex, r), null, new TDAfterSimulation(backpropagation), null,
	       		new TDDecoupledTreeNodeFactory(theMachine), theMachine,
	       		this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);
	}
*/
}
