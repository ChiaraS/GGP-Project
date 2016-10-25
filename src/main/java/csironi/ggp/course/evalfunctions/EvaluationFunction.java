/**
 *
 */
package csironi.ggp.course.evalfunctions;

import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;

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

	public abstract int eval(ProverMachineState state, ProverRole role) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException, StateMachineException;

}
