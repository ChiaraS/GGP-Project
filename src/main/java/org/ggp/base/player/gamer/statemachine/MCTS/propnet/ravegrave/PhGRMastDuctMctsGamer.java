package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;


public abstract class PhGRMastDuctMctsGamer extends GRMastDuctMctsGamer {

	protected double w;

	public PhGRMastDuctMctsGamer() {

		super();

		this.w = 5.0;

	}
/*
	@Override
	public InternalPropnetMCTSManager createPropnetMCTSManager() {

		Random r = new Random();

		CompactRole myRole = this.thePropnetMachine.convertToCompactRole(this.getRole());
		int numRoles = this.thePropnetMachine.getCompactRoles().size();

		PnProverBetaComputer pnProverBetaComputer;

		if(this.cadiaBetaComputer){
			pnProverBetaComputer = new PnProverCADIABetaComputer(this.k);
		}else{
			pnProverBetaComputer = new PnProverGRAVEBetaComputer(this.bias);
		}

		PnProgressiveHistoryGRAVESelection graveSelection = new PnProgressiveHistoryGRAVESelection(numRoles, myRole, r, this.valueOffset, this.minAMAFVisits, new PnProgressiveHistoryGRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, pnProverBetaComputer, this.defaultExploration, this.w));

		Map<CompactMove, MoveStats> mastStatistics = new HashMap<CompactMove, MoveStats>();

		// Note that the after simulation strategy GRAVEAfterSimulation already performs all the after simulation
		// actions needed by the MAST strategy, so we don't need to change it when we use GRAVE and MAST together.
		return new InternalPropnetMCTSManager(graveSelection, new PnNoExpansion() new RandomExpansion(numRoles, myRole, r),
				new PnMASTPlayout(this.thePropnetMachine, r, mastStatistics, this.epsilon), new PnMASTGRAVEBackpropagation(numRoles, myRole, mastStatistics),
				new PnMaximumScoreChoice(myRole, r), null, new PnGRAVEAfterSimulation(graveSelection),
				new PnPhMASTAfterMove(new PnMASTAfterMove(mastStatistics, this.decayFactor), new PnProgressiveHistoryAfterMove(graveSelection)),
				new PnAMAFDecoupledTreeNodeFactory(this.thePropnetMachine),
				this.thePropnetMachine,	this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);
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

		BetaComputer betaComputer;

		if(this.cadiaBetaComputer){
			betaComputer = new CADIABetaComputer(this.k, numRoles, myRoleIndex);
		}else{
			betaComputer = new GRAVEBetaComputer(this.bias, numRoles, myRoleIndex);
		}

		ProgressiveHistoryGRAVESelection graveSelection = new ProgressiveHistoryGRAVESelection(numRoles, myRoleIndex, r, this.valueOffset, this.minAMAFVisits, new ProgressiveHistoryGRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, betaComputer, this.defaultExploration, this.w, numRoles, myRoleIndex));

		Map<Move, MoveStats> mastStatistics = new HashMap<Move, MoveStats>();

		// Note that the after simulation strategy GRAVEAfterSimulation already performs all the after simulation
		// actions needed by the MAST strategy, so we don't need to change it when we use GRAVE and MAST together.
		return new HybridMCTSManager(graveSelection, new NoExpansion() new RandomExpansion(numRoles, myRole, r),
				new MASTPlayout(theMachine, new EpsilonMASTJointMoveSelector(theMachine, r, mastStatistics, this.epsilon, numRoles, myRoleIndex)),
				new MASTGRAVEBackpropagation(numRoles, myRoleIndex, mastStatistics),
				new MaximumScoreChoice(myRoleIndex, r), null, new GRAVEAfterSimulation(graveSelection),
				new PhMASTAfterMove(new MASTAfterMove(mastStatistics, this.decayFactor), new ProgressiveHistoryAfterMove(graveSelection)),
				new AMAFDecoupledTreeNodeFactory(theMachine), theMachine, this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);


	}
	*/

}
