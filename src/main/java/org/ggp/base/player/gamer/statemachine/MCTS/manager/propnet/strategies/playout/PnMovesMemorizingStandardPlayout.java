package org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.propnet.strategies.playout.jointmoveselector.PnJointMoveSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.propnet.PnSimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.InternalPropnetStateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.structure.compact.CompactMachineState;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;

public class PnMovesMemorizingStandardPlayout extends PnStandardPlayout {

	public PnMovesMemorizingStandardPlayout(InternalPropnetStateMachine theMachine,
			PnJointMoveSelector jointMoveSelector) {
		super(theMachine, jointMoveSelector);

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

        List<List<CompactMove>> allJointMoves = new ArrayList<List<CompactMove>>();

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
			state = this.theMachine.getInternalNextState(state, jointMove);

			allJointMoves.add(jointMove);

			nDepth++;

        }while(nDepth < maxDepth && !this.theMachine.isTerminal(state));

        //if(playoutVisitedNodes != null)
        //	playoutVisitedNodes[0] = nDepth;

        Collections.reverse(allJointMoves);

        return new PnSimulationResult(nDepth, this.theMachine.getSafeGoalsAvg(state), allJointMoves);

        //System.out.println("Playout state terminal: " + this.theMachine.isTerminal(state));

		//lastState = this.theMachine.performSafeLimitedDepthCharge(state, playoutVisitedNodes, maxDepth);

		// Now try to get the goals of the state.
        //return this.prepareSimulationResult(state);

	}

	/*
	@Override
	public List<InternalPropnetMove> getJointMove(InternalPropnetMachineState state) throws MoveDefinitionException{
		List<InternalPropnetMove> theChosenMove =  super.getJointMove(state);
		this.allJointMoves.add(theChosenMove);
		return theChosenMove;
	}

	public void clearLastMemorizedPlayout(){
		this.allJointMoves.clear();
	}

	public void printJM(){
		System.out.println("All joint moves: " + this.allJointMoves.size());

		System.out.println("[");

		for(List<InternalPropnetMove> jm : this.allJointMoves){

			System.out.print("( ");
			for(InternalPropnetMove i : jm){
				System.out.print(i + ", ");
			}
			System.out.println(")");
		}

		System.out.println("]");
	}
	*/

}
