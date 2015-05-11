/**
 *
 */
package csironi.ggp.course.evalfunctions;

import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;

/**
 * @author C.Sironi
 *
 */
public class EvalMobility extends EvaluationFunction {

	private int maxMoves;

	/**
	 *
	 */
	public EvalMobility(StateMachine stateMachine) {
		super(stateMachine);
	}

	/* (non-Javadoc)
	 * @see csironi.ggp.course.evalfunctions.EvaluationFunction#eval(org.ggp.base.util.statemachine.MachineState, org.ggp.base.util.statemachine.Role)
	 */
	@Override
	public int eval(MachineState state, Role role) throws MoveDefinitionException {

		List<Move> legalMoves = stateMachine.getLegalMoves(state, role);

		return 0;
	}

}
