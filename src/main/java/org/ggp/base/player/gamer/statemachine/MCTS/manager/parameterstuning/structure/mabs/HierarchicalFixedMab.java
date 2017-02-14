package org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.mabs;



public class HierarchicalFixedMab extends FixedMab {

	private HierarchicalFixedMab[] nextMabs;

	public HierarchicalFixedMab(int[] numMovesPerLevel, int levelIndex) {
		super(numMovesPerLevel[levelIndex]);

		if(levelIndex+1 < numMovesPerLevel.length){
			this.nextMabs = new HierarchicalFixedMab[numMovesPerLevel[levelIndex]];

			for(int i = 0; i < nextMabs.length; i++){
				this.nextMabs[i] = new HierarchicalFixedMab(numMovesPerLevel, levelIndex+1);
			}
		}else{
			this.nextMabs = null;
		}
	}

    public HierarchicalFixedMab[] getNextMabs(){
    	return this.nextMabs;
    }

}
