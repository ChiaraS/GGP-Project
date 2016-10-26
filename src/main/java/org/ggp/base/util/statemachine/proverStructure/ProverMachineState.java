package org.ggp.base.util.statemachine.proverStructure;

import java.util.HashSet;
import java.util.Set;

import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.structure.MachineState;
import org.ggp.base.util.gdl.grammar.GdlSentence;

public class ProverMachineState implements MachineState{
    public ProverMachineState() {
        this.contents = null;
    }

    /**
     * Starts with a simple implementation of a MachineState. StateMachines that
     * want to do more advanced things can subclass this implementation, but for
     * many cases this will do exactly what we want.
     */
    private final Set<GdlSentence> contents;
    public ProverMachineState(Set<GdlSentence> contents)
    {
        this.contents = contents;
    }

    /**
     * getContents returns the GDL sentences which determine the current state
     * of the game being played. Two given states with identical GDL sentences
     * should be identical states of the game.
     */
	public Set<GdlSentence> getContents()
	{
        return contents;
    }

	@Override
	public ProverMachineState clone() {
		return new ProverMachineState(new HashSet<GdlSentence>(contents));
	}

	/* Utility methods */
    @Override
	public int hashCode()
    {
        return getContents().hashCode();
    }

    @Override
	public String toString()
    {
    	Set<GdlSentence> contents = getContents();
    	if(contents == null)
    		return "(MachineState with null contents)";
    	else
    		return contents.toString();
    }

    @Override
	public boolean equals(Object o)
    {
        if ((o != null) && (o instanceof ProverMachineState))
        {
            ProverMachineState state = (ProverMachineState) o;
            return state.getContents().equals(getContents());
        }

        return false;
    }
}