package org.ggp.base.player.gamer.statemachine.MCTS.propnet;


public class OldDuctMctsGamer extends UctMctsGamer {

	public OldDuctMctsGamer(){

		this.unexploredMoveDefaultSelectionValue = Double.MAX_VALUE;
	}

	/*
	@Override
	public InternalPropnetMCTSManager createPropnetMCTSManager(){

	Random r = new Random();

	CompactRole myRole = this.thePropnetMachine.convertToCompactRole(this.getRole());
	int numRoles = this.thePropnetMachine.getCompactRoles().size();

	return new InternalPropnetMCTSManager(new PnUCTSelection(numRoles, myRole, r, this.valueOffset, new PnUCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue)),
       		new PnRandomExpansion(numRoles, myRole, r), new PnRandomPlayout(this.thePropnetMachine),
       		new PnStandardBackpropagation(numRoles, myRole), new PnMaximumScoreChoice(myRole, r), null,
       		null, null, new PnDecoupledTreeNodeFactory(this.thePropnetMachine), this.thePropnetMachine,
       		this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);
	}

	@Override
	public ProverMCTSManager createProverMCTSManager(){


		// ZZZ!

		return null;


	}

	@Override
	public HybridMCTSManager createHybridMCTSManager() {
		// TODO Auto-generated method stub
		return null;
	}
	*/

}
