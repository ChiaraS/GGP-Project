package org.ggp.base.util.propnet.architecture.externalizedState;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlProposition;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateAnd;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateNot;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateOr;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateProposition;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateTransition;
import org.ggp.base.util.statemachine.Role;

public class ExternalizedStatePropNet {

	/** References to every component in the PropNet. */
	private final Set<ExternalizedStateComponent> components;

	/** References to every Proposition in the PropNet. */
	private final Set<ExternalizedStateProposition> propositions;

	/** References to every BaseProposition in the PropNet. */
	private final List<GdlSentence, ExternalizedStateProposition> basePropositions;

	/** References to every InputProposition in the PropNet, indexed by name. */
	private final Map<GdlSentence, ExternalizedStateProposition> inputPropositions;

	/** References to every LegalProposition in the PropNet, indexed by role. */
	private final Map<Role, Set<ExternalizedStateProposition>> legalPropositions;

	/** References to every GoalProposition in the PropNet, indexed by role. */
	private final Map<Role, Set<ExternalizedStateProposition>> goalPropositions;

	/** A reference to the single, unique, InitProposition. */
	private final ExternalizedStateProposition initProposition;

	/** A reference to the single, unique, TerminalProposition. */
	private final ExternalizedStateProposition terminalProposition;

	/** A helper mapping between input/legal propositions. */
	private final Map<ExternalizedStateProposition, ExternalizedStateProposition> legalInputMap;

	/** A helper list of all of the roles. */
	private final List<Role> roles;

	public void addComponent(ExternalizedStateComponent c)
	{
		components.add(c);
		if (c instanceof ExternalizedStateProposition) propositions.add((ExternalizedStateProposition)c);
	}

	/**
	 * Creates a new PropNet from a list of Components, along with indices over
	 * those components.
	 *
	 * @param components
	 *            A list of Components.
	 */
	public ExternalizedStatePropNet(List<Role> roles, Set<ExternalizedStateComponent> components)
	{

	    this.roles = roles;
		this.components = components;
		this.propositions = recordPropositions();
		this.basePropositions = recordBasePropositions();
		this.inputPropositions = recordInputPropositions();
		this.legalPropositions = recordLegalPropositions();
		this.goalPropositions = recordGoalPropositions();
		this.initProposition = recordInitProposition();
		this.terminalProposition = recordTerminalProposition();
		this.legalInputMap = makeLegalInputMap();
	}

	public List<Role> getRoles()
	{
	    return roles;
	}

	public Map<ExternalizedStateProposition, ExternalizedStateProposition> getLegalInputMap()
	{
		return legalInputMap;
	}

	private Map<ExternalizedStateProposition, ExternalizedStateProposition> makeLegalInputMap() {
		Map<ExternalizedStateProposition, ExternalizedStateProposition> legalInputMap = new HashMap<ExternalizedStateProposition, ExternalizedStateProposition>();
		// Create a mapping from Body->Input.
		Map<List<GdlTerm>, ExternalizedStateProposition> inputPropsByBody = new HashMap<List<GdlTerm>, ExternalizedStateProposition>();
		for(ExternalizedStateProposition inputProp : inputPropositions.values()) {
			List<GdlTerm> inputPropBody = (inputProp.getName()).getBody();
			inputPropsByBody.put(inputPropBody, inputProp);
		}
		// Use that mapping to map Input->Legal and Legal->Input
		// based on having the same Body proposition.
		for(Set<ExternalizedStateProposition> legalProps : legalPropositions.values()) {
			for(ExternalizedStateProposition legalProp : legalProps) {
				List<GdlTerm> legalPropBody = (legalProp.getName()).getBody();
				if (inputPropsByBody.containsKey(legalPropBody)) {
					ExternalizedStateProposition inputProp = inputPropsByBody.get(legalPropBody);
    				legalInputMap.put(inputProp, legalProp);
    				legalInputMap.put(legalProp, inputProp);
				}
			}
		}
		return legalInputMap;
	}

	/**
	 * Getter method.
	 *
	 * @return References to every BaseProposition in the PropNet, indexed by
	 *         name.
	 */
	public Map<GdlSentence, ExternalizedStateProposition> getBasePropositions()
	{
		return basePropositions;
	}

	/**
	 * Getter method.
	 *
	 * @return References to every Component in the PropNet.
	 */
	public Set<ExternalizedStateComponent> getComponents()
	{
		return components;
	}

	/**
	 * Getter method.
	 *
	 * @return References to every GoalProposition in the PropNet, indexed by
	 *         player name.
	 */
	public Map<Role, Set<ExternalizedStateProposition>> getGoalPropositions()
	{
		return goalPropositions;
	}

	/**
	 * Getter method. A reference to the single, unique, InitProposition.
	 *
	 * @return
	 */
	public ExternalizedStateProposition getInitProposition()
	{
		return initProposition;
	}

	/**
	 * Getter method.
	 *
	 * @return References to every InputProposition in the PropNet, indexed by
	 *         name.
	 */
	public Map<GdlSentence, ExternalizedStateProposition> getInputPropositions()
	{
		return inputPropositions;
	}

	/**
	 * Getter method.
	 *
	 * @return References to every LegalProposition in the PropNet, indexed by
	 *         player name.
	 */
	public Map<Role, Set<ExternalizedStateProposition>> getLegalPropositions()
	{
		return legalPropositions;
	}

	/**
	 * Getter method.
	 *
	 * @return References to every Proposition in the PropNet.
	 */
	public Set<ExternalizedStateProposition> getPropositions()
	{
		return propositions;
	}

	/**
	 * Getter method.
	 *
	 * @return A reference to the single, unique, TerminalProposition.
	 */
	public ExternalizedStateProposition getTerminalProposition()
	{
		return terminalProposition;
	}

	/**
	 * Returns a representation of the PropNet in .dot format.
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("digraph propNet\n{\n");
		for ( ExternalizedStateComponent component : components )
		{
			sb.append("\t" + component.toString() + "\n");
		}
		sb.append("}");

		return sb.toString();
	}

	/**
     * Outputs the propnet in .dot format to a particular file.
     * This can be viewed with tools like Graphviz and ZGRViewer.
     *
     * @param filename the name of the file to output to
     */
    public void renderToFile(String filename) {
        try {
            File f = new File(filename);
            FileOutputStream fos = new FileOutputStream(f);
            OutputStreamWriter fout = new OutputStreamWriter(fos, "UTF-8");
            fout.write(toString());
            fout.close();
            fos.close();
        } catch(Exception e) {
            GamerLogger.logStackTrace("StateMachine", e);
        }
    }

	/**
	 * Builds an index over the BasePropositions in the PropNet.
	 *
	 * This is done by going over every single-input proposition in the network,
	 * and seeing whether or not its input is a transition, which would mean that
	 * by definition the proposition is a base proposition.
	 *
	 * @return An index over the BasePropositions in the PropNet.
	 */
	private Map<GdlSentence, ExternalizedStateProposition> recordBasePropositions()
	{
		Map<GdlSentence, ExternalizedStateProposition> basePropositions = new HashMap<GdlSentence, ExternalizedStateProposition>();
		for (ExternalizedStateProposition proposition : propositions) {
		    // Skip all propositions without exactly one input.
		    if (proposition.getInputs().size() != 1)
		        continue;

			ExternalizedStateComponent component = proposition.getSingleInput();
			if (component instanceof ExternalizedStateTransition) {
				basePropositions.put(proposition.getName(), proposition);
			}
		}

		return basePropositions;
	}

	/**
	 * Builds an index over the GoalPropositions in the PropNet.
	 *
	 * This is done by going over every function proposition in the network
     * where the name of the function is "goal", and extracting the name of the
     * role associated with that goal proposition, and then using those role
     * names as keys that map to the goal propositions in the index.
	 *
	 * @return An index over the GoalPropositions in the PropNet.
	 */
	private Map<Role, Set<ExternalizedStateProposition>> recordGoalPropositions()
	{
		Map<Role, Set<ExternalizedStateProposition>> goalPropositions = new HashMap<Role, Set<ExternalizedStateProposition>>();
		for (ExternalizedStateProposition proposition : propositions)
		{
		    // Skip all propositions that aren't GdlRelations.
		    if (!(proposition.getName() instanceof GdlRelation))
		        continue;

			GdlRelation relation = (GdlRelation) proposition.getName();
			if (!relation.getName().getValue().equals("goal"))
			    continue;

			Role theRole = new Role((GdlConstant) relation.get(0));
			if (!goalPropositions.containsKey(theRole)) {
				goalPropositions.put(theRole, new HashSet<ExternalizedStateProposition>());
			}
			goalPropositions.get(theRole).add(proposition);
		}

		return goalPropositions;
	}

	/**
	 * Returns a reference to the single, unique, InitProposition.
	 *
	 * @return A reference to the single, unique, InitProposition.
	 */
	private ExternalizedStateProposition recordInitProposition()
	{
		for (ExternalizedStateProposition proposition : propositions)
		{
		    // Skip all propositions that aren't GdlPropositions.
			if (!(proposition.getName() instanceof GdlProposition))
			    continue;

			GdlConstant constant = ((GdlProposition) proposition.getName()).getName();
			if (constant.getValue().toUpperCase().equals("INIT")) {
				return proposition;
			}
		}
		return null;
	}

	/**
	 * Builds an index over the InputPropositions in the PropNet.
	 *
	 * @return An index over the InputPropositions in the PropNet.
	 */
	private Map<GdlSentence, ExternalizedStateProposition> recordInputPropositions()
	{
		Map<GdlSentence, ExternalizedStateProposition> inputPropositions = new HashMap<GdlSentence, ExternalizedStateProposition>();
		for (ExternalizedStateProposition proposition : propositions)
		{
		    // Skip all propositions that aren't GdlFunctions.
			if (!(proposition.getName() instanceof GdlRelation))
			    continue;

			GdlRelation relation = (GdlRelation) proposition.getName();
			if (relation.getName().getValue().equals("does")) {
				inputPropositions.put(proposition.getName(), proposition);
			}
		}

		return inputPropositions;
	}

	/**
	 * Builds an index over the LegalPropositions in the PropNet.
	 *
	 * @return An index over the LegalPropositions in the PropNet.
	 */
	private Map<Role, Set<ExternalizedStateProposition>> recordLegalPropositions()
	{
		Map<Role, Set<ExternalizedStateProposition>> legalPropositions = new HashMap<Role, Set<ExternalizedStateProposition>>();
		for (ExternalizedStateProposition proposition : propositions)
		{
		    // Skip all propositions that aren't GdlRelations.
			if (!(proposition.getName() instanceof GdlRelation))
			    continue;

			GdlRelation relation = (GdlRelation) proposition.getName();
			if (relation.getName().getValue().equals("legal")) {
				GdlConstant name = (GdlConstant) relation.get(0);
				Role r = new Role(name);
				if (!legalPropositions.containsKey(r)) {
					legalPropositions.put(r, new HashSet<ExternalizedStateProposition>());
				}
				legalPropositions.get(r).add(proposition);
			}
		}

		return legalPropositions;
	}

	/**
	 * Builds an index over the Propositions in the PropNet.
	 *
	 * @return An index over Propositions in the PropNet.
	 */
	private Set<ExternalizedStateProposition> recordPropositions()
	{
		Set<ExternalizedStateProposition> propositions = new HashSet<ExternalizedStateProposition>();
		for (ExternalizedStateComponent component : components)
		{
			if (component instanceof ExternalizedStateProposition) {
				propositions.add((ExternalizedStateProposition) component);
			}
		}
		return propositions;
	}

	/**
	 * Records a reference to the single, unique, TerminalProposition.
	 *
	 * @return A reference to the single, unqiue, TerminalProposition.
	 */
	private ExternalizedStateProposition recordTerminalProposition()
	{
		for ( ExternalizedStateProposition proposition : propositions )
		{
			if ( proposition.getName() instanceof GdlProposition )
			{
				GdlConstant constant = ((GdlProposition) proposition.getName()).getName();
				if ( constant.getValue().equals("terminal") )
				{
					return proposition;
				}
			}
		}

		return null;
	}

	public int getSize() {
		return components.size();
	}

	public int getNumAnds() {
		int andCount = 0;
		for(ExternalizedStateComponent c : components) {
			if(c instanceof ExternalizedStateAnd)
				andCount++;
		}
		return andCount;
	}

	public int getNumOrs() {
		int orCount = 0;
		for(ExternalizedStateComponent c : components) {
			if(c instanceof ExternalizedStateOr)
				orCount++;
		}
		return orCount;
	}

	public int getNumNots() {
		int notCount = 0;
		for(ExternalizedStateComponent c : components) {
			if(c instanceof ExternalizedStateNot)
				notCount++;
		}
		return notCount;
	}

	public int getNumLinks() {
		int linkCount = 0;
		for(ExternalizedStateComponent c : components) {
			linkCount += c.getOutputs().size();
		}
		return linkCount;
	}

	/**
	 * Removes a component from the propnet. Be very careful when using
	 * this method, as it is not thread-safe. It is highly recommended
	 * that this method only be used in an optimization period between
	 * the propnet's creation and its initial use, during which it
	 * should only be accessed by a single thread.
	 *
	 * The INIT and terminal components cannot be removed.
	 */
	public void removeComponent(ExternalizedStateComponent c) {


		//Go through all the collections it could appear in
		if(c instanceof ExternalizedStateProposition) {
			ExternalizedStateProposition p = (ExternalizedStateProposition) c;
			GdlSentence name = p.getName();
			if(basePropositions.containsKey(name)) {
				basePropositions.remove(name);
			} else if(inputPropositions.containsKey(name)) {
				inputPropositions.remove(name);
				//The map goes both ways...
				ExternalizedStateProposition partner = legalInputMap.get(p);
				if(partner != null) {
					legalInputMap.remove(partner);
					legalInputMap.remove(p);
				}
			} else if(name == GdlPool.getProposition(GdlPool.getConstant("INIT"))) {
				throw new RuntimeException("The INIT component cannot be removed. Consider leaving it and ignoring it.");
			} else if(name == GdlPool.getProposition(GdlPool.getConstant("terminal"))) {
				throw new RuntimeException("The terminal component cannot be removed.");
			} else {
				for(Set<ExternalizedStateProposition> propositions : legalPropositions.values()) {
					if(propositions.contains(p)) {
						propositions.remove(p);
						ExternalizedStateProposition partner = legalInputMap.get(p);
						if(partner != null) {
							legalInputMap.remove(partner);
							legalInputMap.remove(p);
						}
					}
				}
				for(Set<ExternalizedStateProposition> propositions : goalPropositions.values()) {
					propositions.remove(p);
				}
			}
			propositions.remove(p);
		}
		components.remove(c);

		//Remove all the local links to the component
		for(ExternalizedStateComponent parent : c.getInputs())
			parent.removeOutput(c);
		for(ExternalizedStateComponent child : c.getOutputs())
			child.removeInput(c);
		//These are actually unnecessary...
		//c.removeAllInputs();
		//c.removeAllOutputs();
	}

	/**
	 * This method makes sure that the value of each component in the propnet is consistent with
	 * the values of all its inputs (except base propositions that do not have to be consistent
	 * with their corresponding input transitions).
	 *
	 * !REMARK: this method gets stuck in an infinite computation if the propnet contains cycles
	 * with endlessly flipping values.
	 */
	public void imposeConsistency(){
		//TODO: detect endlessly flipping values to stop computation and avoid getting stuck here
		for(ExternalizedStateComponent component: this.components){
			component.imposeConsistency();
		}
	}

	/**
	 * This method resets the value of all the components of the propnet (e.g. to FALSE for propositions,
	 * to 0 trueInputs for AND and OR) making the propnet not necessarily consistent anymore.
	 * This method exists to be used to reset the propnet if something weird seems to happen with its values.
	 */
	public void resetValues(){
		for(ExternalizedStateComponent component: this.components){
			component.resetValue();
		}
	}

}
