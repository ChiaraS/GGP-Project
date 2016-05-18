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
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.DynamicComponent;
import org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.DynamicPropNet;
import org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.components.DynamicAnd;
import org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.components.DynamicConstant;
import org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.components.DynamicNot;
import org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.components.DynamicOr;
import org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.components.DynamicProposition;
import org.ggp.base.util.propnet.architecture.separateExtendedState.dynamic.components.DynamicTransition;
import org.ggp.base.util.propnet.utils.PROP_TYPE;
import org.ggp.base.util.statemachine.Role;

import com.google.common.collect.HashMultiset;
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
/**
 * This class creates the propnet and provides extra methods to polish it after creation.
 * This class is a refinement/modification of the DynamicPropNetFactory and creates
 * a propnet with a faster structure. Some of the methods that it provides to polish the propnet
 * after creation are taken (and sometimes improved) from the DynamicPropNetFactory,
 * while some others have been implemented from scratch.
 *
 * @author C.Sironi
 *
 */
public class DynamicPropNetFactory {

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
	public static DynamicPropNet create(List<Gdl> description) throws InterruptedException {
		return create(description, false);
	}

	public static DynamicPropNet create(List<Gdl> description, boolean verbose) throws InterruptedException {
		//System.out.println("Building propnet...");

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
		Map<GdlSentence, DynamicComponent> components = new HashMap<GdlSentence, DynamicComponent>();
		Map<GdlSentence, DynamicComponent> negations = new HashMap<GdlSentence, DynamicComponent>();
		DynamicConstant trueComponent = new DynamicConstant(true);
		DynamicConstant falseComponent = new DynamicConstant(false);
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
						DynamicProposition trueProp = new DynamicProposition(trueSentence);
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
			Map<GdlSentence, DynamicComponent> temporaryComponents = new HashMap<GdlSentence, DynamicComponent>();
			Map<GdlSentence, DynamicComponent> temporaryNegations = new HashMap<GdlSentence, DynamicComponent>();
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
		Set<DynamicComponent> componentSet = new HashSet<DynamicComponent>(components.values());
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
		DynamicPropNet propnet = new DynamicPropNet(roles, componentSet, trueComponent, falseComponent);
		if(verbose) {
			System.out.println("Done setting up propnet; took " + (System.currentTimeMillis() - startTime) + "ms, has " + componentSet.size() + " components and " + propnet.getNumLinks() + " links");
			System.out.println("Propnet has " +propnet.getNumAnds()+" ands; "+propnet.getNumOrs()+" ors; "+propnet.getNumNots()+" nots");
			System.out.println("Propnet has " +propnet.getNumBases() + " bases; "+propnet.getNumTransitions()+" transitions; "+propnet.getNumInputs()+" inputs");
		}
		//System.out.println(propnet);
		return propnet;
	}


	private static void removeUselessBasePropositions(
			Map<GdlSentence, DynamicComponent> components, Map<GdlSentence, DynamicComponent> negations, DynamicConstant trueComponent,
			DynamicConstant falseComponent) throws InterruptedException {
		boolean changedSomething = false;
		for(Entry<GdlSentence, DynamicComponent> entry : components.entrySet()) {
			if(entry.getKey().getName() == TRUE) {
				DynamicComponent comp = entry.getValue();
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
	private static void normalizePropositions(Set<DynamicComponent> componentSet) {
		for(DynamicComponent component : componentSet) {
			if(component instanceof DynamicProposition) {
				DynamicProposition p = (DynamicProposition) component;
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
			Map<GdlSentence, DynamicComponent> components) throws InterruptedException {
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
			Map<GdlSentence, DynamicComponent> temporaryComponents,
			Map<GdlSentence, DynamicComponent> temporaryNegations,
			Map<GdlSentence, DynamicComponent> components,
			Map<GdlSentence, DynamicComponent> negations, DynamicComponent trueComponent,
			DynamicComponent falseComponent) throws InterruptedException {
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
			DynamicComponent tempComp = temporaryComponents.get(sentence);
			DynamicComponent realComp = components.get(sentence);
			if(realComp == null) {
				realComp = falseComponent;
			}
			for(DynamicComponent output : tempComp.getOutputs()) {
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
	private static void optimizeAwayTrueAndFalse(Map<GdlSentence, DynamicComponent> components, Map<GdlSentence, DynamicComponent> negations, DynamicComponent trueComponent, DynamicComponent falseComponent) throws InterruptedException {
	    while(hasNonessentialChildren(trueComponent) || hasNonessentialChildren(falseComponent)) {
	    	ConcurrencyUtils.checkForInterruption();
            optimizeAwayTrue(components, negations, null, trueComponent, falseComponent);
            optimizeAwayFalse(components, negations, null, trueComponent, falseComponent);
        }
	}

	//TODO: Create a version with just a set of components that we can share with post-optimizations
	private static void optimizeAwayFalse(Map<GdlSentence, DynamicComponent> components,
			Map<GdlSentence, DynamicComponent> negations, DynamicPropNet pn,
			DynamicComponent trueComponent, DynamicComponent falseComponent) {
        assert((components != null && negations != null) || pn != null);
        assert((components == null && negations == null) || pn == null);
        for (DynamicComponent output : Lists.newArrayList(falseComponent.getOutputs())) {
        	if (isEssentialProposition(output) || output instanceof DynamicTransition) {
        		//Since this is the false constant, there are a few "essential" types
        		//we don't actually want to keep around.
        		if (!isLegalOrGoalProposition(output)) {
        			continue;
        		}
	    	}
			if(output instanceof DynamicProposition) {
				//Move its outputs to be outputs of false
				for(DynamicComponent child : output.getOutputs()) {
					//Disconnect
					child.removeInput(output);
					//output.removeOutput(child); //do at end
					//Reconnect; will get children before returning, if nonessential
					falseComponent.addOutput(child);
					child.addInput(falseComponent);
				}
				output.removeAllOutputs();

				if(!isEssentialProposition(output)) {
					DynamicProposition prop = (DynamicProposition) output;
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
			} else if(output instanceof DynamicAnd) {
				DynamicAnd and = (DynamicAnd) output;
				//Attach children of and to falseComponent
				for(DynamicComponent child : and.getOutputs()) {
					child.addInput(falseComponent);
					falseComponent.addOutput(child);
					child.removeInput(and);
				}
				//Disconnect and completely
				and.removeAllOutputs();
				for(DynamicComponent parent : and.getInputs())
					parent.removeOutput(and);
				and.removeAllInputs();
				if(pn != null)
				    pn.removeComponent(and);
			} else if(output instanceof DynamicOr) {
				DynamicOr or = (DynamicOr) output;
				//Remove as input from or
				or.removeInput(falseComponent);
				falseComponent.removeOutput(or);
				//If or has only one input, remove it
				if(or.getInputs().size() == 1) {
					DynamicComponent in = or.getSingleInput();
					or.removeInput(in);
					in.removeOutput(or);
					for(DynamicComponent out : or.getOutputs()) {
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
			} else if(output instanceof DynamicNot) {
				DynamicNot not = (DynamicNot) output;
				//Disconnect from falseComponent
				not.removeInput(falseComponent);
				falseComponent.removeOutput(not);
				//Connect all children of the not to trueComponent
				for(DynamicComponent child : not.getOutputs()) {
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
			} else if(output instanceof DynamicTransition) {
				//???
				System.err.println("Fix optimizeAwayFalse's case for Transitions");
			}
		}
	}



	private static boolean isLegalOrGoalProposition(DynamicComponent comp) {
		if (!(comp instanceof DynamicProposition)) {
			return false;
		}

		DynamicProposition prop = (DynamicProposition) comp;
		GdlSentence name = prop.getName();
		return name.getName() == GdlPool.LEGAL || name.getName() == GdlPool.GOAL;
	}




	private static void optimizeAwayTrue(
			Map<GdlSentence, DynamicComponent> components, Map<GdlSentence, DynamicComponent> negations, DynamicPropNet pn, DynamicComponent trueComponent,
			DynamicComponent falseComponent) {
	    assert((components != null && negations != null) || pn != null);
	    for (DynamicComponent output : Lists.newArrayList(trueComponent.getOutputs())) {
	    	if (isEssentialProposition(output) || output instanceof DynamicTransition) {
	    		continue;
	    	}
			if(output instanceof DynamicProposition) {
				//Move its outputs to be outputs of true
				for(DynamicComponent child : output.getOutputs()) {
					//Disconnect
					child.removeInput(output);
					//output.removeOutput(child); //do at end
					//Reconnect; will get children before returning, if nonessential
					trueComponent.addOutput(child);
					child.addInput(trueComponent);
				}
				output.removeAllOutputs();

				if(!isEssentialProposition(output)) {
					DynamicProposition prop = (DynamicProposition) output;
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
			} else if(output instanceof DynamicOr) {
				DynamicOr or = (DynamicOr) output;
				//Attach children of or to trueComponent
				for(DynamicComponent child : or.getOutputs()) {
					child.addInput(trueComponent);
					trueComponent.addOutput(child);
					child.removeInput(or);
				}
				//Disconnect or completely
				or.removeAllOutputs();
				for(DynamicComponent parent : or.getInputs())
					parent.removeOutput(or);
				or.removeAllInputs();
				if(pn != null)
				    pn.removeComponent(or);
			} else if(output instanceof DynamicAnd) {
				DynamicAnd and = (DynamicAnd) output;
				//Remove as input from and
				and.removeInput(trueComponent);
				trueComponent.removeOutput(and);
				//If and has only one input, remove it
				if(and.getInputs().size() == 1) {
					DynamicComponent in = and.getSingleInput();
					and.removeInput(in);
					in.removeOutput(and);
					for(DynamicComponent out : and.getOutputs()) {
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
			} else if(output instanceof DynamicNot) {
				DynamicNot not = (DynamicNot) output;
				//Disconnect from trueComponent
				not.removeInput(trueComponent);
				trueComponent.removeOutput(not);
				//Connect all children of the not to falseComponent
				for(DynamicComponent child : not.getOutputs()) {
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
			} else if(output instanceof DynamicTransition) {
				//???
				System.err.println("Fix optimizeAwayTrue's case for Transitions");
			}
		}
	}


	private static boolean hasNonessentialChildren(DynamicComponent trueComponent) {
		for(DynamicComponent child : trueComponent.getOutputs()) {
			if(child instanceof DynamicTransition)
				continue;
			if(!isEssentialProposition(child))
				return true;
			//We don't want any grandchildren, either
			if(!child.getOutputs().isEmpty())
				return true;
		}
		return false;
	}

	private static boolean isEssentialProposition(DynamicComponent component) {
		if(!(component instanceof DynamicProposition))
			return false;

		//We're looking for things that would be outputs of "true" or "false",
		//but we would still want to keep as propositions to be read by the
		//state machine
		DynamicProposition prop = (DynamicProposition) component;
		GdlConstant name = prop.getName().getName();

		return name.equals(LEGAL) /*|| name.equals(NEXT)*/ || name.equals(GOAL)
				|| name.equals(INIT) || name.equals(TERMINAL);
	}


	private static void completeComponentSet(Set<DynamicComponent> componentSet) {
		Set<DynamicComponent> newComponents = new HashSet<DynamicComponent>();
		Set<DynamicComponent> componentsToTry = new HashSet<DynamicComponent>(componentSet);
		while(!componentsToTry.isEmpty()) {
			for(DynamicComponent c : componentsToTry) {
				for(DynamicComponent out : c.getOutputs()) {
					if(!componentSet.contains(out))
						newComponents.add(out);
				}
				for(DynamicComponent in : c.getInputs()) {
					if(!componentSet.contains(in))
						newComponents.add(in);
				}
			}
			componentSet.addAll(newComponents);
			componentsToTry = newComponents;
			newComponents = new HashSet<DynamicComponent>();
		}
	}

	private static void addTransitions(Map<GdlSentence, DynamicComponent> components) {
		for(Entry<GdlSentence, DynamicComponent> entry : components.entrySet()) {
			GdlSentence sentence = entry.getKey();

			if(sentence.getName().equals(NEXT)) {
				//connect to true
				GdlSentence trueSentence = GdlPool.getRelation(TRUE, sentence.getBody());
				DynamicComponent nextComponent = entry.getValue();
				DynamicComponent trueComponent = components.get(trueSentence);
				//There might be no true component (for example, because the bases
				//told us so). If that's the case, don't have a transition.
				if(trueComponent == null) {
				    // Skipping transition to supposedly impossible 'trueSentence'
				    continue;
				}
				DynamicTransition transition = new DynamicTransition();
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
	private static void setUpInit(Map<GdlSentence, DynamicComponent> components,
			DynamicConstant trueComponent, DynamicConstant falseComponent) {
		DynamicProposition initProposition = new DynamicProposition(GdlPool.getProposition(INIT_CAPS));
		for(Entry<GdlSentence, DynamicComponent> entry : components.entrySet()) {
			//Is this something that will be true?
			if(entry.getValue() == trueComponent) {
				if(entry.getKey().getName().equals(INIT)) {
					//Find the corresponding true sentence
					GdlSentence trueSentence = GdlPool.getRelation(TRUE, entry.getKey().getBody());
					//System.out.println("True sentence from init: " + trueSentence);
					DynamicComponent trueSentenceComponent = components.get(trueSentence);
					if(trueSentenceComponent.getInputs().isEmpty()) {
						//Case where there is no transition input
						//Add the transition input, connect to init, continue loop

						// @author c.sironi: Also set to TRUE that fact that the value of this transition
						// depends on the INIT proposition value.
						DynamicTransition transition = new DynamicTransition(true);
						//init goes into transition
						transition.addInput(initProposition);
						initProposition.addOutput(transition);
						//transition goes into component
						trueSentenceComponent.addInput(transition);
						transition.addOutput(trueSentenceComponent);
					} else {
						//The transition already exists
						DynamicComponent transition = trueSentenceComponent.getSingleInput();

						//We want to add init as a thing that precedes the transition
						//Disconnect existing input
						DynamicComponent input = transition.getSingleInput();
						//input and init go into or, or goes into transition
						input.removeOutput(transition);
						transition.removeInput(input);
						List<DynamicComponent> orInputs = new ArrayList<DynamicComponent>(2);
						orInputs.add(input);
						orInputs.add(initProposition);
						orify(orInputs, transition, falseComponent);
						// @author c.sironi: Also set to TRUE that fact that the value of this transition
						// depends on the INIT proposition value.
						((DynamicTransition) transition).setDependingOnInit(true);
					}
				}
			}
		}
	}





	/**
	 * Adds an or gate connecting the inputs to produce the output.
	 * Handles special optimization cases like a true/false input.
	 */
	private static void orify(Collection<DynamicComponent> inputs, DynamicComponent output, DynamicConstant falseProp) {
		//TODO: Look for already-existing ors with the same inputs?
		//Or can this be handled with a GDL transformation?

		//Special case: An input is the true constant
		for(DynamicComponent in : inputs) {
			if(in instanceof DynamicConstant && ((DynamicConstant) in).getValue()) {
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
		DynamicOr or = new DynamicOr();
		for(DynamicComponent in : inputs) {
			if(!(in instanceof DynamicConstant)) {
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
			DynamicComponent in = or.getSingleInput();
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
			Map<GdlSentence, DynamicComponent> components,
			Map<GdlSentence, DynamicComponent> negations,
			DynamicConstant trueComponent, DynamicConstant falseComponent,
			boolean usingBase, boolean usingInput,
			Set<SentenceForm> recursionForms,
			Map<GdlSentence, DynamicComponent> temporaryComponents, Map<GdlSentence, DynamicComponent> temporaryNegations,
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
				DynamicProposition prop = new DynamicProposition(alwaysTrueSentence);
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
				DynamicProposition prop = new DynamicProposition(doesSentence);
				components.put(doesSentence, prop);
			}
			return;
		}
		if(usingBase && form.getName().equals(TRUE)) {
			SentenceForm baseForm = form.withName(BASE);
			for (GdlSentence baseSentence : constantChecker.getTrueSentences(baseForm)) {
				GdlSentence trueSentence = GdlPool.getRelation(TRUE, baseSentence.getBody());
				DynamicProposition prop = new DynamicProposition(trueSentence);
				components.put(trueSentence, prop);
			}
			return;
		}

		Map<GdlSentence, Set<DynamicComponent>> inputsToOr = new HashMap<GdlSentence, Set<DynamicComponent>>();
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
				List<DynamicComponent> componentsToConnect = new ArrayList<DynamicComponent>(rule.arity());
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

						DynamicComponent conj = components.get(transformed);
						//If conj is null and this is a sentence form we're still handling,
						//hook up to a temporary sentence form
						if(conj == null) {
							conj = temporaryComponents.get(transformed);
						}
						if(conj == null && SentenceModelUtils.inSentenceFormGroup(transformed, recursionForms)) {
							//Set up a temporary component
							DynamicProposition tempProp = new DynamicProposition(transformed);
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

						DynamicComponent conj = negations.get(transformed);
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
							DynamicComponent positive = components.get(transformed);
							if(positive == null) {
								positive = temporaryComponents.get(transformed);
							}
							if(positive == null) {
								//Make the temporary proposition
								DynamicProposition tempProp = new DynamicProposition(transformed);
								temporaryComponents.put(transformed, tempProp);
								positive = tempProp;
							}
							//Positive is now set and in temporaryComponents
							//Evidently, wasn't in temporaryNegations
							//So we add the "not" gate and set it in temporaryNegations
							DynamicNot not = new DynamicNot();
							//Add positive as input
							not.addInput(positive);
							positive.addOutput(not);
							temporaryNegations.put(transformed, not);
							conj = not;
						}
						if(conj == null) {
							DynamicComponent positive = components.get(transformed);
							//No, because then that will be attached to "negations", which could be bad

							if(positive == null) {
								//So the positive can't possibly be true (unless we have recurstion)
								//and so this would be positive always
								//We want to just skip this conjunct, so we continue to the next

								continue; //to the next conjunct
							}

							//Check if we're sharing a component with another sentence with a negation
							//(i.e. look for "nots" in our outputs and use those instead)
							DynamicNot existingNotOutput = getNotOutput(positive);
							if(existingNotOutput != null) {
								componentsToConnect.add(existingNotOutput);
								negations.put(transformed, existingNotOutput);
								continue; //to the next conjunct
							}

							DynamicNot not = new DynamicNot();
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
					DynamicProposition andComponent = new DynamicProposition(TEMP);

					andify(componentsToConnect, andComponent, trueComponent);
					if(!isThisConstant(andComponent, falseComponent)) {
						if(!inputsToOr.containsKey(sentence))
							inputsToOr.put(sentence, new HashSet<DynamicComponent>());
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
		for(Entry<GdlSentence, Set<DynamicComponent>> entry : inputsToOr.entrySet()) {
			ConcurrencyUtils.checkForInterruption();

			GdlSentence sentence = entry.getKey();
			Set<DynamicComponent> inputs = entry.getValue();
			Set<DynamicComponent> realInputs = new HashSet<DynamicComponent>();
			for(DynamicComponent input : inputs) {
				if(input instanceof DynamicConstant || input.getInputs().size() == 0) {
					realInputs.add(input);
				} else {
					realInputs.add(input.getSingleInput());
					input.getSingleInput().removeOutput(input);
					input.removeAllInputs();
				}
			}

			DynamicProposition prop = new DynamicProposition(sentence);
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

				DynamicProposition prop = new DynamicProposition(sentence);
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

	private static boolean isThisConstant(DynamicComponent conj, DynamicConstant constantComponent) {
		if(conj == constantComponent)
			return true;
		return (conj instanceof DynamicProposition && conj.getInputs().size() == 1 && conj.getSingleInput() == constantComponent);
	}


	private static DynamicNot getNotOutput(DynamicComponent positive) {
		for(DynamicComponent c : positive.getOutputs()) {
			if(c instanceof DynamicNot) {
				return (DynamicNot) c;
			}
		}
		return null;
	}


	private static List<GdlVariable> getVarsInConjunct(GdlLiteral literal) {
		return GdlUtils.getVariables(literal);
	}



	private static void andify(List<DynamicComponent> inputs, DynamicComponent output, DynamicConstant trueProp) {
		//Special case: If the inputs include false, connect false to thisComponent
		for(DynamicComponent c : inputs) {
			if(c instanceof DynamicConstant && !((DynamicConstant)c).getValue()) {
				//Connect false (c) to the output
				output.addInput(c);
				c.addOutput(output);
				return;
			}
		}

		//For reals... just skip over any true constants
		DynamicAnd and = new DynamicAnd();
		for(DynamicComponent in : inputs) {
			if(!(in instanceof DynamicConstant)) {
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
			DynamicComponent in = and.getSingleInput();
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
	 * NOTE: this method is not used anymore because the fixing of the input-less components has been
	 * directly incorporated in the initialization of the propnet.
	 *
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
	 * are set to false (and we have the external propnet state that initializes such components' default truth
	 * value to false).
	 * However is not impossible that there will be a game that will have input-less components that will require
	 * a different initialization value. Whenever this will happen there are two ways to fix the problem:
	 * 1. Set here the correct input constant for each input-less component.
	 * 2. Change the default initialization value in the external propnet state (having no inputs, each input-less
	 * component will then keep the same truth value for the whole game).
	 *
	 * This said, it must be noticed that, to correctly run the removeUnreachableBasesAndInputs method, this method
	 * is required to be run first!
	 *
	 * @param pn the propNet to fix.
	 * @param trueConstant
	 * @param falseConstant
	 */
	public static void fixInputlessComponents(DynamicPropNet pn){

		DynamicConstant trueConstant = pn.getTrueConstant();
		DynamicConstant falseConstant = pn.getFalseConstant();

		assert(trueConstant != null && falseConstant != null);

		for(DynamicComponent c : pn.getComponents()){
			if(c.getInputs().size() == 0){
				if(c instanceof DynamicProposition){
					if(((DynamicProposition) c).getPropositionType() != PROP_TYPE.INPUT){
						c.addInput(falseConstant);
						falseConstant.addOutput(c);

						//System.out.println("Adding to false: " + c.getComponentType());


					}
				}else if(c instanceof DynamicOr){
					c.addInput(falseConstant);
					falseConstant.addOutput(c);

					//System.out.println("Adding to false: " + c.getComponentType());

				}else if(c instanceof DynamicAnd){
					throw new RuntimeException("Unhandled input-less component type: AND");
				}else if(c instanceof DynamicTransition){
					throw new RuntimeException("Unhandled input-less component type: TRANSITION");
				}else if(c instanceof DynamicNot){
					throw new RuntimeException("Unhandled input-less component type: NOT");
				}
			}
		}

		// Now we can remove the (unnecessary) components that are always true or false.
		//optimizeAwayTrueAndFalse2(pn, trueConstant, falseConstant);

	}

	public static void optimizeAwayConstants(DynamicPropNet pn){
		DynamicConstant trueConstant = pn.getTrueConstant();
		DynamicConstant falseConstant = pn.getFalseConstant();

		optimizeAwayTrueAndFalse2(pn, trueConstant, falseConstant);

		//System.out.println();
		//System.out.println("After optimizing away constants propnet has " + pn.getLegalPropositions().size() + " LEGAL propositions.");
		//System.out.println("After optimizing away constants propnet has " + pn.getInputPropositions().size() + " INPUT propositions.");
		//System.out.println();
	}





	/**
	 * This method removes from the propnet the propositions that have no particular meaning in the game
	 * (i.e. all propositions with type OTHER, otherwise defined anonymous propositions). More precisely,
	 * it connects their single input (if they have one) with each of their outputs.
	 *
	 * NOTE: if the anonymous proposition has no input it cannot be removed. When an OTHER proposition has
	 * no input it means that it is always false. Since it might have a gate as output, we cannot simply
	 * remove it from the propnet, but we must connect FALSE as its input and take care of removing it with
	 * the optimizeAwayTrueAndFalse2() method. If an anonymous proposition with no input is found, this method
	 * does nothing. To deal with such propositions you can call the fixInputlessComponents() method (either
	 * before or after this method).
	 *
	 * NOTE: if the fixInputlessComponents() has been called before this method, no anonymous proposition will
	 * be inputless.
	 *
	 * NOTE: if the proposition has no outputs it is removed anyway and its input will be connected to no new
	 * output.
	 *
	 * @param pn
	 */
	public static void removeAnonymousPropositions(DynamicPropNet pn){

		List<DynamicProposition> toRemove = new ArrayList<DynamicProposition>();

		for(DynamicProposition p : pn.getPropositions()){
			if(p.getPropositionType() == PROP_TYPE.OTHER){
				if(p.getInputs().size() > 1){
					throw new RuntimeException("Found a proposition having more than one input! Something is wrong with the propnet structure!");
				}

				if(p.getInputs().size() == 1){
					DynamicComponent i = p.getSingleInput();
					i.removeOutput(p);
					p.removeInput(i);
					for(DynamicComponent o : p.getOutputs()){
						o.removeInput(p);
						o.addInput(i);
						i.addOutput(o);
					}
					p.removeAllOutputs();
					toRemove.add(p);
				}
			}
		}

		for(DynamicProposition p : toRemove){
			pn.removeComponent(p);
		}

		//System.out.println();
		//System.out.println("After removing anonymous propositions propnet has " + pn.getLegalPropositions().size() + " LEGAL propositions.");
		//System.out.println("After removing anonymous propositions propnet has " + pn.getInputPropositions().size() + " INPUT propositions.");
		//System.out.println();
	}

	/**
	 * Represents the "type" of a node with respect to which truth
	 * values it is capable of having: true, false, either value,
	 * or neither value. Used by
	 * {@link DynamicPropNetFactory#optimizeAwayConstantValueComponents(DynamicPropNet)}.
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
	 *
	 * CONTRACT: the effects of this method are guaranteed to be the expected ones
	 * ONLY if the fixInputlessComponents() method has been called on the propnet
	 * first. Otherwise, if there are input-less components other than the constants
	 * and the input propositions, this method will ignore some of the truth values
	 * that some components might have (e.g. it might not find out that a component
	 * can be both true and false and just assume that it is constant, either always
	 * true or always false).
	 *
	 * @param pn
	 * @throws InterruptedException
	 */
	public static void optimizeAwayConstantValueComponents(DynamicPropNet pn) throws InterruptedException{

		DynamicConstant trueConstant = pn.getTrueConstant();
		DynamicConstant falseConstant = pn.getFalseConstant();

		//If this doesn't contain a component, that's the equivalent of Type.NEITHER
		Map<DynamicComponent, Type> reachability = Maps.newHashMap();
		//Keep track of the number of true inputs to AND gates and false inputs to
		//OR gates.
		Multiset<DynamicComponent> numTrueInputs = HashMultiset.create();
		Multiset<DynamicComponent> numFalseInputs = HashMultiset.create();
		Stack<Pair<DynamicComponent, Type>> toAdd = new Stack<Pair<DynamicComponent, Type>>();

		List<DynamicProposition> legals = pn.getLegalPropositions();
		List<DynamicProposition> inputs = pn.getInputPropositions();

		//All constants have their values
		toAdd.add(Pair.of((DynamicComponent)trueConstant, Type.TRUE));
		toAdd.add(Pair.of((DynamicComponent)falseConstant, Type.FALSE));

		//Every input can be false (we assume that no player will have just one move allowed all game)
        for(DynamicProposition p : pn.getInputPropositions()) {
        	toAdd.add(Pair.of((DynamicComponent) p, Type.FALSE));
        }
	    //Every base with "init" can be true, every base without "init" can be false
	    for(DynamicProposition baseProp : pn.getBasePropositions()) {
	    	DynamicTransition t = (DynamicTransition) baseProp.getSingleInput();
	    	if(t.isDependingOnInit()){
	    		toAdd.add(Pair.of((DynamicComponent) baseProp, Type.TRUE));
	    	}else{
            	toAdd.add(Pair.of((DynamicComponent) baseProp, Type.FALSE));
            }
	    }
	    //There is no INIT proposition. This proposition is treated as any OTHER proposition
	    //==> no need for the following instructions
	    //DynamicProposition initProposition = pn.getInitProposition();
    	//toAdd.add(Pair.of((DynamicComponent) initProposition, Type.BOTH));

    	while (!toAdd.isEmpty()) {
			ConcurrencyUtils.checkForInterruption();
    		Pair<DynamicComponent, Type> curEntry = toAdd.pop();
    		DynamicComponent curComp = curEntry.left;
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
    		if (curComp instanceof DynamicProposition) {
    			typeToAdd = newInputType;
    		} else if (curComp instanceof DynamicTransition) {
    			typeToAdd = newInputType;
    		} else if (curComp instanceof DynamicConstant) {
    			typeToAdd = newInputType;
    		} else if (curComp instanceof DynamicNot) {
    			typeToAdd = newInputType.opposite();
    		} else if (curComp instanceof DynamicAnd) {
    			if (newInputType.hasTrue) {
    				numTrueInputs.add(curComp);
    				if (numTrueInputs.count(curComp) == curComp.getInputs().size()) {
    					typeToAdd = Type.TRUE;
    				}
    			}
    			if (newInputType.hasFalse) {
    				typeToAdd = typeToAdd.with(Type.FALSE);
    			}
    		} else if (curComp instanceof DynamicOr) {
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
    		for (DynamicComponent output : curComp.getOutputs()) {
    			toAdd.add(Pair.of(output, typeToAdd));
    		}
    		// If it's a legal, we must propagate the value to the corresponding input
			if(curComp instanceof DynamicProposition && ((DynamicProposition)curComp).getPropositionType() == PROP_TYPE.LEGAL){
				int index = legals.indexOf(curComp);
				if(index == -1){
					throw new RuntimeException("Found a non-indexed legal proposition! Something must have gone wrong with the propnet initialization!");
				}
				toAdd.add(Pair.of((DynamicComponent) inputs.get(index), typeToAdd));
			}
    	}

    	//Set<DynamicComponent> toRemove = new HashSet<DynamicComponent>();
    	//Set<DynamicProposition> toConvert = new HashSet<DynamicProposition>();

	    //Make them the input of all false/true components
	    for(Entry<DynamicComponent, Type> entry : reachability.entrySet()) {
	        Type type = entry.getValue();
	        if(type == Type.TRUE || type == Type.FALSE) {
	        	DynamicComponent c = entry.getKey();

	        	if (c instanceof DynamicConstant) {
	            	//Skip the constants since we don't need to connect them to a constant value.
	            	continue;
	            }

	        	// If it is a base or input proposition also record that the proposition must be converted into
	        	// an OTHER proposition and the corresponding transition or legal must be removed from the propnet
	        	if(c instanceof DynamicProposition){
	        		DynamicProposition p = (DynamicProposition) c;
	        		switch(p.getPropositionType()){
	        		case BASE:
	        			//toRemove.add(p.getSingleInput());
	        			//toConvert.add(p);
	        			if(type == Type.TRUE){
	        				pn.addAlwaysTrueBase(p.getName());
	        			}

	        			// Check type of corresponding transition. If it's different from the one of the base throw exception
	        			if(reachability.get(p.getSingleInput()) != type){
	        				throw new RuntimeException("Detected transition with different value set than the corresponding always " + (type == Type.TRUE)  + " BASE proposition.");
	        			}

	        			break;
	        		case INPUT:
	        			if(type == Type.TRUE){
	    					throw new RuntimeException("Detected an input proposition always TRUE, when assuming that it is impossible to find one!");
	        			}
	        			int index = inputs.indexOf(p);
	        			if(index == -1){
	    					throw new RuntimeException("Found a non-indexed input proposition! Something must have gone wrong with the propnet initialization!");
	        			}
	        			//toRemove.add(legals.get(index));
	        			//toConvert.add(p);

	        			// Check type of corresponding LEGAL. If it's not false like the one of the input throw exception
	        			if(reachability.get(legals.get(index)) != Type.FALSE){
	        				throw new RuntimeException("Detected LEGAL proposition with different values set than the corresponding always false INPUT proposition.");
	        			}

	        			break;
	        		default:
	        			break;
	        		}
	        	}

	        	if(!(c instanceof DynamicProposition) || (((DynamicProposition)c).getPropositionType() != PROP_TYPE.INPUT)){
		        	for(DynamicComponent i : c.getInputs()){
		        		i.removeOutput(c);
		        	}
		        	c.removeAllInputs();

		        	if(type == Type.TRUE ^ c instanceof DynamicNot){
	        			c.addInput(trueConstant);
	        			trueConstant.addOutput(c);
	        		}else{
	        			c.addInput(falseConstant);
	        			falseConstant.addOutput(c);
	        		}
	        	}
	        }


	    }

	    // Remove all useless transitions and legal propositions from the propnet
	    /*for(DynamicComponent c : toRemove){
	    	pn.removeComponent(c);
	    }

	    for(DynamicProposition p : toConvert){
	    	pn.convertToOther(p);
	    }*/

		//System.out.println();
		//System.out.println("After detecting constant-value components propnet has " + pn.getLegalPropositions().size() + " LEGAL propositions.");
		//System.out.println("After detecting constant-value components propnet has " + pn.getInputPropositions().size() + " INPUT propositions.");
		//System.out.println();

	    optimizeAwayTrueAndFalse2(pn, trueConstant, falseConstant);
	}

	/**
	 * This method checks if any of the output-less components can be removed from the propnet.
	 *
	 * @param pn
	 */
	public static void removeOutputlessComponents(DynamicPropNet pn){

		Set<DynamicComponent> toCheckNow = new HashSet<DynamicComponent>(pn.getComponents());
		Set<DynamicComponent> toCheckNext;

		while(!toCheckNow.isEmpty()){

			toCheckNext = new HashSet<DynamicComponent>();

			for(DynamicComponent c : toCheckNow){
				// We can only remove output-less ANDs, ORs and NOTs...
				if(c instanceof DynamicAnd || c instanceof DynamicOr || c instanceof DynamicNot){
					if(c.getOutputs().size() == 0){
						for(DynamicComponent i : c.getInputs()){
							i.removeOutput(c);
							toCheckNext.add(i);
						}
						c.removeAllInputs();
						pn.removeComponent(c);
					}
				//...or OTHER propositions
				}else if(c instanceof DynamicProposition){
					DynamicProposition p = (DynamicProposition) c;
					if(p.getPropositionType() == PROP_TYPE.OTHER){
						if(p.getOutputs().size() == 0){
							for(DynamicComponent i : p.getInputs()){
								i.removeOutput(p);
								toCheckNext.add(i);
							}
							p.removeAllInputs();
							pn.removeComponent(p);
						}
					}
				}
			}

			toCheckNow = toCheckNext;
		}

		//System.out.println();
		//System.out.println("After removing outputless components propnet has " + pn.getLegalPropositions().size() + " LEGAL propositions.");
		//System.out.println("After removing outputless components propnet has " + pn.getInputPropositions().size() + " INPUT propositions.");
		//System.out.println();
	}

	/**
	 * This method is another version of the optimizeAwayTrueAndFalse() method. This method has been implemented
	 * to be used during propnet optimization AFTER its creation, while the other method deals with the propnet
	 * during creation.
	 *
	 * This method removes from the propnet non-essential components that have the TRUE constant or the FALSE
	 * constant as one of their inputs.
	 *
	 * This method deals with components as follows:
	 *
	 * COMPONENTS WITH TRUE INPUT:
	 * - AND: if TRUE is the only input => all its outputs are true. The AND gate is removed and all its outputs
	 * 		  are connected to true.
	 * 		  Otherwise TRUE is removed as input of AND since its output only depends on the values of its other
	 * 		  inputs.
	 * - OR: if one of the inputs of OR is true it means that the output of OR is always true, so the component
	 * 		 is removed from the propnet and all its outputs are connected to TRUE.
	 * - NOT: if the single input of NOT is TRUE it means that its output is always false. NOT is removed from the
	 * 		  propnet and all its outputs are connected to FALSE.
	 * - TRANSITION: a transition is always assumed to have a BASE proposition as output, so it cannot be removed
	 * 				 from the propnet, unless the corresponding BASE proposition is also proved to be always TRUE
	 * 				 and gets removed. Nothing is done for a transition connected to TRUE (unless it has no base
	 * 				 as output. In this case an exception is thrown).
	 * - CONSTANT: an exception is thrown, since a constant cannot have an input, especially not another constant.
	 * - PROPOSITIONS:
	 * 	-- GOAL, LEGAL, INIT, TERMINAL: this propositions are needed for the correct functioning of the propnet as
	 * 									they give essential informations on the game, so they are never removed.
	 * 									(NOTE that if you decide not to use the INIT proposition in the propnet, you
	 * 									can connect it to false and change its type to OTHER. Also NOTE that a legal
	 * 									being always TRUE doesn't mean that the corresponding input will always be
	 * 									true, if the player has other legal moves it might choose to play one of them).
	 * 	-- BASE, INPUT: an exception is thrown, since base proposition are assumed to always and only have transitions
	 * 					as input and input proposition are assumed to NEVER have inputs.
	 * 	-- OTHER: if a proposition is always true all its outputs will be always true, so the proposition is removed
	 * 			  from the propnet (since it's not giving relevant informations on the game) and all its outputs are
	 * 			  connected to TRUE.
	 *
	 * COMPONENTS WITH FALSE INPUT:
	 * - AND: if one of the inputs of AND is false it means that the output of AND is always false, so the component
	 * 		  is removed from the propnet and all its outputs are connected to FALSE.
	 * - OR: if FALSE is the only input => all its outputs are FALSE. The OR gate is removed and all its outputs
	 * 		  are connected to FALSE.
	 * 		  Otherwise FALSE is removed as input of OR since its output only depends on the values of its other
	 * 		  inputs.
	 * - NOT: if the single input of NOT is FALSE it means that its output is always TRUE. NOT is removed from the
	 * 		  propnet and all its outputs are connected to TRUE.
	 * - TRANSITION: a transition is always assumed to have a BASE proposition as output, so it cannot be removed
	 * 				 from the propnet, unless the corresponding BASE proposition is also proved to be always FALSE
	 * 				 and gets removed. Nothing is done for a transition connected to FALSE (unless it has no base
	 * 				 as output. In this case an exception is thrown).
	 * - CONSTANT: an exception is thrown, since a constant cannot have an input, especially not another constant.
	 * - PROPOSITIONS:
	 * 	-- LEGAL, INIT, TERMINAL: this propositions are needed for the correct functioning of the propnet as they give
	 * 							  essential informations on the game, so they are never removed.
	 * 							  (NOTE that if you decide not to use the INIT proposition in the propnet, you can
	 * 							  connect it to false and change its type to OTHER).
	 * 	-- BASE, INPUT: an exception is thrown, since base proposition are assumed to always and only have transitions
	 * 					as input and input proposition are assumed to NEVER have inputs.
	 * 	-- OTHER: if a proposition is always false all its outputs will be always false, so the proposition is removed
	 * 			  from the propnet (since it's not giving relevant informations on the game) and all its outputs are
	 * 			  connected to FALSE.
	 * 	-- GOAL: if a goal is always false it is removed from the propnet and all its outputs are connected to FALSE.
	 * 	 		 It is safe to remove an always false goal, since we will never need to use it in any state, it will
	 * 			 never be returned.
	 *
	 * The method will iterate over the outputs of TRUE and FALSE until none of them can be removed or modified
	 * anymore.
	 *
	 * NOTE: this method works if some assumptions are made on the structure of the propnet:
	 * 1. The number of transitions and base propositions in the propnet is the same and they are in a 1-to-1
	 * 	  relation (each transition is connected to one and only one base proposition and vice versa). This means
	 * 	  that all the methods that modify the structure of the propnet must keep this assumption true if this
	 *    method is going to be called next. For example, if a method detects that a base proposition is always
	 *    true/false and wants to connect it to the true/false component, then it must remove the corresponding
	 *    transition since it is not needed anymore to determine the truth value of the base proposition in the
	 *    next step. Moreover, the BASE proposition in this case "looses" the state of BASE proposition and becomes
	 *    just as any OTHER proposition, thus its type must be changed from BASE to OTHER (or this method will
	 *    complain as a BASE is not expected to have a constant as input but only a transition).
	 *    Also note that a base being always true/false means that there is no need anymore for the corresponding
	 *    transition, but a transition being always true/false doesn't necessarily mean that the corresponding
	 *    base is always true/false. If you are not using the INIT proposition, for example, a base might be always
	 *    true/false for the whole game, except in the first state (thus if an extra check is needed to detect when
	 *    a base is always true/false. Just checking the value of its transition is not sufficient).
	 * 2. An input proposition can never be always TRUE. This means that we assume that no player can have one and
	 * 	  only one legal move throughout the game, or in other words, every input proposition can become false sooner
	 * 	  or later. On the contrary, it is feasible for an input proposition to always be false throughout the game
	 * 	  (for example if the corresponding legal is always false).
	 * 3. The number of legal and base propositions in the propnet is the same and they are in a 1-to-1 relation
	 * 	  (each legal proposition is related to one and only one input proposition and vice versa). This means that
	 * 	  all the methods that modify the structure of the propnet must keep this assumption true if this method is
	 * 	  going to be called next. For example, if a method detects that an input proposition is always false and
	 * 	  wants to connect it to the false component, then it must also deal with the corresponding legal proposition.
	 * 	  As a legal it is not needed anymore to determine if the value of the base proposition in the next step can
	 *    be true (we already know it will always be false). In this case the legal looses its importance as a LEGAL
	 *    proposition, but since it might have outputs it cannot be directly removed from the propnet, so it should be
	 *    classified as any OTHER proposition and also be connected to the FALSE component (this method will take care
	 *    of removing it). Moreover, the input proposition in this case "looses" the state of INPUT proposition as well
	 *    and becomes just as any OTHER proposition, thus its type must be changed from INPUT to OTHER (or this method
	 *    will complain as aN INPUT is not expected to have any input).
	 *
	 * NOTE that this method will also remove the init propositions connected to true, so there is no need for an
	 * extra method to do that (like in the ExtendedStatePropNetFactory).
	 *
	 * NOTE: this method might create output-less components that can be removed in a further optimization since they
	 * are useless (e.g. output-less gates, output-less OTHER propositions,...).
	 *
	 *
	 * @param pn
	 * @param trueConstant
	 * @param falseConstant
	 */
	private static void optimizeAwayTrueAndFalse2(DynamicPropNet pn, DynamicComponent trueConstant, DynamicComponent falseConstant){

		Set<DynamicComponent> toCheckT = new HashSet<DynamicComponent>(trueConstant.getOutputs());
		Set<DynamicComponent> toCheckF = new HashSet<DynamicComponent>(falseConstant.getOutputs());

		//System.out.println("Otputs of true to check: " + toCheckT.size());
		//System.out.println("Otputs of false to check: " + toCheckF.size());


		while(!(toCheckT.isEmpty() && toCheckF.isEmpty())){
			toCheckT = optimizeAwayTrue2(pn, trueConstant, falseConstant, toCheckT, toCheckF);
			toCheckF = optimizeAwayFalse2(pn, trueConstant, falseConstant, toCheckT, toCheckF);

			//System.out.println("Otputs of true to check: " + toCheckT.size());
			//System.out.println("Otputs of false to check: " + toCheckF.size());

		}

	}

	private static Set<DynamicComponent> optimizeAwayTrue2(DynamicPropNet pn, DynamicComponent trueConstant, DynamicComponent falseConstant, Set<DynamicComponent> toCheckT, Set<DynamicComponent> toCheckF){

		Set<DynamicComponent> toCheckNextT;

		while(!(toCheckT.isEmpty())){

			toCheckNextT = new HashSet<DynamicComponent>();

			for(DynamicComponent c : toCheckT){
				if(c instanceof DynamicConstant){
					throw new RuntimeException("Found a constant component having TRUE as input! Something is wrong with the propnet structure!");
				}else if(c instanceof DynamicAnd){
					if(c.getInputs().size() > 1){
						// One input of AND is TRUE, but since there are other inputs, the output of
						// AND depends entirely on them => true can be removed as input
						trueConstant.removeOutput(c);
						c.removeInput(trueConstant);

						/* ADDITION */
						// NOTE: if after removing the true constant as input the AND gate only has one input left
						// it is useless to keep this gate. We can just connect the single input directly to every
						// output of the gate.
						// NOTE that if this method is called on a propnet that has no AND gates with only one input,
						// the following code should ensure that also after the end of this method the propnet will
						// have no AND gates with only one input.
						if(c.getInputs().size() == 1){
							DynamicComponent i = c.getSingleInput();
							i.removeOutput(c);
							c.removeAllInputs();
							for(DynamicComponent o : c.getOutputs()){
								o.removeInput(c);
								i.addOutput(o);
								o.addInput(i);
							}
							c.removeAllOutputs();
							pn.removeComponent(c);
						}

						/* END OF ADDITION */

					}else{
						// TRUE is the only input of this AND gate => the output of the gate is TRUE as well
						// and the gate can be removed, setting TRUE as input of all its outputs
						trueConstant.removeOutput(c);
						c.removeInput(trueConstant);
						for(DynamicComponent o : c.getOutputs()){
							o.removeInput(c);
							o.addInput(trueConstant);
							// If this component is not an output of TRUE yet, we add it to the set of components
							// to be checked in the next step. If it is already an output of TRUE it means that it
							// will be checked in this step already or that it has been already added to be checked
							// next.
							if(trueConstant.addOutput(o)){
								toCheckNextT.add(o);
							}
						}
						c.removeAllOutputs();
						pn.removeComponent(c);
					}
				}else if(c instanceof DynamicOr){
					// It means that the output of this OR is always TRUE, no matter the other inputs.
					for(DynamicComponent i : c.getInputs()){
						i.removeOutput(c);
					}
					c.removeAllInputs();
					for(DynamicComponent o : c.getOutputs()){
						o.removeInput(c);
						o.addInput(trueConstant);
						if(trueConstant.addOutput(o)){
							toCheckNextT.add(o);
						}
					}
					c.removeAllOutputs();
					pn.removeComponent(c);
				}else if(c instanceof DynamicNot){
					trueConstant.removeOutput(c);
					c.removeInput(trueConstant);
					for(DynamicComponent o : c.getOutputs()){
						o.removeInput(c);
						o.addInput(falseConstant);
						falseConstant.addOutput(o);
						toCheckF.add(o);
					}
					c.removeAllOutputs();
					pn.removeComponent(c);
				}else if(c instanceof DynamicTransition){
					if(c.getOutputs().size() == 0){
						pn.removeComponent(c);
						//throw new RuntimeException("Found a transition with no output! Something is wrong with the propnet structure!");
					}
					// ...else do nothing: cannot remove a transition from the propnet unless its corresponding
					// base is proved to be always TRUE or FALSE and thus gets removed together with the transition.
				}else if(c instanceof DynamicProposition){
					DynamicProposition p = (DynamicProposition) c;

					if(p.getPropositionType() == PROP_TYPE.INPUT){
						throw new RuntimeException("Input propositions cannot have inputs nor be always TRUE!");
					}

					// If the proposition is always TRUE connect all its outputs to true
					for(DynamicComponent o : c.getOutputs()){
						o.removeInput(c);
						o.addInput(trueConstant);
						// If this component is not an output of TRUE yet, we add it to the set of components
						// to be checked in the next step. If it is already an output of TRUE it means that it
						// will already be checked in this step or that it has been already added to be checked
						// next or that it has already been checked that it cannot be removed.
						if(trueConstant.addOutput(o)){
							toCheckNextT.add(o);
						}
					}
					c.removeAllOutputs();

					// If the proposition is BASE or OTHER it's safe to remove it
					if(p.getPropositionType() == PROP_TYPE.BASE || p.getPropositionType() == PROP_TYPE.OTHER){
						trueConstant.removeOutput(c);
						c.removeInput(trueConstant);
						pn.removeComponent(c);
					}


					/*
					switch(p.getPropositionType()){
					//case BASE:
						//throw new RuntimeException("Base propositions cannot have TRUE as input, but only transitions! When connecting a base to TRUE remember to transform it into an OTHER proposition!");
					case INPUT:

					case OTHER: case BASE:
						trueConstant.removeOutput(c);
						c.removeInput(trueConstant);
						for(DynamicComponent o : c.getOutputs()){
							o.removeInput(c);
							o.addInput(trueConstant);
							// If this component is not an output of TRUE yet, we add it to the set of components
							// to be checked in the next step. If it is already an output of TRUE it means that it
							// will already be checked in this step or that it has been already added to be checked
							// next or that it has already been checked that it cannot be removed.
							if(trueConstant.addOutput(o)){
								toCheckNextT.add(o);
							}
						}
						c.removeAllOutputs();
						pn.removeComponent(c);
						break;
					default:
						break;
					}*/
				}
			}

			//System.out.println("To check next true: " + toCheckNextT.size());

			toCheckT = toCheckNextT;
		}

		return toCheckT;
	}

	/**
	 * Checks all inputs of FALSE removing the ones that are useless until no more useless components are left.
	 *
	 * ATTENTION: this method is based on the assumption that whenever a LEGAL is connected to FALSE also the corresponding
	 * INPUT is and vice-versa. If a LEGAL is not connected to FALSE the corresponding INPUT is assumed to have no inputs.
	 *
	 * @param pn the propnet.
	 * @param trueConstant
	 * @param falseConstant
	 * @param toCheckT the outputs of TRUE that still must be checked to see if they can be removed. This collection
	 * is needed in case this method adds an output to the TRUE constant. In this case it must add it also to this
	 * collection to tell to the optimizeAwayTrue2() method to also check that component when removing useless always
	 * true components.
	 * @param toCheckF the outputs of FALSE that this method must checked to see if they can be removed. If an output
	 * of false cannot be removed it will be permanently removed from this list. An output will be either kept or removed
	 * until this collection will be empty.
	 * @return reference to the new collection containing the outputs of FALSE that still must be checked to see if they
	 * can be removed. After running this method this collection is empty, but new components might be added to it by the
	 * optimizeAwayTrue2() method.
	 */
	private static Set<DynamicComponent> optimizeAwayFalse2(DynamicPropNet pn, DynamicComponent trueConstant, DynamicComponent falseConstant,  Set<DynamicComponent> toCheckT, Set<DynamicComponent> toCheckF) {

		Set<DynamicComponent> toCheckNextF;

		while(!(toCheckF.isEmpty())){

			toCheckNextF = new HashSet<DynamicComponent>();

			for(DynamicComponent c : toCheckF){
				if(c instanceof DynamicConstant){
					throw new RuntimeException("Found a constant component having FALSE as input! Something is wrong with the propnet structure!");
				}else if(c instanceof DynamicAnd){
					// It means that the output of this AND is always FALSE, no matter the other inputs.
					for(DynamicComponent i : c.getInputs()){
						i.removeOutput(c);
					}
					c.removeAllInputs();
					for(DynamicComponent o : c.getOutputs()){
						o.removeInput(c);
						o.addInput(falseConstant);
						if(falseConstant.addOutput(o)){
							toCheckNextF.add(o);
						}
					}
					c.removeAllOutputs();
					pn.removeComponent(c);
				}else if(c instanceof DynamicOr){
					if(c.getInputs().size() > 1){
						// One input of OR is FALSE, but since there are other inputs, the output of
						// OR depends entirely on them => FALSE can be removed as input
						falseConstant.removeOutput(c);
						c.removeInput(falseConstant);

						/* ADDITION */
						// NOTE: if after removing the false constant as input the OR gate only has one input left
						// it is useless to keep this gate. We can just connect the single input directly to every
						// output of the gate.
						// NOTE that if this method is called on a propnet that has no OR gates with only one input,
						// the following code should ensure that also after the end of this method the propnet will
						// have no OR gates with only one input.
						if(c.getInputs().size() == 1){
							DynamicComponent i = c.getSingleInput();
							i.removeOutput(c);
							c.removeAllInputs();
							for(DynamicComponent o : c.getOutputs()){
								o.removeInput(c);
								i.addOutput(o);
								o.addInput(i);
							}
							c.removeAllOutputs();
							pn.removeComponent(c);
						}

						/* END OF ADDITION */
					}else{
						// FALSE is the only input of this OR gate => the output of the gate is FALSE as well
						// and the gate can be removed, setting FALSE as input of all its outputs
						falseConstant.removeOutput(c);
						c.removeInput(falseConstant);
						for(DynamicComponent o : c.getOutputs()){
							o.removeInput(c);
							o.addInput(falseConstant);
							// If this component is not an output of TRUE yet, we add it to the set of components
							// to be checked in the next step. If it is already an output of TRUE it means that it
							// will be checked in this step already or that it has been already added to be checked
							// next.
							if(falseConstant.addOutput(o)){
								toCheckNextF.add(o);
							}
						}
						c.removeAllOutputs();
						pn.removeComponent(c);
					}
				}else if(c instanceof DynamicNot){
					falseConstant.removeOutput(c);
					c.removeInput(falseConstant);
					for(DynamicComponent o : c.getOutputs()){
						o.removeInput(c);
						o.addInput(trueConstant);
						trueConstant.addOutput(o);
						toCheckT.add(o);
					}
					c.removeAllOutputs();
					pn.removeComponent(c);
				}else if(c instanceof DynamicTransition){
					if(c.getOutputs().size() == 0){
						pn.removeComponent(c);
						//throw new RuntimeException("Found a transition with no output! Something is wrong with the propnet structure!");
					}
					// ...else do nothing: cannot remove a transition from the propnet unless its corresponding
					// base is proved to be always TRUE or FALSE and thus gets removed together with the transition.
				}else if(c instanceof DynamicProposition){
					DynamicProposition p = (DynamicProposition) c;

					if(p.getPropositionType() == PROP_TYPE.INPUT){
						throw new RuntimeException("Input propositions cannot have inputs even if it's always FALSE!");
					}

					// If the proposition is always FALSE connect all its outputs to FALSE
					for(DynamicComponent o : c.getOutputs()){
						o.removeInput(c);
						o.addInput(falseConstant);
						// If this component is not an output of FALSE yet, we add it to the set of components
						// to be checked in the next step. If it is already an output of TRUE it means that it
						// will be already checked in this step or that it has been already added to be checked
						// next or that it has already been checked that it cannot be removed.
						if(falseConstant.addOutput(o)){
							toCheckNextF.add(o);
						}
					}
					c.removeAllOutputs();

					// If the proposition is a LEGAL we must look for the corresponding INPUT and remove it, connecting all its outputs to the FALSE constant.
					if(p.getPropositionType() == PROP_TYPE.LEGAL){
						int index = pn.getLegalPropositions().indexOf(p);
	        			if(index == -1){
	    					throw new RuntimeException("Found a non-indexed legal proposition! Something must have gone wrong with the propnet initialization!");
	        			}

	        			DynamicProposition correspondingInput = pn.getInputPropositions().get(index);

	        			// We are sure the input is input-less, thus we only take care of the outputs
	        			for(DynamicComponent o : correspondingInput.getOutputs()){
							o.removeInput(correspondingInput);
							o.addInput(falseConstant);
							// If this component is not an output of FALSE yet, we add it to the set of components
							// to be checked in the next step. If it is already an output of TRUE it means that it
							// will be already checked in this step or that it has been already added to be checked
							// next or that it has already been checked that it cannot be removed.
							if(falseConstant.addOutput(o)){
								toCheckNextF.add(o);
							}
						}

	        			correspondingInput.removeAllOutputs();

	        			pn.removeComponent(correspondingInput);

					}

					// If the proposition is not the TERMINAL proposition it's safe to remove it
					if(p.getPropositionType() != PROP_TYPE.TERMINAL){
						falseConstant.removeOutput(c);
						c.removeInput(falseConstant);
						pn.removeComponent(c);
					}

					/*switch(p.getPropositionType()){
					case BASE:
						throw new RuntimeException("Base propositions cannot have FALSE as input, but only transitions! When connecting a base to TRUE remember to transform it into an OTHER proposition!");
					case INPUT:
						throw new RuntimeException("Input propositions cannot have an input! When connecting an input to FALSE remember to transform it into an OTHER proposition!");
					case GOAL: // If a goal is always false we don't need it, we can remove it from the propnet as any OTHER proposition.
					case OTHER:
						falseConstant.removeOutput(c);
						c.removeInput(falseConstant);
						for(DynamicComponent o : c.getOutputs()){
							o.removeInput(c);
							o.addInput(falseConstant);
							// If this component is not an output of FALSE yet, we add it to the set of components
							// to be checked in the next step. If it is already an output of TRUE it means that it
							// will be already checked in this step or that it has been already added to be checked
							// next or that it has already been checked that it cannot be removed.
							if(falseConstant.addOutput(o)){
								toCheckNextF.add(o);
							}
						}
						c.removeAllOutputs();
						pn.removeComponent(c);
						break;
					default:
						break;
					}*/
				}
			}

			//System.out.println("To check next false: " + toCheckNextF.size());

			toCheckF = toCheckNextF;
		}

		return toCheckF;
	}


	public static boolean checkPropnetStructure(DynamicPropNet pn){

		boolean propnetOk = true;

		Map<GdlSentence, Integer> propNumbers = new HashMap<GdlSentence, Integer>();

		for(DynamicComponent c : pn.getComponents()){

			/* NOT FEASIBLE TO CHECK THIS IN A REASONABLE AMOUNT OF TIME FOR MOST GAMES:

			// Check that every input of the component references back the component as output
			for(DynamicComponent in : c.getInputs()){
				boolean correctInputReferences = false;
				for(DynamicComponent inout : in.getOutputs()){
					if(inout == c){
						correctInputReferences = true;
						break;
					}
				}
				if(!correctInputReferences){
					GamerLogger.log("PropStructureChecker", "Component " + c.getComponentType() + " is not referenced back by its input " + in.getComponentType() + ".");
					propnetOk = false;
				}
			}

			// Check that every output of the component references back the component as input
			for(DynamicComponent out : c.getOutputs()){
				boolean correctOutputReferences = false;
				for(DynamicComponent outin : out.getInputs()){
					if(outin == c){
						correctOutputReferences = true;
						break;
					}
				}
				if(!correctOutputReferences){
					GamerLogger.log("PropStructureChecker", "Component " + c.getComponentType() + " is not referenced back by its output " + out.getComponentType() + ".");
					propnetOk = false;
				}
			}
			*/


			// Check for each type of component if it has the correct inputs and outputs
			if(c instanceof DynamicProposition){

				DynamicProposition p = (DynamicProposition) c;

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
					}else if(!(p.getSingleInput() instanceof DynamicTransition)){
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
					/* A goal can have outputs!
					if(p.getOutputs().size() != 0){
						GamerLogger.log("PropStructureChecker", "Component " + p.getComponentType() + " has wrong number of outputs: " + p.getOutputs().size());
						propnetOk = false;
					}*/
					break;
				case TERMINAL:
					if(p.getInputs().size() != 1){
						GamerLogger.log("PropStructureChecker", "Component " + p.getComponentType() + " has wrong number of inputs: " + p.getInputs().size());
						propnetOk = false;
					}
					/* A terminal can have outputs!
					if(p.getOutputs().size() != 0){
						GamerLogger.log("PropStructureChecker", "Component " + p.getComponentType() + " has wrong number of outputs: " + p.getOutputs().size());
						propnetOk = false;
					}*/
					break;
				case INIT:
					if(p.getInputs().size() != 0){
						GamerLogger.log("PropStructureChecker", "Component " + p.getComponentType() + " has wrong number of inputs: " + p.getInputs().size());
						propnetOk = false;
					}
					break;
				case OTHER:
					if(p.getInputs().size() > 1){
						GamerLogger.log("PropStructureChecker", "Component " + p.getComponentType() + " has wrong number of inputs: " + p.getInputs().size());
						propnetOk = false;
					}

					if(p.getOutputs().size() == 0){
						GamerLogger.log("PropStructureChecker", "Component " + p.getComponentType() + " has no outputs and is useless.");
						propnetOk = false;
					}
					break;
				default:
					GamerLogger.log("PropStructureChecker", "Component " + p.getComponentType() + " has no PROP_TYPE assigned.");
					propnetOk = false;
					break;
				}
			}else if(c instanceof DynamicTransition){

				if(c.getInputs().size() != 1){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " doesn't have one and only one input. It has " + c.getInputs().size() + " inputs.");
					propnetOk = false;
				}

				if(c.getOutputs().size() != 1){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " doesn't have one and only one output. It has " + c.getOutputs().size() + " outputs.");
					propnetOk = false;
				}else if(!(c.getSingleOutput() instanceof DynamicProposition)){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " doesn't have a proposition as output. It has " + c.getSingleOutput().getComponentType() + " as output.");
					propnetOk = false;
				}else if(((DynamicProposition) c.getSingleOutput()).getPropositionType() != PROP_TYPE.BASE){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " doesn't have a base proposition as output. It has " + c.getSingleOutput().getComponentType() + " as output.");
					propnetOk = false;
				}

			}else if(c instanceof DynamicConstant){

				if(c.getInputs().size() != 0){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " has " + c.getInputs().size() + " inputs.");
					propnetOk = false;
				}

			}else if(c instanceof DynamicAnd){

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

			}else if(c instanceof DynamicOr){

				if(c.getInputs().size() == 1){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " is unnecessary since it only has one input: " + c.getSingleInput().getComponentType() + ".");
					propnetOk = false;
				}else if(c.getInputs().size() == 0){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " has no inputs: should be connected to a constant proposition.");
					propnetOk = false;

					/*
					String s = "[ ";
					for(DynamicComponent cc : c.getOutputs()){
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

			}else if(c instanceof DynamicNot){

				if(c.getInputs().size() > 1){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " has too many inputs: " + c.getInputs().size());
					propnetOk = false;
				}else if(c.getInputs().size() == 0){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " has no inputs: should be connected to a constant proposition!");
					propnetOk = false;
				}

				if(c.getOutputs().size() == 0){
					GamerLogger.log("PropStructureChecker", "The component " + c.getComponentType() + " is unnecessary since it has no outputs!");
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
		List<DynamicProposition> inputs = pn.getInputPropositions();
		List<DynamicProposition> legals = pn.getLegalPropositions();

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

