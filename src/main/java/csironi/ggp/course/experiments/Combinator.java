/**
 *
 */
package csironi.ggp.course.experiments;

import java.util.ArrayList;
import java.util.List;

/**
 * @author C.Sironi
 *
 */
public class Combinator {


	public static void main(String args[]){
		// 3. Compute all combinations of gamer types.
    	List<List<Integer>> combinations = Combinator.getCombinations(3, 4);

    	// 4. For each combination run the given amount of matches.

    	System.out.println("Combinations:");

    	for(List<Integer> combination : combinations){
    		System.out.print("[ ");
    		for(Integer i : combination){
    			System.out.print(i.intValue() + " ");
    		}
    		System.out.println("]");
    	}
	}







	public static List<List<Integer>> getCombinations(int numElements, int combinationsLength){

		List<List<Integer>> combinations = new ArrayList<List<Integer>>();

		// If any of the inputs is 0 or the number of elements to combine is higher than the length that
		// each combination must have, return an empty list of combinations.
		if(numElements == 0 || combinationsLength == 0 || numElements > combinationsLength){
			return combinations;
		}

		List<Integer> allElements = new ArrayList<Integer>();

		// Build the list of all the elements (each element is identified by an integer number starting from 0).
		for(int i = 0; i < numElements; i++){
			allElements.add(new Integer(i));
		}

		List<Integer> unusedElements = new ArrayList<Integer>(allElements);

		List<Integer> combination = new ArrayList<Integer>();

		for(int i = 0; i < combinationsLength; i++){
			combination.add(null);
		}

		getCombinationsFunction(combinationsLength, allElements, unusedElements, combination, combinations);

		return combinations;

	}

	/**
	 * NOTE: this method creates the combinations starting to fill each combination with elements from the end.
	 *
	 * @param remainingLength
	 * @param allElements
	 * @param unusedElements
	 * @param combination
	 * @param combinations
	 */
	private static void getCombinationsFunction(int remainingLength, List<Integer> allElements, List<Integer> unusedElements, List<Integer> combination, List<List<Integer>> combinations){

		/*
		System.out.println();

		System.out.println("Remaining players: " + remainingPlayers);

		System.out.print("Unused types: [ ");
		for(Integer i : unusedTypes){
			System.out.print(i.intValue() + " ");
		}
		System.out.println("]");

		System.out.print("Types combination: [ ");
		for(int i = 0; i < typesCombination.length; i++){
			System.out.print(typesCombination[i] + " ");
		}
		System.out.println("]");*/



		// If we've reached the specified length for the combination, memorize the combination in the list to return.
		if(remainingLength == 0){

			/**this.runMatch(typesCombination);**/
			//this.print(typesCombination);
			combinations.add(new ArrayList<Integer>(combination));

		// Check if the length of the combination that still must be filled with elements is higher than
		// the number of different elements that have not been included in the combination yet.
		// If yes, we must consider all the possible configurations obtained by assigning to the next position
		// in the combination all possible different elements (including the ones already used)...
		}else if(remainingLength > unusedElements.size()){

			// Consider all elements configurations obtained by assigning to the current position every possible distinct element.
			for(Integer i : allElements){

				combination.set(remainingLength-1, i);

				// Copy the unused elements list and, if not yet done, remove the just used element from the unused elements list.
				ArrayList<Integer> unusedElementsCopy = new ArrayList<Integer>(unusedElements);
				unusedElementsCopy.remove(i);

				// Recursive call. NOTE!: It is not necessary to copy also the list with the elementsCombination.
				// It's enough to copy it only at the end, when it will contain one of the possible combinations
				// of elements, right before adding such combination to the list of all possible combinations.
				getCombinationsFunction((remainingLength-1), allElements, unusedElementsCopy, combination, combinations);

			}

		// ...if not, then we must consider only the configurations that we can obtain by assigning to the next
		// position all the distinct elements not yet assigned so far. This is to make sure that each configuration
		// that we will obtain will contain all possible elements at least once.
		}else{

			// Consider all elements configurations obtained by assigning to the current position only the elements
			// not used so far.
			for(Integer i : unusedElements){

				combination.set(remainingLength-1, i);

				// Copy the unused elements list and, if not yet done, remove the just used element from the unused elements list.
				// It will be removed for sure since we are certain that it is still in the list because it has not been used yet.
				ArrayList<Integer> unusedElementsCopy = new ArrayList<Integer>(unusedElements);
				unusedElementsCopy.remove(i);

				// Recursive call
				getCombinationsFunction((remainingLength-1), allElements, unusedElementsCopy, combination, combinations);

			}
		}

	}

}
