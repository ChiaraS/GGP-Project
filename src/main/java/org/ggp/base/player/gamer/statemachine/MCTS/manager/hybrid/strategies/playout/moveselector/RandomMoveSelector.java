package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.moveselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.Role;

public class RandomMoveSelector extends MoveSelector{

	public RandomMoveSelector(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector){
		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		// No need for any reference
	}

	@Override
	public void clearComponent() {
		// Do nothing
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}

	@Override
	public List<Move> getJointMove(List<List<Move>> legalMovesPerRole, MachineState state) throws MoveDefinitionException, StateMachineException {

		List<Move> jointMove = new ArrayList<Move>();

		if(legalMovesPerRole == null || legalMovesPerRole.isEmpty()) { // If null or empty it means the state is not memorizing the moves
			for(int i = 0; i < this.gameDependentParameters.getTheMachine().getRoles().size(); i++) {
				jointMove.add(this.getMoveForRole(null, state, this.gameDependentParameters.getTheMachine().getRoles().get(i)));
			}
		}else if(legalMovesPerRole.size() == this.gameDependentParameters.getNumRoles()) {
			for(int i = 0; i < this.gameDependentParameters.getTheMachine().getRoles().size(); i++) {
				jointMove.add(this.getMoveForRole(legalMovesPerRole.get(i), state, this.gameDependentParameters.getTheMachine().getRoles().get(i)));
			}
		}else { // We have the moves but not for all roles => there is something wrong with the input
			GamerLogger.logError("MoveSelector", "RandomMoveSelector - Trying to select a joint move giving the wrong number of lists with legal moves for the roles.");
			throw new RuntimeException("RandomMoveSelector - Trying to select a joint move giving the wrong number of lists with legal moves for the roles");
		}

		return jointMove;
	}

	@Override
	public String getComponentParameters(String indentation) {
		return null;
	}

	@Override
	public Move getMoveForRole(List<Move> legalMoves, MachineState state, Role role) throws MoveDefinitionException, StateMachineException {
		if(legalMoves == null || legalMoves.isEmpty()) {
			legalMoves = this.gameDependentParameters.getTheMachine().getLegalMoves(state, role);
		}

		return legalMoves.get(this.random.nextInt(legalMoves.size()));
	}

}
