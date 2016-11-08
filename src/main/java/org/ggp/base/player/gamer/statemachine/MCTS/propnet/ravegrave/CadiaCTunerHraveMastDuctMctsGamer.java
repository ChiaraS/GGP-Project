package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;


public class CadiaCTunerHraveMastDuctMctsGamer extends CadiaCTunerRaveMastDuctMctsGamer {

	public CadiaCTunerHraveMastDuctMctsGamer() {
		super();

		this.cadiaBetaComputer = true;
		this.k = 50;

		this.minAMAFVisits = Integer.MAX_VALUE;
	}

}
