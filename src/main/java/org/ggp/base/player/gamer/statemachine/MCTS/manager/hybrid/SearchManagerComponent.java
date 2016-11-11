package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid;

public abstract class SearchManagerComponent {

	protected GameDependentParameters gameDependentParameters;

	public SearchManagerComponent(GameDependentParameters gameDependentParameters) {
		this.gameDependentParameters = gameDependentParameters;
	}

	public abstract void clearComponent();

	public abstract void setUpComponent();

}
