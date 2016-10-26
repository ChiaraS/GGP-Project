/**
 *
 */
package csironi.ggp.course.evalfunctions;

import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

/**
 * @author C.Sironi
 *
 */
public class EvalProximity extends EvaluationFunction {

	/**
	 * @param stateMachine
	 */
	public EvalProximity(StateMachine stateMachine) {
		super(stateMachine);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see csironi.ggp.course.evalfunctions.EvaluationFunction#eval(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public int eval(ExplicitMachineState state, ExplicitRole role)
			throws MoveDefinitionException, StateMachineException {

		int stateScore = 0;

		try {
			stateScore = this.stateMachine.getGoal(state, role);
		} catch (GoalDefinitionException e) {

		}
		return stateScore;
	}

}
