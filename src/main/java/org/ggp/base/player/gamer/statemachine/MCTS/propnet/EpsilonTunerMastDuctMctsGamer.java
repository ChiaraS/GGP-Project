package org.ggp.base.player.gamer.statemachine.MCTS.propnet;


public class EpsilonTunerMastDuctMctsGamer extends OldMastDuctMctsGamer {

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

	public EpsilonTunerMastDuctMctsGamer() {

		super();

		this.tuneAllRoles = false;

		this.evoC = 0.3;

		this.evoValueOffset = 0.01;

		this.individualsValues = new double[11];

		this.individualsValues[0] = 0.0;
		this.individualsValues[1] = 0.1;
		this.individualsValues[2] = 0.2;
		this.individualsValues[3] = 0.3;
		this.individualsValues[4] = 0.4;
		this.individualsValues[5] = 0.5;
		this.individualsValues[6] = 0.6;
		this.individualsValues[7] = 0.7;
		this.individualsValues[8] = 0.8;
		this.individualsValues[9] = 0.9;
		this.individualsValues[10] = 1.0;

		this.useNormalization = false;

	}

	/*
	@Override
	public InternalPropnetMCTSManager createPropnetMCTSManager(){

		return null;
	}



	@Override
	public ProverMCTSManager createProverMCTSManager(){

		Random r = new Random();

		Role myRole = this.getRole();
		int numRoles = this.getStateMachine().getRoles().size();

		int myRoleIndex = this.getStateMachine().getRoleIndices().get(this.getRole());

		return new ProverMCTSManager(new ProverUCTSelection(numRoles, myRole, r, this.valueOffset, new ProverUCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue)),
	       		new ProverRandomExpansion(numRoles, myRole, r), new ProverRandomPlayout(this.getStateMachine()),
	       		new ProverStandardBackpropagation(numRoles, myRole), new ProverMaximumScoreChoice(myRoleIndex, r),
	       		null, null, new ProverDecoupledTreeNodeFactory(this.getStateMachine()), this.getStateMachine(),
	       		this.gameStepOffset, this.maxSearchDepth);

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

		Map<Move, MoveStats> mastStatistics = new HashMap<Move, MoveStats>();

		EpsilonMASTJointMoveSelector epsilonMASTJointMoveSelector = new EpsilonMASTJointMoveSelector(theMachine, r, mastStatistics, this.epsilon, numRoles, myRoleIndex);

		SingleParameterEvolutionManager evolutionManager = new SingleParameterEvolutionManager(r, this.evoC, this.evoValueOffset, populations, this.useNormalization);

		return new HybridMCTSManager(new UCTSelection(numRoles, myRoleIndex, r, this.valueOffset, new UCTEvaluator(this.c, this.unexploredMoveDefaultSelectionValue, numRoles, myRoleIndex)),
	       		new RandomExpansion(numRoles, myRoleIndex, r),
	       		new MASTPlayout(theMachine, epsilonMASTJointMoveSelector),
	       		new MASTBackpropagation(numRoles, myRoleIndex, mastStatistics),
	       		new MaximumScoreChoice(myRoleIndex, r),
	       		new EvoBeforeSimulation(evolutionManager, epsilonMASTJointMoveSelector),
	       		new EvoAfterSimulation(evolutionManager, myRoleIndex),
	       		new EvoMASTAfterMove(new MASTAfterMove(mastStatistics, this.decayFactor), new EvoAfterMove(evolutionManager)),
	       		new DecoupledTreeNodeFactory(theMachine),
	       		theMachine, this.gameStepOffset, this.maxSearchDepth, this.logTranspositionTable);

	}
	*/

}
