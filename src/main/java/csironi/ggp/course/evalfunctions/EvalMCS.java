/**
 *
 */
package csironi.ggp.course.evalfunctions;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

/**
 * @author C.Sironi
 *
 */
public class EvalMCS extends EvaluationFunction {

	/**
	 * Number of playouts to be performed from the node to estimate its score.
	 */
	int nPlayouts;

	/**
	 * @param stateMachine
	 */
	public EvalMCS(StateMachine stateMachine, int nPlayouts) {
		super(stateMachine);
		this.nPlayouts = nPlayouts;
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see csironi.ggp.course.evalfunctions.EvaluationFunction#eval(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public int eval(MachineState state, Role role)
			throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException {

		int stateScore = 0;

		for(int i = 1; i <= this.nPlayouts; i++){
			stateScore += randomPlayout(state, role);
		}

		return stateScore/this.nPlayouts;
	}

	private int randomPlayout(MachineState state, Role role) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException{

		if(this.stateMachine.isTerminal(state)){
			return this.stateMachine.getGoal(state, role);
		}

		return randomPlayout(this.stateMachine.getRandomNextState(state), role);
	}

}
