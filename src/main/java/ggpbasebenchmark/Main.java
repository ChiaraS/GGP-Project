/**
 * 
 */
package ggpbasebenchmark;

import java.io.File;

public class Main {

	/**
	 * @param args arguments are: REASONER {dfs|fdfs|mc} PLAYCLOCK|DEPTH GDLFILE TRACEFILE
	 * 
	 * valid values for REASONER are: all values of ReasonerType
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		// parse arguments
		if(args.length != 5) {
			printUsage();
			System.exit(2);
		}
		ReasonerType reasonerType = null;
		try {
			reasonerType = ReasonerType.valueOf(args[0].toUpperCase());
		} catch (Exception ex) {
			System.err.println("unknown reasoner: " + args[0]);
			printUsage();
			System.exit(1);
		}
		BenchmarkMethod method = null;
		try {
			method = BenchmarkMethod.valueOf(args[1].toUpperCase());
		} catch (Exception ex) {
			System.err.println("unknown method: " + args[1]);
			printUsage();
			System.exit(1);
		}
		int playclock = Integer.parseInt(args[2]);
		File gdlFile = new File(args[3]);
		File traceFile = new File(args[4]);
		Benchmark benchmark = new Benchmark(reasonerType, method, playclock, gdlFile, traceFile);
		benchmark.run();
	}

	public static void printUsage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Parameters: REASONER {dfs|fdfs|mc} PLAYCLOCK|DEPTH GDLFILE TRACEFILE\n\tvalid values for REASONER are:\n");
		for (ReasonerType t : ReasonerType.values()) {
			sb.append("\t\t").append(t.toString()).append("\n");
		}
		System.out.println(sb.toString());
	}
}
