package org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid;

/**
 * This class specifies parameters and methods that must be common to all components of the search manager
 * (the components of the search manager are strategies (e.g. selection, playout, before/after simulation,
 * backpropagation, ecc...), move evaluators (e.g. UCTEvaluator, GRAVEEvaluator, ecc...), single or joint
 * move selectors (e.g RandomJointMoveSelector, EpsilonMASTJointMoveSelector, ecc...)).
 *
 * ATTENTION: each component must respect a rule. The constructor must ONLY set the reference to (non primitive)
 * class parameters. The initialization of the content of these parameters needed to play a game must be performed
 * in the setUpComponent() method before playing the game and after knowing which game will be played. The search
 * manager and all its component must be created only once when creating the player, and thus before knowing which
 * game the player has to play. Moreover, since the search manager will be used to play any game during the whole
 * "life" of the game player the methods clear() and setUpComponent() must make sure respectively that, between
 * two games, the search manager is cleared from all data of the previous game and initialized with the data of
 * the new game. (The choice of separating the two methods instead of having a single one that does everything
 * whenever a new game must be played is due to the fact that like this we can clear memory (and run GC) between
 * games without wasting the metagame time for that).
 *
 * @author c.sironi
 *
 */
public abstract class SearchManagerComponent {

	protected GameDependentParameters gameDependentParameters;

	public SearchManagerComponent(GameDependentParameters gameDependentParameters) {
		this.gameDependentParameters = gameDependentParameters;
	}

	public abstract void clearComponent();

	public abstract void setUpComponent();

}
