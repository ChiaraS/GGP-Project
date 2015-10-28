package org.ggp.base.util.propnet.creationManager;

import java.util.List;
import java.util.Map;

import org.apache.lucene.util.OpenBitSet;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.propnet.architecture.externalizedState.ExternalizedStateComponent;
import org.ggp.base.util.propnet.architecture.externalizedState.ExternalizedStatePropNet;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateAnd;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateNot;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateOr;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateProposition;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateProposition.PROP_TYPE;
import org.ggp.base.util.propnet.architecture.externalizedState.components.ExternalizedStateTransition;
import org.ggp.base.util.propnet.factory.ExternalizedStatePropnetFactory;
import org.ggp.base.util.propnet.state.ExternalPropnetState;
import org.ggp.base.util.statemachine.Role;

/**
 * This class takes care of the followings:
 *
 * 1. Create the propNet structure;
 * 2. Optimize the propNet structure (e.g. remove redundant components, shrink
 *    the propNet structure...);
 * 3. Initialize a consistent propNet state (i.e. assign to every proposition a
 * 	  truth value that's consistent with the value of its inputs). Note that this
 * 	  state will be memorized externally and not in each component of the propNet.
 *
 * This class tries to create the propNet. If it fails (i.e. gets interrupted before
 * the propNet has been completely created), the propnet and its external state will
 * be set to null. If it manages to build the propnet in time, it will try to
 * incrementally optimize it until it is interrupted. In this case, when it will be
 * interrupted, the propNet parameter will be set to the last completed optimization
 * of the propnet, so that it can be used and won't be in an inconsistent state.
 * The propnet state will also be initialized accordingly.
 *
 * @author C.Sironi
 *
 */
public class ExternalStatePropnetCreationManager extends Thread{

	private List<Gdl> description;

	private long timeout;

	private ExternalizedStatePropNet propNet;

	private long propNetConstructionTime;

	private long totalInitTime;

	private ExternalPropnetState initialPropnetState;

	public ExternalStatePropnetCreationManager(List<Gdl> description, long timeout) {
		this.description = description;
		this.timeout = timeout;
	}

	@Override
	public void run(){

		// TODO: use the timeout to decide if it is worth trying another optimization
		// or if there probably is not enough time and we don't want to risk taking too
		// long to interrupt in case the time was not enough.

		// 1. Create the propnet.
		long startTime = System.currentTimeMillis();

    	try{
    		this.propNet = ExternalizedStatePropnetFactory.create(description);
    	}catch(InterruptedException e){
    		GamerLogger.logError("PropnetManager", "Propnet creation interrupted!");
    		GamerLogger.logStackTrace("PropnetManager", e);
    		this.propNet = null;
    		this.initialPropnetState = null;
    		this.propNetConstructionTime = -1;
    		this.totalInitTime = System.currentTimeMillis() - startTime;
    		return;
    	}
    	// Compute the time taken to construct the propnet
    	this.propNetConstructionTime = System.currentTimeMillis() - startTime;
		GamerLogger.log("StateMachine", "[Propnet Creator] Propnet creation done. It took " + this.propNetConstructionTime + "ms.");

		System.out.println("Propnet has: " + this.propNet.getSize() + " COMPONENTS, " + this.propNet.getNumPropositions() + " PROPOSITIONS, " + this.propNet.getNumLinks() + " LINKS.");
		System.out.println("Propnet has: " + this.propNet.getNumAnds() + " ANDS, " + this.propNet.getNumOrs() + " ORS, " + this.propNet.getNumNots() + " NOTS.");
		System.out.println("Propnet has: " + this.propNet.getNumBases() + " BASES, " + this.propNet.getNumTransitions() + " TRANSITIONS.");
		System.out.println("Propnet has: " + this.propNet.getNumInputs() + " INPUTS, " + this.propNet.getNumLegals() + " LEGALS.");
		System.out.println("Propnet has: " + this.propNet.getNumGoals() + " GOALS.");
		System.out.println("Propnet has: " + this.propNet.getNumInits() + " INITS, " + this.propNet.getNumTerminals() + " TERMINALS.");




		/* Check if manager has been interrupted between creation and initialization of the propnet.
		 * In this case the propnet structure has been completely created but there is no time for
		 * initialization of the corresponding state. Use this check or not? If not it means that
		 * whenever the manager gets interrupted, if there is a completed version of the propnet
		 * structure available the corresponding state will for sure be initialized so the propnet
		 * can be used. Note that there is a tradeoff between having the guarantee that whenever a
		 * propnet structure is available we also have the corresponding state and having guarantee
		 * taht the player will not time out while getting ready to play.
		try{
			ConcurrencyUtils.checkForInterruption();
		}catch(InterruptedException e){
			GamerLogger.logError("PropnetManager", "Manager interrupted before ropnet state initialization!");
    		GamerLogger.logStackTrace("PropnetManager", e);
    		this.propNet = null;
    		this.initialPropnetState = null;
    		this.propNetConstructionTime = -1;
    		return;
		}
		*/
		this.computeInitialPropNetState();

		this.totalInitTime = System.currentTimeMillis() - startTime;
	}

	/**
	 * This method creates an initial consistent external state for the propnet.
	 * The external state contains the truth values for all the propositions in the
	 * propnet. These values are initialized such that the value of each component
	 * is consistent with the value(s) of its input(s). Moreover, this initial state
	 * for the propnet is set to correspond to the initial state of the game (i.e.
	 * the base propositions that are true by init are set to true, while all other
	 * base propositions are set to false).
	 */
	private void computeInitialPropNetState(){
		List<ExternalizedStateProposition> basePropositions = this.propNet.getBasePropositions();
		OpenBitSet initialState = new OpenBitSet(basePropositions.size());
		OpenBitSet nextState = new OpenBitSet(basePropositions.size());

		// TODO: this assumes that the propnet contains the exact same number of base propositions and
		// transitions, thus when removing a base proposition always make sure to also remove the
		// corresponding transition.
		for(int i = 0; i < basePropositions.size(); i++){
			ExternalizedStateProposition p = basePropositions.get(i);
			p.setIndex(i);
			p.getSingleInput().setIndex(i);
			// If it's a base proposition true in the initial state, set it to true in the bit array
			// representing the initial state.
			if(((ExternalizedStateTransition) p.getSingleInput()).isDependingOnInit()){
				initialState.set(i);
			}
		}

		List<ExternalizedStateProposition> inputPropositions = this.propNet.getInputPropositions();
		int i = 0;
		for(ExternalizedStateProposition p : inputPropositions){
			p.setIndex(i);
			i++;
		}

		OpenBitSet currentJointMove = new OpenBitSet(inputPropositions.size());

		this.propNet.getTerminalProposition().setIndex(0);

		List<Role> roles = this.propNet.getRoles();

		i = 1;

		Map<Role, List<ExternalizedStateProposition>> goalsPerRole = this.propNet.getGoalsPerRole();
		int[] firstGoalIndices = new int[roles.size()+1];

		int j;
		for(j = 0; j < roles.size(); j++){
			firstGoalIndices[j] = i;
			Role r = roles.get(j);
			for(ExternalizedStateProposition roleGoals : goalsPerRole.get(r)){
				roleGoals.setIndex(i);
				i++;
			}
		}
		firstGoalIndices[j] = i;

		Map<Role, List<ExternalizedStateProposition>> legalsPerRole = this.propNet.getLegalsPerRole();
		int[] firstLegalIndices = new int[roles.size()+1];

		for(j = 0; j < roles.size(); j++){
			firstLegalIndices[j] = i;
			Role r = roles.get(j);
			for(ExternalizedStateProposition roleLegals : legalsPerRole.get(r)){
				roleLegals.setIndex(i);
				i++;
			}
		}
		firstLegalIndices[j] = i;

		for(ExternalizedStateComponent c : this.propNet.getComponents()){
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

		List<ExternalizedStateComponent> andOrGates = this.propNet.getAndOrGates();

		int l = 0;
		int[] andOrGatesValues = new int[andOrGates.size()];
		for(ExternalizedStateComponent c : andOrGates){
			if(c instanceof ExternalizedStateAnd){
				andOrGatesValues[l] = Integer.MAX_VALUE - c.getInputs().size() + 1;
			}else if(c instanceof ExternalizedStateOr){
				andOrGatesValues[l] = Integer.MAX_VALUE;
			}
			c.setIndex(l);
			l++;
		}

		this.initialPropnetState = new ExternalPropnetState(initialState, nextState, currentJointMove, firstGoalIndices, firstLegalIndices, andOrGatesValues, otherComponents);

		for(ExternalizedStateComponent c : this.propNet.getComponents()){
			c.imposeConsistency(this.initialPropnetState);
		}
	}

	/**
	 * Getter method.
	 *
	 * @return the object representing the structure of the propnet.
	 */
	public ExternalizedStatePropNet getPropnet(){
		return this.propNet;
	}

	public long getPropnetConstructionTime(){
		return this.propNetConstructionTime;
	}

	public long getTotalInitTime(){
		return this.totalInitTime;
	}

	public ExternalPropnetState getInitialPropnetState(){
		return this.initialPropnetState.clone();
	}

}
