package org.ggp.base.player.gamer.statemachine.MCTS.propnet;


public class LimSimDuctMctsGamer extends DuctMctsGamer {

	private int numExpectedIterations;

	public LimSimDuctMctsGamer() {
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

		return new HybridMCTSManager(new UCTSelection(numRoles, myRoleIndex, r, this.valueOffset, new UCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, numRoles, myRoleIndex)),
	       		new NoExpansion(), new RandomPlayout(theMachine), new StandardBackpropagation(numRoles, myRoleIndex),
	       		new MaximumScoreChoice(myRoleIndex, r), null, null, null, new DecoupledTreeNodeFactory(theMachine),
	       		theMachine,	this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable, this.numExpectedIterations);
	}
	*/

}
