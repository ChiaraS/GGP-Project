package cyclesjohnsonmeyer.de.normalisiert.utils.graphs;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;


/**
 * Testfile for elementary cycle search.
 *
 * @author Frank Meyer
 *
 * @author C.Sironi (modified code to use different data structures)
 *
 */
public class TestCycles {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws java.io.IOException {
		if (args.length != 1) {
			System.out.println("usage: echo \"v1 v2\nv1 v3\n...\" | de.normalisiert.utils.graphs.TestCycles num_vertices");
			System.exit(1);
		}

		int num_vertices = Integer.parseInt(args[0]);

		String nodes[] = new String[num_vertices];
		boolean adjMatrix[][] = new boolean[num_vertices][num_vertices];

		for (int i = 0; i < num_vertices; i++) {
			nodes[i] = Integer.toString(i);
		}

		/*adjMatrix[0][1] = true;
		adjMatrix[1][2] = true;
		adjMatrix[2][0] = true;
		adjMatrix[2][4] = true;
		adjMatrix[1][3] = true;
		adjMatrix[3][6] = true;
		adjMatrix[6][5] = true;
		adjMatrix[5][3] = true;
		adjMatrix[6][7] = true;
		adjMatrix[7][8] = true;
		adjMatrix[7][9] = true;
		adjMatrix[9][6] = true;*/

		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		String line;

		while ((line = stdin.readLine()) != null && line.length()!= 0) {
			String[] vertices = line.split(" ", 2);
			int v1 = Integer.parseInt(vertices[0]);
			int v2 = Integer.parseInt(vertices[1]);
			adjMatrix[v1][v2] = true;
		}

		ElementaryCyclesSearch ecs = new ElementaryCyclesSearch(adjMatrix);
		List<List<Integer>> cycles = ecs.getElementaryCycles();
		for(List<Integer> cycle : cycles){
			System.out.print("[ ");
			for (Integer i : cycle) {
				System.out.print(i + " ");
			}
			System.out.println("]");
		}

	}

}
