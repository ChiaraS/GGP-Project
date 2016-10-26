package org.ggp.base.util.statemachine.proverStructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ggp.base.player.gamer.statemachine.MCS.manager.hybrid.structure.Role;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlRelation;


/**
 * A Role represents the name used for a player in a game description.
 * <p>
 * The list of roles defined in a game description can be extracted
 * using the {@link #computeRoles(List)} method.
 */
@SuppressWarnings("serial")
public class ProverRole implements Serializable, Role{
    protected final GdlConstant name;

    public ProverRole(GdlConstant name)
    {
        this.name = name;
    }

    @Override
    public boolean equals(Object o)
    {
        if ((o != null) && (o instanceof ProverRole))
        {
            ProverRole role = (ProverRole) o;
            return role.name.equals(name);
        }

        return false;
    }

    public GdlConstant getName()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    @Override
    public String toString()
    {
        return name.toString();
    }

    /**
     * Compute all of the roles in a game, in the correct order.
     * <p>
     * Order matters, because a joint move is defined as an ordered list
     * of moves, in which the order determines which player took which of
     * the moves. This function will give an ordered list in which the roles
     * have that correct order.
     */
    public static List<ProverRole> computeRoles(List<? extends Gdl> description)
    {
        List<ProverRole> roles = new ArrayList<ProverRole>();
        for (Gdl gdl : description) {
            if (gdl instanceof GdlRelation) {
                GdlRelation relation = (GdlRelation) gdl;
                if (relation.getName().getValue().equals("role")) {
                    roles.add(new ProverRole((GdlConstant) relation.get(0)));
                }
            }
        }
        return roles;
    }
}