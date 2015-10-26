package org.ggp.base.util.propnet.architecture.externalizedState;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.util.OpenBitSet;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlProposition;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.extendedState.components.ExtendedStateProposition;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateAnd;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateConstant;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateNot;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateOr;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateProposition;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateProposition.PROP_TYPE;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateTransition;
import org.ggp.base.util.propnet.state.ExternalPropnetState;
import org.ggp.base.util.statemachine.Role;

public class ExternalizedStatePropNet {

	/** References to every component in the PropNet. */
	private final Set<ExternalizedStateComponent> components;

	/** A helper list of all of the roles. */
	private final List<Role> roles;

	/** References to every Proposition in the PropNet. */
	private final Set<ExternalizedStateProposition> propositions;

	/** Reference to the single TRUE constant in the propnet */
	private ExternalizedStateConstant trueConstant;

	/** Reference to the single FALSE constant in the prpnet */
	private ExternalizedStateConstant falseConstant;

	/**
	 * References to every BaseProposition in the PropNet.
	 * Corresponds to the current state in the ExternalPropnetState class.
	 * This list and the current and next state in the ExternalPropnetState class all
	 * have the elements in the same order.
	 */
	private final List<ExternalizedStateProposition> basePropositions;

	/**
	 * References to every InputProposition in the PropNet, ordered by role.
	 * Roles are in the same order as in the roles list.
	 * The order is the same as the values of the currentJointMove in the ExternalPropnetState class.
	 */
	private List<ExternalizedStateProposition> inputPropositions;

	/**
	 * References to every input proposition in the PropNet, grouped by role.
	 * The order of the input propositions for every role is the same as the order of the legal
	 * propositions for the same role.
	 */
	private final Map<Role, ArrayList<ExternalizedStateProposition>> inputsPerRole;

	/**
	 * References to every legal proposition in the PropNet, grouped by role.
	 * The order of the legal propositions for every role is the same as the order of the input
	 * propositions for the same role.
	 */
	private final Map<Role, ArrayList<ExternalizedStateProposition>> legalsPerRole;

	/** A reference to the single, unique, InitProposition. */
	private ExternalizedStateProposition initProposition;

	/** A reference to the single, unique, TerminalProposition. */
	private ExternalizedStateProposition terminalProposition;

	private List<ExternalizedStateComponent> andOrGates;

	/**
	 * List of all the goals that corresponds to a goal proposition, grouped by role
	 * and listed in the same order as the role propositions values in the ExternalPropnetState class.
	 */
	private int[][] goalValues;

	/** References to every GoalProposition in the PropNet, indexed by role. */
	private final Map<Role, List<ExternalizedStateProposition>> goalPropositions;

	private ExternalPropnetState initialPropnetState;

	/**
	 * Creates a new PropNet from a list of Components.
	 *
	 * @param components
	 *            A list of Components.
	 */
	public ExternalizedStatePropNet(List<Role> roles, Set<ExternalizedStateComponent> components)
	{
	    this.roles = roles;
		this.components = components;
		this.propositions = new HashSet<ExternalizedStateProposition>();
		this.basePropositions = new ArrayList<ExternalizedStateProposition>();

		this.inputsPerRole = new HashMap<Role, ArrayList<ExternalizedStateProposition>>();
		this.legalsPerRole = new HashMap<Role, ArrayList<ExternalizedStateProposition>>();
		Map<Role, Map<List<GdlTerm>, Integer>> moveIndices = new HashMap<Role, Map<List<GdlTerm>, Integer>>();
		Map<Role, Integer> currentIndices = new HashMap<Role, Integer>();

		this.goalPropositions = new HashMap<Role, List<ExternalizedStateProposition>>();

		for(Role r : this.roles){
			this.inputsPerRole.put(r, new ArrayList<ExternalizedStateProposition>());
			this.legalsPerRole.put(r, new ArrayList<ExternalizedStateProposition>());
			moveIndices.put(r, new HashMap<List<GdlTerm>, Integer>());
			currentIndices.put(r, new Integer(0));
			this.goalPropositions.put(r, new ArrayList<ExternalizedStateProposition>());
		}

		this.andOrGates = new ArrayList<ExternalizedStateComponent>();



		for(ExternalizedStateComponent c : this.components){
			if(c instanceof ExternalizedStateConstant){
				if(((ExternalizedStateConstant) c).getValue()){
					if(this.trueConstant == null){
						this.trueConstant = (ExternalizedStateConstant) c;
					}else{
						throw new RuntimeException("Found more than only one TRUE constant in the propnet!");
					}
				}else{
					if(this.falseConstant == null){
						this.falseConstant = (ExternalizedStateConstant) c;
					}else{
						throw new RuntimeException("Found more than only one FALSE constant in the propnet!");
					}
				}
			}else if(c instanceof ExternalizedStateProposition){

				ExternalizedStateProposition p = (ExternalizedStateProposition) c;
				this.propositions.add(p);

				// Check if it's a base.
			    if(p.getInputs().size() == 1 && p.getSingleInput() instanceof ExternalizedStateTransition){
			    	this.basePropositions.add(p);
			    	// Set that the type of this proposition is BASE
			    	p.setPropositionType(PROP_TYPE.BASE);
			    // Check if it's an input or legal
				}else if(p.getName() instanceof GdlRelation){
					GdlRelation relation = (GdlRelation) p.getName();
					if (relation.getName().getValue().equals("does")) {

						GdlConstant name = (GdlConstant) relation.get(0);
						Role r = new Role(name);
						List<GdlTerm> gdlMove = p.getName().getBody();
						Integer index = moveIndices.get(r).get(gdlMove);

						// The index for this move and role exist already.
						if(index != null){
							// Put the input proposition in the correct position.
							// The corresponding legal proposition is already in the correct
							// place in the legal propositions array for the role.
							inputsPerRole.get(r).set(index.intValue(), p);
						}else{
							// First add the index for this move.
							index = currentIndices.remove(r);
							moveIndices.get(r).put(gdlMove, new Integer(index.intValue()));
							this.inputsPerRole.get(r).add(p);
							this.legalsPerRole.get(r).add(null);
							currentIndices.put(r, new Integer(index.intValue() + 1));
						}

						// Set that the type of this proposition is INPUT
						p.setPropositionType(PROP_TYPE.INPUT);

					}else if(relation.getName().getValue().equals("legal")){

						GdlConstant name = (GdlConstant) relation.get(0);
						Role r = new Role(name);
						List<GdlTerm> gdlMove = p.getName().getBody();
						Integer index = moveIndices.get(r).get(gdlMove);

						// The index for this move and role exist already.
						if(index != null){
							// Put the input proposition in the correct position.
							// The corresponding legal proposition is already in the correct
							// place in the legal propositions array for the role.
							legalsPerRole.get(r).set(index.intValue(), p);
						}else{
							// First add the index for this move.
							index = currentIndices.remove(r);
							moveIndices.get(r).put(gdlMove, new Integer(index.intValue()));
							this.legalsPerRole.get(r).add(p);
							this.inputsPerRole.get(r).add(null);
							currentIndices.put(r, new Integer(index.intValue() + 1));
						}

						// Set that the type of this proposition is INPUT
						p.setPropositionType(PROP_TYPE.LEGAL);
					}else if(relation.getName().getValue().equals("goal")){
						Role r = new Role((GdlConstant) relation.get(0));
						this.goalPropositions.get(r).add(p);
						p.setPropositionType(PROP_TYPE.GOAL);
					}
				}else if(p.getName() instanceof GdlProposition){

					GdlConstant constant = ((GdlProposition) p.getName()).getName();

					if(constant.getValue().equals("terminal")){
						this.terminalProposition = p;
						p.setPropositionType(PROP_TYPE.TERMINAL);
					}else if (constant.getValue().toUpperCase().equals("INIT")){
						this.initProposition = p;
						p.setPropositionType(PROP_TYPE.INIT);
					}
				}else{
					p.setPropositionType(PROP_TYPE.OTHER);
				}
			}else if(c instanceof ExternalizedStateTransition){

			}else if(c instanceof ExternalizedStateNot){

			}else if(c instanceof ExternalizedStateAnd){
				this.andOrGates.add(c);
			}else if(c instanceof ExternalizedStateOr){
				this.andOrGates.add(c);
			}else{
				throw new RuntimeException("Unhandled component type " + c.getClass());
			}
		}
	}


	public void initializePropnet(){
		OpenBitSet initialState = new OpenBitSet(this.basePropositions.size());
		OpenBitSet nextState = new OpenBitSet(this.basePropositions.size());

		// TODO: this assumes that the propnet contains the exact same number of base propositions and
		// transitions, thus when removing a base proposition always make sure to also remove the
		// corresponding transition.
		for(int i = 0; i < this.basePropositions.size(); i++){
			ExternalizedStateProposition p = this.basePropositions.get(i);
			p.setIndex(i);
			p.getSingleInput().setIndex(i);
			// If it's a base proposition true in the initial state, set it to true in the bit array
			// representing the initial state
			if(((ExternalizedStateTransition) p.getSingleInput()).isDependingOnInit()){
				initialState.set(i);
			}
		}

		this.inputPropositions = new ArrayList<ExternalizedStateProposition>();
		int i = 0;
		for(Role r : this.roles){
			for(ExternalizedStateProposition p : this.inputsPerRole.get(r)){
				this.inputPropositions.add(p);
				p.setIndex(i);
				i++;
			}
		}

		OpenBitSet currentJointMove = new OpenBitSet(this.inputPropositions.size());

		this.terminalProposition.setIndex(0);

		int[] firstGoalIndices = new int[this.roles.size()+1];

		i = 1;

		this.goalValues = new int[this.roles.size()][];
		int j;
		for(j = 0; j < this.roles.size(); j++){
			firstGoalIndices[j] = i;
			Role r = this.roles.get(j);
			List<ExternalizedStateProposition> goalsForRole = this.goalPropositions.get(r);
			this.goalValues[j] = new int[goalsForRole.size()];
			for(int k = 0; k < goalsForRole.size(); k++){
				GdlRelation relation = (GdlRelation) goalsForRole.get(k).getName();
				GdlConstant constant = (GdlConstant) relation.get(1);
				this.goalValues[j][k] = Integer.parseInt(constant.toString());
				goalsForRole.get(k).setIndex(i);
				i++;
			}
		}
		firstGoalIndices[j] = i;

		int[] firstLegalIndices = new int[this.roles.size()+1];

		for(j = 0; j < this.roles.size(); j++){
			firstLegalIndices[j] = i;
			Role r = this.roles.get(j);
			List<ExternalizedStateProposition> legalsForRole = this.legalsPerRole.get(r);
			for(int k = 0; k < legalsForRole.size(); k++){
				legalsForRole.get(k).setIndex(i);
				i++;
			}
		}
		firstLegalIndices[j] = i;

		for(ExternalizedStateComponent c : this.components){
			if(c instanceof ExternalizedStateProposition){
				if(((ExternalizedStateProposition) c).getPropositionType() == PROP_TYPE.OTHER ||
						((ExternalizedStateProposition) c).getPropositionType() == PROP_TYPE.INIT){
					((ExternalizedStateProposition) c).setIndex(i);
					i++;
				}
			}else if(c instanceof ExternalizedStateNot){
				((ExternalizedStateNot) c).setIndex(i);
				i++;
			}
		}

		OpenBitSet otherComponents = new OpenBitSet(i);

		int l = 0;
		int[] andOrGatesValues = new int[this.andOrGates.size()];
		for(ExternalizedStateComponent c : this.andOrGates){
			if(c instanceof ExternalizedStateAnd){
				andOrGatesValues[l] = Integer.MAX_VALUE - c.getInputs().size() + 1;
			}else if(c instanceof ExternalizedStateOr){
				andOrGatesValues[l] = Integer.MAX_VALUE;
			}
			c.setIndex(l);
			l++;
		}

		this.initialPropnetState = new ExternalPropnetState(initialState, nextState, currentJointMove, firstGoalIndices, firstLegalIndices, andOrGatesValues, otherComponents);

	}


	/**
	 * Getter method.
	 *
	 * @return references to every BaseProposition in the PropNet in the correct order.
	 */
	public List<ExternalizedStateProposition> getBasePropositions(){
		return this.basePropositions;
	}

	/**
	 * Getter method.
	 *
	 * @return References to every InputProposition in the PropNet, in the correct order.
	 */
	public List<ExternalizedStateProposition> getInputPropositions()
	{
		return this.inputPropositions;
	}

	public Integer[] getGoals(int index){
		return this.goalValues[index];
	}








	/**
	 * Helper method for parsing the value of a goal proposition
	 * @param goalProposition
	 * @return the integer value of the goal proposition
	 */
    private int getGoalValue(ExtendedStateProposition goalProposition){
		GdlRelation relation = (GdlRelation) goalProposition.getName();
		GdlConstant constant = (GdlConstant) relation.get(1);
		return Integer.parseInt(constant.toString());
	}













	/** References to every LegalProposition in the PropNet, indexed by role. */
	private final Map<Role, Set<ExternalizedStateProposition>> legalPropositions;


	/** A helper mapping between input/legal propositions. */
	private final Map<ExternalizedStateProposition, ExternalizedStateProposition> legalInputMap;



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
	/*public ExternalizedStatePropNet(List<Role> roles, Set<ExternalizedStateComponent> components)
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
	}*/

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
