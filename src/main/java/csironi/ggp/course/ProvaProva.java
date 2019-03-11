package csironi.ggp.course;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.LogTreeNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.MctsJointMove;
import org.ggp.base.util.statemachine.cache.RefactoredTtlCache;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;

public class ProvaProva {

	public static void main(String[] args){
		//for (Class<?> gamerClass : ProjectSearcher.INTERNAL_PROPNET_GAMERS.getConcreteClasses()) {
		//	System.out.println(gamerClass.getSimpleName());
		//}

		int xMin = 0;
		int x = 10;
		double yMax = 5;
		double d = 0.5;

		String s = xMin + " " + x + " " + (-yMax-d) + " " + (yMax+d);

		System.out.println(s);

		List<Move> jointMove1 = new ArrayList<Move>();

		jointMove1.add(new CompactMove(2));
		jointMove1.add(new CompactMove(6));

		List<Move> jointMove2 = new ArrayList<Move>();

		jointMove2.add(new CompactMove(6));
		jointMove2.add(new CompactMove(2));

		List<Move> jointMove3 = new ArrayList<Move>();

		jointMove3.add(new CompactMove(2));
		jointMove3.add(new CompactMove(6));

		MctsJointMove jm1 = new MctsJointMove(jointMove1);
		MctsJointMove jm2 = new MctsJointMove(jointMove2);
		MctsJointMove jm3 = new MctsJointMove(jointMove3);

		//LogTreeNode node = new LogTreeNode(1,2);
		//node.addChild(jm1, new LogTreeNode(2,2));

		System.out.println();

		//for(Entry<MctsJointMove,LogTreeNode> jm : node.getChildren().entrySet()) {
		//	System.out.println("( " + jm.getKey() + ", " + jm.getValue() + ")");
		//}

		//node.addChild(jm2, new LogTreeNode(3,2));

		System.out.println();

		//for(Entry<MctsJointMove,LogTreeNode> jm : node.getChildren().entrySet()) {
		//	System.out.println("( " + jm.getKey() + ", " + jm.getValue() + ")");
		//}

		//node.addChild(jm3, new LogTreeNode(4,2));

		System.out.println();

		//for(Entry<MctsJointMove,LogTreeNode> jm : node.getChildren().entrySet()) {
		//	System.out.println("( " + jm.getKey() + ", " + jm.getValue() + ")");
		//}







		RefactoredTtlCache<String, String> theCache = new RefactoredTtlCache<String, String>(1);

		System.out.println("Empty? " + theCache.isEmpty());

		theCache.put("Chiara", "CRF");

		System.out.println("Empty? " + theCache.isEmpty());

		System.out.println("Old value for Annalisa: " + theCache.put("Annalisa", "NLS"));
		System.out.println("Old value for Annalisa: " + theCache.put("Annalisa", "NNL"));
		theCache.put("Luisa", "LSH");
		theCache.put("Federico", "FDR");
		theCache.put("Joost", "JST");

		System.out.println("Luisa? " + theCache.containsKey("Luisa"));

		System.out.println("Luisa = " + theCache.get("Luisa"));

		System.out.println("FDR? " + theCache.containsValue("FDR"));

		System.out.println(theCache);

		theCache.prune();

		System.out.println(theCache);

		theCache.get("Annalisa");
		theCache.get("Federico");
		theCache.get("Luisa");

		System.out.println(theCache);

		theCache.prune();

		System.out.println(theCache);

		theCache.prune();

		System.out.println(theCache);

	}

}
