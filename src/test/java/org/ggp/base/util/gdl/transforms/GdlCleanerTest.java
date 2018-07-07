package org.ggp.base.util.gdl.transforms;

import java.util.List;
import java.util.Random;

import org.ggp.base.util.game.TestGameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitMachineState;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;
import org.ggp.base.validator.StaticValidator;
import org.junit.Assert;
import org.junit.Test;

public class GdlCleanerTest extends Assert {

    @Test
    public void testCleanNotDistinct() throws Exception {
    	List<Gdl> description = new TestGameRepository().getGame("test_clean_not_distinct").getRules();
        description = GdlCleaner.run(description);

        StaticValidator.validateDescription(description);

        StateMachine sm = new ProverStateMachine(new Random());
        sm.initialize(description, Long.MAX_VALUE);
        ExplicitMachineState state = sm.getExplicitInitialState();
        assertEquals(1, sm.getExplicitRoles().size());
        ExplicitRole player = sm.getExplicitRoles().get(0);
        assertEquals(1, sm.getExplicitLegalMoves(state, player).size());
        state = sm.getNextStates(state).get(0);
        assertTrue(sm.isTerminal(state));
        assertEquals(100, sm.getGoal(state, player));
    }

}
