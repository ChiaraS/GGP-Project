package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SearchManagerComponent;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.moveselector.MoveSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.MctsNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.SimulationResult;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.reflection.ProjectSearcher;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.MachineState;
import org.ggp.base.util.statemachine.structure.Move;

/**
 * For this class we can also set the number of playouts that should be performed every time from the given
 * state. Usually, for each call of the playout method only one playout is performed, but for this class we
 * can set the parameter numSimulationsPerPlayout to specify how many playouts must be performed every time
 * the playout method is called. NOTE that we allow to have more repetitions for the playout to enable a
 * comparison with the
 *
 * @author C.Sironi
 *
 */
public class StandardPlayout extends PlayoutStrategy {

	/**
	 * NOTE: to obtain the desired playout type set the moveSelector and the StandardPlayout subclass as follows:
	 * RANDOM PLAYOUT = RandomMoveSelector - StandardPlayout
	 * MAST PLAYOUT = EpsilonMastMoveSelector - MovesMemorizingStandardPlayout
	 * GRAVE PLAYOUT = RandomMoveSelector - MovesMemorizingStandardPlayout
	 */
	protected MoveSelector moveSelector;

	/**
	 * List with all the joint moves selected so far in the current simulation.
	 */
	//protected List<MctsJointMove> currentSimulationJointMoves;

	public StandardPlayout(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		try {
			this.moveSelector = (MoveSelector) SearchManagerComponent.getConstructorForSearchManagerComponent(SearchManagerComponent.getCorrespondingClass(ProjectSearcher.MOVE_SELECTORS.getConcreteClasses(),
					gamerSettings.getPropertyValue("PlayoutStrategy" + id + ".moveSelectorType"))).newInstance(gameDependentParameters, random, gamerSettings, sharedReferencesCollector);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO: fix this!
			GamerLogger.logError("SearchManagerCreation", "Error when instantiating MoveSelector " + gamerSettings.getPropertyValue("PlayoutStrategy" + id + ".moveSelectorType") + ".");
			GamerLogger.logStackTrace("SearchManagerCreation", e);
			throw new RuntimeException(e);
		}

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {

		super.setReferences(sharedReferencesCollector);

		this.moveSelector.setReferences(sharedReferencesCollector);

		//this.currentSimulationJointMoves = sharedReferencesCollector.getCurrentSimulationJointMoves();
	}

	@Override
	public void clearComponent() {
		this.moveSelector.clearComponent();
	}

	@Override
	public void setUpComponent() {
		this.moveSelector.setUpComponent();
	}

	@Override
	public SimulationResult[] playout(MctsNode node, /*List<Move> jointMove,*/ MachineState state, int maxDepth) {

		SimulationResult[] result = new SimulationResult[1];

		result[0] = this.singlePlayout(node, state, maxDepth);

		return result;
	}

	@Override
	public SimulationResult singlePlayout(MctsNode node, MachineState state, int maxDepth) {

        boolean terminal = true;

        if(node != null) {
        	terminal = node.isTerminal();
        }else {
	        try {
				terminal = this.gameDependentParameters.getTheMachine().isTerminal(state);
			} catch (StateMachineException e) {
				GamerLogger.logError("MctsManager", "Exception computing state terminality while performing a playout.");
				GamerLogger.logStackTrace("MctsManager", e);
				terminal = true;
			}
        }

        // NOTE that this is just an extra check: if the state is terminal or the depth limit has been reached,
     	// we just return the final goals of the state. At the moment the MCTS manager already doesn't call the
        // play-out if the state is terminal or if the depth limit has been reached, so this check will never be
        // true, but it's here just to be safe.
		if(terminal || maxDepth == 0){

			GamerLogger.logError("MctsManager", "Playout strategy shouldn't be called on a terminal node. The MctsManager must take care of computing the simulation result in this case.");

			return new SimulationResult(0, this.gameDependentParameters.getTheMachine().getSafeGoalsAvgForAllRoles(state));

		}

        int nDepth = 0;

        List<Move> jointMove;

        do{ // NOTE: if any of the try blocks fails on the first iteration this method will return a result with only the terminal goals of the starting state of the playout, depth 0 and empty moves list

        	jointMove = null;
			try {
				jointMove = this.getJointMove(node, state);
			} catch (MoveDefinitionException | StateMachineException e) {
				GamerLogger.logError("MctsManager", "Exception getting a joint move while performing a playout.");
				GamerLogger.logStackTrace("MctsManager", e);
				break;
			}
			try {
				state = this.getNextState(node, state, jointMove);
			} catch (TransitionDefinitionException | StateMachineException e) {
				GamerLogger.logError("MctsManager", "Exception getting the next state while performing a playout.");
				GamerLogger.logStackTrace("MctsManager", e);
				break;
			}

			node = null;

			nDepth++;

            try {
				terminal = this.gameDependentParameters.getTheMachine().isTerminal(state);
			} catch (StateMachineException e) {
				GamerLogger.logError("MctsManager", "Exception computing state terminality while performing a playout.");
				GamerLogger.logStackTrace("MctsManager", e);
				terminal = true;
				break;
			}

        }while(nDepth < maxDepth && !terminal);

        return new SimulationResult(nDepth, this.gameDependentParameters.getTheMachine().getSafeGoalsAvgForAllRoles(state));

	}

	@Override
	public String getComponentParameters(String indentation) {

		String superParams = super.getComponentParameters(indentation);
		if(superParams != null){
			return superParams + indentation + "JOINT_MOVE_SELECTOR = " + this.moveSelector.printComponent(indentation + "  ");
		}else{
			return indentation + "JOINT_MOVE_SELECTOR = " + this.moveSelector.printComponent(indentation + "  ");
		}

	}

	@Override
	public Move getMoveForRole(MctsNode node, MachineState state, int roleIndex) throws MoveDefinitionException, StateMachineException {
		return this.moveSelector.getMoveForRole(node, state, roleIndex);
	}

}
