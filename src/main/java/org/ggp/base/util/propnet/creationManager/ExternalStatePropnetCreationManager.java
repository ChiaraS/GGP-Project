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
 * @author C.Sironi
 *
 */
public class ExternalStatePropnetCreationManager {

	private ExternalizedStatePropNet propNet;

	private long propNetConstructionTime;

	private ExternalPropnetState initialPropnetState;

	public ExternalStatePropnetCreationManager() {
		// TODO Auto-generated constructor stub
	}

	public void createOptimizeInitializePropnet(List<Gdl> description, long timeout){

		// 1. Create the propnet.
		long startTime = System.currentTimeMillis();

    	try{
    		this.propNet = ExternalizedStatePropnetFactory.create(description);
    	}catch(InterruptedException e){
    		/* TODO
    		GamerLogger.logError("StateMachine", "[Propnet] Propnet creation interrupted!");
    		GamerLogger.logStackTrace("StateMachine", e);
    		throw new StateMachineInitializationException(e);
    		*/
    	}
    	// Compute the time taken to construct the propnet
    	this.propNetConstructionTime = System.currentTimeMillis() - startTime;
		GamerLogger.log("StateMachine", "[Propnet Creator] Propnet creation done. It took " + (this.propNetConstructionTime) + "ms.");

		this.computeInitialPropNetState();
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

	public ExternalPropnetState getInitialPropnetState(){
		return this.initialPropnetState.clone();
	}

}
