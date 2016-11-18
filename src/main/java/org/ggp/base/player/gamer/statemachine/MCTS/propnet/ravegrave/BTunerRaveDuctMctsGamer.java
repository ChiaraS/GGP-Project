package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;


public class BTunerRaveDuctMctsGamer extends RaveDuctMctsGamer {

	/**
	 * True if the EvolutionManager must be set to tune the value for each role
	 * independently. False if it must tune only the value of the role being
	 * played by the agent in the real game.
	 */
	protected boolean tuneAllRoles;

	protected double evoC;

	protected double evoValueOffset;

	protected double[] individualsValues;

	protected boolean useNormalization;

	public BTunerRaveDuctMctsGamer() {

		super();

		this.tuneAllRoles = false;

		this.evoC = 0.3;

		this.evoValueOffset = 0.01;

		this.individualsValues = new double[8];

		this.individualsValues[0] = 1000;
		this.individualsValues[1] = 100;
		this.individualsValues[2] = 10;
		this.individualsValues[3] = 1;
		this.individualsValues[4] = 0.1;
		this.individualsValues[5] = 0.01;
		this.individualsValues[6] = 0.001;
		this.individualsValues[7] = 0.0001;

		this.useNormalization = false;

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

		Individual[][] population = new Individual[1][];
		population[0] = new Individual[this.individualsValues.length];

		for(int i = 0; i < this.individualsValues.length; i++){
			population[0][i] = new Individual(this.individualsValues[i]);
		}

		SingleParameterEvolutionManager evolutionManager = new SingleParameterEvolutionManager(r, this.evoC, this.evoValueOffset, population, this.useNormalization);

		return new InternalPropnetMCTSManager(graveSelection, new PnNoExpansion() new RandomExpansion(numRoles, myRole, r),
				new PnGRAVEPlayout(this.thePropnetMachine), new PnGRAVEBackpropagation(numRoles, myRole), new PnMaximumScoreChoice(myRole, r),
				new PnEvoBeforeSimulation(evolutionManager, pnProverBetaComputer),
				new PnEvoGRAVEAfterSimulation(new PnGRAVEAfterSimulation(graveSelection), new PnEvoAfterSimulation(evolutionManager, myRole)),
				new PnEvoAfterMove(evolutionManager), new PnAMAFDecoupledTreeNodeFactory(this.thePropnetMachine),
				this.thePropnetMachine, this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);

	}

	@Override
	public ProverMCTSManager createProverMCTSManager(){


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

		BetaComputer betaComputer;

		if(this.cadiaBetaComputer){
			betaComputer = new CADIABetaComputer(this.k, numRoles, myRoleIndex);
		}else{
			betaComputer = new GRAVEBetaComputer(this.bias, numRoles, myRoleIndex);
		}

		GRAVESelection graveSelection = new GRAVESelection(numRoles, myRoleIndex, r, this.valueOffset, this.minAMAFVisits, new GRAVEEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, betaComputer, this.defaultExploration, numRoles, myRoleIndex));

		Individual[][] populations;

		int numPopulations;

		if(this.tuneAllRoles){
			numPopulations = numRoles;
		}else{
			numPopulations = 1;
		}

		populations = new Individual[numPopulations][];

		for(int i = 0; i < populations.length; i++){

			populations[i] = new Individual[this.individualsValues.length];

			for(int j = 0; j < populations[i].length; j++){
				populations[i][j] = new Individual(this.individualsValues[j]);
			}
		}

		Individual[] individuals = new Individual[this.individualsValues.length];

		for(int i = 0; i < this.individualsValues.length; i++){
			individuals[i] = new Individual(this.individualsValues[i]);
		}

		SingleParameterEvolutionManager evolutionManager = new SingleParameterEvolutionManager(r, this.evoC, this.evoValueOffset, populations, this.useNormalization);

		return new HybridMCTSManager(graveSelection, new NoExpansion() /*new RandomExpansion(numRoles, myRole, r),
				new GRAVEPlayout(theMachine), new GRAVEBackpropagation(numRoles, myRoleIndex), new MaximumScoreChoice(myRoleIndex, r),
				new EvoBeforeSimulation(evolutionManager, betaComputer),
				new EvoGRAVEAfterSimulation(new GRAVEAfterSimulation(graveSelection), new EvoAfterSimulation(evolutionManager, myRoleIndex)),
				new EvoAfterMove(evolutionManager), new AMAFDecoupledTreeNodeFactory(theMachine),
				theMachine, this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);

	}
	*/

}
