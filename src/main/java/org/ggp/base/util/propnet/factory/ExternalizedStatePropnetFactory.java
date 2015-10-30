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
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.externalizedState.ExternalizedStateComponent;
import org.ggp.base.util.propnet.architecture.externalizedState.ExternalizedStatePropNet;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateAnd;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateConstant;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateNot;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateOr;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateProposition;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateProposition.PROP_TYPE;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateTransition;
import org.ggp.base.util.statemachine.Role;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

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
/**
 * This class creates the propnet and provides extra methods to polish it after creation.
 * This class is a refinement/modification of the ExternalizedStatePropNetFactory and creates
 * a propnet with a faster structure. Some of the methods that it provides to polish the propnet
 * after creation are taken (and sometimes improved) from the ExternalizedStatePropNetFactory,
 * while some others have been implemented from scratch.
 *
 * @author C.Sironi
 *
 */
public class ExternalizedStatePropnetFactory {

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
	public static ExternalizedStatePropNet create(List<Gdl> description) throws InterruptedException {
		return create(description, false);
	}

	public static ExternalizedStatePropNet create(List<Gdl> description, boolean verbose) throws InterruptedException {
		System.out.println("Building propnet...");

		long startTime = System.currentTimeMillis();

		description = GdlCleaner.run(description);
		description = DeORer.run(description);
		description = VariableConstrainer.replaceFunctionValuedVariables(description);
		description = Relationizer.run(description);

		description = CondensationIsolator.run(description);


		if(verbose)
			for(Gdl gdl : description)
				System.out.println(gdl);

		//We want to start with a rule graph and follow the rule graph.
		//Start by finding general information about the game
		SentenceDomainModel model = SentenceDomainModelFactory.createWithCartesianDomains(description);
		//Restrict domains to values that could actually come up in rules.
		//See chinesecheckers4's "count" relation for an example of why this
		//could be useful.
		model = SentenceDomainModelOptimizer.restrictDomainsToUsefulValues(model);

		if(verbose)
			System.out.println("Setting constants...");

		ConstantChecker constantChecker = ConstantCheckerFactory.createWithForwardChaining(model);
		if(verbose)
			System.out.println("Done setting constants");

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
		}
		ConcurrencyUtils.checkForInterruption();
		List<SentenceForm> topologicalOrdering = getTopologicalOrdering(model.getSentenceForms(), dependencyGraph, usingBase, usingInput);
		if(verbose)
			System.out.println("done");

		List<Role> roles = Role.computeRoles(description);
		Map<GdlSentence, ExternalizedStateComponent> components = new HashMap<GdlSentence, ExternalizedStateComponent>();
		Map<GdlSentence, ExternalizedStateComponent> negations = new HashMap<GdlSentence, ExternalizedStateComponent>();
		ExternalizedStateConstant trueComponent = new ExternalizedStateConstant(true);
		ExternalizedStateConstant falseComponent = new ExternalizedStateConstant(false);
		Map<SentenceForm, FunctionInfo> functionInfoMap = new HashMap<SentenceForm, FunctionInfo>();
		Map<SentenceForm, Collection<GdlSentence>> completedSentenceFormValues = new HashMap<SentenceForm, Collection<GdlSentence>>();
		for(SentenceForm form : topologicalOrdering) {
			ConcurrencyUtils.checkForInterruption();

			if(verbose) {
				System.out.print("Adding sentence form " + form);
				System.out.flush();
			}
			if(constantChecker.isConstantForm(form)) {
				if(verbose)
					System.out.println(" (constant)");
				//Only add it if it's important
				if(form.getName().equals(LEGAL)
						|| form.getName().equals(GOAL)
						|| form.getName().equals(INIT)
						|| form.getName().equals(NEXT)
						|| form.getName().equals(TERMINAL)) {
					//Add it
					for (GdlSentence trueSentence : constantChecker.getTrueSentences(form)) {
						ExternalizedStateProposition trueProp = new ExternalizedStateProposition(trueSentence);
						trueProp.addInput(trueComponent);
						trueComponent.addOutput(trueProp);
						components.put(trueSentence, trueComponent);
					}
				}

				if(verbose)
					System.out.println("Checking whether " + form + " is a functional constant...");
				addConstantsToFunctionInfo(form, constantChecker, functionInfoMap);
				addFormToCompletedValues(form, completedSentenceFormValues, constantChecker);

				continue;
			}
			if(verbose)
				System.out.println();
			//TODO: Adjust "recursive forms" appropriately
			//Add a temporary sentence form thingy? ...
			Map<GdlSentence, ExternalizedStateComponent> temporaryComponents = new HashMap<GdlSentence, ExternalizedStateComponent>();
			Map<GdlSentence, ExternalizedStateComponent> temporaryNegations = new HashMap<GdlSentence, ExternalizedStateComponent>();
			addSentenceForm(form, model, components, negations, trueComponent, falseComponent, usingBase, usingInput, Collections.singleton(form), temporaryComponents, temporaryNegations, functionInfoMap, constantChecker, completedSentenceFormValues);
			//TODO: Pass these over groups of multiple sentence forms
			if(verbose && !temporaryComponents.isEmpty())
				System.out.println("Processing temporary components...");
			processTemporaryComponents(temporaryComponents, temporaryNegations, components, negations, trueComponent, falseComponent);
			addFormToCompletedValues(form, completedSentenceFormValues, components);
			//if(verbose)
				//TODO: Add this, but with the correct total number of components (not just Propositions)
				//System.out.println("  "+completedSentenceFormValues.get(form).size() + " components added");
		}
		//Connect "next" to "true"
		if(verbose)
			System.out.println("Adding transitions...");
		addTransitions(components);
		//Set up "init" proposition
		if(verbose)
			System.out.println("Setting up 'init' proposition...");
		setUpInit(components, trueComponent, falseComponent);
		//Now we can safely...
		removeUselessBasePropositions(components, negations, trueComponent, falseComponent);
		if(verbose)
			System.out.println("Creating component set...");
		Set<ExternalizedStateComponent> componentSet = new HashSet<ExternalizedStateComponent>(components.values());
		//Try saving some memory here...
		components = null;
		negations = null;
		completeComponentSet(componentSet);
		ConcurrencyUtils.checkForInterruption();
		if(verbose)
			System.out.println("Initializing propnet object...");
		//Make it look the same as the PropNetFactory results, until we decide
		//how we want it to look
		normalizePropositions(componentSet);
		ExternalizedStatePropNet propnet = new ExternalizedStatePropNet(roles, componentSet, trueComponent, falseComponent);
		if(verbose) {
			System.out.println("Done setting up propnet; took " + (System.currentTimeMillis() - startTime) + "ms, has " + componentSet.size() + " components and " + propnet.getNumLinks() + " links");
			System.out.println("Propnet has " +propnet.getNumAnds()+" ands; "+propnet.getNumOrs()+" ors; "+propnet.getNumNots()+" nots");
			System.out.println("Propnet has " +propnet.getNumBases() + " bases; "+propnet.getNumTransitions()+" transitions; "+propnet.getNumInputs()+" inputs");
		}
		//System.out.println(propnet);
		return propnet;
	}


	private static void removeUselessBasePropositions(
			Map<GdlSentence, ExternalizedStateComponent> components, Map<GdlSentence, ExternalizedStateComponent> negations, ExternalizedStateConstant trueComponent,
			ExternalizedStateConstant falseComponent) throws InterruptedException {
		boolean changedSomething = false;
		for(Entry<GdlSentence, ExternalizedStateComponent> entry : components.entrySet()) {
			if(entry.getKey().getName() == TRUE) {
				ExternalizedStateComponent comp = entry.getValue();
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
	private static void normalizePropositions(Set<ExternalizedStateComponent> componentSet) {
		for(ExternalizedStateComponent component : componentSet) {
			if(component instanceof ExternalizedStateProposition) {
				ExternalizedStateProposition p = (ExternalizedStateProposition) component;
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
			Map<GdlSentence, ExternalizedStateComponent> components) throws InterruptedException {
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
			Map<GdlSentence, ExternalizedStateComponent> temporaryComponents,
			Map<GdlSentence, ExternalizedStateComponent> temporaryNegations,
			Map<GdlSentence, ExternalizedStateComponent> components,
			Map<GdlSentence, ExternalizedStateComponent> negations, ExternalizedStateComponent trueComponent,
			ExternalizedStateComponent falseComponent) throws InterruptedException {
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
			ExternalizedStateComponent tempComp = temporaryComponents.get(sentence);
			ExternalizedStateComponent realComp = components.get(sentence);
			if(realComp == null) {
				realComp = falseComponent;
			}
			for(ExternalizedStateComponent output : tempComp.getOutputs()) {
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
	 * Doesn't actually work that way... shoot. Need something that will remove the
	 * component from the propnet entirely.
	 * @throws InterruptedException
	 */
	private static void optimizeAwayTrueAndFalse(Map<GdlSentence, ExternalizedStateComponent> components, Map<GdlSentence, ExternalizedStateComponent> negations, ExternalizedStateComponent trueComponent, ExternalizedStateComponent falseComponent) throws InterruptedException {
	    while(hasNonessentialChildren(trueComponent) || hasNonessentialChildren(falseComponent)) {
	    	ConcurrencyUtils.checkForInterruption();
            optimizeAwayTrue(components, negations, null, trueComponent, falseComponent);
            optimizeAwayFalse(components, negations, null, trueComponent, falseComponent);
        }
	}

	//TODO: Create a version with just a set of components that we can share with post-optimizations
	private static void optimizeAwayFalse(Map<GdlSentence, ExternalizedStateComponent> components,
			Map<GdlSentence, ExternalizedStateComponent> negations, ExternalizedStatePropNet pn,
			ExternalizedStateComponent trueComponent, ExternalizedStateComponent falseComponent) {
        assert((components != null && negations != null) || pn != null);
        assert((components == null && negations == null) || pn == null);
        for (ExternalizedStateComponent output : Lists.newArrayList(falseComponent.getOutputs())) {
        	if (isEssentialProposition(output) || output instanceof ExternalizedStateTransition) {
        		//Since this is the false constant, there are a few "essential" types
        		//we don't actually want to keep around.
        		if (!isLegalOrGoalProposition(output)) {
        			continue;
        		}
	    	}
			if(output instanceof ExternalizedStateProposition) {
				//Move its outputs to be outputs of false
				for(ExternalizedStateComponent child : output.getOutputs()) {
					//Disconnect
					child.removeInput(output);
					//output.removeOutput(child); //do at end
					//Reconnect; will get children before returning, if nonessential
					falseComponent.addOutput(child);
					child.addInput(falseComponent);
				}
				output.removeAllOutputs();

				if(!isEssentialProposition(output)) {
					ExternalizedStateProposition prop = (ExternalizedStateProposition) output;
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
			} else if(output instanceof ExternalizedStateAnd) {
				ExternalizedStateAnd and = (ExternalizedStateAnd) output;
				//Attach children of and to falseComponent
				for(ExternalizedStateComponent child : and.getOutputs()) {
					child.addInput(falseComponent);
					falseComponent.addOutput(child);
					child.removeInput(and);
				}
				//Disconnect and completely
				and.removeAllOutputs();
				for(ExternalizedStateComponent parent : and.getInputs())
					parent.removeOutput(and);
				and.removeAllInputs();
				if(pn != null)
				    pn.removeComponent(and);
			} else if(output instanceof ExternalizedStateOr) {
				ExternalizedStateOr or = (ExternalizedStateOr) output;
				//Remove as input from or
				or.removeInput(falseComponent);
				falseComponent.removeOutput(or);
				//If or has only one input, remove it
				if(or.getInputs().size() == 1) {
					ExternalizedStateComponent in = or.getSingleInput();
					or.removeInput(in);
					in.removeOutput(or);
					for(ExternalizedStateComponent out : or.getOutputs()) {
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
			} else if(output instanceof ExternalizedStateNot) {
				ExternalizedStateNot not = (ExternalizedStateNot) output;
				//Disconnect from falseComponent
				not.removeInput(falseComponent);
				falseComponent.removeOutput(not);
				//Connect all children of the not to trueComponent
				for(ExternalizedStateComponent child : not.getOutputs()) {
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
			} else if(output instanceof ExternalizedStateTransition) {
				//???
				System.err.println("Fix optimizeAwayFalse's case for Transitions");
			}
		}
	}



	private static boolean isLegalOrGoalProposition(ExternalizedStateComponent comp) {
		if (!(comp instanceof ExternalizedStateProposition)) {
			return false;
		}

		ExternalizedStateProposition prop = (ExternalizedStateProposition) comp;
		GdlSentence name = prop.getName();
		return name.getName() == GdlPool.LEGAL || name.getName() == GdlPool.GOAL;
	}




	private static void optimizeAwayTrue(
			Map<GdlSentence, ExternalizedStateComponent> components, Map<GdlSentence, ExternalizedStateComponent> negations, ExternalizedStatePropNet pn, ExternalizedStateComponent trueComponent,
			ExternalizedStateComponent falseComponent) {
	    assert((components != null && negations != null) || pn != null);
	    for (ExternalizedStateComponent output : Lists.newArrayList(trueComponent.getOutputs())) {
	    	if (isEssentialProposition(output) || output instanceof ExternalizedStateTransition) {
	    		continue;
	    	}
			if(output instanceof ExternalizedStateProposition) {
				//Move its outputs to be outputs of true
				for(ExternalizedStateComponent child : output.getOutputs()) {
					//Disconnect
					child.removeInput(output);
					//output.removeOutput(child); //do at end
					//Reconnect; will get children before returning, if nonessential
					trueComponent.addOutput(child);
					child.addInput(trueComponent);
				}
				output.removeAllOutputs();

				if(!isEssentialProposition(output)) {
					ExternalizedStateProposition prop = (ExternalizedStateProposition) output;
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
			} else if(output instanceof ExternalizedStateOr) {
				ExternalizedStateOr or = (ExternalizedStateOr) output;
				//Attach children of or to trueComponent
				for(ExternalizedStateComponent child : or.getOutputs()) {
					child.addInput(trueComponent);
					trueComponent.addOutput(child);
					child.removeInput(or);
				}
				//Disconnect or completely
				or.removeAllOutputs();
				for(ExternalizedStateComponent parent : or.getInputs())
					parent.removeOutput(or);
				or.removeAllInputs();
				if(pn != null)
				    pn.removeComponent(or);
			} else if(output instanceof ExternalizedStateAnd) {
				ExternalizedStateAnd and = (ExternalizedStateAnd) output;
				//Remove as input from and
				and.removeInput(trueComponent);
				trueComponent.removeOutput(and);
				//If and has only one input, remove it
				if(and.getInputs().size() == 1) {
					ExternalizedStateComponent in = and.getSingleInput();
					and.removeInput(in);
					in.removeOutput(and);
					for(ExternalizedStateComponent out : and.getOutputs()) {
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
			} else if(output instanceof ExternalizedStateNot) {
				ExternalizedStateNot not = (ExternalizedStateNot) output;
				//Disconnect from trueComponent
				not.removeInput(trueComponent);
				trueComponent.removeOutput(not);
				//Connect all children of the not to falseComponent
				for(ExternalizedStateComponent child : not.getOutputs()) {
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
			} else if(output instanceof ExternalizedStateTransition) {
				//???
				System.err.println("Fix optimizeAwayTrue's case for Transitions");
			}
		}
	}


	private static boolean hasNonessentialChildren(ExternalizedStateComponent trueComponent) {
		for(ExternalizedStateComponent child : trueComponent.getOutputs()) {
			if(child instanceof ExternalizedStateTransition)
				continue;
			if(!isEssentialProposition(child))
				return true;
			//We don't want any grandchildren, either
			if(!child.getOutputs().isEmpty())
				return true;
		}
		return false;
	}

	private static boolean isEssentialProposition(ExternalizedStateComponent component) {
		if(!(component instanceof ExternalizedStateProposition))
			return false;

		//We're looking for things that would be outputs of "true" or "false",
		//but we would still want to keep as propositions to be read by the
		//state machine
		ExternalizedStateProposition prop = (ExternalizedStateProposition) component;
		GdlConstant name = prop.getName().getName();

		return name.equals(LEGAL) /*|| name.equals(NEXT)*/ || name.equals(GOAL)
				|| name.equals(INIT) || name.equals(TERMINAL);
	}


	private static void completeComponentSet(Set<ExternalizedStateComponent> componentSet) {
		Set<ExternalizedStateComponent> newComponents = new HashSet<ExternalizedStateComponent>();
		Set<ExternalizedStateComponent> componentsToTry = new HashSet<ExternalizedStateComponent>(componentSet);
		while(!componentsToTry.isEmpty()) {
			for(ExternalizedStateComponent c : componentsToTry) {
				for(ExternalizedStateComponent out : c.getOutputs()) {
					if(!componentSet.contains(out))
						newComponents.add(out);
				}
				for(ExternalizedStateComponent in : c.getInputs()) {
					if(!componentSet.contains(in))
						newComponents.add(in);
				}
			}
			componentSet.addAll(newComponents);
			componentsToTry = newComponents;
			newComponents = new HashSet<ExternalizedStateComponent>();
		}
	}

	private static void addTransitions(Map<GdlSentence, ExternalizedStateComponent> components) {
		for(Entry<GdlSentence, ExternalizedStateComponent> entry : components.entrySet()) {
			GdlSentence sentence = entry.getKey();

			if(sentence.getName().equals(NEXT)) {
				//connect to true
				GdlSentence trueSentence = GdlPool.getRelation(TRUE, sentence.getBody());
				ExternalizedStateComponent nextComponent = entry.getValue();
				ExternalizedStateComponent trueComponent = components.get(trueSentence);
				//There might be no true component (for example, because the bases
				//told us so). If that's the case, don't have a transition.
				if(trueComponent == null) {
				    // Skipping transition to supposedly impossible 'trueSentence'
				    continue;
				}
				ExternalizedStateTransition transition = new ExternalizedStateTransition();
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
	private static void setUpInit(Map<GdlSentence, ExternalizedStateComponent> components,
			ExternalizedStateConstant trueComponent, ExternalizedStateConstant falseComponent) {
		ExternalizedStateProposition initProposition = new ExternalizedStateProposition(GdlPool.getProposition(INIT_CAPS));
		for(Entry<GdlSentence, ExternalizedStateComponent> entry : components.entrySet()) {
			//Is this something that will be true?
			if(entry.getValue() == trueComponent) {
				if(entry.getKey().getName().equals(INIT)) {
					//Find the corresponding true sentence
					GdlSentence trueSentence = GdlPool.getRelation(TRUE, entry.getKey().getBody());
					//System.out.println("True sentence from init: " + trueSentence);
					ExternalizedStateComponent trueSentenceComponent = components.get(trueSentence);
					if(trueSentenceComponent.getInputs().isEmpty()) {
						//Case where there is no transition input
						//Add the transition input, connect to init, continue loop

						// @author c.sironi: Also set to TRUE that fact that the value of this transition
						// depends on the INIT proposition value.
						ExternalizedStateTransition transition = new ExternalizedStateTransition(true);
						//init goes into transition
						transition.addInput(initProposition);
						initProposition.addOutput(transition);
						//transition goes into component
						trueSentenceComponent.addInput(transition);
						transition.addOutput(trueSentenceComponent);
					} else {
						//The transition already exists
						ExternalizedStateComponent transition = trueSentenceComponent.getSingleInput();

						//We want to add init as a thing that precedes the transition
						//Disconnect existing input
						ExternalizedStateComponent input = transition.getSingleInput();
						//input and init go into or, or goes into transition
						input.removeOutput(transition);
						transition.removeInput(input);
						List<ExternalizedStateComponent> orInputs = new ArrayList<ExternalizedStateComponent>(2);
						orInputs.add(input);
						orInputs.add(initProposition);
						orify(orInputs, transition, falseComponent);
						// @author c.sironi: Also set to TRUE that fact that the value of this transition
						// depends on the INIT proposition value.
						((ExternalizedStateTransition) transition).setDependingOnInit(true);
					}
				}
			}
		}
	}





	/**
	 * Adds an or gate connecting the inputs to produce the output.
	 * Handles special optimization cases like a true/false input.
	 */
	private static void orify(Collection<ExternalizedStateComponent> inputs, ExternalizedStateComponent output, ExternalizedStateConstant falseProp) {
		//TODO: Look for already-existing ors with the same inputs?
		//Or can this be handled with a GDL transformation?

		//Special case: An input is the true constant
		for(ExternalizedStateComponent in : inputs) {
			if(in instanceof ExternalizedStateConstant && ((ExternalizedStateConstant) in).getValue()) {
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
		ExternalizedStateOr or = new ExternalizedStateOr();
		for(ExternalizedStateComponent in : inputs) {
			if(!(in instanceof ExternalizedStateConstant)) {
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
			ExternalizedStateComponent in = or.getSingleInput();
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
			Map<GdlSentence, ExternalizedStateComponent> components,
			Map<GdlSentence, ExternalizedStateComponent> negations,
			ExternalizedStateConstant trueComponent, ExternalizedStateConstant falseComponent,
			boolean usingBase, boolean usingInput,
			Set<SentenceForm> recursionForms,
			Map<GdlSentence, ExternalizedStateComponent> temporaryComponents, Map<GdlSentence, ExternalizedStateComponent> temporaryNegations,
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
				ExternalizedStateProposition prop = new ExternalizedStateProposition(alwaysTrueSentence);
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
				ExternalizedStateProposition prop = new ExternalizedStateProposition(doesSentence);
				components.put(doesSentence, prop);
			}
			return;
		}
		if(usingBase && form.getName().equals(TRUE)) {
			SentenceForm baseForm = form.withName(BASE);
			for (GdlSentence baseSentence : constantChecker.getTrueSentences(baseForm)) {
				GdlSentence trueSentence = GdlPool.getRelation(TRUE, baseSentence.getBody());
				ExternalizedStateProposition prop = new ExternalizedStateProposition(trueSentence);
				components.put(trueSentence, prop);
			}
			return;
		}

		Map<GdlSentence, Set<ExternalizedStateComponent>> inputsToOr = new HashMap<GdlSentence, Set<ExternalizedStateComponent>>();
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
				List<ExternalizedStateComponent> componentsToConnect = new ArrayList<ExternalizedStateComponent>(rule.arity());
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

						ExternalizedStateComponent conj = components.get(transformed);
						//If conj is null and this is a sentence form we're still handling,
						//hook up to a temporary sentence form
						if(conj == null) {
							conj = temporaryComponents.get(transformed);
						}
						if(conj == null && SentenceModelUtils.inSentenceFormGroup(transformed, recursionForms)) {
							//Set up a temporary component
							ExternalizedStateProposition tempProp = new ExternalizedStateProposition(transformed);
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

						ExternalizedStateComponent conj = negations.get(transformed);
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
							ExternalizedStateComponent positive = components.get(transformed);
							if(positive == null) {
								positive = temporaryComponents.get(transformed);
							}
							if(positive == null) {
								//Make the temporary proposition
								ExternalizedStateProposition tempProp = new ExternalizedStateProposition(transformed);
								temporaryComponents.put(transformed, tempProp);
								positive = tempProp;
							}
							//Positive is now set and in temporaryComponents
							//Evidently, wasn't in temporaryNegations
							//So we add the "not" gate and set it in temporaryNegations
							ExternalizedStateNot not = new ExternalizedStateNot();
							//Add positive as input
							not.addInput(positive);
							positive.addOutput(not);
							temporaryNegations.put(transformed, not);
							conj = not;
						}
						if(conj == null) {
							ExternalizedStateComponent positive = components.get(transformed);
							//No, because then that will be attached to "negations", which could be bad

							if(positive == null) {
								//So the positive can't possibly be true (unless we have recurstion)
								//and so this would be positive always
								//We want to just skip this conjunct, so we continue to the next

								continue; //to the next conjunct
							}

							//Check if we're sharing a component with another sentence with a negation
							//(i.e. look for "nots" in our outputs and use those instead)
							ExternalizedStateNot existingNotOutput = getNotOutput(positive);
							if(existingNotOutput != null) {
								componentsToConnect.add(existingNotOutput);
								negations.put(transformed, existingNotOutput);
								continue; //to the next conjunct
							}

							ExternalizedStateNot not = new ExternalizedStateNot();
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
					ExternalizedStateProposition andComponent = new ExternalizedStateProposition(TEMP);

					andify(componentsToConnect, andComponent, trueComponent);
					if(!isThisConstant(andComponent, falseComponent)) {
						if(!inputsToOr.containsKey(sentence))
							inputsToOr.put(sentence, new HashSet<ExternalizedStateComponent>());
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
		for(Entry<GdlSentence, Set<ExternalizedStateComponent>> entry : inputsToOr.entrySet()) {
			ConcurrencyUtils.checkForInterruption();

			GdlSentence sentence = entry.getKey();
			Set<ExternalizedStateComponent> inputs = entry.getValue();
			Set<ExternalizedStateComponent> realInputs = new HashSet<ExternalizedStateComponent>();
			for(ExternalizedStateComponent input : inputs) {
				if(input instanceof ExternalizedStateConstant || input.getInputs().size() == 0) {
					realInputs.add(input);
				} else {
					realInputs.add(input.getSingleInput());
					input.getSingleInput().removeOutput(input);
					input.removeAllInputs();
				}
			}

			ExternalizedStateProposition prop = new ExternalizedStateProposition(sentence);
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

				ExternalizedStateProposition prop = new ExternalizedStateProposition(sentence);
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

	private static boolean isThisConstant(ExternalizedStateComponent conj, ExternalizedStateConstant constantComponent) {
		if(conj == constantComponent)
			return true;
		return (conj instanceof ExternalizedStateProposition && conj.getInputs().size() == 1 && conj.getSingleInput() == constantComponent);
	}


	private static ExternalizedStateNot getNotOutput(ExternalizedStateComponent positive) {
		for(ExternalizedStateComponent c : positive.getOutputs()) {
			if(c instanceof ExternalizedStateNot) {
				return (ExternalizedStateNot) c;
			}
		}
		return null;
	}


	private static List<GdlVariable> getVarsInConjunct(GdlLiteral literal) {
		return GdlUtils.getVariables(literal);
	}



	private static void andify(List<ExternalizedStateComponent> inputs, ExternalizedStateComponent output, ExternalizedStateConstant trueProp) {
		//Special case: If the inputs include false, connect false to thisComponent
		for(ExternalizedStateComponent c : inputs) {
			if(c instanceof ExternalizedStateConstant && !((ExternalizedStateConstant)c).getValue()) {
				//Connect false (c) to the output
				output.addInput(c);
				c.addOutput(output);
				return;
			}
		}

		//For reals... just skip over any true constants
		ExternalizedStateAnd and = new ExternalizedStateAnd();
		for(ExternalizedStateComponent in : inputs) {
			if(!(in instanceof ExternalizedStateConstant)) {
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
			ExternalizedStateComponent in = and.getSingleInput();
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
	 * This method checks every component in the propnet looking for the ones that have no input.
	 * Except input propositions and constants, all components in the propnet are supposed to have
	 * at least an input. If one that has no inputs is detected it is connected to a constant
	 * (either TRUE or FALSE).
	 *
	 * Note that not for every input-less component we know what to do. The behavior of the propnet
	 * has been tested to be consistent when we connect the input-less components as follows:
	 *
	 * - OR: connect to FALSE.
	 * - PROPOSITION (other than INPUT): connect to FALSE (Note that this includes also the INIT
	 * proposition. Since we don't ever use it in the propnet, it's ok to always set it to false).
	 *
	 * For other components we are not exactly sure. The following is an hypothesis, but has not been
	 * tested, since in the test repository there are no games that lead to a propnet with these types
	 * of input-less components:
	 *
	 * - AND: connect to FALSE.
	 * - NOT: connect to TRUE.
	 * - TRANSITION:  connect to FALSE.
	 *
	 * For components for which we are not sure, if there is one of them with no inputs we throw an exception
	 *
	 * IMPORTANT NOTE: as it is, the propnet works correctly on all games in the repository even if this method
	 * is never run. This is because the propnet for such games is meant to work when all the input-less components
	 * are set to false (and we have the exetrnal propnet state that initializes such components' default truth
	 * value to false).
	 * However is not impossible that there will be a game that will have input-less components that will require
	 * a different initialization value. Whenever this will happen there are two ways to fix the problem:
	 * 1. Set here the correct input constant for each input-less component.
	 * 2. Change the default initialization value in the external propnet state (having no inputs, each input-less
	 * component will then keep the same truth value for the whole game).
	 *
	 * This said, it must be noticed that, to correctly run the removeUnreachableBasesAndInputs method, this method
	 * is required to be run first!	 *
	 *
	 * @param pn the propNet to fix.
	 * @param trueConstant
	 * @param falseConstant
	 */
	public static void fixInputlessComponents(ExternalizedStatePropNet pn, ExternalizedStateConstant trueConstant, ExternalizedStateConstant falseConstant){

		assert(trueConstant != null && falseConstant != null);

		for(ExternalizedStateComponent c : pn.getComponents()){
			if(c.getInputs().size() == 0){
				if(c instanceof ExternalizedStateProposition){
					if(((ExternalizedStateProposition) c).getPropositionType() != PROP_TYPE.INPUT){
						c.addInput(falseConstant);
						falseConstant.addOutput(c);
					}
				}else if(c instanceof ExternalizedStateOr){
					c.addInput(falseConstant);
					falseConstant.addOutput(c);
				}else if(c instanceof ExternalizedStateAnd){
					throw new RuntimeException("Unhandled input-less component type: NOT");
				}else if(c instanceof ExternalizedStateTransition){
					throw new RuntimeException("Unhandled input-less component type: NOT");
				}else if(c instanceof ExternalizedStateNot){
					throw new RuntimeException("Unhandled input-less component type: NOT");
				}
			}
		}

		// TODO
		// Now we can remove the (unnecessary) components that are always true or false.
		//optimizeAwayTrueAndFalse()

	}

	////////FIX
	private static void optimizeAwayTrueAndFalse2(ExternalizedStatePropNet pn, ExternalizedStateComponent trueComponent, ExternalizedStateComponent falseComponent) {

		Set<ExternalizedStateComponent> toCheckTrue = new HashSet<ExternalizedStateComponent>(trueComponent.getOutputs());
		Set<ExternalizedStateComponent> toCheckFalse = new HashSet<ExternalizedStateComponent>(falseComponent.getOutputs());

		while(!(toCheckTrue.isEmpty() && toCheckFalse.isEmpty())){

		}

		while(hasNonessentialChildren(trueComponent) || hasNonessentialChildren(falseComponent)) {
	        optimizeAwayTrue(null, null, pn, trueComponent, falseComponent);
	        optimizeAwayFalse(null, null, pn, trueComponent, falseComponent);
	    }
	}

	/*
	//TODO: Create a version with just a set of components that we can share with post-optimizations
	private static void optimizeAwayFalse2(ExternalizedStatePropNet pn, ExternalizedStateComponent trueComponent, ExternalizedStateComponent falseComponent) {

        for (ExternalizedStateComponent output : Lists.newArrayList(falseComponent.getOutputs())) {
        	if (isEssentialProposition(output) || output instanceof ExternalizedStateTransition) {
        		//Since this is the false constant, there are a few "essential" types
        		//we don't actually want to keep around.
        		if (!isLegalOrGoalProposition(output)) {
        			continue;
        		}
	    	}
			if(output instanceof ExternalizedStateProposition) {
				//Move its outputs to be outputs of false
				for(ExternalizedStateComponent child : output.getOutputs()) {
					//Disconnect
					child.removeInput(output);
					//output.removeOutput(child); //do at end
					//Reconnect; will get children before returning, if nonessential
					falseComponent.addOutput(child);
					child.addInput(falseComponent);
				}
				output.removeAllOutputs();

				if(!isEssentialProposition(output)) {
					ExternalizedStateProposition prop = (ExternalizedStateProposition) output;
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
			} else if(output instanceof ExternalizedStateAnd) {
				ExternalizedStateAnd and = (ExternalizedStateAnd) output;
				//Attach children of and to falseComponent
				for(ExternalizedStateComponent child : and.getOutputs()) {
					child.addInput(falseComponent);
					falseComponent.addOutput(child);
					child.removeInput(and);
				}
				//Disconnect and completely
				and.removeAllOutputs();
				for(ExternalizedStateComponent parent : and.getInputs())
					parent.removeOutput(and);
				and.removeAllInputs();
				if(pn != null)
				    pn.removeComponent(and);
			} else if(output instanceof ExternalizedStateOr) {
				ExternalizedStateOr or = (ExternalizedStateOr) output;
				//Remove as input from or
				or.removeInput(falseComponent);
				falseComponent.removeOutput(or);
				//If or has only one input, remove it
				if(or.getInputs().size() == 1) {
					ExternalizedStateComponent in = or.getSingleInput();
					or.removeInput(in);
					in.removeOutput(or);
					for(ExternalizedStateComponent out : or.getOutputs()) {
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
			} else if(output instanceof ExternalizedStateNot) {
				ExternalizedStateNot not = (ExternalizedStateNot) output;
				//Disconnect from falseComponent
				not.removeInput(falseComponent);
				falseComponent.removeOutput(not);
				//Connect all children of the not to trueComponent
				for(ExternalizedStateComponent child : not.getOutputs()) {
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
			} else if(output instanceof ExternalizedStateTransition) {
				//???
				System.err.println("Fix optimizeAwayFalse's case for Transitions");
			}
		}
	}*/























	public static boolean checkPropnetStructure(ExternalizedStatePropNet pn){

		boolean propnetOk = true;

		Map<GdlSentence, Integer> propNumbers = new HashMap<GdlSentence, Integer>();

		for(ExternalizedStateComponent c : pn.getComponents()){

			/* NOT FEASIBLE TO CHECK THIS IN A REASONABLE AMOUNT OF TIME FOR MOST GAMES:

			// Check that every input of the component references back the component as output
			for(ForwardInterruptingComponent in : c.getInputs()){
				boolean correctInputReferences = false;
				for(ForwardInterruptingComponent inout : in.getOutputs()){
					if(inout == c){
						correctInputReferences = true;
						break;
					}
				}
				if(!correctInputReferences){
					GamerLogger.log("PropStructureChecker", "Component " + c.getType() + " is not referenced back by its input " + in.getType() + ".");
					propnetOk = false;
				}
			}

			// Check that every output of the component references back the component as input
			for(ForwardInterruptingComponent out : c.getOutputs()){
				boolean correctOutputReferences = false;
				for(ForwardInterruptingComponent outin : out.getInputs()){
					if(outin == c){
						correctOutputReferences = true;
						break;
					}
				}
				if(!correctOutputReferences){
					GamerLogger.log("PropStructureChecker", "Component " + c.getType() + " is not referenced back by its output " + out.getType() + ".");
					propnetOk = false;
				}
			}
			*/

			// Check for each type of component if it has the correct inputs and outputs
			if(c instanceof ExternalizedStateProposition){

				ExternalizedStateProposition p = (ExternalizedStateProposition) c;

				Integer count;
				switch(p.getPropositionType()){
				case OTHER:
					break;
				default:
					count = propNumbers.get(p.getName());
					if(count == null){
						propNumbers.put(p.getName(), new Integer(1));
					}else{
						propNumbers.put(p.getName(), new Integer(count.intValue() + 1));
					}
					break;
				}

				switch(p.getPropositionType()){
				case BASE:
					if(p.getInputs().size() != 1){
						GamerLogger.log("PropStructureChecker", "Component " + p.getComponentType() + " has wrong number of inputs: " + p.getInputs().size());
						propnetOk = false;
					}else if(!(p.getSingleInput() instanceof ExternalizedStateTransition)){
						GamerLogger.log("PropStructureChecker", "Component " + p.getComponentType() + " has no TRANSITION as input but " + p.getSingleInput().getClass().getName());
						propnetOk = false;
					}
					break;
				case INPUT:
					if(p.getInputs().size() != 0){
						GamerLogger.log("PropStructureChecker", "Component " + p.getComponentType() + " has wrong number of inputs: " + p.getInputs().size());
						propnetOk = false;
					}
					break;
				case LEGAL:
					if(p.getInputs().size() != 1){
						GamerLogger.log("PropStructureChecker", "Component " + p.getComponentType() + " has wrong number of inputs: " + p.getInputs().size());
						propnetOk = false;
					}
					break;
				case GOAL:
					if(p.getInputs().size() != 1){
						GamerLogger.log("PropStructureChecker", "Component " + p.getComponentType() + " has wrong number of inputs: " + p.getInputs().size());
						propnetOk = false;
					}
					if(p.getOutputs().size() != 0){
						GamerLogger.log("PropStructureChecker", "Component " + p.getComponentType() + " has wrong number of outputs: " + p.getOutputs().size());
						propnetOk = false;
					}
					break;
				case TERMINAL:
					if(p.getInputs().size() != 1){
						GamerLogger.log("PropStructureChecker", "Component " + p.getComponentType() + " has wrong number of inputs: " + p.getInputs().size());
						propnetOk = false;
					}
					if(p.getOutputs().size() != 0){
						GamerLogger.log("PropStructureChecker", "Component " + p.getComponentType() + " has wrong number of outputs: " + p.getOutputs().size());
						propnetOk = false;
					}
					break;
				case INIT:
					if(p.getInputs().size() != 0){
						GamerLogger.log("PropStructureChecker", "Component " + p.getComponentType() + " has wrong number of inputs: " + p.getInputs().size());
						propnetOk = false;
					}
					break;
				case OTHER:
					if(p.getInputs().size() != 1){
						GamerLogger.log("PropStructureChecker", "Component " + p.getComponentType() + " has wrong number of inputs: " + p.getInputs().size());
						propnetOk = false;
					}
					break;
				default:
					GamerLogger.log("PropStructureChecker", "Component " + p.getComponentType() + " has no PROP_TYPE assigned.");
					propnetOk = false;
					break;
				}
			}else if(c instanceof ExternalizedStateTransition){

				if(c.getInputs().size() != 1){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " doesn't have one and only one input. It has " + c.getInputs().size() + " inputs.");
					propnetOk = false;
				}

				if(c.getOutputs().size() != 1){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " doesn't have one and only one output. It has " + c.getOutputs().size() + " outputs.");
					propnetOk = false;
				}else if(!(c.getSingleOutput() instanceof ExternalizedStateProposition)){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " doesn't have a proposition as output. It has " + c.getSingleOutput().getComponentType() + " as output.");
					propnetOk = false;
				}else if(((ExternalizedStateProposition) c.getSingleOutput()).getPropositionType() != PROP_TYPE.BASE){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " doesn't have a base proposition as output. It has " + c.getSingleOutput().getComponentType() + " as output.");
					propnetOk = false;
				}

			}else if(c instanceof ExternalizedStateConstant){

				if(c.getInputs().size() != 0){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " has " + c.getInputs().size() + " inputs.");
					propnetOk = false;
				}

			}else if(c instanceof ExternalizedStateAnd){

				if(c.getInputs().size() == 1){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " is unnecessary since it only has one input: " + c.getSingleInput().getComponentType() + ".");
					propnetOk = false;
				}else if(c.getInputs().size() == 0){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " has no inputs: should be connected to a constant proposition.");
					propnetOk = false;
				}

				if(c.getOutputs().size() == 0){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " is unnecessary since it has no outputs!");
					propnetOk = false;
				}

			}else if(c instanceof ExternalizedStateOr){

				if(c.getInputs().size() == 1){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " is unnecessary since it only has one input: " + c.getSingleInput().getComponentType() + ".");
					propnetOk = false;
				}else if(c.getInputs().size() == 0){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " has no inputs: should be connected to a constant proposition.");
					propnetOk = false;

					/*
					String s = "[ ";
					for(ExternalizedStateComponent cc : c.getOutputs()){
						s += cc.getType();
						s += " ";
					}
					s += "]";
					GamerLogger.log("PropStructureChecker", "Children: " + s);
					*/
				}

				if(c.getOutputs().size() == 0){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " is unnecessary since it has no outputs!");
					propnetOk = false;
				}

			}else if(c instanceof ExternalizedStateNot){

				if(c.getInputs().size() > 1){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " has too many inputs: " + c.getInputs().size());
					propnetOk = false;
				}else if(c.getInputs().size() == 0){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " has no inputs: should be connected to a constant proposition!");
					propnetOk = false;
				}
			}
		}

		for(Entry<GdlSentence, Integer> e : propNumbers.entrySet()){
			if(e.getValue().intValue() != 1 ){
				propnetOk = false;
				GamerLogger.log("PropStructureChecker", "There are " + e.getValue().intValue() + " propositions with name " + e.getKey() + " .");
			}
		}

		// Check if there is a legal not corresponding to an input or viceversa
		List<ExternalizedStateProposition> inputs = pn.getInputPropositions();
		List<ExternalizedStateProposition> legals = pn.getLegalPropositions();

		if(inputs.size() != legals.size()){
			propnetOk = false;
			GamerLogger.log("PropStructureChecker", "The lists with INPUT and LEGAL propositions don't have the same size.");
		}else{
			for(int i = 0; i < inputs.size(); i++){
				if(inputs.get(i) == null){
					propnetOk = false;
					GamerLogger.log("PropStructureChecker", "The proposition " + legals.get(i).getComponentType() + " doesn't have a corresponding INPUT proposition.");
				}
				if(legals.get(i) == null){
					propnetOk = false;
					GamerLogger.log("PropStructureChecker", "The proposition " + inputs.get(i).getComponentType() + " doesn't have a corresponding LEGAL proposition.");
				}
			}
		}

		return propnetOk;
	}

}
