package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.ggp.base.player.gamer.statemachine.GamerSettings;
import org.ggp.base.player.gamer.statemachine.MCS.manager.MoveStats;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.GameDependentParameters;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.SharedReferencesCollector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.structures.NGramTreeNode;
import org.ggp.base.util.logging.GamerLogger;
import org.ggp.base.util.statemachine.structure.Move;

import csironi.ggp.course.utils.MyPair;

public class NstAfterMove extends AfterMoveStrategy {

	private List<NGramTreeNode<MoveStats>> nstStatistics;

	private double decayFactor;

	private boolean logNstStats;

	public NstAfterMove(GameDependentParameters gameDependentParameters, Random random,
			GamerSettings gamerSettings, SharedReferencesCollector sharedReferencesCollector, String id) {

		super(gameDependentParameters, random, gamerSettings, sharedReferencesCollector, id);

		this.decayFactor = gamerSettings.getDoublePropertyValue("AfterMoveStrategy" + id + ".decayFactor");

		this.logNstStats = gamerSettings.getBooleanPropertyValue("AfterMoveStrategy" + id + ".logNstStats");

		//this.gameStep = 0;
	}

	@Override
	public void setReferences(SharedReferencesCollector sharedReferencesCollector) {
		this.nstStatistics = sharedReferencesCollector.getNstStatistics();
	}

	@Override
	public void clearComponent() {
		// Do nothing (because the NST statistics will be already cleared by the strategy that populates them,
		// i.e. the after simulation strategy).
	}

	@Override
	public void setUpComponent() {
		//this.gameStep = 0;
	}


	@Override
	public void afterMoveActions() {
		if(this.logNstStats){
			this.logNstStats();
		}

		if(this.decayFactor == 0.0){ // If we want to throw away everything, we just clear all the stats. No need to iterate.
			for(int roleIndex = 0; roleIndex < this.nstStatistics.size(); roleIndex++){
				this.nstStatistics.get(roleIndex).clear();
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
			for(int roleIndex = 0; roleIndex < this.nstStatistics.size(); roleIndex++){
				this.decayStatistics(this.nstStatistics.get(roleIndex).getNextMoveNodes());
			}
		}

		if(this.logNstStats){
			this.logNstStats();
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

	public void decayStatistics(Map<Move, NGramTreeNode<MoveStats>> nGramStatistics){
		if(!nGramStatistics.isEmpty()){
			Iterator<Entry<Move,NGramTreeNode<MoveStats>>> iterator;
			Entry<Move,NGramTreeNode<MoveStats>> theEntry;
			iterator = nGramStatistics.entrySet().iterator();
			while(iterator.hasNext()){
				theEntry = iterator.next();
				this.decayStatistics(theEntry.getValue().getNextMoveNodes());
				theEntry.getValue().getStatistic().decreaseByFactor(this.decayFactor);
				if(theEntry.getValue().getStatistic().getVisits() == 0 &&
						theEntry.getValue().hasNoChildren()){
					iterator.remove();
				}
			}
		}
	}

	/**
	 * Prints all the N-grams statistics memorized in the tree rooted in this node, in order of
	 * length. Receives as input a string with all the moves in the ngram to which the statistics in the
	 * node correspond. This string is extended with each of the moves memorized in this node
	 * and the corresponding statistics are printed. Then, the method is recursively called
	 * on all the children. Note that this method visits the tree with BFS,
	 *
	 * @param nGram
	 * @return
	 */
	private void logNstStats(){

		String toLog = "STEP=;" + this.gameDependentParameters.getGameStep() + ";\n";

		if(this.nstStatistics == null){
			for(int roleIndex = 0; roleIndex < this.gameDependentParameters.getNumRoles(); roleIndex++){
				toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex)) + ";\n");
				toLog += "null;\n";
			}
		}else{
			List<MyPair<List<Move>,NGramTreeNode<MoveStats>>> currentLevel;
			List<MyPair<List<Move>,NGramTreeNode<MoveStats>>> nextLevel;
			double scoreSum;
			double visits;
			for(int roleIndex = 0; roleIndex < this.nstStatistics.size(); roleIndex++){
				toLog += ("ROLE=;" + this.gameDependentParameters.getTheMachine().convertToExplicitRole(this.gameDependentParameters.getTheMachine().getRoles().get(roleIndex)) + ";\n");
				currentLevel = new ArrayList<MyPair<List<Move>,NGramTreeNode<MoveStats>>>();
				nextLevel = new ArrayList<MyPair<List<Move>,NGramTreeNode<MoveStats>>>();
				// Add the 1-grams to the current level
				for(Entry<Move,NGramTreeNode<MoveStats>> nGramStats : this.nstStatistics.get(roleIndex).getNextMoveNodes().entrySet()){
					List<Move> nGram = new ArrayList<Move>();
					nGram.add(nGramStats.getKey());
					currentLevel.add(new MyPair<List<Move>,NGramTreeNode<MoveStats>>(nGram,nGramStats.getValue()));
				}
				int nGramLength = 1;

				while(!currentLevel.isEmpty()){
					toLog += ("N_GRAM_LENGTH=;" + nGramLength + ";\n");
					for(MyPair<List<Move>,NGramTreeNode<MoveStats>> nGramTreeNode: currentLevel){
						scoreSum = nGramTreeNode.getSecond().getStatistic().getScoreSum();
						visits = nGramTreeNode.getSecond().getStatistic().getVisits();
						toLog += ("MOVE=;" + this.getNGramString(nGramTreeNode.getFirst()) +
						";SCORE_SUM=;" + scoreSum + ";VISITS=;" + visits + ";AVG_VALUE=;" + (scoreSum/visits) + ";\n");

						for(Entry<Move,NGramTreeNode<MoveStats>> nGramStats : nGramTreeNode.getSecond().getNextMoveNodes().entrySet()){
							List<Move> nGram = new ArrayList<Move>(nGramTreeNode.getFirst());
							nGram.add(nGramStats.getKey());
							nextLevel.add(new MyPair<List<Move>,NGramTreeNode<MoveStats>>(nGram,nGramStats.getValue()));
						}

					}
					currentLevel = new ArrayList<MyPair<List<Move>,NGramTreeNode<MoveStats>>>(nextLevel);
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

		String nstStatisticsString;

		if(this.nstStatistics != null){
			nstStatisticsString = "[ ";

			int roleIndex = 0;
			for(NGramTreeNode<MoveStats> roleNstStats : this.nstStatistics){
				nstStatisticsString += (roleNstStats == null ? "null " : "Tree" + roleIndex + " "); // this only counts the first level of the n-grams tree, i.e. only 1-grams.
				roleIndex++;
			}

			nstStatisticsString += "]";

		}else{
			nstStatisticsString = "null";
		}

		String params = indentation + "DECAY_FACTOR = " + this.decayFactor +
				indentation + "LOG_NST_STATS = " + this.logNstStats +
				indentation + "nst_statistics = " + nstStatisticsString;

		return params;

	}


	/* CODE FOR DEBUGGING */
	public static void logNstStats(List<NGramTreeNode<MoveStats>> nstStatistics){

		String toLog = "STEP=;" + 1 + ";\n";

		if(nstStatistics == null){
			//for(int roleIndex = 0; roleIndex < nstStatistics.size(); roleIndex++){
				toLog += ("ROLE=;" + 0 + ";\n");
				toLog += "null;\n";
			//}
		}else{
			List<MyPair<List<Move>,NGramTreeNode<MoveStats>>> currentLevel;
			List<MyPair<List<Move>,NGramTreeNode<MoveStats>>> nextLevel;
			double scoreSum;
			double visits;
			for(int roleIndex = 0; roleIndex < nstStatistics.size(); roleIndex++){
				toLog += ("ROLE=;" + roleIndex + ";\n");
				currentLevel = new ArrayList<MyPair<List<Move>,NGramTreeNode<MoveStats>>>();
				nextLevel = new ArrayList<MyPair<List<Move>,NGramTreeNode<MoveStats>>>();
				// Add the 1-grams to the current level
				for(Entry<Move,NGramTreeNode<MoveStats>> nGramStats : nstStatistics.get(roleIndex).getNextMoveNodes().entrySet()){
					List<Move> nGram = new ArrayList<Move>();
					nGram.add(nGramStats.getKey());
					currentLevel.add(new MyPair<List<Move>,NGramTreeNode<MoveStats>>(nGram,nGramStats.getValue()));
				}
				int nGramLength = 1;

				while(!currentLevel.isEmpty()){
					toLog += ("N_GRAM_LENGTH=;" + nGramLength + ";\n");
					for(MyPair<List<Move>,NGramTreeNode<MoveStats>> nGramTreeNode: currentLevel){
						scoreSum = nGramTreeNode.getSecond().getStatistic().getScoreSum();
						visits = nGramTreeNode.getSecond().getStatistic().getVisits();
						toLog += ("MOVE=;" + getNGramString2(nGramTreeNode.getFirst()) +
						";SCORE_SUM=;" + scoreSum + ";VISITS=;" + visits + ";AVG_VALUE=;" + (scoreSum/visits) + ";\n");

						for(Entry<Move,NGramTreeNode<MoveStats>> nGramStats : nGramTreeNode.getSecond().getNextMoveNodes().entrySet()){
							List<Move> nGram = new ArrayList<Move>(nGramTreeNode.getFirst());
							nGram.add(nGramStats.getKey());
							nextLevel.add(new MyPair<List<Move>,NGramTreeNode<MoveStats>>(nGram,nGramStats.getValue()));
						}

					}
					currentLevel = new ArrayList<MyPair<List<Move>,NGramTreeNode<MoveStats>>>(nextLevel);
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

/*
	public static void main(String[] args){

		List<NGramTreeNode<MoveStats>> nstStatistics = new ArrayList<NGramTreeNode<MoveStats>>();

		NGramTreeNode<MoveStats> node1A = new NGramTreeNode<MoveStats>(new MoveStats(1000,30));
		NGramTreeNode<MoveStats> node1B = new NGramTreeNode<MoveStats>(new MoveStats(2000,80));

		NGramTreeNode<MoveStats> node1C = new NGramTreeNode<MoveStats>(new MoveStats(60,1));
		NGramTreeNode<MoveStats> node1D = new NGramTreeNode<MoveStats>(new MoveStats(40,1));
		NGramTreeNode<MoveStats> node1E = new NGramTreeNode<MoveStats>(new MoveStats(20,8));
		NGramTreeNode<MoveStats> node1F = new NGramTreeNode<MoveStats>(new MoveStats(10,4));

		NGramTreeNode<MoveStats> node1G = new NGramTreeNode<MoveStats>(new MoveStats(100,2));
		NGramTreeNode<MoveStats> node1H = new NGramTreeNode<MoveStats>(new MoveStats(300,3));
		NGramTreeNode<MoveStats> node1I = new NGramTreeNode<MoveStats>(new MoveStats(0,3));

		NGramTreeNode<MoveStats> node1J = new NGramTreeNode<MoveStats>(new MoveStats(0,0));

		node1F.addNextMoveNode(new CompactMove(8), node1A);
		node1F.addNextMoveNode(new CompactMove(9), node1B);

		node1G.addNextMoveNode(new CompactMove(4), node1C);
		node1G.addNextMoveNode(new CompactMove(5), node1D);

		node1I.addNextMoveNode(new CompactMove(6), node1E);
		node1I.addNextMoveNode(new CompactMove(7), node1F);

		node1J.addNextMoveNode(new CompactMove(1), node1G);
		node1J.addNextMoveNode(new CompactMove(2), node1H);
		node1J.addNextMoveNode(new CompactMove(3), node1I);

		nstStatistics.add(node1J);


		////// ROLE 2

		NGramTreeNode<MoveStats> node2A = new NGramTreeNode<MoveStats>(new MoveStats(10000,300));
		NGramTreeNode<MoveStats> node2B = new NGramTreeNode<MoveStats>(new MoveStats(20000,800));

		NGramTreeNode<MoveStats> node2C = new NGramTreeNode<MoveStats>(new MoveStats(600,10));
		NGramTreeNode<MoveStats> node2D = new NGramTreeNode<MoveStats>(new MoveStats(400,10));
		NGramTreeNode<MoveStats> node2E = new NGramTreeNode<MoveStats>(new MoveStats(200,80));
		NGramTreeNode<MoveStats> node2F = new NGramTreeNode<MoveStats>(new MoveStats(100,40));

		NGramTreeNode<MoveStats> node2G = new NGramTreeNode<MoveStats>(new MoveStats(1000,20));
		NGramTreeNode<MoveStats> node2H = new NGramTreeNode<MoveStats>(new MoveStats(3000,30));
		NGramTreeNode<MoveStats> node2I = new NGramTreeNode<MoveStats>(new MoveStats(00,30));

		NGramTreeNode<MoveStats> node2J = new NGramTreeNode<MoveStats>(new MoveStats(0,0));

		node2F.addNextMoveNode(new CompactMove(8), node2A);
		node2F.addNextMoveNode(new CompactMove(9), node2B);

		node2G.addNextMoveNode(new CompactMove(4), node2C);
		node2G.addNextMoveNode(new CompactMove(5), node2D);

		node2I.addNextMoveNode(new CompactMove(6), node2E);
		node2I.addNextMoveNode(new CompactMove(7), node2F);

		node2J.addNextMoveNode(new CompactMove(1), node2G);
		node2J.addNextMoveNode(new CompactMove(2), node2H);
		node2J.addNextMoveNode(new CompactMove(3), node2I);

		nstStatistics.add(node2J);

		NstAfterMove.logNstStats(nstStatistics);

	}

*/


}
