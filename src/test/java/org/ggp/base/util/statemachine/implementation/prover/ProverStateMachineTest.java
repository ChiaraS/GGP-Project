package org.ggp.base.util.statemachine.implementation.prover;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ggp.base.util.game.TestGameRepository;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.statemachine.proverStructure.ProverMachineState;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;
import org.ggp.base.util.statemachine.proverStructure.ProverRole;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class ProverStateMachineTest extends Assert {

    protected final ProverStateMachine sm = new ProverStateMachine();
    protected final GdlConstant C1 = GdlPool.getConstant("1");
    protected final GdlConstant C2 = GdlPool.getConstant("2");
    protected final GdlConstant C3 = GdlPool.getConstant("3");
    protected final GdlConstant C50 = GdlPool.getConstant("50");
    protected final GdlConstant C100 = GdlPool.getConstant("100");

    @Test
    public void testProverOnTicTacToe() throws Exception {
        List<Gdl> ticTacToeDesc = new TestGameRepository().getGame("ticTacToe").getRules();
        sm.initialize(ticTacToeDesc, Long.MAX_VALUE);
        ProverMachineState state = sm.getInitialState();
        assertFalse(sm.isTerminal(state));
        GdlConstant X_PLAYER = GdlPool.getConstant("xplayer");
        GdlConstant O_PLAYER = GdlPool.getConstant("oplayer");
        ProverRole xRole = new ProverRole(X_PLAYER);
        ProverRole oRole = new ProverRole(O_PLAYER);
        List<ProverRole> roles = Arrays.asList(xRole, oRole);
        assertEquals(roles, sm.getRoles());

        assertEquals(9, sm.getLegalJointMoves(state).size());
        assertEquals(9, sm.getLegalMoves(state, xRole).size());
        assertEquals(1, sm.getLegalMoves(state, oRole).size());
        ProverMove noop = new ProverMove(GdlPool.getConstant("noop"));
        assertEquals(noop, sm.getLegalMoves(state, oRole).get(0));

        ProverMove m11 = move("mark 1 1");
        assertTrue(sm.getLegalMoves(state, xRole).contains(m11));
        state = sm.getNextState(state, Arrays.asList(new ProverMove[] {m11, noop}));
        assertFalse(sm.isTerminal(state));

        ProverMove m13 = move("mark 1 3");
        assertTrue(sm.getLegalMoves(state, oRole).contains(m13));
        state = sm.getNextState(state, Arrays.asList(new ProverMove[] {noop, m13}));
        assertFalse(sm.isTerminal(state));

        ProverMove m31 = move("mark 3 1");
        assertTrue(sm.getLegalMoves(state, xRole).contains(m31));
        state = sm.getNextState(state, Arrays.asList(new ProverMove[] {m31, noop}));
        assertFalse(sm.isTerminal(state));

        ProverMove m22 = move("mark 2 2");
        assertTrue(sm.getLegalMoves(state, oRole).contains(m22));
        state = sm.getNextState(state, Arrays.asList(new ProverMove[] {noop, m22}));
        assertFalse(sm.isTerminal(state));

        ProverMove m21 = move("mark 2 1");
        assertTrue(sm.getLegalMoves(state, xRole).contains(m21));
        state = sm.getNextState(state, Arrays.asList(new ProverMove[] {m21, noop}));
        assertTrue(sm.isTerminal(state));
        assertEquals(100, sm.getGoal(state, xRole));
        assertEquals(0, sm.getGoal(state, oRole));
        assertEquals(Arrays.asList(new Integer[] {100, 0}), sm.getGoals(state));

        //My expectations for the behavior, but there's no consensus...
        /*Move m23 = new Move(GdlPool.getRelation(PLAY, new GdlTerm[] {C2, C3, O}));
        try {
            sm.getNextState(state, Arrays.asList(new Move[] {noop, m23}));
            fail("Should throw an exception when trying to transition from a terminal state");
        } catch(TransitionDefinitionException e) {
            //Expected
        }*/
    }

    @Test
    public void testCase1A() throws Exception {
        List<Gdl> desc = new TestGameRepository().getGame("test_case_1a").getRules();
        sm.initialize(desc, Long.MAX_VALUE);
        ProverMachineState state = sm.getInitialState();
        ProverRole you = new ProverRole(GdlPool.getConstant("you"));
        assertFalse(sm.isTerminal(state));
        assertEquals(100, sm.getGoal(state, you));
        assertEquals(Collections.singletonList(100), sm.getGoals(state));
        state = sm.getNextState(state, Collections.singletonList(move("proceed")));
        assertTrue(sm.isTerminal(state));
        assertEquals(100, sm.getGoal(state, you));
        assertEquals(Collections.singletonList(100), sm.getGoals(state));
    }

    @Test
    public void testCase3C() throws Exception {
        List<Gdl> desc = new TestGameRepository().getGame("test_case_3c").getRules();
        sm.initialize(desc, Long.MAX_VALUE);
        ProverMachineState state = sm.getInitialState();
        ProverRole xplayer = new ProverRole(GdlPool.getConstant("xplayer"));
        assertFalse(sm.isTerminal(state));
        assertEquals(1, sm.getLegalMoves(state, xplayer).size());
        assertEquals(move("win"), sm.getLegalMoves(state, xplayer).get(0));
        state = sm.getNextState(state, Collections.singletonList(move("win")));
        assertTrue(sm.isTerminal(state));
        assertEquals(100, sm.getGoal(state, xplayer));
        assertEquals(Collections.singletonList(100), sm.getGoals(state));
    }

    @Test
    public void testCase5A() throws Exception {
        List<Gdl> desc = new TestGameRepository().getGame("test_case_5a").getRules();
        sm.initialize(desc, Long.MAX_VALUE);
        ProverMachineState state = sm.getInitialState();
        ProverRole you = new ProverRole(GdlPool.getConstant("you"));
        assertFalse(sm.isTerminal(state));
        assertEquals(1, sm.getLegalMoves(state, you).size());
        assertEquals(move("proceed"), sm.getLegalMoves(state, you).get(0));
        state = sm.getNextState(state, Collections.singletonList(move("proceed")));
        assertTrue(sm.isTerminal(state));
        assertEquals(100, sm.getGoal(state, you));
        assertEquals(Collections.singletonList(100), sm.getGoals(state));
    }

    @Test
    public void testCase5B() throws Exception {
        List<Gdl> desc = new TestGameRepository().getGame("test_case_5b").getRules();
        sm.initialize(desc, Long.MAX_VALUE);
        ProverMachineState state = sm.getInitialState();
        ProverRole you = new ProverRole(GdlPool.getConstant("you"));
        assertFalse(sm.isTerminal(state));
        assertEquals(1, sm.getLegalMoves(state, you).size());
        assertEquals(move("draw 1 1 1 2"), sm.getLegalMoves(state, you).get(0));
        state = sm.getNextState(state, Collections.singletonList(move("draw 1 1 1 2")));
        assertTrue(sm.isTerminal(state));
    }

    @Test
    public void testCase5C() throws Exception {
        List<Gdl> desc = new TestGameRepository().getGame("test_case_5c").getRules();
        sm.initialize(desc, Long.MAX_VALUE);
        ProverMachineState state = sm.getInitialState();
        ProverRole you = new ProverRole(GdlPool.getConstant("you"));
        assertFalse(sm.isTerminal(state));
        assertEquals(1, sm.getLegalMoves(state, you).size());
        assertEquals(move("proceed"), sm.getLegalMoves(state, you).get(0));
        state = sm.getNextState(state, Collections.singletonList(move("proceed")));
        assertTrue(sm.isTerminal(state));
        assertEquals(100, sm.getGoal(state, you));
        assertEquals(Collections.singletonList(100), sm.getGoals(state));
    }

    @Test
    public void testCase5D() throws Exception {
        List<Gdl> desc = new TestGameRepository().getGame("test_case_5d").getRules();
        sm.initialize(desc, Long.MAX_VALUE);
        ProverMachineState state = sm.getInitialState();
        ProverRole you = new ProverRole(GdlPool.getConstant("you"));
        assertFalse(sm.isTerminal(state));
        assertEquals(1, sm.getLegalMoves(state, you).size());
        assertEquals(move("proceed"), sm.getLegalMoves(state, you).get(0));
        state = sm.getNextState(state, Collections.singletonList(move("proceed")));
        assertTrue(sm.isTerminal(state));
        assertEquals(100, sm.getGoal(state, you));
        assertEquals(Collections.singletonList(100), sm.getGoals(state));
    }

    @Test
    public void testCase5E() throws Exception {
        List<Gdl> desc = new TestGameRepository().getGame("test_case_5e").getRules();
        sm.initialize(desc, Long.MAX_VALUE);
        ProverMachineState state = sm.getInitialState();
        ProverRole robot = new ProverRole(GdlPool.getConstant("robot"));
        assertFalse(sm.isTerminal(state));
        System.out.println(sm.getLegalMoves(state, robot));
        assertEquals(7, sm.getLegalMoves(state, robot).size());
        assertEquals(ImmutableSet.of(
				move("reduce a 0"),
				move("reduce a 1"),
				move("reduce c 0"),
				move("reduce c 1"),
				move("reduce c 2"),
				move("reduce c 3"),
				move("reduce c 4")),
				ImmutableSet.copyOf(sm.getLegalMoves(state, robot)));
    }

    @Test
    public void testDistinctAtBeginningOfRule() throws Exception {
        List<Gdl> desc = new TestGameRepository().getGame("test_distinct_beginning_rule").getRules();
        sm.initialize(desc, Long.MAX_VALUE);
        ProverMachineState state = sm.getInitialState();
        ProverRole you = new ProverRole(GdlPool.getConstant("you"));
        assertFalse(sm.isTerminal(state));
        assertEquals(2, sm.getLegalMoves(state, you).size());
        state = sm.getNextState(state, Collections.singletonList(move("do a b")));
        assertTrue(sm.isTerminal(state));
        assertEquals(100, sm.getGoal(state, you));
        assertEquals(Collections.singletonList(100), sm.getGoals(state));
    }

    protected ProverMove move(String description) {
        String[] parts = description.split(" ");
        GdlConstant head = GdlPool.getConstant(parts[0]);
        if(parts.length == 1)
            return new ProverMove(head);
        List<GdlTerm> body = new ArrayList<GdlTerm>();
        for(int i = 1; i < parts.length; i++) {
            body.add(GdlPool.getConstant(parts[i]));
        }
        return new ProverMove(GdlPool.getFunction(head, body));
    }
}
