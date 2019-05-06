package csironi.ggp.course.statsSummarizer.treeDrawer.chart;

import java.util.ArrayList;

import org.jfree.data.xy.XYSeries;

public class JFCTurn {

	private int gameTurn;

	private int totalIterations;

	//private RGBColor strokeColor;

	/**
	 * Lines corresponding to actions added to the tree in this game turn
	 */
	private ArrayList<XYSeries> series;
	/**
	 * Line corresponding to the action selected at the end of the game turn
	 */
	private XYSeries finalMoveSeries;

	public JFCTurn(int gameTurn) {
		this.gameTurn = gameTurn;
	}

	//public boolean initializeGameTurn(String linesCoordinates, String chosenMoveAndIterations, double xMax, double leftMargin, double topMargin) {

	//	return (this.parseCoordinates(linesCoordinates, xMax, leftMargin, topMargin) &&
	//			this.parseFinalMoveAndIterations(chosenMoveAndIterations, xMax, leftMargin, topMargin));

	//}

	private boolean parseCoordinates(String linesCoordinates, double yMax, int gameTurn) {

		this.series = new ArrayList<XYSeries>();

		String[] coordinatesString = linesCoordinates.split(" ");

		if(coordinatesString.length % 4 != 0) {
			System.out.println("Wrong number of coordinate values for turn " + this.gameTurn +
					"! Found " + coordinatesString.length + " single coordinate values. Ignoring game turn in the printing of the tree!");
			return false;
		}

		try {
			int index = 0;
			while(index <= coordinatesString.length - 4) {

				double x1 = Double.parseDouble(coordinatesString[index]);
				double x2 = Double.parseDouble(coordinatesString[index+1]);
				double y1 = Double.parseDouble(coordinatesString[index+2]) + yMax;
				double y2 = Double.parseDouble(coordinatesString[index+3]) + yMax;

				//series.add(this.toJFCSeries(x1, x2, y1, y2);

				index+=4;
			}
		}catch(NumberFormatException nfe){
			System.out.println("Wrong format of coordinate values for turn " + this.gameTurn + "!");
					/*The error is in the following four coordinates: [ " +
					coordinatesString[index] + " " + coordinatesString[index+1] + " " +
					coordinatesString[index+2] + " " + coordinatesString[index+3] + " ]");*/
			return false;
		}

		return true;
	}

	private boolean parseFinalMoveAndIterations(String chosenMoveAndIterations, double yMax) {

		String[] chosenMoveAndIterationsString = chosenMoveAndIterations.split(" ");

		if(chosenMoveAndIterationsString.length != 6) {
			System.out.println("Wrong number of entries for chosen move and iterations for turn " + this.gameTurn +
					"! Found " + chosenMoveAndIterationsString.length + " enrties instead of 6!");
			return false;
		}

		try {

			this.totalIterations = Integer.parseInt(chosenMoveAndIterationsString[0]);
			int nextTurn = Integer.parseInt(chosenMoveAndIterationsString[1]);

			if(nextTurn != this.gameTurn+1) {
				System.out.println("Wrong format of log file. Current turn is " + this.gameTurn +
						", but the line with the chosen move reports " + nextTurn + " as the next turn!");
				return false;
			}

			double x1 = Double.parseDouble(chosenMoveAndIterationsString[2]);
			double x2 = Double.parseDouble(chosenMoveAndIterationsString[3]);
			double y1 = Double.parseDouble(chosenMoveAndIterationsString[4]) + yMax;
			double y2 = Double.parseDouble(chosenMoveAndIterationsString[5]) + yMax;

			//this.finalMoveLine = new JFCLine(x1, x2, y1, y2);

		}catch(NumberFormatException nfe){
			System.out.println("Wrong format of coordinate values for final move of turn " + this.gameTurn + "!");
			return false;
		}

		return true;
	}

	public XYSeries toJFCSeries(double x1, double x2, double y1, double y2, int count) {
		XYSeries s = new XYSeries("" + this.gameTurn + "." + count);
		s.add(x1, y1);
		s.add(x2, y2);
		return s;
	}

	@Override
	public String toString() {

		String svgString = "";

	//	if(!(this.lines == null)) {
	//		for(SVGLine line : this.lines) {
	//			svgString += "<line " + line + " stroke=" + SVGTree.DEFAULT_STROKE + " stroke-width=" + SVGTree.DEFAULT_STROKE_WIDTH + " />";
	//			svgString += "\n";
	//		}
	//	}
	//	svgString += "<line " + this.finalMoveLine + " stroke=" + SVGTree.DEFAULT_CHOSEN_MOVE_STROKE + " stroke-width=" + SVGTree.DEFAULT_CHOSEN_MOVE_STROKE_WIDTH + " />";
	//	svgString += "\n";

		return svgString;
	}

	public int getTotIterations() {
		return this.totalIterations;
	}

	public int getTurn() {
		return this.gameTurn;
	}

}
