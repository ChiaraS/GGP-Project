package csironi.ggp.course;

import java.util.List;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.implementation.propnet.ForwardInterruptingPropNetStateMachine;

public class ProvaPropNetStateMachine {

	public static void main(String[] args) {
		String BeLdescription = "( ( role robot ) ( base p ) ( base q ) ( base r ) ( base 1 ) ( base 2 ) ( base 3 ) ( base 4 ) ( base 5 ) ( base 6 ) ( base 7 ) ( input robot a ) ( input robot b ) ( input robot c ) ( init 1 ) ( legal robot a ) ( legal robot b ) ( legal robot c ) ( <= ( next p ) ( does robot a ) ( not ( true p ) ) ) ( <= ( next p ) ( does robot b ) ( true q ) ) ( <= ( next p ) ( does robot c ) ( true p ) ) ( <= ( next q ) ( does robot a ) ( true q ) ) ( <= ( next q ) ( does robot b ) ( true p ) ) ( <= ( next q ) ( does robot c ) ( true r ) ) ( <= ( next r ) ( does robot a ) ( true r ) ) ( <= ( next r ) ( does robot b ) ( true r ) ) ( <= ( next r ) ( does robot c ) ( true q ) ) ( <= ( next ?y ) ( true ?x ) ( successor ?x ?y ) ) ( <= ( goal robot 100 ) ( true p ) ( true q ) ( true r ) ) ( <= ( goal robot 0 ) ( not ( true p ) ) ) ( <= ( goal robot 0 ) ( not ( true q ) ) ) ( <= ( goal robot 0 ) ( not ( true r ) ) ) ( <= terminal ( true p ) ( true q ) ( true r ) ) ( <= terminal ( true 7 ) ) ( successor 1 2 ) ( successor 2 3 ) ( successor 3 4 ) ( successor 4 5 ) ( successor 5 6 ) ( successor 6 7 ) )";

		Game BeLGame = Game.createEphemeralGame(BeLdescription);

		List<Gdl> BeLList = BeLGame.getRules();

		ForwardInterruptingPropNetStateMachine m = new ForwardInterruptingPropNetStateMachine();
		m.initialize(BeLList);

		MachineState initialState = m.getInitialState();

		if(initialState == null){
			System.out.println("Cacchiarola!");
		}else{
			System.out.println("Initial state: ");
			System.out.println(initialState);
		}

	}

}
