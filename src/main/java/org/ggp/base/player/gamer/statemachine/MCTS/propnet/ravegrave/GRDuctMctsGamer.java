package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

import org.ggp.base.player.gamer.statemachine.MCTS.propnet.DuctMctsGamer;

public abstract class GRDuctMctsGamer extends DuctMctsGamer {

	protected int minAMAFVisits;

	/**
	 * True if the player must use the CADIABetaComputer, false if it must use the GRAVEBetaComputer
	 */
	protected boolean cadiaBetaComputer;

	/**
	 * Value for k to use when the player uses the CADIABetaComputer.
	 */
	protected int k;

	/**
	 * Value for bias to use when the player uses the GRAVEBetaComputer.
	 */
	protected double bias;

	protected double defaultExploration;

	public GRDuctMctsGamer(){

		super();

		this.c = 0.2;
		this.unexploredMoveDefaultSelectionValue = 1.0;

		//this.logTranspositionTable = true;

		this.minAMAFVisits = 0;

		this.cadiaBetaComputer = true;
		this.k = 250;

		this.defaultExploration = 1.0;

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

		PnGRAVESelection graveSelection = new PnGRAVESelection(numRoles, myRole, r, this.valueOffset, this.minAMAFVisits, new PnGRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, pnProverBetaComputer, this.defaultExploration));

		return new InternalPropnetMCTSManager(graveSelection, new PnNoExpansion() new RandomExpansion(numRoles, myRole, r),
				new PnGRAVEPlayout(this.thePropnetMachine), new PnGRAVEBackpropagation(numRoles, myRole),
				new PnMaximumScoreChoice(myRole, r), null, new PnGRAVEAfterSimulation(graveSelection),
				null, new PnAMAFDecoupledTreeNodeFactory(this.thePropnetMachine), this.thePropnetMachine,
	       		this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);

	}

	@Override
	public ProverMCTSManager createProverMCTSManager(){

		Random r = new Random();

		ExplicitRole myRole = this.getRole();
		int numRoles = this.getStateMachine().getExplicitRoles().size();

		int myRoleIndex = this.getStateMachine().getRoleIndices().get(this.getRole());

		PnProverBetaComputer pnProverBetaComputer;

		if(this.cadiaBetaComputer){
			pnProverBetaComputer = new PnProverCADIABetaComputer(this.k);
		}else{
			pnProverBetaComputer = new PnProverGRAVEBetaComputer(this.bias);
		}

		ProverGRAVESelection graveSelection = new ProverGRAVESelection(numRoles, myRole, r, this.valueOffset, this.minAMAFVisits, new ProverGRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, pnProverBetaComputer, this.defaultExploration));

		return new ProverMCTSManager(graveSelection, new ProverNoExpansion() new RandomExpansion(numRoles, myRole, r),
				new ProverGRAVEPlayout(this.getStateMachine()), new ProverGRAVEBackpropagation(numRoles, myRole),
				new ProverMaximumScoreChoice(myRoleIndex, r), null, new ProverGRAVEAfterSimulation(graveSelection),
				null, new ProverAMAFDecoupledTreeNodeFactory(this.getStateMachine()), this.getStateMachine(),
	       		this.gameStepOffset, this.maxSearchDepth);


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

		BetaComputer betaComputer;

		if(this.cadiaBetaComputer){
			betaComputer = new CADIABetaComputer(this.k, numRoles, myRoleIndex);
		}else{
			betaComputer = new GRAVEBetaComputer(this.bias, numRoles, myRoleIndex);
		}

		GRAVESelection graveSelection = new GRAVESelection(numRoles, myRoleIndex, r, this.valueOffset, this.minAMAFVisits, new GRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, betaComputer, this.defaultExploration, numRoles, myRoleIndex));

		return new HybridMCTSManager(graveSelection, new NoExpansion() new RandomExpansion(numRoles, myRole, r),
				new GRAVEPlayout(theMachine), new GRAVEBackpropagation(numRoles, myRoleIndex),
				new MaximumScoreChoice(myRoleIndex, r), null, new GRAVEAfterSimulation(graveSelection),
				null, new AMAFDecoupledTreeNodeFactory(theMachine), theMachine,
	       		this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);

	}
	*/
}
