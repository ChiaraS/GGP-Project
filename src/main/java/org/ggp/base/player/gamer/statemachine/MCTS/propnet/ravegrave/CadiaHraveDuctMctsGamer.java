package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;



public class CadiaHraveDuctMctsGamer extends CadiaRaveDuctMctsGamer {

	public CadiaHraveDuctMctsGamer() {

		super();

		this.cadiaBetaComputer = true;
		this.k = 50;

		this.minAMAFVisits = Integer.MAX_VALUE;

	}

}
