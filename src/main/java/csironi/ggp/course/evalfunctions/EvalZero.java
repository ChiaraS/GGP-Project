/**
 *
 */
package csironi.ggp.course.evalfunctions;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;

/**
 * @author C.Sironi
 *
 */
public class EvalZero extends EvaluationFunction {

	/**
	 *
	 */
	public EvalZero(StateMachine stateMachine) {
		super(stateMachine);
	}

	/* (non-Javadoc)
	 * @see csironi.ggp.course.evalfunctions.EvaluationFunction#eval(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public int eval(MachineState state, Role role) {
		return 0;
	}

}
