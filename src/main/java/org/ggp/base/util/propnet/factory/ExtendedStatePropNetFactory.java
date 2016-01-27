package org.ggp.base.util.propnet.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.ggp.base.util.Pair;
import org.ggp.base.util.concurrency.ConcurrencyUtils;
import org.ggp.base.util.gdl.GdlUtils;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlDistinct;
import org.ggp.base.util.gdl.grammar.GdlLiteral;
import org.ggp.base.util.gdl.grammar.GdlNot;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.gdl.grammar.GdlProposition;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlRule;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.gdl.grammar.GdlVariable;
import org.ggp.base.util.gdl.model.SentenceDomainModel;
import org.ggp.base.util.gdl.model.SentenceDomainModelFactory;
import org.ggp.base.util.gdl.model.SentenceDomainModelOptimizer;
import org.ggp.base.util.gdl.model.SentenceForm;
import org.ggp.base.util.gdl.model.SentenceForms;
import org.ggp.base.util.gdl.model.SentenceModelUtils;
import org.ggp.base.util.gdl.model.assignments.AssignmentIterator;
import org.ggp.base.util.gdl.model.assignments.Assignments;
import org.ggp.base.util.gdl.model.assignments.AssignmentsFactory;
import org.ggp.base.util.gdl.model.assignments.FunctionInfo;
import org.ggp.base.util.gdl.model.assignments.FunctionInfoImpl;
import org.ggp.base.util.gdl.transforms.CommonTransforms;
import org.ggp.base.util.gdl.transforms.CondensationIsolator;
import org.ggp.base.util.gdl.transforms.ConstantChecker;
import org.ggp.base.util.gdl.transforms.ConstantCheckerFactory;
import org.ggp.base.util.gdl.transforms.DeORer;
import org.ggp.base.util.gdl.transforms.GdlCleaner;
import org.ggp.base.util.gdl.transforms.Relationizer;
import org.ggp.base.util.gdl.transforms.VariableConstrainer;
import org.ggp.base.util.propnet.architecture.extendedState.ExtendedStateComponent;
import org.ggp.base.util.propnet.architecture.extendedState.ExtendedStatePropNet;
import org.ggp.base.util.propnet.architecture.extendedState.components.ExtendedStateAnd;
import org.ggp.base.util.propnet.architecture.extendedState.components.ExtendedStateConstant;
import org.ggp.base.util.propnet.architecture.extendedState.components.ExtendedStateNot;
import org.ggp.base.util.propnet.architecture.extendedState.components.ExtendedStateOr;
import org.ggp.base.util.propnet.architecture.extendedState.components.ExtendedStateProposition;
import org.ggp.base.util.propnet.architecture.extendedState.components.ExtendedStateTransition;
import org.ggp.base.util.statemachine.Role;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

/*
 * A propnet factory meant to optimize the propnet before it's even built,
 * mostly through transforming the GDL. (The transformations identify certain
 * classes of rules that have poor performance and replace them with equivalent
 * rules that have better performance, with performance measured by the size of
 * the propnet.)
 *
 * Known issues:
 * - Does not work on games with many advanced forms of recursion. These include:
 *   - Anything that breaks the SentenceModel
 *   - Multiple sentence forms which reference one another in rules
 *   - Not 100% confirmed to work on games where recursive rules have multiple
 *     recursive conjuncts
 * - Currently runs some of the transformations multiple times. A Description
 *   object containing information about the description and its properties would
 *   alleviate this.
 * - It does not have a way of automatically solving the "unaffected piece rule" problem.
 * - Depending on the settings and the situation, the behavior of the
 *   CondensationIsolator can be either too aggressive or not aggressive enough.
 *   Both result in excessively large games. A more sophisticated version of the
 *   CondensationIsolator could solve these problems. A stopgap alternative is to
 *   try both settings and use the smaller propnet (or the first to be created,
 *   if multithreading).
 *
 */
public class ExtendedStatePropNetFactory {

	/**
	 * Static reference to the logger
	 */
	/*private static final Logger LOGGER;

	static{

		LOGGER = LogManager.getRootLogger();

	}*/

	static final private GdlConstant LEGAL = GdlPool.getConstant("legal");
	static final private GdlConstant NEXT = GdlPool.getConstant("next");
	static final private GdlConstant TRUE = GdlPool.getConstant("true");
	static final private GdlConstant DOES = GdlPool.getConstant("does");
	static final private GdlConstant GOAL = GdlPool.getConstant("goal");
	static final private GdlConstant INIT = GdlPool.getConstant("init");
	//TODO: This currently doesn't actually give a different constant from INIT
	static final private GdlConstant INIT_CAPS = GdlPool.getConstant("INIT");
	static final private GdlConstant TERMINAL = GdlPool.getConstant("terminal");
    static final private GdlConstant BASE = GdlPool.getConstant("base");
    static final private GdlConstant INPUT = GdlPool.getConstant("input");
	static final private GdlProposition TEMP = GdlPool.getProposition(GdlPool.getConstant("TEMP"));

	/**
	 * Creates a PropNet for the game with the given description.
	 *
	 * @throws InterruptedException if the thread is interrupted during
	 * PropNet creation.
	 */
	public static ExtendedStatePropNet create(List<Gdl> description) throws InterruptedException {
		return create(description, false);
	}

	public static ExtendedStatePropNet create(List<Gdl> description, boolean verbose) throws InterruptedException {
		System.out.println("Building propnet...");
		//LOGGER.info("[PropnetFactory] Building propnet...");

		long startTime = System.currentTimeMillis();

		description = GdlCleaner.run(description);
		description = DeORer.run(description);
		description = VariableConstrainer.replaceFunctionValuedVariables(description);
		description = Relationizer.run(description);

		description = CondensationIsolator.run(description);


		if(verbose)
			for(Gdl gdl : description)
				System.out.println(gdl);

		/*
		if(verbose){
			String gdlString = "";
			for(Gdl gdl : description)
				gdlString += gdl + "\n";
			GamerLogger.log("PropnetFactory", gdlString);
		}
		*/

		//We want to start with a rule graph and follow the rule graph.
		//Start by finding general information about the game
		SentenceDomainModel model = SentenceDomainModelFactory.createWithCartesianDomains(description);
		//Restrict domains to values that could actually come up in rules.
		//See chinesecheckers4's "count" relation for an example of why this
		//could be useful.
		model = SentenceDomainModelOptimizer.restrictDomainsToUsefulValues(model);

		if(verbose){
			System.out.println("Setting constants...");
			//LOGGER.info("[PropnetFactory] Setting constants...");
		}

		ConstantChecker constantChecker = ConstantCheckerFactory.createWithForwardChaining(model);
		if(verbose){
			System.out.println("Done setting constants.");
			//LOGGER.info("[PropnetFactory] Done setting constants.");
		}

		Set<String> sentenceFormNames = SentenceForms.getNames(model.getSentenceForms());
		boolean usingBase = sentenceFormNames.contains("base");
		boolean usingInput = sentenceFormNames.contains("input");


		//For now, we're going to build this to work on those with a
		//particular restriction on the dependency graph:
		//Recursive loops may only contain one sentence form.
		//This describes most games, but not all legal games.
		Multimap<SentenceForm, SentenceForm> dependencyGraph = model.getDependencyGraph();
		if(verbose) {
			System.out.print("Computing topological ordering... ");
			System.out.flush();
			//LOGGER.info("[PropnetFactory] Computing topological ordering... ");
		}
		ConcurrencyUtils.checkForInterruption();
		List<SentenceForm> topologicalOrdering = getTopologicalOrdering(model.getSentenceForms(), dependencyGraph, usingBase, usingInput);
		if(verbose){
			System.out.println("done");
			//LOGGER.info("[PropnetFactory] done");
		}

		List<Role> roles = Role.computeRoles(description);
		Map<GdlSentence, ExtendedStateComponent> components = new HashMap<GdlSentence, ExtendedStateComponent>();
		Map<GdlSentence, ExtendedStateComponent> negations = new HashMap<GdlSentence, ExtendedStateComponent>();
		ExtendedStateConstant trueComponent = new ExtendedStateConstant(true);
		ExtendedStateConstant falseComponent = new ExtendedStateConstant(false);
		Map<SentenceForm, FunctionInfo> functionInfoMap = new HashMap<SentenceForm, FunctionInfo>();
		Map<SentenceForm, Collection<GdlSentence>> completedSentenceFormValues = new HashMap<SentenceForm, Collection<GdlSentence>>();
		for(SentenceForm form : topologicalOrdering) {
			ConcurrencyUtils.checkForInterruption();

			if(verbose) {
				System.out.print("Adding sentence form " + form);
				System.out.flush();
				//LOGGER.info("[PropnetFactory] Adding sentence form " + form);
			}
			if(constantChecker.isConstantForm(form)) {
				if(verbose){
					System.out.println(" (constant)");
					//LOGGER.info("[PropnetFactory]  (constant)");
				}
				//Only add it if it's important
				if(form.getName().equals(LEGAL)
						|| form.getName().equals(GOAL)
						|| form.getName().equals(INIT)
						|| form.getName().equals(NEXT)
						|| form.getName().equals(TERMINAL)) {
					//Add it
					for (GdlSentence trueSentence : constantChecker.getTrueSentences(form)) {
						ExtendedStateProposition trueProp = new ExtendedStateProposition(trueSentence);
						trueProp.addInput(trueComponent);
						trueComponent.addOutput(trueProp);
						components.put(trueSentence, trueComponent);
					}
				}

				if(verbose){
					System.out.println("Checking whether " + form + " is a functional constant...");
					//LOGGER.info("[PropnetFactory] Checking whether " + form + " is a functional constant...");
				}
				addConstantsToFunctionInfo(form, constantChecker, functionInfoMap);
				addFormToCompletedValues(form, completedSentenceFormValues, constantChecker);

				continue;
			}
			if(verbose){
				System.out.println();
				//GamerLogger.log(GamerLogger.FORMAT.PLAIN_FORMAT, "PropnetFactory", "");
			}
			//TODO: Adjust "recursive forms" appropriately
			//Add a temporary sentence form thingy? ...
			Map<GdlSentence, ExtendedStateComponent> temporaryComponents = new HashMap<GdlSentence, ExtendedStateComponent>();
			Map<GdlSentence, ExtendedStateComponent> temporaryNegations = new HashMap<GdlSentence, ExtendedStateComponent>();
			addSentenceForm(form, model, components, negations, trueComponent, falseComponent, usingBase, usingInput, Collections.singleton(form), temporaryComponents, temporaryNegations, functionInfoMap, constantChecker, completedSentenceFormValues);
			//TODO: Pass these over groups of multiple sentence forms
			if(verbose && !temporaryComponents.isEmpty()){
				System.out.println("Processing temporary components...");
				//LOGGER.info("[PropnetFactory] Processing temporary components...");
			}
			processTemporaryComponents(temporaryComponents, temporaryNegations, components, negations, trueComponent, falseComponent);
			addFormToCompletedValues(form, completedSentenceFormValues, components);
			//if(verbose)
				//TODO: Add this, but with the correct total number of components (not just Propositions)
				//System.out.println("  "+completedSentenceFormValues.get(form).size() + " components added");
		}
		//Connect "next" to "true"
		if(verbose){
			System.out.println("Adding transitions...");
			//LOGGER.info("[PropnetFactory] Adding transitions...");
		}
		addTransitions(components);
		//Set up "init" proposition
		if(verbose){
			System.out.println("Setting up 'init' proposition...");
			//LOGGER.info("[PropnetFactory] Setting up 'init' proposition...");
		}
		setUpInit(components, trueComponent, falseComponent);
		//Now we can safely...
		removeUselessBasePropositions(components, negations, trueComponent, falseComponent);
		if(verbose){
			System.out.println("Creating component set...");
			//LOGGER.info("[PropnetFactory] Creating component set...");
		}
		Set<ExtendedStateComponent> componentSet = new HashSet<ExtendedStateComponent>(components.values());
		//Try saving some memory here...
		components = null;
		negations = null;
		completeComponentSet(componentSet);
		ConcurrencyUtils.checkForInterruption();
		if(verbose){
			System.out.println("Initializing propnet object...");
			//LOGGER.info("[PropnetFactory] Initializing propnet object...");
		}
		//Make it look the same as the PropNetFactory results, until we decide
		//how we want it to look
		normalizePropositions(componentSet);
		ExtendedStatePropNet propnet = new ExtendedStatePropNet(roles, componentSet);
		if(verbose) {
			System.out.println("Done setting up propnet; took " + (System.currentTimeMillis() - startTime) + "ms, has " + componentSet.size() + " components and " + propnet.getNumLinks() + " links");
			System.out.println("Propnet has " +propnet.getNumAnds()+" ands; "+propnet.getNumOrs()+" ors; "+propnet.getNumNots()+" nots");
			//LOGGER.info("[PropnetFactory] Done setting up propnet; took " + (System.currentTimeMillis() - startTime) + "ms, has " + componentSet.size() + " components and " + propnet.getNumLinks() + " links.");
			//LOGGER.info("[PropnetFactory] Propnet has " +propnet.getNumAnds()+" ands; "+propnet.getNumOrs()+" ors; "+propnet.getNumNots()+" nots");
		}
		//System.out.println(propnet);
		return propnet;
	}


	private static void removeUselessBasePropositions(
			Map<GdlSentence, ExtendedStateComponent> components, Map<GdlSentence, ExtendedStateComponent> negations, ExtendedStateConstant trueComponent,
			ExtendedStateConstant falseComponent) throws InterruptedException {
		boolean changedSomething = false;
		for(Entry<GdlSentence, ExtendedStateComponent> entry : components.entrySet()) {
			if(entry.getKey().getName() == TRUE) {
				ExtendedStateComponent comp = entry.getValue();
				if(comp.getInputs().size() == 0) {
					comp.addInput(falseComponent);
					falseComponent.addOutput(comp);
					changedSomething = true;
				}
			}
		}
		if(!changedSomething)
			return;

		optimizeAwayTrueAndFalse(components, negations, trueComponent, falseComponent);
	}

	/**
	 * Changes the propositions contained in the propnet so that they correspond
	 * to the outputs of the PropNetFactory. This is for consistency and for
	 * backwards compatibility with respect to state machines designed for the
	 * old propnet factory. Feel free to remove this for your player.
	 *
	 * @param componentSet
	 */
	private static void normalizePropositions(Set<ExtendedStateComponent> componentSet) {
		for(ExtendedStateComponent component : componentSet) {
			if(component instanceof ExtendedStateProposition) {
				ExtendedStateProposition p = (ExtendedStateProposition) component;
				GdlSentence sentence = p.getName();
				if(sentence instanceof GdlRelation) {
					GdlRelation relation = (GdlRelation) sentence;
					if(relation.getName().equals(NEXT)) {
						p.setName(GdlPool.getProposition(GdlPool.getConstant("anon")));
					}
				}
			}
		}
	}

	private static void addFormToCompletedValues(
			SentenceForm form,
			Map<SentenceForm, Collection<GdlSentence>> completedSentenceFormValues,
			ConstantChecker constantChecker) {
		List<GdlSentence> sentences = new ArrayList<GdlSentence>();
		sentences.addAll(constantChecker.getTrueSentences(form));

		completedSentenceFormValues.put(form, sentences);
	}


	private static void addFormToCompletedValues(
			SentenceForm form,
			Map<SentenceForm, Collection<GdlSentence>> completedSentenceFormValues,
			Map<GdlSentence, ExtendedStateComponent> components) throws InterruptedException {
		//Kind of inefficient. Could do better by collecting these as we go,
		//then adding them back into the CSFV map once the sentence forms are complete.
		//completedSentenceFormValues.put(form, new ArrayList<GdlSentence>());
		List<GdlSentence> sentences = new ArrayList<GdlSentence>();
		for(GdlSentence sentence : components.keySet()) {
			ConcurrencyUtils.checkForInterruption();
			if(form.matches(sentence)) {
				//The sentence has a node associated with it
				sentences.add(sentence);
			}
		}
		completedSentenceFormValues.put(form, sentences);
	}


	private static void addConstantsToFunctionInfo(SentenceForm form,
			ConstantChecker constantChecker, Map<SentenceForm, FunctionInfo> functionInfoMap) throws InterruptedException {
		functionInfoMap.put(form, FunctionInfoImpl.create(form, constantChecker));
	}


	private static void processTemporaryComponents(
			Map<GdlSentence, ExtendedStateComponent> temporaryComponents,
			Map<GdlSentence, ExtendedStateComponent> temporaryNegations,
			Map<GdlSentence, ExtendedStateComponent> components,
			Map<GdlSentence, ExtendedStateComponent> negations, ExtendedStateComponent trueComponent,
			ExtendedStateComponent falseComponent) throws InterruptedException {
		//For each component in temporary components, we want to "put it back"
		//into the main components section.
		//We also want to do optimization here...
		//We don't want to end up with anything following from true/false.

		//Everything following from a temporary component (its outputs)
		//should instead become an output of the actual component.
		//If there is no actual component generated, then the statement
		//is necessarily FALSE and should be replaced by the false
		//component.
		for(GdlSentence sentence : temporaryComponents.keySet()) {
			ExtendedStateComponent tempComp = temporaryComponents.get(sentence);
			ExtendedStateComponent realComp = components.get(sentence);
			if(realComp == null) {
				realComp = falseComponent;
			}
			for(ExtendedStateComponent output : tempComp.getOutputs()) {
				//Disconnect
				output.removeInput(tempComp);
				//tempComp.removeOutput(output); //do at end
				//Connect
				output.addInput(realComp);
				realComp.addOutput(output);
			}
			tempComp.removeAllOutputs();

			if(temporaryNegations.containsKey(sentence)) {
				//Should be pointing to a "not" that now gets input from realComp
				//Should be fine to put into negations
				negations.put(sentence, temporaryNegations.get(sentence));
				//If this follows true/false, will get resolved by the next set of optimizations
			}

			optimizeAwayTrueAndFalse(components, negations, trueComponent, falseComponent);

		}
	}

	/**
	 * Components and negations may be null, if e.g. this is a post-optimization.
	 * TrueComponent and falseComponent are required.
	 *
	 * Checks for both the TRUE and the FALSE component if they have non essential
	 * children. If they do, the non essential children are "removed". Since this
	 * process might add some new children both to the TRUE and FALSE component,
	 * the process is repeated until both the TRUE and FALSE component are left
	 * with no non essential children.
	 *
	 * NON ESSENTIAL CHILD:
	 * - if it is not a transition and...
	 * 	-- ...it has children (the TRUE (or FALSE in case of a not) component can be
	 * 		connected directly to them).
	 * 	-- ...or it is not a proposition (in this case it also has no children so it
	 * 		is useless to keep a connective gate with no outputs).
	 * 	-- ...or it is a proposition that is not one of the standard GDL propositions
	 * 		(LEGAL, INIT, GOAL, TERMINAL).
	 *
	 * Doesn't actually work that way... shoot. Need something that will remove the
	 * component from the propnet entirely.
	 * @throws InterruptedException
	 */
	private static void optimizeAwayTrueAndFalse(Map<GdlSentence, ExtendedStateComponent> components, Map<GdlSentence, ExtendedStateComponent> negations, ExtendedStateComponent trueComponent, ExtendedStateComponent falseComponent) throws InterruptedException {
	    while(hasNonessentialChildren(trueComponent) || hasNonessentialChildren(falseComponent)) {
	    	ConcurrencyUtils.checkForInterruption();
            optimizeAwayTrue(components, negations, null, trueComponent, falseComponent);
            optimizeAwayFalse(components, negations, null, trueComponent, falseComponent);
        }
	}

	private static void optimizeAwayTrueAndFalse(ExtendedStatePropNet pn, ExtendedStateComponent trueComponent, ExtendedStateComponent falseComponent) {
	    while(hasNonessentialChildren(trueComponent) || hasNonessentialChildren(falseComponent)) {
	        optimizeAwayTrue(null, null, pn, trueComponent, falseComponent);
	        optimizeAwayFalse(null, null, pn, trueComponent, falseComponent);
	    }
	}

	//TODO: Create a version with just a set of components that we can share with post-optimizations
	private static void optimizeAwayFalse(
			Map<GdlSentence, ExtendedStateComponent> components, Map<GdlSentence, ExtendedStateComponent> negations, ExtendedStatePropNet pn, ExtendedStateComponent trueComponent,
			ExtendedStateComponent falseComponent) {
        assert((components != null && negations != null) || pn != null);
        assert((components == null && negations == null) || pn == null);
        for (ExtendedStateComponent output : Lists.newArrayList(falseComponent.getOutputs())) {
        	if (isEssentialProposition(output) || output instanceof ExtendedStateTransition) {
        		//Since this is the false constant, there are a few "essential" types
        		//we don't actually want to keep around.
        		if (!isLegalOrGoalProposition(output)) {
        			continue;
        		}
	    	}
			if(output instanceof ExtendedStateProposition) {
				//Move its outputs to be outputs of false
				for(ExtendedStateComponent child : output.getOutputs()) {
					//Disconnect
					child.removeInput(output);
					//output.removeOutput(child); //do at end
					//Reconnect; will get children before returning, if nonessential
					falseComponent.addOutput(child);
					child.addInput(falseComponent);
				}
				output.removeAllOutputs();

				if(!isEssentialProposition(output)) {
					ExtendedStateProposition prop = (ExtendedStateProposition) output;
					//Remove the proposition entirely
					falseComponent.removeOutput(output);
					output.removeInput(falseComponent);
					//Update its location to the trueComponent in our map
					if(components != null) {
					    components.put(prop.getName(), falseComponent);
					    negations.put(prop.getName(), trueComponent);
					} else {
					    pn.removeComponent(output);
					}
				}
			} else if(output instanceof ExtendedStateAnd) {
				ExtendedStateAnd and = (ExtendedStateAnd) output;
				//Attach children of and to falseComponent
				for(ExtendedStateComponent child : and.getOutputs()) {
					child.addInput(falseComponent);
					falseComponent.addOutput(child);
					child.removeInput(and);
				}
				//Disconnect and completely
				and.removeAllOutputs();
				for(ExtendedStateComponent parent : and.getInputs())
					parent.removeOutput(and);
				and.removeAllInputs();
				if(pn != null)
				    pn.removeComponent(and);
			} else if(output instanceof ExtendedStateOr) {
				ExtendedStateOr or = (ExtendedStateOr) output;
				//Remove as input from or
				or.removeInput(falseComponent);
				falseComponent.removeOutput(or);
				//If or has only one input, remove it
				if(or.getInputs().size() == 1) {
					ExtendedStateComponent in = or.getSingleInput();
					or.removeInput(in);
					in.removeOutput(or);
					for(ExtendedStateComponent out : or.getOutputs()) {
						//Disconnect from and
						out.removeInput(or);
						//or.removeOutput(out); //do at end
						//Connect directly to the new input
						out.addInput(in);
						in.addOutput(out);
					}
					or.removeAllOutputs();
					if (pn != null) {
					    pn.removeComponent(or);
					}
				} else if (or.getInputs().size() == 0) {
					if (pn != null) {
						pn.removeComponent(or);
					}
				}
			} else if(output instanceof ExtendedStateNot) {
				ExtendedStateNot not = (ExtendedStateNot) output;
				//Disconnect from falseComponent
				not.removeInput(falseComponent);
				falseComponent.removeOutput(not);
				//Connect all children of the not to trueComponent
				for(ExtendedStateComponent child : not.getOutputs()) {
					//Disconnect
					child.removeInput(not);
					//not.removeOutput(child); //Do at end
					//Connect to trueComponent
					child.addInput(trueComponent);
					trueComponent.addOutput(child);
				}
				not.removeAllOutputs();
				if(pn != null)
				    pn.removeComponent(not);
			} else if(output instanceof ExtendedStateTransition) {
				//???
				System.err.println("Fix optimizeAwayFalse's case for Transitions");
			}
		}
	}


	private static boolean isLegalOrGoalProposition(ExtendedStateComponent comp) {
		if (!(comp instanceof ExtendedStateProposition)) {
			return false;
		}

		ExtendedStateProposition prop = (ExtendedStateProposition) comp;
		GdlSentence name = prop.getName();
		return name.getName() == GdlPool.LEGAL || name.getName() == GdlPool.GOAL;
	}

	/**
	 * For each output of the TRUE component:
	 * -
	 *
	 *
	 * @param components
	 * @param negations
	 * @param pn
	 * @param trueComponent
	 * @param falseComponent
	 */
	private static void optimizeAwayTrue(
			Map<GdlSentence, ExtendedStateComponent> components, Map<GdlSentence, ExtendedStateComponent> negations, ExtendedStatePropNet pn, ExtendedStateComponent trueComponent,
			ExtendedStateComponent falseComponent) {
	    assert((components != null && negations != null) || pn != null);
	    for (ExtendedStateComponent output : Lists.newArrayList(trueComponent.getOutputs())) {
	    	if (isEssentialProposition(output) || output instanceof ExtendedStateTransition) {
	    		continue;
	    	}
			if(output instanceof ExtendedStateProposition) {
				//Move its outputs to be outputs of true
				for(ExtendedStateComponent child : output.getOutputs()) {
					//Disconnect
					child.removeInput(output);
					//output.removeOutput(child); //do at end
					//Reconnect; will get children before returning, if nonessential
					trueComponent.addOutput(child);
					child.addInput(trueComponent);
				}
				output.removeAllOutputs();

				if(!isEssentialProposition(output)) {
					ExtendedStateProposition prop = (ExtendedStateProposition) output;
					//Remove the proposition entirely
					trueComponent.removeOutput(output);
					output.removeInput(trueComponent);
					//Update its location to the trueComponent in our map
					if(components != null) {
					    components.put(prop.getName(), trueComponent);
					    negations.put(prop.getName(), falseComponent);
					} else {
					    pn.removeComponent(output);
					}
				}
			} else if(output instanceof ExtendedStateOr) {
				ExtendedStateOr or = (ExtendedStateOr) output;
				//Attach children of or to trueComponent
				for(ExtendedStateComponent child : or.getOutputs()) {
					child.addInput(trueComponent);
					trueComponent.addOutput(child);
					child.removeInput(or);
				}
				//Disconnect or completely
				or.removeAllOutputs();
				for(ExtendedStateComponent parent : or.getInputs())
					parent.removeOutput(or);
				or.removeAllInputs();
				if(pn != null)
				    pn.removeComponent(or);
			} else if(output instanceof ExtendedStateAnd) {
				ExtendedStateAnd and = (ExtendedStateAnd) output;
				//Remove as input from and
				and.removeInput(trueComponent);
				trueComponent.removeOutput(and);
				//If and has only one input, remove it
				if(and.getInputs().size() == 1) {
					ExtendedStateComponent in = and.getSingleInput();
					and.removeInput(in);
					in.removeOutput(and);
					for(ExtendedStateComponent out : and.getOutputs()) {
						//Disconnect from and
						out.removeInput(and);
						//and.removeOutput(out); //do at end
						//Connect directly to the new input
						out.addInput(in);
						in.addOutput(out);
					}
					and.removeAllOutputs();
					if (pn != null) {
					    pn.removeComponent(and);
					}
				} else if (and.getInputs().size() == 0) {
					if (pn != null) {
						pn.removeComponent(and);
					}
				}
			} else if(output instanceof ExtendedStateNot) {
				ExtendedStateNot not = (ExtendedStateNot) output;
				//Disconnect from trueComponent
				not.removeInput(trueComponent);
				trueComponent.removeOutput(not);
				//Connect all children of the not to falseComponent
				for(ExtendedStateComponent child : not.getOutputs()) {
					//Disconnect
					child.removeInput(not);
					//not.removeOutput(child); //Do at end
					//Connect to falseComponent
					child.addInput(falseComponent);
					falseComponent.addOutput(child);
				}
				not.removeAllOutputs();
				if(pn != null)
				    pn.removeComponent(not);
			} else if(output instanceof ExtendedStateTransition) {
				//???
				System.err.println("Fix optimizeAwayTrue's case for Transitions");
			}
		}
	}


	private static boolean hasNonessentialChildren(ExtendedStateComponent trueComponent) {
		for(ExtendedStateComponent child : trueComponent.getOutputs()) {
			if(child instanceof ExtendedStateTransition)
				continue;
			if(!isEssentialProposition(child))
				return true;
			//We don't want any grandchildren, either
			if(!child.getOutputs().isEmpty())
				return true;
		}
		return false;
	}


	private static boolean isEssentialProposition(ExtendedStateComponent component) {
		if(!(component instanceof ExtendedStateProposition))
			return false;

		//We're looking for things that would be outputs of "true" or "false",
		//but we would still want to keep as propositions to be read by the
		//state machine
		ExtendedStateProposition prop = (ExtendedStateProposition) component;
		GdlConstant name = prop.getName().getName();

		return name.equals(LEGAL) /*|| name.equals(NEXT)*/ || name.equals(GOAL)
				|| name.equals(INIT) || name.equals(TERMINAL);
	}


	private static void completeComponentSet(Set<ExtendedStateComponent> componentSet) {
		Set<ExtendedStateComponent> newComponents = new HashSet<ExtendedStateComponent>();
		Set<ExtendedStateComponent> componentsToTry = new HashSet<ExtendedStateComponent>(componentSet);
		while(!componentsToTry.isEmpty()) {
			for(ExtendedStateComponent c : componentsToTry) {
				for(ExtendedStateComponent out : c.getOutputs()) {
					if(!componentSet.contains(out))
						newComponents.add(out);
				}
				for(ExtendedStateComponent in : c.getInputs()) {
					if(!componentSet.contains(in))
						newComponents.add(in);
				}
			}
			componentSet.addAll(newComponents);
			componentsToTry = newComponents;
			newComponents = new HashSet<ExtendedStateComponent>();
		}
	}


	private static void addTransitions(Map<GdlSentence, ExtendedStateComponent> components) {
		for(Entry<GdlSentence, ExtendedStateComponent> entry : components.entrySet()) {
			GdlSentence sentence = entry.getKey();

			if(sentence.getName().equals(NEXT)) {
				//connect to true
				GdlSentence trueSentence = GdlPool.getRelation(TRUE, sentence.getBody());
				ExtendedStateComponent nextComponent = entry.getValue();
				ExtendedStateComponent trueComponent = components.get(trueSentence);
				//There might be no true component (for example, because the bases
				//told us so). If that's the case, don't have a transition.
				if(trueComponent == null) {
				    // Skipping transition to supposedly impossible 'trueSentence'
				    continue;
				}
				ExtendedStateTransition transition = new ExtendedStateTransition();
				transition.addInput(nextComponent);
				nextComponent.addOutput(transition);
				transition.addOutput(trueComponent);
				trueComponent.addInput(transition);
			}
		}
	}

	//TODO: Replace with version using constantChecker only
	//TODO: This can give problematic results if interpreted in
	//the standard way (see test_case_3d)
	private static void setUpInit(Map<GdlSentence, ExtendedStateComponent> components,
			ExtendedStateConstant trueComponent, ExtendedStateConstant falseComponent) {
		ExtendedStateProposition initProposition = new ExtendedStateProposition(GdlPool.getProposition(INIT_CAPS));
		for(Entry<GdlSentence, ExtendedStateComponent> entry : components.entrySet()) {
			//Is this something that will be true?
			if(entry.getValue() == trueComponent) {
				if(entry.getKey().getName().equals(INIT)) {
					//Find the corresponding true sentence
					GdlSentence trueSentence = GdlPool.getRelation(TRUE, entry.getKey().getBody());
					//System.out.println("True sentence from init: " + trueSentence);
					ExtendedStateComponent trueSentenceComponent = components.get(trueSentence);
					if(trueSentenceComponent.getInputs().isEmpty()) {
						//Case where there is no transition input
						//Add the transition input, connect to init, continue loop

						// @author c.sironi: Also set to TRUE that fact that the value of this transition
						// depends on the INIT proposition value.
						ExtendedStateTransition transition = new ExtendedStateTransition(true);
						//init goes into transition
						transition.addInput(initProposition);
						initProposition.addOutput(transition);
						//transition goes into component
						trueSentenceComponent.addInput(transition);
						transition.addOutput(trueSentenceComponent);
					} else {
						//The transition already exists
						ExtendedStateComponent transition = trueSentenceComponent.getSingleInput();

						//We want to add init as a thing that precedes the transition
						//Disconnect existing input
						ExtendedStateComponent input = transition.getSingleInput();
						//input and init go into or, or goes into transition
						input.removeOutput(transition);
						transition.removeInput(input);
						List<ExtendedStateComponent> orInputs = new ArrayList<ExtendedStateComponent>(2);
						orInputs.add(input);
						orInputs.add(initProposition);
						orify(orInputs, transition, falseComponent);
						// @author c.sironi: Also set to TRUE that fact that the value of this transition
						// depends on the INIT proposition value.
						((ExtendedStateTransition) transition).setDependingOnInit(true);
					}
				}
			}
		}
	}

	/**
	 * Adds an or gate connecting the inputs to produce the output.
	 * Handles special optimization cases like a true/false input.
	 */
	private static void orify(Collection<ExtendedStateComponent> inputs, ExtendedStateComponent output, ExtendedStateConstant falseProp) {
		//TODO: Look for already-existing ors with the same inputs?
		//Or can this be handled with a GDL transformation?

		//Special case: An input is the true constant
		for(ExtendedStateComponent in : inputs) {
			if(in instanceof ExtendedStateConstant && in.getValue()) {
				//True constant: connect that to the component, done
				in.addOutput(output);
				output.addInput(in);
				return;
			}
		}

		//Special case: An input is "or"
		//I'm honestly not sure how to handle special cases here...
		//What if that "or" gate has multiple outputs? Could that happen?

		//For reals... just skip over any false constants
		ExtendedStateOr or = new ExtendedStateOr();
		for(ExtendedStateComponent in : inputs) {
			if(!(in instanceof ExtendedStateConstant)) {
				in.addOutput(or);
				or.addInput(in);
			}
		}
		//What if they're all false? (Or inputs is empty?) Then no inputs at this point...
		if(or.getInputs().isEmpty()) {
			//Hook up to "false"
			falseProp.addOutput(output);
			output.addInput(falseProp);
			return;
		}
		//If there's just one, on the other hand, don't use the or gate
		if(or.getInputs().size() == 1) {
			ExtendedStateComponent in = or.getSingleInput();
			in.removeOutput(or);
			or.removeInput(in);
			in.addOutput(output);
			output.addInput(in);
			return;
		}
		or.addOutput(output);
		output.addInput(or);
	}

	//TODO: This code is currently used by multiple classes, so perhaps it should be
	//factored out into the SentenceModel.
	private static List<SentenceForm> getTopologicalOrdering(
			Set<SentenceForm> forms,
			Multimap<SentenceForm, SentenceForm> dependencyGraph, boolean usingBase, boolean usingInput) throws InterruptedException {
		//We want each form as a key of the dependency graph to
		//follow all the forms in the dependency graph, except maybe itself
		Queue<SentenceForm> queue = new LinkedList<SentenceForm>(forms);
		List<SentenceForm> ordering = new ArrayList<SentenceForm>(forms.size());
		Set<SentenceForm> alreadyOrdered = new HashSet<SentenceForm>();
		while(!queue.isEmpty()) {
			SentenceForm curForm = queue.remove();
			boolean readyToAdd = true;
			//Don't add if there are dependencies
			for(SentenceForm dependency : dependencyGraph.get(curForm)) {
				if(!dependency.equals(curForm) && !alreadyOrdered.contains(dependency)) {
					readyToAdd = false;
					break;
				}
			}
			//Don't add if it's true/next/legal/does and we're waiting for base/input
			if(usingBase && (curForm.getName().equals(TRUE) || curForm.getName().equals(NEXT) || curForm.getName().equals(INIT))) {
				//Have we added the corresponding base sf yet?
				SentenceForm baseForm = curForm.withName(BASE);
				if(!alreadyOrdered.contains(baseForm)) {
					readyToAdd = false;
				}
			}
			if(usingInput && (curForm.getName().equals(DOES) || curForm.getName().equals(LEGAL))) {
				SentenceForm inputForm = curForm.withName(INPUT);
				if(!alreadyOrdered.contains(inputForm)) {
					readyToAdd = false;
				}
			}
			//Add it
			if(readyToAdd) {
				ordering.add(curForm);
				alreadyOrdered.add(curForm);
			} else {
				queue.add(curForm);
			}
			//TODO: Add check for an infinite loop here, or stratify loops

			ConcurrencyUtils.checkForInterruption();
		}
		return ordering;
	}

	private static void addSentenceForm(SentenceForm form, SentenceDomainModel model,
			Map<GdlSentence, ExtendedStateComponent> components,
			Map<GdlSentence, ExtendedStateComponent> negations,
			ExtendedStateConstant trueComponent, ExtendedStateConstant falseComponent,
			boolean usingBase, boolean usingInput,
			Set<SentenceForm> recursionForms,
			Map<GdlSentence, ExtendedStateComponent> temporaryComponents, Map<GdlSentence, ExtendedStateComponent> temporaryNegations,
			Map<SentenceForm, FunctionInfo> functionInfoMap, ConstantChecker constantChecker,
			Map<SentenceForm, Collection<GdlSentence>> completedSentenceFormValues) throws InterruptedException {
		//This is the meat of it (along with the entire Assignments class).
		//We need to enumerate the possible propositions in the sentence form...
		//We also need to hook up the sentence form to the inputs that can make it true.
		//We also try to optimize as we go, which means possibly removing the
		//proposition if it isn't actually possible, or replacing it with
		//true/false if it's a constant.

		Set<GdlSentence> alwaysTrueSentences = model.getSentencesListedAsTrue(form);
		Set<GdlRule> rules = model.getRules(form);

		for(GdlSentence alwaysTrueSentence : alwaysTrueSentences) {
			//We add the sentence as a constant
			if(alwaysTrueSentence.getName().equals(LEGAL)
					|| alwaysTrueSentence.getName().equals(NEXT)
					|| alwaysTrueSentence.getName().equals(GOAL)) {
				ExtendedStateProposition prop = new ExtendedStateProposition(alwaysTrueSentence);
				//Attach to true
				trueComponent.addOutput(prop);
				prop.addInput(trueComponent);
				//Still want the same components;
				//we just don't want this to be anonymized
			}
			//Assign as true
			components.put(alwaysTrueSentence, trueComponent);
			negations.put(alwaysTrueSentence, falseComponent);
			continue;
		}

		//For does/true, make nodes based on input/base, if available
		if(usingInput && form.getName().equals(DOES)) {
			//Add only those propositions for which there is a corresponding INPUT
			SentenceForm inputForm = form.withName(INPUT);
			for (GdlSentence inputSentence : constantChecker.getTrueSentences(inputForm)) {
				GdlSentence doesSentence = GdlPool.getRelation(DOES, inputSentence.getBody());
				ExtendedStateProposition prop = new ExtendedStateProposition(doesSentence);
				components.put(doesSentence, prop);
			}
			return;
		}
		if(usingBase && form.getName().equals(TRUE)) {
			SentenceForm baseForm = form.withName(BASE);
			for (GdlSentence baseSentence : constantChecker.getTrueSentences(baseForm)) {
				GdlSentence trueSentence = GdlPool.getRelation(TRUE, baseSentence.getBody());
				ExtendedStateProposition prop = new ExtendedStateProposition(trueSentence);
				components.put(trueSentence, prop);
			}
			return;
		}

		Map<GdlSentence, Set<ExtendedStateComponent>> inputsToOr = new HashMap<GdlSentence, Set<ExtendedStateComponent>>();
		for(GdlRule rule : rules) {
			Assignments assignments = AssignmentsFactory.getAssignmentsForRule(rule, model, functionInfoMap, completedSentenceFormValues);

			//Calculate vars in live (non-constant, non-distinct) conjuncts
			Set<GdlVariable> varsInLiveConjuncts = getVarsInLiveConjuncts(rule, constantChecker.getConstantSentenceForms());
			varsInLiveConjuncts.addAll(GdlUtils.getVariables(rule.getHead()));
			Set<GdlVariable> varsInRule = new HashSet<GdlVariable>(GdlUtils.getVariables(rule));
			boolean preventDuplicatesFromConstants =
				(varsInRule.size() > varsInLiveConjuncts.size());

			//Do we just pass those to the Assignments class in that case?
			for(AssignmentIterator asnItr = assignments.getIterator(); asnItr.hasNext(); ) {
				Map<GdlVariable, GdlConstant> assignment = asnItr.next();
				if(assignment == null) continue; //Not sure if this will ever happen

				ConcurrencyUtils.checkForInterruption();

				GdlSentence sentence = CommonTransforms.replaceVariables(rule.getHead(), assignment);

				//Now we go through the conjuncts as before, but we wait to hook them up.
				List<ExtendedStateComponent> componentsToConnect = new ArrayList<ExtendedStateComponent>(rule.arity());
				for(GdlLiteral literal : rule.getBody()) {
					if(literal instanceof GdlSentence) {
						//Get the sentence post-substitutions
						GdlSentence transformed = CommonTransforms.replaceVariables((GdlSentence) literal, assignment);

						//Check for constant-ness
						SentenceForm conjunctForm = model.getSentenceForm(transformed);
						if(constantChecker.isConstantForm(conjunctForm)) {
							if(!constantChecker.isTrueConstant(transformed)) {
								List<GdlVariable> varsToChange = getVarsInConjunct(literal);
								asnItr.changeOneInNext(varsToChange, assignment);
								componentsToConnect.add(null);
							}
							continue;
						}

						ExtendedStateComponent conj = components.get(transformed);
						//If conj is null and this is a sentence form we're still handling,
						//hook up to a temporary sentence form
						if(conj == null) {
							conj = temporaryComponents.get(transformed);
						}
						if(conj == null && SentenceModelUtils.inSentenceFormGroup(transformed, recursionForms)) {
							//Set up a temporary component
							ExtendedStateProposition tempProp = new ExtendedStateProposition(transformed);
							temporaryComponents.put(transformed, tempProp);
							conj = tempProp;
						}
						//Let's say this is false; we want to backtrack and change the right variable
						if(conj == null || isThisConstant(conj, falseComponent)) {
							List<GdlVariable> varsInConjunct = getVarsInConjunct(literal);
							asnItr.changeOneInNext(varsInConjunct, assignment);
							//These last steps just speed up the process
							//telling the factory to ignore this rule
							componentsToConnect.add(null);
							continue; //look at all the other restrictions we'll face
						}

						componentsToConnect.add(conj);
					} else if(literal instanceof GdlNot) {
						//Add a "not" if necessary
						//Look up the negation
						GdlSentence internal = (GdlSentence) ((GdlNot) literal).getBody();
						GdlSentence transformed = CommonTransforms.replaceVariables(internal, assignment);

						//Add constant-checking here...
						SentenceForm conjunctForm = model.getSentenceForm(transformed);
						if(constantChecker.isConstantForm(conjunctForm)) {
							if(constantChecker.isTrueConstant(transformed)) {
								List<GdlVariable> varsToChange = getVarsInConjunct(literal);
								asnItr.changeOneInNext(varsToChange, assignment);
								componentsToConnect.add(null);
							}
							continue;
						}

						ExtendedStateComponent conj = negations.get(transformed);
						if(isThisConstant(conj, falseComponent)) {
							//We need to change one of the variables inside
							List<GdlVariable> varsInConjunct = getVarsInConjunct(internal);
							asnItr.changeOneInNext(varsInConjunct, assignment);
							//ignore this rule
							componentsToConnect.add(null);
							continue;
						}
						if(conj == null) {
							conj = temporaryNegations.get(transformed);
						}
						//Check for the recursive case:
						if(conj == null && SentenceModelUtils.inSentenceFormGroup(transformed, recursionForms)) {
							ExtendedStateComponent positive = components.get(transformed);
							if(positive == null) {
								positive = temporaryComponents.get(transformed);
							}
							if(positive == null) {
								//Make the temporary proposition
								ExtendedStateProposition tempProp = new ExtendedStateProposition(transformed);
								temporaryComponents.put(transformed, tempProp);
								positive = tempProp;
							}
							//Positive is now set and in temporaryComponents
							//Evidently, wasn't in temporaryNegations
							//So we add the "not" gate and set it in temporaryNegations
							ExtendedStateNot not = new ExtendedStateNot();
							//Add positive as input
							not.addInput(positive);
							positive.addOutput(not);
							temporaryNegations.put(transformed, not);
							conj = not;
						}
						if(conj == null) {
							ExtendedStateComponent positive = components.get(transformed);
							//No, because then that will be attached to "negations", which could be bad

							if(positive == null) {
								//So the positive can't possibly be true (unless we have recurstion)
								//and so this would be positive always
								//We want to just skip this conjunct, so we continue to the next

								continue; //to the next conjunct
							}

							//Check if we're sharing a component with another sentence with a negation
							//(i.e. look for "nots" in our outputs and use those instead)
							ExtendedStateNot existingNotOutput = getNotOutput(positive);
							if(existingNotOutput != null) {
								componentsToConnect.add(existingNotOutput);
								negations.put(transformed, existingNotOutput);
								continue; //to the next conjunct
							}

							ExtendedStateNot not = new ExtendedStateNot();
							not.addInput(positive);
							positive.addOutput(not);
							negations.put(transformed, not);
							conj = not;
						}
						componentsToConnect.add(conj);
					} else if(literal instanceof GdlDistinct) {
						//Already handled; ignore
					} else {
						throw new RuntimeException("Unwanted GdlLiteral type");
					}
				}
				if(!componentsToConnect.contains(null)) {
					//Connect all the components
					ExtendedStateProposition andComponent = new ExtendedStateProposition(TEMP);

					andify(componentsToConnect, andComponent, trueComponent);
					if(!isThisConstant(andComponent, falseComponent)) {
						if(!inputsToOr.containsKey(sentence))
							inputsToOr.put(sentence, new HashSet<ExtendedStateComponent>());
						inputsToOr.get(sentence).add(andComponent);
						//We'll want to make sure at least one of the non-constant
						//components is changing
						if(preventDuplicatesFromConstants) {
							asnItr.changeOneInNext(varsInLiveConjuncts, assignment);
						}
					}
				}
			}
		}

		//At the end, we hook up the conjuncts
		for(Entry<GdlSentence, Set<ExtendedStateComponent>> entry : inputsToOr.entrySet()) {
			ConcurrencyUtils.checkForInterruption();

			GdlSentence sentence = entry.getKey();
			Set<ExtendedStateComponent> inputs = entry.getValue();
			Set<ExtendedStateComponent> realInputs = new HashSet<ExtendedStateComponent>();
			for(ExtendedStateComponent input : inputs) {
				if(input instanceof ExtendedStateConstant || input.getInputs().size() == 0) {
					realInputs.add(input);
				} else {
					realInputs.add(input.getSingleInput());
					input.getSingleInput().removeOutput(input);
					input.removeAllInputs();
				}
			}

			ExtendedStateProposition prop = new ExtendedStateProposition(sentence);
			orify(realInputs, prop, falseComponent);
			components.put(sentence, prop);
		}

		//True/does sentences will have none of these rules, but
		//still need to exist/"float"
		//We'll do this if we haven't used base/input as a basis
		if(form.getName().equals(TRUE)
				|| form.getName().equals(DOES)) {
			for(GdlSentence sentence : model.getDomain(form)) {
				ConcurrencyUtils.checkForInterruption();

				ExtendedStateProposition prop = new ExtendedStateProposition(sentence);
				components.put(sentence, prop);
			}
		}

	}


	private static Set<GdlVariable> getVarsInLiveConjuncts(
			GdlRule rule, Set<SentenceForm> constantSentenceForms) {
		Set<GdlVariable> result = new HashSet<GdlVariable>();
		for(GdlLiteral literal : rule.getBody()) {
			if(literal instanceof GdlRelation) {
				if(!SentenceModelUtils.inSentenceFormGroup((GdlRelation)literal, constantSentenceForms))
					result.addAll(GdlUtils.getVariables(literal));
			} else if(literal instanceof GdlNot) {
				GdlNot not = (GdlNot) literal;
				GdlSentence inner = (GdlSentence) not.getBody();
				if(!SentenceModelUtils.inSentenceFormGroup(inner, constantSentenceForms))
					result.addAll(GdlUtils.getVariables(literal));
			}
		}
		return result;
	}

	private static boolean isThisConstant(ExtendedStateComponent conj, ExtendedStateConstant constantComponent) {
		if(conj == constantComponent)
			return true;
		return (conj instanceof ExtendedStateProposition && conj.getInputs().size() == 1 && conj.getSingleInput() == constantComponent);
	}


	private static ExtendedStateNot getNotOutput(ExtendedStateComponent positive) {
		for(ExtendedStateComponent c : positive.getOutputs()) {
			if(c instanceof ExtendedStateNot) {
				return (ExtendedStateNot) c;
			}
		}
		return null;
	}


	private static List<GdlVariable> getVarsInConjunct(GdlLiteral literal) {
		return GdlUtils.getVariables(literal);
	}


	private static void andify(List<ExtendedStateComponent> inputs, ExtendedStateComponent output, ExtendedStateConstant trueProp) {
		//Special case: If the inputs include false, connect false to thisComponent
		for(ExtendedStateComponent c : inputs) {
			if(c instanceof ExtendedStateConstant && !c.getValue()) {
				//Connect false (c) to the output
				output.addInput(c);
				c.addOutput(output);
				return;
			}
		}

		//For reals... just skip over any true constants
		ExtendedStateAnd and = new ExtendedStateAnd();
		for(ExtendedStateComponent in : inputs) {
			if(!(in instanceof ExtendedStateConstant)) {
				in.addOutput(and);
				and.addInput(in);
			}
		}
		//What if they're all true? (Or inputs is empty?) Then no inputs at this point...
		if(and.getInputs().isEmpty()) {
			//Hook up to "true"
			trueProp.addOutput(output);
			output.addInput(trueProp);
			return;
		}
		//If there's just one, on the other hand, don't use the and gate
		if(and.getInputs().size() == 1) {
			ExtendedStateComponent in = and.getSingleInput();
			in.removeOutput(and);
			and.removeInput(in);
			in.addOutput(output);
			output.addInput(in);
			return;
		}
		and.addOutput(output);
		output.addInput(and);
	}

	/**
	 * Represents the "type" of a node with respect to which truth
	 * values it is capable of having: true, false, either value,
	 * or neither value. Used by
	 * {@link ExtendedStatePropNetFactory#removeUnreachableBasesAndInputs(ExtendedStatePropNet, Set)}.
	 */
	private static enum Type { NEITHER(false, false),
						TRUE(true, false),
						FALSE(false, true),
						BOTH(true, true);
		private final boolean hasTrue;
		private final boolean hasFalse;

		Type(boolean hasTrue, boolean hasFalse) {
			this.hasTrue = hasTrue;
			this.hasFalse = hasFalse;
		}

		public boolean includes(Type other) {
			switch (other) {
			case BOTH:
				return hasTrue && hasFalse;
			case FALSE:
				return hasFalse;
			case NEITHER:
				return true;
			case TRUE:
				return hasTrue;
			}
			throw new RuntimeException();
		}

		public Type with(Type otherType) {
			if (otherType == null) {
				otherType = NEITHER;
			}
			switch (otherType) {
			case BOTH:
				return BOTH;
			case NEITHER:
				return this;
			case TRUE:
				if (hasFalse) {
					return BOTH;
				} else {
					return TRUE;
				}
			case FALSE:
				if (hasTrue) {
					return BOTH;
				} else {
					return FALSE;
				}
			}
			throw new RuntimeException();
		}

		public Type minus(Type other) {
			switch (other) {
			case BOTH:
				return NEITHER;
			case TRUE:
				return hasFalse ? FALSE : NEITHER;
			case FALSE:
				return hasTrue ? TRUE : NEITHER;
			case NEITHER:
				return this;
			}
			throw new RuntimeException();
		}

		public Type opposite() {
			switch (this) {
			case TRUE:
				return FALSE;
			case FALSE:
				return TRUE;
			case NEITHER:
			case BOTH:
				return this;
			}
			throw new RuntimeException();
		}
	}

	/**
	 * Removes from the propnet all components that are discovered through type
	 * inference to only ever be true or false, replacing them with their values
	 * appropriately. This method may remove base and input propositions that are
	 * shown to be always false (or, in the case of base propositions, those that
	 * are always true).
	 *
	 * For each component this method iteratively checks which values it can assume
	 * during the game, depending on the values it's inputs can assume during the
	 * game.
	 * More precisely, for each component it checks which values it can assume in
	 * the initial state (NONE, TRUE, FALSE, BOTH), sets that all his children can
	 * also assume that value and puts the children in the stack of components to be
	 * checked, i.e. check which value they can assume and repeat the process with
	 * their children.
	 * Once this check is over, i.e. we know for all components the values that they
	 * can assume during the game, this method replaces the inputs of each always TRUE
	 * component with a TRUE constant and the inputs of each always FALSE component
	 * with a FALSE constant.
	 *
	 * NOTE: it also makes sure to set correctly all inputs and outputs of the modified
	 * components.
	 * NOTE: this method might leave the propnet with more than only one TRUE constant
	 * and FALSE constant.
	 *
	 * @param basesTrueByInit The set of base propositions that are true on the
	 * first turn of the game.
	 */
	public static void removeUnreachableBasesAndInputs(ExtendedStatePropNet pn, Set<ExtendedStateProposition> basesTrueByInit) throws InterruptedException {
		//If this doesn't contain a component, that's the equivalent of Type.NEITHER
		Map<ExtendedStateComponent, Type> reachability = Maps.newHashMap();
		//Keep track of the number of true inputs to AND gates and false inputs to
		//OR gates.
		Multiset<ExtendedStateComponent> numTrueInputs = HashMultiset.create();
		Multiset<ExtendedStateComponent> numFalseInputs = HashMultiset.create();
		Stack<Pair<ExtendedStateComponent, Type>> toAdd = new Stack<Pair<ExtendedStateComponent, Type>>();

		//It's easier here if we get just the one-way version of the map
		Map<ExtendedStateProposition, ExtendedStateProposition> legalsToInputs = Maps.newHashMap();
		for (ExtendedStateProposition legalProp : Iterables.concat(pn.getLegalPropositions().values())) {
			ExtendedStateProposition inputProp = pn.getLegalInputMap().get(legalProp);
			if (inputProp != null) {
				legalsToInputs.put(legalProp, inputProp);
			}
		}

		//All constants have their values
		for (ExtendedStateComponent c : pn.getComponents()) {
			ConcurrencyUtils.checkForInterruption();
			if (c instanceof ExtendedStateConstant) {
				if (c.getValue()) {
					toAdd.add(Pair.of(c, Type.TRUE));
				} else {
					toAdd.add(Pair.of(c, Type.FALSE));
				}
			}
		}

		//Every input can be false (we assume that no player will have just one move allowed all game)
        for(ExtendedStateProposition p : pn.getInputPropositions().values()) {
        	toAdd.add(Pair.of((ExtendedStateComponent) p, Type.FALSE));
        }
	    //Every base with "init" can be true, every base without "init" can be false
	    for(ExtendedStateProposition baseProp : pn.getBasePropositions().values()) {
            if (basesTrueByInit.contains(baseProp)) {
            	toAdd.add(Pair.of((ExtendedStateComponent) baseProp, Type.TRUE));
            } else {
            	toAdd.add(Pair.of((ExtendedStateComponent) baseProp, Type.FALSE));
            }
	    }
	    //Keep INIT, for those who use it
	    ExtendedStateProposition initProposition = pn.getInitProposition();
    	toAdd.add(Pair.of((ExtendedStateComponent) initProposition, Type.BOTH));

    	while (!toAdd.isEmpty()) {
			ConcurrencyUtils.checkForInterruption();
    		Pair<ExtendedStateComponent, Type> curEntry = toAdd.pop();
    		ExtendedStateComponent curComp = curEntry.left;
    		Type newInputType = curEntry.right;
    		Type oldType = reachability.get(curComp);
    		if (oldType == null) {
    			oldType = Type.NEITHER;
    		}

    		//We want to send only the new addition to our children,
    		//for consistency in our parent-true and parent-false
    		//counts.
    		//Make sure we don't double-apply a type.

    		Type typeToAdd = Type.NEITHER; // Any new values that we discover we can have this iteration.
    		if (curComp instanceof ExtendedStateProposition) {
    			typeToAdd = newInputType;
    		} else if (curComp instanceof ExtendedStateTransition) {
    			typeToAdd = newInputType;
    		} else if (curComp instanceof ExtendedStateConstant) {
    			typeToAdd = newInputType;
    		} else if (curComp instanceof ExtendedStateNot) {
    			typeToAdd = newInputType.opposite();
    		} else if (curComp instanceof ExtendedStateAnd) {
    			if (newInputType.hasTrue) {
    				numTrueInputs.add(curComp);
    				if (numTrueInputs.count(curComp) == curComp.getInputs().size()) {
    					typeToAdd = Type.TRUE;
    				}
    			}
    			if (newInputType.hasFalse) {
    				typeToAdd = typeToAdd.with(Type.FALSE);
    			}
    		} else if (curComp instanceof ExtendedStateOr) {
    			if (newInputType.hasFalse) {
    				numFalseInputs.add(curComp);
    				if (numFalseInputs.count(curComp) == curComp.getInputs().size()) {
    					typeToAdd = Type.FALSE;
    				}
    			}
    			if (newInputType.hasTrue) {
    				typeToAdd = typeToAdd.with(Type.TRUE);
    			}
    		} else {
    			throw new RuntimeException("Unhandled component type " + curComp.getClass());
    		}

    		if (oldType.includes(typeToAdd)) {
    			//We don't know anything new about curComp
    			continue;
    		}
    		reachability.put(curComp, typeToAdd.with(oldType));
    		typeToAdd = typeToAdd.minus(oldType);
    		if (typeToAdd == Type.NEITHER) {
    			throw new RuntimeException("Something's messed up here");
    		}

    		//Add all our children to the stack
    		for (ExtendedStateComponent output : curComp.getOutputs()) {
    			toAdd.add(Pair.of(output, typeToAdd));
    		}
			if (legalsToInputs.containsKey(curComp)) {
				ExtendedStateProposition inputProp = legalsToInputs.get(curComp);
				if (inputProp == null) {
					throw new IllegalStateException();
				}
				toAdd.add(Pair.of((ExtendedStateComponent) inputProp, typeToAdd));
			}
    	}

    	ExtendedStateConstant trueConst = new ExtendedStateConstant(true);
    	ExtendedStateConstant falseConst = new ExtendedStateConstant(false);
	    pn.addComponent(trueConst);
	    pn.addComponent(falseConst);
	    //Make them the input of all false/true components
	    for(Entry<ExtendedStateComponent, Type> entry : reachability.entrySet()) {
	        Type type = entry.getValue();
	        if(type == Type.TRUE || type == Type.FALSE) {
	            ExtendedStateComponent c = entry.getKey();
	            if (c instanceof ExtendedStateConstant) {
	            	//Don't bother trying to remove this
	            	continue;
	            }
	            //Disconnect from inputs
	            for(ExtendedStateComponent input : c.getInputs()) {
	                input.removeOutput(c);
	            }
	            c.removeAllInputs();
	            if(type == Type.TRUE ^ (c instanceof ExtendedStateNot)) {
	                c.addInput(trueConst);
	                trueConst.addOutput(c);
	            } else {
                    c.addInput(falseConst);
                    falseConst.addOutput(c);
	            }
	        }
	    }

	    optimizeAwayTrueAndFalse(pn, trueConst, falseConst);
	}

    /**
	 * Optimizes an already-existing propnet by removing useless leaves.
	 * These are components that have no outputs, but have no special
	 * meaning in GDL that requires them to stay.
	 *
	 * TODO: Currently fails on propnets with cycles.
	 * @param pn
	 */
	public static void lopUselessLeaves(ExtendedStatePropNet pn) {
		//Approach: Collect useful propositions based on a backwards
		//search from goal/legal/terminal (passing through transitions)
		Set<ExtendedStateComponent> usefulComponents = new HashSet<ExtendedStateComponent>();
		//TODO: Also try with queue?
		Stack<ExtendedStateComponent> toAdd = new Stack<ExtendedStateComponent>();
		toAdd.add(pn.getTerminalProposition());
		usefulComponents.add(pn.getInitProposition()); //Can't remove it...
		for(Set<ExtendedStateProposition> goalProps : pn.getGoalPropositions().values())
			toAdd.addAll(goalProps);
		for(Set<ExtendedStateProposition> legalProps : pn.getLegalPropositions().values())
			toAdd.addAll(legalProps);
		while(!toAdd.isEmpty()) {
			ExtendedStateComponent curComp = toAdd.pop();
			if(usefulComponents.contains(curComp))
				//We've already added it
				continue;
			usefulComponents.add(curComp);
			toAdd.addAll(curComp.getInputs());
		}

		//Remove the components not marked as useful
		List<ExtendedStateComponent> allComponents = new ArrayList<ExtendedStateComponent>(pn.getComponents());
		for(ExtendedStateComponent c : allComponents) {
			if(!usefulComponents.contains(c))
				pn.removeComponent(c);
		}
	}

	/**
	 * Optimizes an already-existing propnet by removing propositions
	 * of the form (init ?x). Does NOT remove the proposition "INIT".
	 * @param pn
	 */
	public static void removeInits(ExtendedStatePropNet pn) {
		List<ExtendedStateProposition> toRemove = new ArrayList<ExtendedStateProposition>();
		for(ExtendedStateProposition p : pn.getPropositions()) {
			if(p.getName() instanceof GdlRelation) {
				GdlRelation relation = (GdlRelation) p.getName();
				if(relation.getName() == INIT) {
					toRemove.add(p);
				}
			}
		}

		for(ExtendedStateProposition p : toRemove) {
			pn.removeComponent(p);
		}
	}

	/**
	 * Potentially optimizes an already-existing propnet by removing propositions
	 * with no special meaning. The inputs and outputs of those propositions
	 * are connected to one another. This is unlikely to improve performance
	 * unless values of every single component are stored (outside the
	 * propnet).
	 *
	 * @param pn
	 */
	public static void removeAnonymousPropositions(ExtendedStatePropNet pn) {
		List<ExtendedStateProposition> toSplice = new ArrayList<ExtendedStateProposition>();
		List<ExtendedStateProposition> toReplaceWithFalse = new ArrayList<ExtendedStateProposition>();
		for(ExtendedStateProposition p : pn.getPropositions()) {
			//If it's important, continue to the next proposition
			if(p.getInputs().size() == 1 && p.getSingleInput() instanceof ExtendedStateTransition)
				//It's a base proposition
				continue;
			GdlSentence sentence = p.getName();
			if(sentence instanceof GdlProposition) {
				if(sentence.getName() == TERMINAL || sentence.getName() == INIT_CAPS)
					continue;
			} else {
				GdlRelation relation = (GdlRelation) sentence;
				GdlConstant name = relation.getName();
				if(name == LEGAL || name == GOAL || name == DOES
						|| name == INIT)
					continue;
			}
			if(p.getInputs().size() < 1) {
				//Needs to be handled separately...
				//because this is an always-false true proposition
				//and it might have and gates as outputs
				toReplaceWithFalse.add(p);
				continue;
			}
			if(p.getInputs().size() != 1)
				System.err.println("Might have falsely declared " + p.getName() + " to be unimportant?");
			//Not important
			//System.out.println("Removing " + p);
			toSplice.add(p);
		}
		for(ExtendedStateProposition p : toSplice) {
			//Get the inputs and outputs...
			Set<ExtendedStateComponent> inputs = p.getInputs();
			Set<ExtendedStateComponent> outputs = p.getOutputs();
			//Remove the proposition...
			pn.removeComponent(p);
			//And splice the inputs and outputs back together
			if(inputs.size() > 1)
				System.err.println("Programmer made a bad assumption here... might lead to trouble?");
			for(ExtendedStateComponent input : inputs) {
				for(ExtendedStateComponent output : outputs) {
					input.addOutput(output);
					output.addInput(input);
				}
			}
		}
		for(ExtendedStateProposition p : toReplaceWithFalse) {
			System.out.println("Should be replacing " + p + " with false, but should do that in the OPNF, really; better equipped to do that there");
		}
	}
}

