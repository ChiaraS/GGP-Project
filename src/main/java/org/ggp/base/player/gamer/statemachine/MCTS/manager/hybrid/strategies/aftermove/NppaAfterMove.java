package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.structures.NGramTreeNode;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.structures.PpaInfo;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.Move;
import org.ggp.base.util.statemachine.structure.compact.CompactMove;

import csironi.ggp.course.utils.MyPair;

public class NppaAfterMove extends AfterMoveStrategy {

	private List<NGramTreeNode<PpaInfo>> nppaStatistics;

	private double decayFactor;

	private boolean logNppaStats;

	public NppaAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.decayFactor = gamerSettings.getDoublePropertyValue("AfterMoveStrategy" + id + ".decayFactor");

		this.logNppaStats = gamerSettings.getBooleanPropertyValue("AfterMoveStrategy" + id + ".logNppaStats");

	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.nppaStatistics = sharedReferencesCollector.getNppaStatistics();
	}

	@Override
	public void clearComponent() {
		// Do nothing (because the NPPA statistics will be already cleared by the strategy that populates them,
		// i.e. the after simulation strategy).
	}

	@Override
	public void setUpComponent() {
		// Do nothing
	}


	@Override
	public void afterMoveActions() {

		if(this.logNppaStats){
			this.logNppaStats();
		}

		if(this.decayFactor == 0.0){ // If we want to throw away everything, we just clear all the stats. No need to iterate.
			for(int roleIndex = 0; roleIndex < this.nppaStatistics.size(); roleIndex++){
				this.nppaStatistics.get(roleIndex).clear();
			}
		}else if(this.decayFactor != 1.0){ // If the decay factor is 1.0 we keep everything without modifying anything.
			// Decrease statistics in each n-gram node, then check if the visits became 0. If so, check if the node
			// still has children, thus the longer n-grams still have non-zero visits (note that this should never happen,
			// i.e a shorter n-gram always has at least the same visits as the longer n-grams that contain it. However, because
			// we are rounding, it could still happen. If an n-gram with 0 visits has no children, remove the statistic.
			// -> This means that if the n-gram will be explored again in the next step of
			// the search, a new entry for the move will be created. However it's highly likely that the
			// number of visits decreases to 0 because this move is never explored again because the real
			// game ended up in a part of the tree where this move will not be legal anymore. In this case
			// we won't keep around statistics that we will never use again, but we risk also to end up
			// removing the statistic object for a move that will be explored again during the next steps
			// and we will have to recreate the object (in this case we'll consider as garbage an object
			// that instead we would have needed again).
			for(int roleIndex = 0; roleIndex < this.nppaStatistics.size(); roleIndex++){
				this.decayStatistics(this.nppaStatistics.get(roleIndex).getNextMoveNodes());
			}
		}

		if(this.logNppaStats){
			this.logNppaStats();
		}

		//this.gameStep++;

		/*
		String toPrint = "MastStats[";
		if(this.mastStatistics == null){
			toPrint += "null]\n";
		}else{
			for(Entry<Move, MoveStats> mastStatistic : this.mastStatistics.entrySet()){
				toPrint += "\n  MOVE(" + mastStatistic.getKey().toString() + "), " + mastStatistic.getValue().toString();
			}
			toPrint += "  ]\n";
		}

		toPrint += "]";

		System.out.println(toPrint);
		*/

		// VERSION 2: decrease and don't check anything.
		/*
		for(MoveStats m : this.mastStatistics.values()){
			m.decreaseByFactor(this.decayFactor);
		}
		*/

	}

	public void decayStatistics(Map<Move, NGramTreeNode<PpaInfo>> nGramStatistics){

		// Decrease the weight. If the weight is now 0, remove the entry form the map. In this way, we can get
		// rid of weights for moves that will not be legal anymore in the game and are only occupying space.
		// If they are not used anymore, over time the decay will make their weight 0 and thus we will remove
		// them to free space. If the weight becomes zero for a coincidence and the move can still be legal,
		// its weight will be re-added the next time the move is visited.

		if(!nGramStatistics.isEmpty()){
			Iterator<Entry<Move,NGramTreeNode<PpaInfo>>> iterator;
			Entry<Move,NGramTreeNode<PpaInfo>> theEntry;
			iterator = nGramStatistics.entrySet().iterator();
			while(iterator.hasNext()){
				theEntry = iterator.next();
				this.decayStatistics(theEntry.getValue().getNextMoveNodes());
				theEntry.getValue().getStatistic().decayWeight(this.decayFactor, this.gameDependentParameters.getTotIterations());
				if(theEntry.getValue().getStatistic().getWeight() == 0 &&
						theEntry.getValue().hasNoChildren()){
					iterator.remove();
				}
			}
		}
	}

	/**
	 * Prints all the N-grams statistics memorized in the nppaStatistics variable in order of role and
	 * length. It uses a list with all the moves in the n-gram to which the statistics in each
	 * node correspond. This list is extended with each of the moves memorized in a node
	 * and the corresponding statistics are printed. Then, this is repeated on all the children of
	 * the current n-gram node. Note that this method visits the tree with BFS,
	 *
	 * @return
	 */
	private void logNppaStats(){

		String toLog = "STEP=;" + this.gameDependentParameters.getGameStep() + ";\n";

		if(this.nppaStatistics == null){
			for(int roleIndex = 0; roleIndex < this.gameDependentParameters.getNumRoles(); roleIndex++){
				toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex)) + ";\n");
				toLog += "null;\n";
			}
		}else{
			List<MyPair<List<Move>,NGramTreeNode<PpaInfo>>> currentLevel;
			List<MyPair<List<Move>,NGramTreeNode<PpaInfo>>> nextLevel;
			for(int roleIndex = 0; roleIndex < this.nppaStatistics.size(); roleIndex++){
				toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex)) + ";\n");
				currentLevel = new ArrayList<MyPair<List<Move>,NGramTreeNode<PpaInfo>>>();
				nextLevel = new ArrayList<MyPair<List<Move>,NGramTreeNode<PpaInfo>>>();
				// Add the 1-grams to the current level
				for(Entry<Move,NGramTreeNode<PpaInfo>> nGramStats : this.nppaStatistics.get(roleIndex).getNextMoveNodes().entrySet()){
					List<Move> nGram = new ArrayList<Move>();
					nGram.add(nGramStats.getKey());
					currentLevel.add(new MyPair<List<Move>,NGramTreeNode<PpaInfo>>(nGram,nGramStats.getValue()));
				}
				int nGramLength = 1;

				while(!currentLevel.isEmpty()){
					toLog += ("N_GRAM_LENGTH=;" + nGramLength + ";\n");
					for(MyPair<List<Move>,NGramTreeNode<PpaInfo>> nGramTreeNode: currentLevel){
						toLog += ("MOVE=;" + this.getNGramString(nGramTreeNode.getFirst()) + ";" + nGramTreeNode.getSecond().getStatistic() + ";\n");
						for(Entry<Move,NGramTreeNode<PpaInfo>> nGramStats : nGramTreeNode.getSecond().getNextMoveNodes().entrySet()){
							List<Move> nGram = new ArrayList<Move>(nGramTreeNode.getFirst());
							nGram.add(nGramStats.getKey());
							nextLevel.add(new MyPair<List<Move>,NGramTreeNode<PpaInfo>>(nGram,nGramStats.getValue()));
						}

					}
					currentLevel = new ArrayList<MyPair<List<Move>,NGramTreeNode<PpaInfo>>>(nextLevel);
					nextLevel.clear();
					nGramLength++;
				}
			}
		}

		toLog += "\n";

		GamerLogger.log(GamerLogger.FORMAT.CSV_FORMAT, "NstStats", toLog);

	}

	private String getNGramString(List<Move> reversedNGram){
		String nGram = "]";

		for(Move m : reversedNGram){
			nGram = this.gameDependentParameters.getTheMachine().convertToExplicitMove(m) + " " + nGram;
		}

		nGram = "[ " + nGram;

		return nGram;
	}

	@Override
	public String getComponentParameters(String indentation) {

		String nppaStatisticsString;

		if(this.nppaStatistics != null){
			nppaStatisticsString = "[ ";

			int roleIndex = 0;
			for(NGramTreeNode<PpaInfo> roleNppaStats : this.nppaStatistics){
				nppaStatisticsString += (roleNppaStats == null ? "null " : "Tree" + roleIndex + " "); // this only counts the first level of the n-grams tree, i.e. only 1-grams.
				roleIndex++;
			}

			nppaStatisticsString += "]";

		}else{
			nppaStatisticsString = "null";
		}

		String params = indentation + "DECAY_FACTOR = " + this.decayFactor +
				indentation + "LOG_NPPA_STATS = " + this.logNppaStats +
				indentation + "nppa_statistics = " + nppaStatisticsString;

		return params;

	}


	/* CODE FOR DEBUGGING */
	public static void logNppaStats(List<NGramTreeNode<PpaInfo>> nppaStatistics){

		String toLog = "STEP=;" + 1 + ";\n";

		if(nppaStatistics == null){
			//for(int roleIndex = 0; roleIndex < nppaStatistics.size(); roleIndex++){
				toLog += ("ROLE=;" + 0 + ";\n");
				toLog += "null;\n";
			//}
		}else{
			List<MyPair<List<Move>,NGramTreeNode<PpaInfo>>> currentLevel;
			List<MyPair<List<Move>,NGramTreeNode<PpaInfo>>> nextLevel;
			for(int roleIndex = 0; roleIndex < nppaStatistics.size(); roleIndex++){
				toLog += ("ROLE=;" + roleIndex + ";\n");
				currentLevel = new ArrayList<MyPair<List<Move>,NGramTreeNode<PpaInfo>>>();
				nextLevel = new ArrayList<MyPair<List<Move>,NGramTreeNode<PpaInfo>>>();
				// Add the 1-grams to the current level
				for(Entry<Move,NGramTreeNode<PpaInfo>> nGramStats : nppaStatistics.get(roleIndex).getNextMoveNodes().entrySet()){
					List<Move> nGram = new ArrayList<Move>();
					nGram.add(nGramStats.getKey());
					currentLevel.add(new MyPair<List<Move>,NGramTreeNode<PpaInfo>>(nGram,nGramStats.getValue()));
				}
				int nGramLength = 1;

				while(!currentLevel.isEmpty()){
					toLog += ("N_GRAM_LENGTH=;" + nGramLength + ";\n");
					for(MyPair<List<Move>,NGramTreeNode<PpaInfo>> nGramTreeNode: currentLevel){
						toLog += ("MOVE=;" + getNGramString2(nGramTreeNode.getFirst()) + ";" + nGramTreeNode.getSecond().getStatistic() + "\n");
						for(Entry<Move,NGramTreeNode<PpaInfo>> nGramStats : nGramTreeNode.getSecond().getNextMoveNodes().entrySet()){
							List<Move> nGram = new ArrayList<Move>(nGramTreeNode.getFirst());
							nGram.add(nGramStats.getKey());
							nextLevel.add(new MyPair<List<Move>,NGramTreeNode<PpaInfo>>(nGram,nGramStats.getValue()));
						}

					}
					currentLevel = new ArrayList<MyPair<List<Move>,NGramTreeNode<PpaInfo>>>(nextLevel);
					nextLevel.clear();
					nGramLength++;
				}
			}
		}

		toLog += "\n";

		System.out.println(toLog);

	}

	public static String getNGramString2(List<Move> reversedNGram){
		String nGram = "]";

		for(Move m : reversedNGram){
			nGram = m.toString() + " " + nGram;
		}

		nGram = "[ " + nGram;

		return nGram;
	}


	public static void main(String[] args){

		List<NGramTreeNode<PpaInfo>> nppaStatistics = new ArrayList<NGramTreeNode<PpaInfo>>();

		NGramTreeNode<PpaInfo> node1A = new NGramTreeNode<PpaInfo>(new PpaInfo(1000,30,false,-1));
		NGramTreeNode<PpaInfo> node1B = new NGramTreeNode<PpaInfo>(new PpaInfo(2000,80,false,-1));

		NGramTreeNode<PpaInfo> node1C = new NGramTreeNode<PpaInfo>(new PpaInfo(60,1,false,-1));
		NGramTreeNode<PpaInfo> node1D = new NGramTreeNode<PpaInfo>(new PpaInfo(40,1,false,-1));
		NGramTreeNode<PpaInfo> node1E = new NGramTreeNode<PpaInfo>(new PpaInfo(20,8,false,-1));
		NGramTreeNode<PpaInfo> node1F = new NGramTreeNode<PpaInfo>(new PpaInfo(10,4,false,-1));

		NGramTreeNode<PpaInfo> node1G = new NGramTreeNode<PpaInfo>(new PpaInfo(100,2,false,-1));
		NGramTreeNode<PpaInfo> node1H = new NGramTreeNode<PpaInfo>(new PpaInfo(300,3,false,-1));
		NGramTreeNode<PpaInfo> node1I = new NGramTreeNode<PpaInfo>(new PpaInfo(0,3,false,-1));

		NGramTreeNode<PpaInfo> node1J = new NGramTreeNode<PpaInfo>(new PpaInfo(0,0,false,-1));

		node1F.addNextMoveNode(new CompactMove(8), node1A);
		node1F.addNextMoveNode(new CompactMove(9), node1B);

		node1G.addNextMoveNode(new CompactMove(4), node1C);
		node1G.addNextMoveNode(new CompactMove(5), node1D);

		node1I.addNextMoveNode(new CompactMove(6), node1E);
		node1I.addNextMoveNode(new CompactMove(7), node1F);

		node1J.addNextMoveNode(new CompactMove(1), node1G);
		node1J.addNextMoveNode(new CompactMove(2), node1H);
		node1J.addNextMoveNode(new CompactMove(3), node1I);

		nppaStatistics.add(node1J);


		////// ROLE 2

		NGramTreeNode<PpaInfo> node2A = new NGramTreeNode<PpaInfo>(new PpaInfo(10000,300,false,-1));
		NGramTreeNode<PpaInfo> node2B = new NGramTreeNode<PpaInfo>(new PpaInfo(20000,800,false,-1));

		NGramTreeNode<PpaInfo> node2C = new NGramTreeNode<PpaInfo>(new PpaInfo(600,10,false,-1));
		NGramTreeNode<PpaInfo> node2D = new NGramTreeNode<PpaInfo>(new PpaInfo(400,10,false,-1));
		NGramTreeNode<PpaInfo> node2E = new NGramTreeNode<PpaInfo>(new PpaInfo(200,80,false,-1));
		NGramTreeNode<PpaInfo> node2F = new NGramTreeNode<PpaInfo>(new PpaInfo(100,40,false,-1));

		NGramTreeNode<PpaInfo> node2G = new NGramTreeNode<PpaInfo>(new PpaInfo(1000,20,false,-1));
		NGramTreeNode<PpaInfo> node2H = new NGramTreeNode<PpaInfo>(new PpaInfo(3000,30,false,-1));
		NGramTreeNode<PpaInfo> node2I = new NGramTreeNode<PpaInfo>(new PpaInfo(00,30,false,-1));

		NGramTreeNode<PpaInfo> node2J = new NGramTreeNode<PpaInfo>(new PpaInfo(0,0,false,-1));

		node2F.addNextMoveNode(new CompactMove(8), node2A);
		node2F.addNextMoveNode(new CompactMove(9), node2B);

		node2G.addNextMoveNode(new CompactMove(4), node2C);
		node2G.addNextMoveNode(new CompactMove(5), node2D);

		node2I.addNextMoveNode(new CompactMove(6), node2E);
		node2I.addNextMoveNode(new CompactMove(7), node2F);

		node2J.addNextMoveNode(new CompactMove(1), node2G);
		node2J.addNextMoveNode(new CompactMove(2), node2H);
		node2J.addNextMoveNode(new CompactMove(3), node2I);

		nppaStatistics.add(node2J);

		NppaAfterMove.logNppaStats(nppaStatistics);

	}



}
