package org.ggp.base.util.statemachine.structure.explicit;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.statemachine.structure.Role;


/**
 * A Role represents the name used for a player in a game description.
 * <p>
 * The list of roles defined in a game description can be extracted
 * using the {@link #computeRoles(List)} method.
 */
@SuppressWarnings("serial")
public class ExplicitRole extends Role{
    protected final GdlConstant name;

    public ExplicitRole(GdlConstant name)
    {
        this.name = name;
    }

    @Override
    public boolean equals(Object o)
    {
        if ((o != null) && (o instanceof ExplicitRole))
        {
            ExplicitRole role = (ExplicitRole) o;
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
    public static List<ExplicitRole> computeRoles(List<? extends Gdl> description)
    {
        List<ExplicitRole> roles = new ArrayList<ExplicitRole>();
        for (Gdl gdl : description) {
            if (gdl instanceof GdlRelation) {
                GdlRelation relation = (GdlRelation) gdl;
                if (relation.getName().getValue().equals("role")) {
                    roles.add(new ExplicitRole((GdlConstant) relation.get(0)));
                }
            }
        }
        return roles;
    }
}