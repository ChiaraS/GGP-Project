package org.ggp.base.util.reflection;

import java.lang.reflect.Modifier;

import org.ggp.base.apps.kiosk.GameCanvas;
import org.ggp.base.player.gamer.Gamer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.AfterGameStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftermove.AfterMoveStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.aftersimulation.AfterSimulationStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.BackpropagationStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.backpropagation.nodeupdaters.NodeUpdater;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforemove.BeforeMoveStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.beforesimualtion.BeforeSimulationStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.expansion.ExpansionStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.movechoice.MoveChoiceStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.PlayoutStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.playout.moveselector.MoveSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.SelectionStrategy;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.MoveEvaluator;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.hybrid.strategies.selection.evaluators.grave.BetaComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.ParametersTuner;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.biascomputers.BiasComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution.EvolutionManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution.crossover.CrossoverManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution.fitness.FitnessComputer;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.evolution.mutation.MutationManager;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.selectors.TunerSelector;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.parameterstuning.structure.parametersorders.ParametersOrder;
import org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure.hybrid.TreeNodeFactory;
import org.ggp.base.player.gamer.statemachine.propnet.InternalPropnetGamer;
import org.ggp.base.player.gamer.statemachine.prover.ProverGamer;
import org.reflections.Reflections;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class ProjectSearcher {
	public static void main(String[] args)
	{
		System.out.println(GAMERS);
		System.out.println(PROVER_GAMERS);
		System.out.println(INTERNAL_PROPNET_GAMERS);
        System.out.println(GAME_CANVASES);
	}

    private static final Reflections REFLECTIONS = new Reflections();

    public static final LoadedClasses<Gamer> GAMERS = new LoadedClasses<Gamer>(Gamer.class);
    public static final LoadedClasses<ProverGamer> PROVER_GAMERS = new LoadedClasses<ProverGamer>(ProverGamer.class);
    public static final LoadedClasses<InternalPropnetGamer> INTERNAL_PROPNET_GAMERS = new LoadedClasses<InternalPropnetGamer>(InternalPropnetGamer.class);
    public static final LoadedClasses<GameCanvas> GAME_CANVASES = new LoadedClasses<GameCanvas>(GameCanvas.class);

    // Classes needed to create the search manager
    public static final LoadedClasses<BetaComputer> BETA_COMPUTERS = new LoadedClasses<BetaComputer>(BetaComputer.class);
    public static final LoadedClasses<MoveSelector> MOVE_SELECTORS = new LoadedClasses<MoveSelector>(MoveSelector.class);
    public static final LoadedClasses<MoveEvaluator> MOVE_EVALUATORS = new LoadedClasses<MoveEvaluator>(MoveEvaluator.class);
    public static final LoadedClasses<NodeUpdater> NODE_UPDATERS = new LoadedClasses<NodeUpdater>(NodeUpdater.class);
    public static final LoadedClasses<ParametersTuner> PARAMETERS_TUNERS = new LoadedClasses<ParametersTuner>(ParametersTuner.class);
    public static final LoadedClasses<TunerSelector> TUNER_SELECTORS = new LoadedClasses<TunerSelector>(TunerSelector.class);
    public static final LoadedClasses<ParametersOrder> PARAMETERS_ORDER = new LoadedClasses<ParametersOrder>(ParametersOrder.class);
    public static final LoadedClasses<BiasComputer> BIAS_COMPUTERS = new LoadedClasses<BiasComputer>(BiasComputer.class);
    public static final LoadedClasses<EvolutionManager> EVOLUTION_MANAGERS = new LoadedClasses<EvolutionManager>(EvolutionManager.class);
    public static final LoadedClasses<CrossoverManager> CROSSOVER_MANAGERS = new LoadedClasses<CrossoverManager>(CrossoverManager.class);
    public static final LoadedClasses<MutationManager> MUTATION_MANAGERS = new LoadedClasses<MutationManager>(MutationManager.class);
    public static final LoadedClasses<FitnessComputer> FITNESS_COMPUTER = new LoadedClasses<FitnessComputer>(FitnessComputer.class);
    // Strategies
    public static final LoadedClasses<BeforeMoveStrategy> BEFORE_MOVE_STRATEGIES = new LoadedClasses<BeforeMoveStrategy>(BeforeMoveStrategy.class);
    public static final LoadedClasses<AfterMoveStrategy> AFTER_MOVE_STRATEGIES = new LoadedClasses<AfterMoveStrategy>(AfterMoveStrategy.class);
    public static final LoadedClasses<AfterGameStrategy> AFTER_GAME_STRATEGIES = new LoadedClasses<AfterGameStrategy>(AfterGameStrategy.class);
    public static final LoadedClasses<AfterSimulationStrategy> AFTER_SIMULATION_STRATEGIES = new LoadedClasses<AfterSimulationStrategy>(AfterSimulationStrategy.class);
    public static final LoadedClasses<BackpropagationStrategy> BACKPROPAGATION_STRATEGIES = new LoadedClasses<BackpropagationStrategy>(BackpropagationStrategy.class);
    public static final LoadedClasses<BeforeSimulationStrategy> BEFORE_SIMULATION_STRATEGIES = new LoadedClasses<BeforeSimulationStrategy>(BeforeSimulationStrategy.class);
    public static final LoadedClasses<ExpansionStrategy> EXPANSION_STRATEGIES = new LoadedClasses<ExpansionStrategy>(ExpansionStrategy.class);
    public static final LoadedClasses<MoveChoiceStrategy> MOVE_CHOICE_STRATEGIES = new LoadedClasses<MoveChoiceStrategy>(MoveChoiceStrategy.class);
    public static final LoadedClasses<PlayoutStrategy> PLAYOUT_STRATEGIES = new LoadedClasses<PlayoutStrategy>(PlayoutStrategy.class);
    public static final LoadedClasses<SelectionStrategy> SELECTION_STRATEGIES = new LoadedClasses<SelectionStrategy>(SelectionStrategy.class);
    // Tree node factory
    public static final LoadedClasses<TreeNodeFactory> TREE_NODE_FACTORIES = new LoadedClasses<TreeNodeFactory>(TreeNodeFactory.class);

    public static final <T> ImmutableSet<Class<? extends T>> getAllClassesThatAre(Class<T> klass) {
    	return new LoadedClasses<T>(klass).getConcreteClasses();
    }

    public static class LoadedClasses<T> {
        private static Predicate<Class<?>> IS_CONCRETE_CLASS = new Predicate<Class<?>>() {
            @Override
            public boolean apply(Class<?> klass) {
                return !Modifier.isAbstract(klass.getModifiers());
            }
        };

        private final Class<T> interfaceClass;
        private final ImmutableSet<Class<? extends T>> allClasses;
        private final ImmutableSet<Class<? extends T>> concreteClasses;

        private LoadedClasses(Class<T> interfaceClass) {
            this.interfaceClass = interfaceClass;
            this.allClasses = ImmutableSet.copyOf(REFLECTIONS.getSubTypesOf(interfaceClass));
            this.concreteClasses = ImmutableSet.copyOf(Sets.filter(allClasses, IS_CONCRETE_CLASS));
        }

        public Class<T> getInterfaceClass() {
            return interfaceClass;
        }

        public ImmutableSet<Class<? extends T>> getConcreteClasses() {
            return concreteClasses;
        }

        public ImmutableSet<Class<? extends T>> getAllClasses() {
            return allClasses;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("allClasses", allClasses)
                    .add("interfaceClass", interfaceClass)
                    .add("concreteClasses", concreteClasses)
                    .toString();
        }
    }
}
