/**
 *
 */
package csironi.ggp.course.evalfunctions;

import java.util.List;

import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMove;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

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
	public double eval(ExplicitMachineState state, ExplicitRole role) throws MoveDefinitionException, StateMachineException {

		List<ExplicitMove> legalMoves = stateMachine.getExplicitLegalMoves(state, role);

		return 0.0;
	}

}
