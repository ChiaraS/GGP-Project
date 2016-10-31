package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout;

import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.jointmoveselector.PnJointMoveSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnSimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;

public class PnStandardPlayout implements PnPlayoutStrategy {

	protected InternalPropnetStateMachine theMachine;

	protected PnJointMoveSelector jointMoveSelector;

	public PnStandardPlayout(InternalPropnetStateMachine theMachine, PnJointMoveSelector jointMoveSelector) {
		this.theMachine = theMachine;
		this.jointMoveSelector = jointMoveSelector;
	}

	@Override
	public PnSimulationResult playout(CompactMachineState state, int maxDepth) {
		//InternalPropnetMachineState lastState;

		// NOTE that this is just an extra check: if the state is terminal or the depth limit has been reached,
		// we just return the final goals of the state. At the moment the MCTS manager already doesn't call the
        // play-out if the state is terminal or if the depth limit has been reached, so this check will never be
        // true, but it's here just to be safe.
		if(this.theMachine.isTerminal(state) || maxDepth == 0){

			//if(playoutVisitedNodes != null)
	        //	playoutVisitedNodes[0] = 0;

			GamerLogger.logError("MCTSManager", "Playout strategy shouldn't be called on a terminal node. The MCTSManager must take care of computing the simulation result in this case.");
			//throw new RuntimeException("Playout strategy called on a terminal node.");

			return new PnSimulationResult(0, this.theMachine.getSafeGoalsAvg(state));

		}

        int nDepth = 0;

        List<CompactMove> jointMove;

        do{

        	jointMove = null;
			try {
				//jointMove = this.getJointMove(state);
				jointMove = this.jointMoveSelector.getJointMove(state);
			} catch (MoveDefinitionException e) {
				GamerLogger.logError("MCTSManager", "Exception getting a joint move while performing a playout.");
				GamerLogger.logStackTrace("MCTSManager", e);
				break;
			}
			//state = this.getNextState(state, jointMove);
			state = this.theMachine.getCompactNextState(state, jointMove);

			nDepth++;

        }while(nDepth < maxDepth && !this.theMachine.isTerminal(state));

        //if(playoutVisitedNodes != null)
        //	playoutVisitedNodes[0] = nDepth;

        //System.out.println("Playout state terminal: " + this.theMachine.isTerminal(state));

		//lastState = this.theMachine.performSafeLimitedDepthCharge(state, playoutVisitedNodes, maxDepth);

		// Now try to get the goals of the state.
        //return this.prepareSimulationResult(state);

        return new PnSimulationResult(nDepth, this.theMachine.getSafeGoalsAvg(state));

	}

	/*
	public List<InternalPropnetMove> getJointMove(InternalPropnetMachineState state) throws MoveDefinitionException{
		return this.jointMoveSelector.getJointMove(state);
	}

	public InternalPropnetMachineState getNextState(InternalPropnetMachineState state, List<InternalPropnetMove> jointMove){
		return this.theMachine.getInternalNextState(state, jointMove);
	}

	public SimulationResult prepareSimulationResult(InternalPropnetMachineState state){
		SimulationResult simulationResult = new SimulationResult();

		simulationResult.addGoals(this.theMachine.getSafeGoalsAvg(state));

		return simulationResult;

	}

	*/

	@Override
	public String getStrategyParameters() {
		return this.jointMoveSelector.printJointMoveSelector();
	}

	@Override
	public String printStrategy() {
		String params = this.getStrategyParameters();

		if(params != null){
			return "[PLAYOUT_STRATEGY = " + this.getClass().getSimpleName() + ", " + params + "]";
		}else{
			return "[PLAYOUT_STRATEGY = " + this.getClass().getSimpleName() + "]";
		}
	}

}
