/**
 *
 */
package csironi.ggp.course.experiments;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.logging.GamerLogger;

/**
 * @author C.Sironi
 *
 */
public class Combinator {


	public static void main(String args[]){

		//System.out.println("Combos");

		//Combinator.combination(1, 1);

		// 3. Compute all combinations of gamer types.
    	List<List<Integer>> combinations = Combinator.getCombinations(4, 3);

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

	/**
	 * This method returns all the possible permutations of the combinations of length combinationsLength
	 * that can be obtained using always the highest possible number of distinct elements in a set with
	 * cardinality numElements.
	 *
	 * This means that if numElements <= combinationsLength, this method will return all the possible
	 * permutations of all the possible combinations that include always every element at least (but not
	 * necessarily only) once.
	 * While if numElements > combinationsLength, this method will return all the possible permutations of
	 * all the possible combinations of combinationsLength distinct elements over numElements distinct elements.
	 *
	 * E.G:
	 * - getCombinations(2, 3) will return [[0,0,1][0,1,0][0,1,1][1,0,0][1,0,1][1,1,0]], i.e. getSubCombinations(2, 3)
	 * - getCombinations(3, 2) will return [[0,1][1,0][0,2][2,0][1,2][2,1]], i.e. use the result of performing
	 * 	 getSubCombinations(2, 2) to build all the combinations for every combination in buildCombination(2,3).
	 * 	 More precisely, buildCombination(2,3) will build the combinations [[0,1][0,2][1,2]] and using
	 *   getSubCombinations(2, 2) = [[0,1][1,0]] as indices will build the permutations for every combination in
	 *   [[0,1][0,2][1,2]].
	 *
	 * @param numElements number of distinct elements to combine and permute.
	 * @param combinationsLength the length that each permutation must have.
	 * @return all the possible permutations of the combinations of length combinationsLength that can be
	 * obtained using always the highest possible number of distinct elements in a set with cardinality
	 * numElements.
	 */
	public static List<List<Integer>> getCombinations(int numElements, int combinationsLength){

		// If any of the inputs is 0 return an empty list of combinations.
		if(numElements == 0 || combinationsLength == 0){
			return new ArrayList<List<Integer>>();
		}

		if(numElements <= combinationsLength){
			return getPermutations(numElements, combinationsLength);
		}

		return buildCombinations(numElements, combinationsLength);

	}

	private static List<List<Integer>> buildCombinations(int n, int k){

		List<List<Integer>> allCombinations = new ArrayList<List<Integer>>();

		List<Integer> allElements = new ArrayList<Integer>();

		// Build the list of all the elements (each element is identified by an integer number starting from 0).
		for(int i = 0; i < n; i++){
			allElements.add(new Integer(i));
		}

		// If we have more elements than the length that a combination must have, we compute all the possible
		// combinations that can be obtained with a number of distinct elements exactly equal to the length of
		// the combination.
		List<List<Integer>> permutations = getPermutations(k, k);

        if(k > n){
            GamerLogger.logError("Combinator", "Invalid input for computing combinations of K over N elements. K=" + k + " shouldn't be greater than N=" + n + ". ");
            return allCombinations;
        }
        // calculate the possible combinations
        // e.g. c(4,2)
        // c(N,K);

        // get the combination by index
        // e.g. 01 --> AB , 23 --> CD
        //int combination[] = new int[k];

        List<Integer> combinationList = new ArrayList<Integer>();
        for(int i = 0; i < k; i++){
        	combinationList.add(null);
        }


        // position of current index
        //  if (r = 1)              r*
        //  index ==>        0   |   1   |   2
        //  element ==>      A   |   B   |   C
        int r = 0;
        int index = 0;

        while(r >= 0){
            // possible indexes for 1st position "r=0" are "0,1,2" --> "A,B,C"
            // possible indexes for 2nd position "r=1" are "1,2,3" --> "B,C,D"

            // for r = 0 ==> index < (4+ (0 - 2)) = 2
            if(index <= (n + (r - k))){

            	//combination[r] = index;
            	combinationList.set(r, allElements.get(index));

                // if we are at the last position print and increase the index
                if(r == k-1){

                    //do something with the combination e.g. add to list or print
                	addPermutations(combinationList, permutations, allCombinations);

                    //System.out.println(Arrays.toString(combination));

                	index++;
                }
                else{
                    // select index for next position
                    //index = combination[r]+1;
                	index = combinationList.get(r).intValue()+1;
                    r++;
                }
            }
            else{
                r--;
                if(r > 0)
                    //index = combination[r]+1;
                	index = combinationList.get(r).intValue()+1;
                else
                    //index = combination[0]+1;
                	index = combinationList.get(0).intValue()+1;
            }
        }

        return allCombinations;
    }

	/**
	 *
	 * @param numElements
	 * @param combinationsLength
	 * @return
	 */
	private static List<List<Integer>> getPermutations(int numElements, int combinationsLength){

		List<List<Integer>> permutations = new ArrayList<List<Integer>>();

		// If any of the inputs is 0 or the number of elements to combine is higher than the length that
		// each combination must have, return an empty list of combinations.
		if(numElements == 0 || combinationsLength == 0 || numElements > combinationsLength){
			return permutations;
		}

		List<Integer> allElements = new ArrayList<Integer>();

		// Build the list of all the elements (each element is identified by an integer number starting from 0).
		for(int i = 0; i < numElements; i++){
			allElements.add(new Integer(i));
		}

		List<Integer> unusedElements = new ArrayList<Integer>(allElements);

		List<Integer> permutation = new ArrayList<Integer>();

		for(int i = 0; i < combinationsLength; i++){
			permutation.add(null);
		}

		buildPermutations(combinationsLength, allElements, unusedElements, permutation, permutations);

		return permutations;

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
	private static void buildPermutations(int remainingLength, List<Integer> allElements, List<Integer> unusedElements, List<Integer> permutation, List<List<Integer>> permutations){

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
			permutations.add(new ArrayList<Integer>(permutation));

		// Check if the length of the combination that still must be filled with elements is higher than
		// the number of different elements that have not been included in the combination yet.
		// If yes, we must consider all the possible configurations obtained by assigning to the next position
		// in the combination all possible different elements (including the ones already used)...
		}else if(remainingLength > unusedElements.size()){

			// Consider all elements configurations obtained by assigning to the current position every possible distinct element.
			for(Integer i : allElements){

				permutation.set(remainingLength-1, i);

				// Copy the unused elements list and, if not yet done, remove the just used element from the unused elements list.
				ArrayList<Integer> unusedElementsCopy = new ArrayList<Integer>(unusedElements);
				unusedElementsCopy.remove(i);

				// Recursive call. NOTE!: It is not necessary to copy also the list with the elementsCombination.
				// It's enough to copy it only at the end, when it will contain one of the possible combinations
				// of elements, right before adding such combination to the list of all possible combinations.
				buildPermutations((remainingLength-1), allElements, unusedElementsCopy, permutation, permutations);

			}

		// ...if not, then we must consider only the configurations that we can obtain by assigning to the next
		// position all the distinct elements not yet assigned so far. This is to make sure that each configuration
		// that we will obtain will contain all possible elements at least once.
		}else{

			// Consider all elements configurations obtained by assigning to the current position only the elements
			// not used so far.
			for(Integer i : unusedElements){

				permutation.set(remainingLength-1, i);

				// Copy the unused elements list and, if not yet done, remove the just used element from the unused elements list.
				// It will be removed for sure since we are certain that it is still in the list because it has not been used yet.
				ArrayList<Integer> unusedElementsCopy = new ArrayList<Integer>(unusedElements);
				unusedElementsCopy.remove(i);

				// Recursive call
				buildPermutations((remainingLength-1), allElements, unusedElementsCopy, permutation, permutations);

			}
		}

	}

	private static void addPermutations(List<Integer> combination, List<List<Integer>> permutations, List<List<Integer>> allCombinations){

		List<Integer> newPermutation;

		for(List<Integer> permutation : permutations){

			newPermutation = new ArrayList<Integer>();

			for(Integer i : permutation){
				newPermutation.add(combination.get(i.intValue()));
			}

			allCombinations.add(newPermutation);
		}
	}

}
