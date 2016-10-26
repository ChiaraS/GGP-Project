/**
 *
 */
package csironi.ggp.course.evalfunctions;

import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

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
	public int eval(ExplicitMachineState state, ExplicitRole role) {
		return 0;
	}

}
