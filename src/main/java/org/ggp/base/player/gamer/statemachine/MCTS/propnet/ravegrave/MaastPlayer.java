package org.ggp.base.player.gamer.statemachine.MCTS.propnet.ravegrave;

public class MaastPlayer extends CadiaGraveDuctMctsGamer {

	public MaastPlayer() {

		super();

		this.logTranspositionTable = false;

	}

	/* (non-Javadoc)
	 * @see org.ggp.base.player.gamer.Gamer#getName()
	 */
	@Override
	public String getName() {
		/*String type = "";
		if(this.singleGame){
			type = "SingleGame";
		}else{
			type = "Starndard";
		}
		return getClass().getSimpleName() + "-" + type;*/
		return "MaastPlayer";
	}

}
