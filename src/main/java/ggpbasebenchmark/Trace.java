package ggpbasebenchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.gdl.factory.GdlFactory;
import org.ggp.base.util.statemachine.proverStructure.ProverMove;
import org.ggp.base.util.symbol.factory.SymbolFactory;
import org.ggp.base.util.symbol.grammar.SymbolAtom;
import org.ggp.base.util.symbol.grammar.SymbolList;

public class Trace extends ArrayList<List<ProverMove>> {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1876437366509484985L;

	public static Trace loadFromFile(File traceFile) throws Exception {
		
		Trace result = new Trace();
		BufferedReader input = new BufferedReader(new FileReader(traceFile));
		try {
			String line;
			// read input line by line
			while ((line = input.readLine()) != null) {
				// feed each line to the parser
				SymbolList list = (SymbolList) SymbolFactory.create(line);
				SymbolAtom head = (SymbolAtom) list.get(0);
				if (!"moves".equals(head.getValue().toLowerCase())) {
					 throw new Exception("Wrong format of trace file! Unrecognized line: " + line);
				}
				// built a joint move out of the moves in the line
				List<ProverMove> moves = new ArrayList<ProverMove>();
				for (int i = 1; i < list.size(); i++)
				{
					moves.add(new ProverMove(GdlFactory.createTerm(list.get(i))));
				}
				result.add(moves);
			}
		} finally {
			input.close();
		}
		return result;
	}
	
}
