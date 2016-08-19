package csironi.ggp.course;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.game.GameRepository;
import org.ggp.base.util.gdl.factory.GdlFactory;
import org.ggp.base.util.gdl.factory.exceptions.GdlFormatException;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.StateMachineException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.symbol.factory.SymbolFactory;
import org.ggp.base.util.symbol.factory.exceptions.SymbolFormatException;
import org.ggp.base.util.symbol.grammar.SymbolList;

/**
 * This class reproduces a situation where the GGP Base Prover fails to compute
 * the goals of a player (player x) in a terminal state of the game "quad_7x7".
 *
 * @author C.Sironi
 *
 */
public class ProvaProver {

	public static void main(String args[]) throws SymbolFormatException, GdlFormatException, GoalDefinitionException, StateMachineException {

		GameRepository theRepository = GameRepository.getDefaultRepository();
        for(String gameKey : theRepository.getGameKeys()) {
            if(!gameKey.contains("quad_7x7")) continue;

            List<Gdl> description = theRepository.getGame(gameKey).getRules();

            ProverStateMachine machine = new ProverStateMachine();

            machine.initialize(description, Long.MAX_VALUE);

            String stringState = "( ( true ( cell r2 c6 x ) ) ( true ( cell r3 c7 o ) ) ( true ( cell r5 c5 o ) ) ( true ( cell r2 c3 w ) ) ( true ( cell r2 c4 o ) ) ( true ( cell r4 c7 x ) ) ( true ( white x 0 ) ) ( true ( cell r5 c4 w ) ) ( true ( cell r7 c3 b ) ) ( true ( cell r5 c7 o ) ) ( true ( cell r6 c7 w ) ) ( true ( cell r4 c4 b ) ) ( true ( cell r3 c2 b ) ) ( true ( cell r6 c3 w ) ) ( true ( cell r6 c5 b ) ) ( true ( cell r3 c1 x ) ) ( true ( cell r2 c1 w ) ) ( true ( quad x 0 ) ) ( true ( cell r1 c2 o ) ) ( true ( cell r7 c6 b ) ) ( true ( cell r4 c5 o ) ) ( true ( cell r6 c1 x ) ) ( true ( control x ) ) ( true ( quad o 0 ) ) ( true ( cell r6 c4 w ) ) ( true ( cell r7 c4 o ) ) ( true ( cell r5 c1 x ) ) ( true ( cell r7 c5 b ) ) ( true ( cell r3 c5 b ) ) ( true ( cell r2 c2 b ) ) ( true ( cell r4 c1 b ) ) ( true ( cell r1 c6 w ) ) ( true ( cell r3 c4 x ) ) ( true ( cell r4 c2 o ) ) ( true ( white o 0 ) ) ( true ( cell r1 c5 x ) ) ( true ( cell r6 c2 o ) ) ( true ( cell r2 c7 o ) ) ( true ( cell r3 c3 w ) ) ( true ( cell r5 c3 x ) ) ( true ( cell r5 c2 o ) ) ( true ( cell r2 c5 x ) ) ( true ( cell r1 c4 x ) ) ( true ( cell r5 c6 b ) ) ( true ( cell r1 c3 b ) ) ( true ( cell r4 c3 x ) ) ( true ( cell r4 c6 o ) ) ( true ( cell r3 c6 w ) ) ( true ( cell r6 c6 x ) ) ( true ( cell r7 c2 w ) ) )";

            SymbolList list = (SymbolList) SymbolFactory.create(stringState);

            Set<GdlSentence> gdlState = new HashSet<GdlSentence>();

            for (int i = 0; i < list.size(); i++)
	        {
            	gdlState.add((GdlSentence) GdlFactory.create(list.get(i)));
	        }

            System.out.println(gdlState);

            MachineState state = new MachineState(gdlState);

            System.out.println(state);

            List<Role> roles = Role.computeRoles(description);

            Role role = null;
            for(Role r : roles){
            	System.out.println(r);
            	if(r.toString().equals("x")){
            		role = r;
            	}
            }

            System.out.println(role);

            int g = machine.getGoal(state, role);

            System.out.println(g);

        }
	}

}
