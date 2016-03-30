package csironi.ggp.course.experiments.propnet;

import java.util.List;

import org.ggp.base.util.gdl.grammar.Gdl;

public class PropnetTester {

	public PropnetTester() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args){

	}

	private static void buildPropnet(List<Gdl> description, long givenInitTime){



		// If we are here it means that the manager stopped running. We must check if it has created a usable propnet or not.

		//ImmutablePropNet propnet = manager.getImmutablePropnet();
		//ImmutableSeparatePropnetState propnetState = manager.getInitialPropnetState();

		// Create the state machine giving it the propnet and the propnet state.
		// NOTE that if any of the two is null, it means that the propnet creation/initialization went wrong
		// and this will be detected by the state machine during initialization.
	    //thePropnetMachine = new SeparateInternalPropnetStateMachine(propnet, propnetState);

	}

}
