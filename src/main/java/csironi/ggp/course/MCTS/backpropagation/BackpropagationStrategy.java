/**
 *
 */
package csironi.ggp.course.MCTS.backpropagation;

import java.util.List;


/**
 * @author C.Sironi
 *
 */
public interface BackpropagationStrategy {

	public void update(List<Integer> goals);

}
