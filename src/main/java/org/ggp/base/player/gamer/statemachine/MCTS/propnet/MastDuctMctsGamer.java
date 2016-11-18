package org.ggp.base.player.gamer.statemachine.MCTS.propnet;


public class MastDuctMctsGamer extends DuctMctsGamer {

	protected double epsilon;

	protected double decayFactor;

	public MastDuctMctsGamer() {
		super();
		this.epsilon = 0.4;
		this.decayFactor = 0.2;
	}

	/*
	@Override
	public InternalPropnetMCTSManager createPropnetMCTSManager(){

		Random r = new Random();

		CompactRole myRole = this.thePropnetMachine.convertToCompactRole(this.getRole());
		int numRoles = this.thePropnetMachine.getCompactRoles().size();

		Map<CompactMove, MoveStats> mastStatistics = new HashMap<CompactMove, MoveStats>();


		MASTStrategy mast = new MASTStrategy(this.thePropnetMachine, this.epsilon, r, mastStatistics, numRoles, myRole);

		return new InternalPropnetMCTSManager(new UCTSelection(numRoles, myRole, r, this.uctOffset, c),
	       		new RandomExpansion(numRoles, myRole, r), mast,	mast, new MaximumScoreChoice(myRole, r),
	       		null, new MASTAfterMove(mastStatistics, decayFactor), new PnDUCTTreeNodeFactory(this.thePropnetMachine),
	       		this.thePropnetMachine, gameStepOffset,	maxSearchDepth);


		return new InternalPropnetMCTSManager(new PnUCTSelection(numRoles, myRole, r, this.valueOffset, new PnUCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue)),
	       		new PnRandomExpansion(numRoles, myRole, r), new PnMASTPlayout(this.thePropnetMachine, r, mastStatistics, this.epsilon),
	       		new PnMASTBackpropagation(numRoles, myRole, mastStatistics),
	       		new PnMaximumScoreChoice(myRole, r), null, null,
	       		new PnMASTAfterMove(mastStatistics, this.decayFactor),
	       		new PnDecoupledTreeNodeFactory(this.thePropnetMachine),
	       		this.thePropnetMachine, this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);
	}

	@Override
	public ProverMCTSManager createProverMCTSManager(){


		// ZZZ!

		return null;


	}

	@Override
	public HybridMCTSManager createHybridMCTSManager(){

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

		Map<Move, MoveStats> mastStatistics = new HashMap<Move, MoveStats>();

		return new HybridMCTSManager(new UCTSelection(numRoles, myRoleIndex, r, this.valueOffset, new UCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, numRoles, myRoleIndex)),
	       		new RandomExpansion(numRoles, myRoleIndex, r), new MASTPlayout(theMachine, new EpsilonMASTJointMoveSelector(theMachine, r, mastStatistics, this.epsilon, numRoles, myRoleIndex)),
	       		new MASTBackpropagation(numRoles, myRoleIndex, mastStatistics),
	       		new MaximumScoreChoice(myRoleIndex, r), null, null,
	       		new MASTAfterMove(mastStatistics, this.decayFactor),
	       		new DecoupledTreeNodeFactory(theMachine),
	       		theMachine, this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);
	}
	*/

}
