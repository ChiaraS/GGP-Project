/**
 *
 */
package org.ggp.base.player.gamer.statemachine.MCTS.propnet;




/**
 * @author C.Sironi
 *
 */
public class SlowSUCTMCTSGamer extends UctMctsGamer {

	/**
	 *
	 */
	public SlowSUCTMCTSGamer() {
		super();
	}

	/*
	@Override
	public InternalPropnetMCTSManager createPropnetMCTSManager(){

		Random r = new Random();

		CompactRole myRole = this.thePropnetMachine.convertToCompactRole(this.getRole());
		int numRoles = this.thePropnetMachine.getCompactRoles().size();

		return new InternalPropnetMCTSManager(new PnUCTSelection(numRoles, myRole, r, this.valueOffset, new PnUCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue)),
	       		new PnRandomExpansion(numRoles, myRole, r), new PnRandomPlayout(this.thePropnetMachine),
	       		new PnStandardBackpropagation(numRoles, myRole),	new PnMaximumScoreChoice(myRole, r), null,
	       		null, null, new PnSlowSequentialTreeNodeFactory(this.thePropnetMachine, myRole),
	       		this.thePropnetMachine,	this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);
	}

	@Override
	public ProverMCTSManager createProverMCTSManager(){


		// TODO: IMPLEMENT

		return null;


	}

	@Override
	public HybridMCTSManager createHybridMCTSManager() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 *
	 */
	/*
	public SlowSUCTMCTSGamer(InternalPropnetStateMachine thePropnetMachine) {
		super(thePropnetMachine);
		this.DUCT = false;
	}
	*/

}
