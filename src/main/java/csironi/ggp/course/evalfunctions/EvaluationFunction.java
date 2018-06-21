/**
 *
 */
package csironi.ggp.course.evalfunctions;

import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

/**
 * @author C.Sironi
 *
 */
public abstract class EvaluationFunction {

	StateMachine stateMachine;

	/**
	 *
	 */
	public EvaluationFunction(StateMachine stateMachine) {
		this.stateMachine = stateMachine;
	}

	public abstract double eval(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException, StateMachineException;

}
