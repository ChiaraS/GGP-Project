package org.ggp.base.player.gamer.statemachine.MCTS.manager.treestructure;


/**
 * This class represents one episode (i.e. one simulation) using a list of all the joint moves
 * performed during the simulation and a list of the intermediate goals obtained by performing
 * such joint moves (note that if not necessary for the algorithm the intermediate goals are
 * not memorized but only final goals are memorized, and also all the joint moves if not necessary
 * are not memorized. HOWEVER, whenever intermediate goals and all the joint moves are memorized
 * then both lists have the same length, i.e. there is a 1-to-1 correspondence between joint move
 * and intermediate goals).
 *
 * NOTE that the order of the two list goes from last to first visited move/obtained goal in the
 * simulation.
 *
 * @author c.sironi
 *
 */
public class SimulationResult {


}
