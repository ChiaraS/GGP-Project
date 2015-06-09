/**
 *
 */
package csironi.ggp.course;

import java.util.List;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.propnet.architecture.PropNet;
import org.ggp.base.util.propnet.factory.OptimizingPropNetFactory;

/**
 * @author C.Sironi
 *
 */
public class ProvaPropnet {

	public static void main(String []args){

		String incompleteDescription = " ( ( role xplayer ) ( role oplayer ) ( init ( cell 1 1 b ) ) ( init ( cell 1 2 b ) ) ( init ( cell 1 3 b ) ) ( init ( cell 2 1 b ) ) ( init ( cell 2 2 b ) ) ( init ( cell 2 3 b ) ) ( init ( cell 3 1 b ) ) ( init ( cell 3 2 b ) ) ( init ( cell 3 3 b ) ) ( init ( control xplayer ) ) ( <= ( next ( cell ?m ?n x ) ) ( does xplayer ( mark ?m ?n ) ) ( true ( cell ?m ?n b ) ) ) ( <= ( next ( cell ?m ?n o ) ) ( does oplayer ( mark ?m ?n ) ) ( true (cell ?m ?n b ) ) ) ( <= ( next ( cell ?m ?n ?w ) ) ( true ( cell ?m ?n ?w ) ) ( distinct ?w b ) ) ( <= ( next ( cell ?m ?n b ) ) ( does ?w ( mark ?j ?k ) ) ( true ( cell ?m ?n b ) ) ( or ( distinct ?m ?j ) ( distinct ?n ?k ) ) ) ( <= ( next ( control xplayer ) ) ( true ( control oplayer ) ) ) ( <= ( next ( control oplayer ) ) ( true ( control xplayer ) ) ) ( <= ( row ?m ?x ) ( true ( cell ?m 1 ?x ) ) ( true ( cell ?m 2 ?x ) ) ( true ( cell ?m 3 ?x ) ) ) ( <= ( column ?n ?x ) ( true ( cell 1 ?n ?x ) ) ( true ( cell 2 ?n ?x ) ) ( true ( cell 3 ?n ?x ) ) ) ( <= ( diagonal ?x ) ( true ( cell 1 1 ?x ) ) ( true ( cell 2 2 ?x ) ) ( true ( cell 3 3 ?x ) ) ) ( <= ( diagonal ?x ) ( true ( cell 1 3 ?x ) ) ( true ( cell 2 2 ?x ) ) ( true ( cell 3 1 ?x ) ) ) ( <= ( line ?x ) ( row ?m ?x ) ) ( <= ( line ?x ) ( column ?m ?x ) ) ( <= ( line ?x ) ( diagonal ?x ) ) ( <= open ( true ( cell ?m ?n b ) ) ) ( <= ( legal ?w ( mark ?x ?y ) ) ( true ( cell ?x ?y b ) ) ( true ( control ?w ) ) ) ( <= ( legal xplayer noop ) ( true ( control oplayer ) ) ) ( <= ( legal oplayer noop ) ( true ( control xplayer ) ) ) ( <= ( goal xplayer 100 ) ( line x ) ) ( <= ( goal xplayer 50 ) ( not ( line x ) ) ( not ( line o ) ) ( not open ) ) ( <= ( goal xplayer 0 ) ( line o ) ) ( <= ( goal oplayer 100 ) ( line o ) ) ( <= ( goal oplayer 50 ) ( not ( line x ) ) ( not ( line o ) ) ( not open ) ) ( <= ( goal oplayer 0 ) ( line x ) ) ( <= terminal ( line x ) ) ( <= terminal ( line o ) ) ( <= terminal ( not open ) ) ) ";

		String completeDescription = " ( ( role xplayer ) ( role oplayer ) ( index 1 ) ( index 2 ) ( index 3 ) ( <= ( base ( cell ?x ?y b ) ) ( index ?x ) ( index ?y ) ) ( <= ( base ( cell ?x ?y x ) ) ( index ?x ) ( index ?y ) ) ( <= ( base ( cell ?x ?y o ) ) ( index ?x ) ( index ?y ) ) ( <= ( base ( control ?p ) )	( role ?p ) ) ( <= ( input ?p ( mark ?x ?y ) ) ( index ?x ) ( index ?y ) ( role ?p ) ) ( <= ( input ?p noop ) ( role ?p ) ) ( init ( cell 1 1 b ) ) ( init ( cell 1 2 b ) ) ( init ( cell 1 3 b ) ) ( init ( cell 2 1 b ) ) ( init ( cell 2 2 b ) ) ( init ( cell 2 3 b ) ) ( init ( cell 3 1 b ) ) ( init ( cell 3 2 b ) ) ( init ( cell 3 3 b ) ) ( init ( control xplayer ) ) ( <= ( next ( cell ?m ?n x ) ) ( does xplayer ( mark ?m ?n ) ) ( true ( cell ?m ?n b ) ) ) ( <= ( next ( cell ?m ?n o ) ) ( does oplayer ( mark ?m ?n ) ) ( true ( cell ?m ?n b ) ) ) ( <= ( next ( cell ?m ?n ?w ) ) ( true ( cell ?m ?n ?w ) ) ( distinct ?w b ) ) ( <= ( next ( cell ?m ?n b ) ) ( does ?w ( mark ?j ?k ) ) ( true ( cell ?m ?n b ) ) ( or ( distinct ?m ?j ) ( distinct ?n ?k ) ) ) ( <= ( next ( control xplayer ) ) ( true ( control oplayer ) ) ) ( <= ( next ( control oplayer ) ) ( true ( control xplayer ) ) ) ( <= ( row ?m ?x ) ( true ( cell ?m 1 ?x ) ) ( true ( cell ?m 2 ?x ) ) ( true ( cell ?m 3 ?x ) ) ) ( <= ( column ?n ?x ) ( true ( cell 1 ?n ?x ) ) ( true ( cell 2 ?n ?x ) ) ( true ( cell 3 ?n ?x ) ) ) ( <= ( diagonal ?x ) ( true ( cell 1 1 ?x ) ) ( true ( cell 2 2 ?x ) ) ( true ( cell 3 3 ?x ) ) ) ( <= ( diagonal ?x ) ( true ( cell 1 3 ?x ) ) ( true ( cell 2 2 ?x ) ) ( true ( cell 3 1 ?x ) ) ) ( <= ( line ?x ) ( row ?m ?x ) ) ( <= ( line ?x ) ( column ?m ?x ) ) ( <= ( line ?x ) ( diagonal ?x ) ) ( <= open ( true ( cell ?m ?n b ) ) ) ( <= ( legal ?w ( mark ?x ?y ) ) ( true ( cell ?x ?y b ) ) ( true ( control ?w ) ) ) ( <= ( legal xplayer noop ) ( true ( control oplayer ) ) ) ( <= ( legal oplayer noop ) ( true ( control xplayer ) ) ) ( <= ( goal xplayer 100 ) ( line x ) ) ( <= ( goal xplayer 50 ) ( not ( line x ) ) ( not ( line o ) ) ( not open ) ) ( <= ( goal xplayer 0 ) ( line o ) ) ( <= ( goal oplayer 100 ) ( line o ) ) ( <= ( goal oplayer 50 ) ( not ( line x ) ) ( not ( line o ) ) ( not open ) ) ( <= ( goal oplayer 0 ) ( line x ) ) ( <= terminal ( line x ) ) ( <= terminal ( line o ) ) ( <= terminal ( not open ) ) ) ";

		System.out.println(incompleteDescription);
		System.out.println(completeDescription);

		Game incompleteGame = Game.createEphemeralGame(incompleteDescription);
		Game completeGame = Game.createEphemeralGame(completeDescription);

		List<Gdl> incompleteList = incompleteGame.getRules();
		List<Gdl> completeList = completeGame.getRules();

		System.out.println(incompleteList.toString());
		System.out.println(completeList.toString());

		PropNet incompletePropNet = null;
		PropNet completePropNet = null;

		try {
			incompletePropNet = OptimizingPropNetFactory.create(incompleteList, true);
			completePropNet = OptimizingPropNetFactory.create(completeList, true);
		} catch (InterruptedException e) {
			System.out.println("Uh oh");
		}

		String incompleteString = null;
		String completeString = null;

		if(incompletePropNet != null || completePropNet != null){
			incompleteString = incompletePropNet.toString();
			completeString = completePropNet.toString();
		}

		System.out.println(incompleteString);
		System.out.println(completeString);

		if(incompleteString.equals(completeString)){
			System.out.println("WOW");
		}
	}
}
