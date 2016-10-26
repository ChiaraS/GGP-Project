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

import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlProposition;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateAnd;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateConstant;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateNot;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateOr;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateProposition;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateTransition;
import org.ggp.base.util.propnet.utils.PROP_TYPE;
import org.ggp.base.util.statemachine.structure.explicit.ExplicitRole;

public class ExternalizedStatePropNet {

	/********************************** Parameters **********************************/

	private final long initTime;

	public long getInitTime(){
		return this.initTime;
	}

	/** References to every component in the PropNet. */
	private final Set<ExternalizedStateComponent> components;


	/** References to every Proposition in the PropNet. */
	private final Set<ExternalizedStateProposition> propositions;

	/** A helper list of all of the roles. */
	private final List<ExplicitRole> roles;

	/** Reference to the single TRUE constant in the propnet */
	private ExternalizedStateConstant trueConstant;

	/** Reference to the single FALSE constant in the prpnet */
	private ExternalizedStateConstant falseConstant;

	/** A reference to the single, unique, InitProposition. */
	//private ExternalizedStateProposition initProposition;

	/** A reference to the single, unique, TerminalProposition. */
	private ExternalizedStateProposition terminalProposition;

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
	 * References to every LegalProposition in the PropNet, ordered by role.
	 * Roles are in the same order as in the roles list.
	 * The order is the same as the values of the inputPropositions list.
	 */
	private List<ExternalizedStateProposition> legalPropositions;

	/**
	 * References to every input proposition in the PropNet, grouped by role.
	 * The order of the input propositions for every role is the same as the order of the legal
	 * propositions for the same role.
	 */
	private final Map<ExplicitRole, List<ExternalizedStateProposition>> inputsPerRole;

	/**
	 * References to every legal proposition in the PropNet, grouped by role.
	 * The order of the legal propositions for every role is the same as the order of the input
	 * propositions for the same role.
	 */
	private final Map<ExplicitRole, List<ExternalizedStateProposition>> legalsPerRole;

	/** References to every GoalProposition in the PropNet, indexed by role. */
	private final Map<ExplicitRole, List<ExternalizedStateProposition>> goalsPerRole;

	/**
	 * List of all the goals that corresponds to a goal proposition, grouped by role
	 * and listed in the same order as the role propositions values in the ExternalPropnetState class.
	 */
	private Map<ExplicitRole, List<Integer>> goalValues;

	/**
	 * List of all the AND and OR gates.
	 */
	private List<ExternalizedStateComponent> andOrGates;





	/**
	 * Creates a new PropNet from a list of Components.
	 *
	 * TODO: Also checks if a gate or a proposition has no inputs and if so connects it to the FALSE component
	 * (TRUE component if it is a NOT).
	 * Note: if a BASE proposition has no input it won't be detected as base proposition, so can be
	 * treated as any other proposition and connected to FALSE.
	 * Note: an INPUT proposition must be excluded from this check as it is normal for it not to have any
	 * input.
	 *
	 * ATTENTION: the algorithm that detects an input/legal propositions and puts it in the corresponding map
	 * if and only if it has its legal/input counterpart works only if for each different move there is at
	 * most only one input proposition and at most only one legal proposition. (Note that with move we intend
	 * the list of GdlTerms that come after the keyword 'does' in an input proposition or after the keyword
	 * 'legal' in a legal proposition: e.g. (xplayer (mark 1 1)).
	 *
	 * @param components
	 *            A list of Components.
	 */
	public ExternalizedStatePropNet(List<ExplicitRole> roles, Set<ExternalizedStateComponent> components, ExternalizedStateConstant trueConstant, ExternalizedStateConstant falseConstant){

		/*
		 * ALGORITHMS TO FIND INPUT AND LEGALS:
		 *
		 * - ALG1: keeps 2 moves maps, one with pairs (GDLMove, correspondingLegalProp), and one with pairs (GDLMove, correspondingInputProp).
		 * 		   Whenever we find a legal or input proposition, if the corresponding input or legal proposition has already been found,
		 * 		   we add both propositions in the same position of the legalsPerRole and InputsPerRole maps, otherwise we add the pair
		 * 		   (GDLMove, correspondingProp) to the moves map and wait until we find the corresponding proposition.
		 * - ALG2: same as the previous one, but the maps of moves are divided per role.
		 * - ALG3: memorizes in two maps all the propositions that can be input or legal, indexed by GDLMove. At the end, for each legal
		 * 		   proposition checks if the corresponding input proposition exists. If yes they are both added to the same position of the
		 * 		   legalsPerRole and InputsPerRole maps, otherwise they are not classified as input/legal propositions. This algorithm removes
		 * 		   input propositions from the possibleInputs map as soon as they are added to the inputsPerRole map.
		 * - ALG4: same as ALG3, but it doesn't remove input propositions from the possibleInputs map when they are added to the inputsPerRole
		 * 		   map.
		 */
		//System.out.println();

		long start = System.currentTimeMillis();

	    this.roles = roles;

	    //for(Role r : roles){
	    //	System.out.println(r);
	    //}
		this.components = components;
		this.falseConstant = falseConstant;
		this.trueConstant = trueConstant;
		this.components.add(trueConstant);
		this.components.add(falseConstant);

		this.propositions = new HashSet<ExternalizedStateProposition>();
		this.basePropositions = new ArrayList<ExternalizedStateProposition>();

		this.inputsPerRole = new HashMap<ExplicitRole, List<ExternalizedStateProposition>>();
		this.legalsPerRole = new HashMap<ExplicitRole, List<ExternalizedStateProposition>>();
		//Map<Role, List<ExternalizedStateProposition>> inputsPerRole = new HashMap<Role, List<ExternalizedStateProposition>>();
		//Map<Role, List<ExternalizedStateProposition>> legalsPerRole = new HashMap<Role, List<ExternalizedStateProposition>>();

		//Map<Role, Map<List<GdlTerm>, Integer>> moveIndices = new HashMap<Role, Map<List<GdlTerm>, Integer>>();
		//Map<Role, Integer> currentIndices = new HashMap<Role, Integer>();

		/* ALG1 -START
		Map<List<GdlTerm>, ExternalizedStateProposition> possibleInputs = new HashMap<List<GdlTerm>, ExternalizedStateProposition>();
		Map<List<GdlTerm>, ExternalizedStateProposition> possibleLegals = new HashMap<List<GdlTerm>, ExternalizedStateProposition>();
		ALG1 - END */

		/* ALG2 - START */
		Map<ExplicitRole, Map<List<GdlTerm>, ExternalizedStateProposition>> possibleInputs = new HashMap<ExplicitRole, Map<List<GdlTerm>, ExternalizedStateProposition>>();
		Map<ExplicitRole, Map<List<GdlTerm>, ExternalizedStateProposition>> possibleLegals = new HashMap<ExplicitRole, Map<List<GdlTerm>, ExternalizedStateProposition>>();
		/* ALG2 - END */

		/* ALG3 - START
		Map<Role, Map<List<GdlTerm>, ExternalizedStateProposition>> possibleInputs = new HashMap<Role, Map<List<GdlTerm>, ExternalizedStateProposition>>();
		Map<Role, Map<List<GdlTerm>, ExternalizedStateProposition>> possibleLegals = new HashMap<Role, Map<List<GdlTerm>, ExternalizedStateProposition>>();
		 ALG3 - END */

		this.goalsPerRole = new HashMap<ExplicitRole, List<ExternalizedStateProposition>>();
		this.goalValues = new HashMap<ExplicitRole, List<Integer>>();

		for(ExplicitRole r : this.roles){
			this.inputsPerRole.put(r, new ArrayList<ExternalizedStateProposition>());
			this.legalsPerRole.put(r, new ArrayList<ExternalizedStateProposition>());
			//inputsPerRole.put(r, new ArrayList<ExternalizedStateProposition>());
			//legalsPerRole.put(r, new ArrayList<ExternalizedStateProposition>());

			//moveIndices.put(r, new HashMap<List<GdlTerm>, Integer>());
			//currentIndices.put(r, new Integer(0));

			/* ALG2 - START */
			possibleInputs.put(r, new HashMap<List<GdlTerm>, ExternalizedStateProposition>());
			possibleLegals.put(r, new HashMap<List<GdlTerm>, ExternalizedStateProposition>());
			/* ALG2 - END */

			/* ALG3 - START
			possibleInputs.put(r, new HashMap<List<GdlTerm>, ExternalizedStateProposition>());
			possibleLegals.put(r, new HashMap<List<GdlTerm>, ExternalizedStateProposition>());
			 ALG3 - END */

			this.goalsPerRole.put(r, new ArrayList<ExternalizedStateProposition>());
			this.goalValues.put(r, new ArrayList<Integer>());
		}

		this.andOrGates = new ArrayList<ExternalizedStateComponent>();

		for(ExternalizedStateComponent c : this.components){
			if(c instanceof ExternalizedStateConstant){
				if(((ExternalizedStateConstant) c).getValue()){
					if(this.trueConstant != c){
						throw new RuntimeException("Found more than only one TRUE constant in the propnet!");
					}
				}else{
					if(this.falseConstant != c){
						throw new RuntimeException("Found more than only one FALSE constant in the propnet!");
					}
				}
			}else if(c instanceof ExternalizedStateProposition){

				ExternalizedStateProposition p = (ExternalizedStateProposition) c;
				this.propositions.add(p);
				// So that if the following code doesn't detect it as a special type
				// of proposition, its type won't be null.
				p.setPropositionType(PROP_TYPE.OTHER);

				// Check if it's a base.
			    if(p.getInputs().size() == 1 && p.getSingleInput() instanceof ExternalizedStateTransition){
			    	this.basePropositions.add(p);
			    	// Set that the type of this proposition is BASE
			    	p.setPropositionType(PROP_TYPE.BASE);
			    // Check if it's an input or legal
				}else if(p.getName() instanceof GdlRelation){
					GdlRelation relation = (GdlRelation) p.getName();
					if(relation.getName().getValue().equals("does")){ // Possible input

						/* ALG3 - START
						// Get the GDL move corresponding to this proposition
						List<GdlTerm> gdlMove = p.getName().getBody();
						// Get the role performing the move
						GdlConstant name = (GdlConstant) relation.get(0);
						Role r = new Role(name);

						// Get the map of possible legals for the role
						Map<List<GdlTerm>, ExternalizedStateProposition> possibleInputsPerRole = possibleInputs.get(r);

						// If we have no map, r is not a relevant role, so we just classify the proposition as an OTHER proposition...
						if(possibleInputsPerRole != null){
							//...otherwise we put this proposition in the map of possible inputs.
							possibleInputsPerRole.put(gdlMove, p);
						}
						 ALG3 - END */

						/* ALG2 - START */
						// Get the GDL move corresponding to this proposition
						List<GdlTerm> gdlMove = p.getName().getBody();
						// Get the role performing the move
						GdlConstant name = (GdlConstant) relation.get(0);
						ExplicitRole r = new ExplicitRole(name);

						// Get the map of possible legals for the role
						Map<List<GdlTerm>, ExternalizedStateProposition> possibleLegalsPerRole = possibleLegals.get(r);

						// If we have no map, r is not a relevant role, so we just classify the proposition as an OTHER proposition...
						if(possibleLegalsPerRole != null){
							//...otherwise we check if we already found its corresponding legal (if it exists).
							// If we found it, we remove it from the list of 'possible' legals since we will put it in the
							// list of 'actual' legals.
							ExternalizedStateProposition correspondingLegal = possibleLegalsPerRole.remove(gdlMove);
							// If we haven't found it yet, add the input to the possible inputs list.
							if(correspondingLegal == null){
								possibleInputs.get(r).put(gdlMove, p);
							}else{
								p.setPropositionType(PROP_TYPE.INPUT);
								inputsPerRole.get(r).add(p);
								correspondingLegal.setPropositionType(PROP_TYPE.LEGAL);
								legalsPerRole.get(r).add(correspondingLegal);
							}
						}
						/* ALG2 - END */

						/* ALG1 - START
						// Get the GDL move corresponding to this proposition
						List<GdlTerm> gdlMove = p.getName().getBody();

						// Check if we already found its corresponding legal
						ExternalizedStateProposition correspondingLegal = possibleLegals.remove(gdlMove);

						if(correspondingLegal == null){
							possibleInputs.put(gdlMove, p);
						}else{
							GdlConstant name = (GdlConstant) relation.get(0);
							Role r = new Role(name);
							if(this.roles.contains(r)){
								p.setPropositionType(PROP_TYPE.INPUT);
								inputsPerRole.get(r).add(p);
								correspondingLegal.setPropositionType(PROP_TYPE.LEGAL);
								legalsPerRole.get(r).add(correspondingLegal);
							}
						}
						ALG1 - END */

					}else if(relation.getName().getValue().equals("legal")){ // Possible legal move

						/* ALG3 - START
						// Get the GDL move corresponding to this proposition
						List<GdlTerm> gdlMove = p.getName().getBody();
						// Get the role performing the move
						GdlConstant name = (GdlConstant) relation.get(0);
						Role r = new Role(name);

						// Get the map of possible inputs for the role
						Map<List<GdlTerm>, ExternalizedStateProposition> possibleLegalsPerRole = possibleLegals.get(r);

						// If we have no map, r is not a relevant role, so we just classify the proposition as an OTHER proposition...
						if(possibleLegalsPerRole != null){
							//...otherwise we put this proposition in the map of possible legals.
							possibleLegals.get(r).put(gdlMove, p);
						}
						 ALG3 - END */

						/* ALG2 - START */
						// Get the GDL move corresponding to this proposition
						List<GdlTerm> gdlMove = p.getName().getBody();
						// Get the role performing the move
						GdlConstant name = (GdlConstant) relation.get(0);
						ExplicitRole r = new ExplicitRole(name);

						// Get the map of possible inputs for the role
						Map<List<GdlTerm>, ExternalizedStateProposition> possibleInputsPerRole = possibleInputs.get(r);

						// If we have no map, r is not a relevant role, so we just classify the proposition as an OTHER proposition...
						if(possibleInputsPerRole != null){
							//...otherwise we check if we already found its corresponding legal (if it exists).
							// If we found it, we remove it from the list of 'possible' legals since we will put it in the
							// list of 'actual' legals.
							ExternalizedStateProposition correspondingInput = possibleInputsPerRole.remove(gdlMove);
							// If we haven't found it yet, add the input to the possible inputs list.
							if(correspondingInput == null){
								possibleLegals.get(r).put(gdlMove, p);
							}else{
								p.setPropositionType(PROP_TYPE.LEGAL);
								legalsPerRole.get(r).add(p);
								correspondingInput.setPropositionType(PROP_TYPE.INPUT);
								inputsPerRole.get(r).add(correspondingInput);
							}
						}
						/* ALG2 - END */

						/* ALG1 - START
						// Get the GDL move corresponding to this proposition
						List<GdlTerm> gdlMove = p.getName().getBody();

						// Check if we already found its corresponding input
						ExternalizedStateProposition correspondingInput = possibleInputs.remove(gdlMove);

						if(correspondingInput == null){
							// Note: here we don't check if the role is valid. We could, but it
							// will be checked anyway later if this move will be removed. And if
							// it won't be removed we don't care if the role is valid.
							possibleLegals.put(gdlMove, p);
						}else{
							GdlConstant name = (GdlConstant) relation.get(0);
							Role r = new Role(name);
							if(this.roles.contains(r)){
								p.setPropositionType(PROP_TYPE.LEGAL);
								legalsPerRole.get(r).add(p);
								correspondingInput.setPropositionType(PROP_TYPE.INPUT);
								inputsPerRole.get(r).add(correspondingInput);
							}
						}
						ALG1 - END */
					}else if(relation.getName().getValue().equals("goal")){
						GdlConstant name = (GdlConstant) relation.get(0);
						//System.out.println(name);
						ExplicitRole r = new ExplicitRole(name);
						if(this.roles.contains(r)){
							this.goalsPerRole.get(r).add(p);
							this.goalValues.get(r).add(this.getGoalValue(p));
							p.setPropositionType(PROP_TYPE.GOAL);
						}
					}
				}else if(p.getName() instanceof GdlProposition){

					GdlConstant constant = ((GdlProposition) p.getName()).getName();

					if(constant.getValue().equals("terminal")){
						if(this.terminalProposition == null){
							this.terminalProposition = p;
							p.setPropositionType(PROP_TYPE.TERMINAL);
						}else{
							throw new RuntimeException("Found more than only one TERMINAL proposition in the propnet!");
						}
					}/*else if (constant.getValue().toUpperCase().equals("INIT")){
						if(this.initProposition == null){
							this.initProposition = p;
							p.setPropositionType(PROP_TYPE.INIT);
						}else{
							throw new RuntimeException("Found more than only one INIT proposition in the propnet!");
						}
					}*/ // Since I don't use the init proposition, commenting this part of code means that the init
					// proposition will be treated as any OTHER proposition, and thus connected to FALSE as any OTHER
					// input-less proposition. Uncomment this to keep it! If you want to keep it also pay attention when
					// using some propnet optimization methods as they might remove it anyway.
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

		//System.out.println("Iteration over components: " + (System.currentTimeMillis() - start) + "ms");
		//start = System.currentTimeMillis();

		// If a constant is null, create and add it to the propnet
		// Note that the constant must also be added to the components. However it will
		// not be associated with an index when the propnet state will be created, its
		// index will always be set to -1 since the propnet state will not include its
		// value. The constant will store its fixed value internally.
		/*if(this.trueConstant == null){
			this.trueConstant = new ExternalizedStateConstant(true);
			this.components.add(this.trueConstant);
		}
		if(this.falseConstant == null){
			this.falseConstant = new ExternalizedStateConstant(false);
			this.components.add(this.falseConstant);
		}*/

		//System.out.println("Check constants: " + (System.currentTimeMillis() - start) + "ms");
		//start = System.currentTimeMillis();

		/*
		 * This part of code will remove from the map of INPUTs the ones that have no LEGAL counterpart
		 * and remove from the map of LEGALs the ones that have no INPUT counterpart.
		 * Having no counterpart means that the corresponding proposition is null, thus also the null
		 * elements in the map will be removed.
		 * After being removed, the propositions will be considered as OTHER propositions.
		 */
		/*for(Role r : this.roles){
			List<ExternalizedStateProposition> roleInputs = this.inputsPerRole.get(r);
			List<ExternalizedStateProposition> roleLegals = this.legalsPerRole.get(r);

			int i = roleInputs.indexOf(null);

			while(i != -1){
				roleInputs.remove(i);
				// The reset of the type to OTHER is used to tell the proposition not to consider
				// itself as a legal proposition anymore. No sense in still considering it a legal
				// proposition since there is no input counterpart.
			    roleLegals.remove(i).setPropositionType(PROP_TYPE.OTHER);

				i = roleInputs.indexOf(null);
			}

			i = roleLegals.indexOf(null);

			while(i != -1){
				// The reset of the type to OTHER is used to tell the proposition not to consider
				// itself as an input proposition anymore. No sense in still considering it an input
				// proposition since there is no legal counterpart.
				roleInputs.remove(i).setPropositionType(PROP_TYPE.OTHER);
				roleLegals.remove(i);

				i = roleLegals.indexOf(null);
			}
		}

		System.out.println("Fix legal inputs: " + (System.currentTimeMillis() - start) + "ms");
		start = System.currentTimeMillis();

		*/


		this.inputPropositions = new ArrayList<ExternalizedStateProposition>();
		this.legalPropositions = new ArrayList<ExternalizedStateProposition>();

		//int x = 0;
		for(ExplicitRole r : this.roles){

			/* ALG3 - START
			Map<List<GdlTerm>,ExternalizedStateProposition> possibleLegalsPerRole = possibleLegals.get(r);
			Map<List<GdlTerm>,ExternalizedStateProposition> possibleInputsPerRole = possibleInputs.get(r);
			for(Entry<List<GdlTerm>,ExternalizedStateProposition> legalEntry : possibleLegalsPerRole.entrySet()){ */
				// TODO: PROVA CON GET INVECE DI REMOVE
				/*ALG4 - START */ // ALG4 is exactly the same as ALG3, except this one line
				//ExternalizedStateProposition input = possibleInputsPerRole.get(legalEntry.getKey());
				/*ALG4 - END */
				/*ALG3 - START */
				//ExternalizedStateProposition input = possibleInputsPerRole.remove(legalEntry.getKey());
				/*ALG3 - END */
			/*	if(input != null){
					this.inputsPerRole.get(r).add(input);
					input.setPropositionType(PROP_TYPE.INPUT);
					this.legalsPerRole.get(r).add(legalEntry.getValue());
					legalEntry.getValue().setPropositionType(PROP_TYPE.LEGAL);
				}
			}
			 ALG3 - END */

			for(ExternalizedStateProposition p : this.inputsPerRole.get(r)){
				this.inputPropositions.add(p);
			}
			for(ExternalizedStateProposition p : this.legalsPerRole.get(r)){
				this.legalPropositions.add(p);
			}
			/*
			for(ExternalizedStateProposition p : inputsPerRole.get(r)){
				this.inputPropositions.add(p);
			}
			for(ExternalizedStateProposition p : legalsPerRole.get(r)){
				this.legalPropositions.add(p);
			}
			*/
		}

		this.initTime = System.currentTimeMillis() - start;
	}

	/**
	 * Helper method for parsing the value of a goal proposition
	 * @param goalProposition
	 * @return the integer value of the goal proposition
	 */
    private int getGoalValue(ExternalizedStateProposition goalProposition){
		GdlRelation relation = (GdlRelation) goalProposition.getName();
		GdlConstant constant = (GdlConstant) relation.get(1);
		return Integer.parseInt(constant.toString());
	}


	/************************************** Getters and setters ***************************************/

	/**
	 * Getter method.
	 *
	 * @return References to every Component in the PropNet.
	 */
	public Set<ExternalizedStateComponent> getComponents(){
		return components;
	}

	/**
	 * Getter method.
	 *
	 * @return References to every Proposition in the PropNet.
	 */
	public Set<ExternalizedStateProposition> getPropositions(){
		return propositions;
	}

	/**
	 * Getter method.
	 *
	 * @return ordered list of roles.
	 */
	public List<ExplicitRole> getRoles(){
	    return roles;
	}

	/**
	 * Getter method.
	 *
	 * @return the single TRUE constant in the propNet.
	 */
	public ExternalizedStateConstant getTrueConstant(){
		return this.trueConstant;
	}

	/**
	 * Getter method.
	 *
	 * @return the single FALSE constant in the propNet.
	 */
	public ExternalizedStateConstant getFalseConstant(){
		return this.falseConstant;
	}

	/**
	 * Getter method.
	 *
	 * @return A reference to the single, unique, InitProposition.
	 */
	/*
	public ExternalizedStateProposition getInitProposition(){
		return initProposition;
	}
	*/

	/**
	 * Getter method.
	 *
	 * @return A reference to the single, unique, TerminalProposition.
	 */
	public ExternalizedStateProposition getTerminalProposition(){
		return terminalProposition;
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
	 * @return References to every InputProposition in the PropNet, grouped by role
	 * and ordered.
	 */
	public List<ExternalizedStateProposition> getInputPropositions(){
		return this.inputPropositions;
	}

	/**
	 * Getter method.
	 *
	 * @return References to every InputProposition in the PropNet, grouped by role
	 * and ordered.
	 */
	public List<ExternalizedStateProposition> getLegalPropositions(){
		return this.legalPropositions;
	}

	/**
	 * Getter method.
	 *
	 * @return References to every InputProposition in the PropNet, indexed by
	 *         player name and ordered.
	 */
	public Map<ExplicitRole, List<ExternalizedStateProposition>> getInputsPerRole(){
		return this.inputsPerRole;
	}

	/**
	 * Getter method.
	 *
	 * @return References to every LegalProposition in the PropNet, indexed by
	 *         player name and ordered.
	 */
	public Map<ExplicitRole, List<ExternalizedStateProposition>> getLegalsPerRole(){
		return this.legalsPerRole;
	}

	/**
	 * Getter method.
	 *
	 * @return References to every GoalProposition in the PropNet, indexed by
	 *         player name.
	 */
	public Map<ExplicitRole, List<ExternalizedStateProposition>> getGoalsPerRole(){
		return this.goalsPerRole;
	}

	/**
	 * Getter method.
	 *
	 * @return The goal values corresponding to each goal proposition, divided by role
	 * 		   and in the same order as the goalsPerRole propositions.
	 */
	public Map<ExplicitRole, List<Integer>> getGoalValues(){
		return this.goalValues;
	}

	/**
	 * Getter method.
	 *
	 * @return References to every AND and OR gate in the PropNet.
	 */
	public List<ExternalizedStateComponent> getAndOrGates(){
		return this.andOrGates;
	}

	/** References to every LegalProposition in the PropNet, indexed by role. */
//	private final Map<Role, Set<ExternalizedStateProposition>> legalPropositions;


	/** A helper mapping between input/legal propositions. */
//	private final Map<ExternalizedStateProposition, ExternalizedStateProposition> legalInputMap;


/*
	public void addComponent(ExternalizedStateComponent c)
	{
		components.add(c);
		if (c instanceof ExternalizedStateProposition) propositions.add((ExternalizedStateProposition)c);
	}
*/
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


/*
	public Map<ExternalizedStateProposition, ExternalizedStateProposition> getLegalInputMap()
	{
		return legalInputMap;
	}
*/
	/*
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

	*/

	/**
	 * Getter method.
	 *
	 * @return References to every LegalProposition in the PropNet, indexed by
	 *         player name.
	 */
	/*public Map<Role, Set<ExternalizedStateProposition>> getLegalPropositions()
	{
		return legalPropositions;
	}
*/

	/**
	 * Returns a representation of the PropNet in .dot format.
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();

		sb.append("digraph propNet\n{\n");
		for (ExternalizedStateComponent component : components){
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
    public void renderToFile(String filename){
        try{
            File f = new File(filename);
            FileOutputStream fos = new FileOutputStream(f);
            OutputStreamWriter fout = new OutputStreamWriter(fos, "UTF-8");
            fout.write(toString());
            fout.close();
            fos.close();
        }catch(Exception e){
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
	/*private Map<GdlSentence, ExternalizedStateProposition> recordBasePropositions()
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
	}*/

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
	/*private Map<Role, Set<ExternalizedStateProposition>> recordGoalPropositions()
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
	}*/

	/**
	 * Returns a reference to the single, unique, InitProposition.
	 *
	 * @return A reference to the single, unique, InitProposition.
	 */
/*	private ExternalizedStateProposition recordInitProposition()
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
*/
	/**
	 * Builds an index over the InputPropositions in the PropNet.
	 *
	 * @return An index over the InputPropositions in the PropNet.
	 */
/*	private Map<GdlSentence, ExternalizedStateProposition> recordInputPropositions()
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
*/
	/**
	 * Builds an index over the LegalPropositions in the PropNet.
	 *
	 * @return An index over the LegalPropositions in the PropNet.
	 */
/*	private Map<Role, Set<ExternalizedStateProposition>> recordLegalPropositions()
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
*/
	/**
	 * Builds an index over the Propositions in the PropNet.
	 *
	 * @return An index over Propositions in the PropNet.
	 */
/*	private Set<ExternalizedStateProposition> recordPropositions()
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
*/
	/**
	 * Records a reference to the single, unique, TerminalProposition.
	 *
	 * @return A reference to the single, unqiue, TerminalProposition.
	 */
/*	private ExternalizedStateProposition recordTerminalProposition()
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
*/

    /**
     * Computes the size of the propNet.
     *
     * @return  the size of the propNet, i.e. the number of components in it.
     */
	public int getSize(){
		return this.components.size();
	}

    /**
     * Computes the number of propositions in the propNet.
     *
     * @return  the number of propositions in the propNet.
     */
	public int getNumPropositions(){
		return this.propositions.size();
	}

	/**
	 * Computes the number of AND gates in the propNet.
	 *
	 * @return the number of AND gates in the propNet.
	 */
	public int getNumAnds(){
		int andCount = 0;
		for(ExternalizedStateComponent c : this.andOrGates){
			if(c instanceof ExternalizedStateAnd)
				andCount++;
		}
		return andCount;
	}

	/**
	 * Computes the number of OR gates in the propNet.
	 *
	 * @return the number of OR gates in the propNet.
	 */
	public int getNumOrs(){
		int orCount = 0;
		for(ExternalizedStateComponent c : this.andOrGates){
			if(c instanceof ExternalizedStateOr)
				orCount++;
		}
		return orCount;
	}

	/**
	 * Computes the number of NOT components in the propNet.
	 *
	 * @return the number of NOT components in the propNet.
	 */
	public int getNumNots(){
		int notCount = 0;
		for(ExternalizedStateComponent c : this.components){
			if(c instanceof ExternalizedStateNot)
				notCount++;
		}
		return notCount;
	}

	/**
	 * Computes the number of links in the propNet.
	 *
	 * @return the number of links in the propNet.
	 */
	public int getNumLinks(){
		int linkCount = 0;
		for(ExternalizedStateComponent c : this.components){
			linkCount += c.getOutputs().size();
		}
		return linkCount;
	}

	/**
	 * Computes the number of BASE propositions in the propNet.
	 *
	 * @return the number of BASE propositions in the propNet.
	 */
	public int getNumBases(){
		int basesCount = 0;
		for(ExternalizedStateComponent c : this.components){
			if(c instanceof ExternalizedStateProposition && ((ExternalizedStateProposition)c).getPropositionType() == PROP_TYPE.BASE)
				basesCount++;
		}
		return basesCount;
	}

	/**
	 * Computes the number of INPUT propositions in the propNet.
	 *
	 * @return the number of INPUT propositions in the propNet.
	 */
	public int getNumInputs(){
		int inputsCount = 0;
		for(ExternalizedStateComponent c : this.components){
			if(c instanceof ExternalizedStateProposition && ((ExternalizedStateProposition)c).getPropositionType() == PROP_TYPE.INPUT)
				inputsCount++;
		}
		return inputsCount;
	}

	/**
	 * Computes the number of GOAL propositions in the propNet.
	 *
	 * @return the number of GOAL propositions in the propNet.
	 */
	public int getNumGoals(){
		int goalsCount = 0;
		for(ExternalizedStateComponent c : this.components){
			if(c instanceof ExternalizedStateProposition && ((ExternalizedStateProposition)c).getPropositionType() == PROP_TYPE.GOAL)
				goalsCount++;
		}
		return goalsCount;
	}

	/**
	 * Computes the number of LEGAL propositions in the propNet.
	 *
	 * @return the number of LEGAL propositions in the propNet.
	 */
	public int getNumLegals(){
		int legalsCount = 0;
		for(ExternalizedStateComponent c : this.components){
			if(c instanceof ExternalizedStateProposition && ((ExternalizedStateProposition)c).getPropositionType() == PROP_TYPE.LEGAL)
				legalsCount++;
		}
		return legalsCount;
	}

	/**
	 * Computes the number of OTHER propositions in the propNet.
	 *
	 * @return the number of OTHER propositions in the propNet.
	 */
	public int getNumOthers(){
		int othersCount = 0;
		for(ExternalizedStateComponent c : this.components){
			if(c instanceof ExternalizedStateProposition && ((ExternalizedStateProposition)c).getPropositionType() == PROP_TYPE.OTHER)
				othersCount++;
		}
		return othersCount;
	}

	/**
	 * Computes the number of INIT (not "init") propositions in the propNet.
	 *
	 * @return the number of INIT (not "init") propositions in the propNet.
	 */
	public int getNumInits(){
		int initsCount = 0;
		for(ExternalizedStateComponent c : this.components){
			if(c instanceof ExternalizedStateProposition && ((ExternalizedStateProposition)c).getPropositionType() == PROP_TYPE.INIT)
				initsCount++;
		}
		return initsCount;
	}

	/**
	 * Computes the number of TERMINAL propositions in the propNet.
	 *
	 * @return the number of TERMINAL propositions in the propNet.
	 */
	public int getNumTerminals(){
		int terminalsCount = 0;
		for(ExternalizedStateComponent c : this.components){
			if(c instanceof ExternalizedStateProposition && ((ExternalizedStateProposition)c).getPropositionType() == PROP_TYPE.TERMINAL)
				terminalsCount++;
		}
		return terminalsCount;
	}

	/**
	 * Computes the number of TRANSITIONS in the propNet.
	 *
	 * @return the number of TRANSITIONS in the propNet.
	 */
	public int getNumTransitions(){
		int transitionsCount = 0;
		for(ExternalizedStateComponent c : this.components){
			if(c instanceof ExternalizedStateTransition)
				transitionsCount++;
		}
		return transitionsCount;
	}

	/**
	 * Computes the number of CONSTANTS in the propNet.
	 *
	 * @return the number of CONSTANTS in the propNet.
	 */
	public int getNumConstants(){
		int constantsCount = 0;
		for(ExternalizedStateComponent c : this.components){
			if(c instanceof ExternalizedStateConstant)
				constantsCount++;
		}
		return constantsCount;
	}


	/**
	 * Removes a component from the propnet. Be very careful when using
	 * this method, as it is not thread-safe. It is highly recommended
	 * that this method only be used in an optimization period between
	 * the propnet's creation and its initial use, during which it
	 * should only be accessed by a single thread.
	 *
	 * ATTENTION: use this method only before initializing the propnet
	 * state and setting the indices of each component in the state. If
	 * you use it after that, the propNet state will not correspond
	 * anymore to the propnet structure. Whenever you call this method
	 * you will have to recompute the propnet state and set the indices.
	 *
	 * ATTENTION: ANY(!) component can be removed from the propnet. Pay
	 * attention not to remove a component that is fundamental for the
	 * the correct functioning of your implementation of the propnet
	 * state machine (i.e. don't remove the terminal proposition if it
	 * is the one that gets checked at for every state to know if the
	 * state is terminal, or don't remove the INIT proposition if your
	 * implementation of the state machine uses it to determine which
	 * base propositions are true in the initial state).
	 */
	public void removeComponent(ExternalizedStateComponent c) {


		//Go through all the collections it could appear in
		if(c instanceof ExternalizedStateProposition) {
			ExternalizedStateProposition p = (ExternalizedStateProposition) c;

			ExplicitRole r;

			switch(p.getPropositionType()){
			case BASE:
				this.basePropositions.remove(p);
				break;
			case INPUT:
				this.inputPropositions.remove(p);
				// Find the role for this input
				//r = new Role((GdlConstant) ((GdlRelation) p.getName()).get(0));
				//this.inputsPerRole.get(r).remove(p);
				break;
			case LEGAL:
				this.legalPropositions.remove(p);
				// Find the role for this legal
				//r = new Role((GdlConstant) ((GdlRelation) p.getName()).get(0));
				//this.legalsPerRole.get(r).remove(p);
				break;
			case GOAL:
				// Find the role for this goal
				r = new ExplicitRole((GdlConstant) ((GdlRelation) p.getName()).get(0));
				List<ExternalizedStateProposition> goals = this.goalsPerRole.get(r);
				int index = goals.indexOf(p);
				this.goalValues.get(r).remove(index);
				break;
			case TERMINAL:
				if(this.terminalProposition == p){
					this.terminalProposition = null;
				}
				break;
			case INIT:
				/*if(this.initProposition == p){
					this.initProposition = null;
				}*/
				break;
			default:
				break;
			}

			this.propositions.remove(p);
		}else if(c instanceof ExternalizedStateConstant){
			if(this.trueConstant == c){
				this.trueConstant = null;
			}else if(this.falseConstant == c){
				this.falseConstant = null;
			}
		}else if(c instanceof ExternalizedStateAnd || c instanceof ExternalizedStateOr){
			this.andOrGates.remove(c);
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
}
