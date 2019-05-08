package csironi.ggp.course.statsSummarizer.treeDrawer.svg;

import java.util.ArrayList;

/**
 * This class prints the tree vertically, while the logs are logging coordinates to print horizontally,
 * therefore coordinates from the file are inverted so that the x here corresponds to y in the file and viceversa
 *
 * @author C.Sironi
 *
 */
public class SVGGameTurn {

	private int gameTurn;

	private int totalIterations;

	//private RGBColor strokeColor;

	/**
	 * Lines corresponding to actions added to the tree in this game turn
	 */
	private ArrayList<SVGLine> lines;
	/**
	 * Line corresponding to the action selected at the end of the game turn
	 */
	private SVGLine finalMoveLine;

	public SVGGameTurn(int gameTurn) {
		this.gameTurn = gameTurn;
	}

	public boolean initializeGameTurn(String linesCoordinates, String chosenMoveAndIterations, double xMax, double leftMargin, double topMargin) {

		return (this.parseCoordinates(linesCoordinates, xMax, leftMargin, topMargin) &&
				this.parseFinalMoveAndIterations(chosenMoveAndIterations, xMax, leftMargin, topMargin));

	}

	private boolean parseCoordinates(String linesCoordinates, double xMax, double leftMargin, double topMargin) {

		this.lines = new ArrayList<SVGLine>();

		String[] coordinatesString = linesCoordinates.split(" ");

		if(coordinatesString.length % 4 != 0) {
			System.out.println("Wrong number of coordinate values for turn " + this.gameTurn +
					"! Found " + coordinatesString.length + " single coordinate values. Ignoring game turn in the printing of the tree!");
			return false;
		}

		try {
			int index = 0;
			while(index <= coordinatesString.length - 4) {

				double x1 = Double.parseDouble(coordinatesString[index+2]);
				double x2 = Double.parseDouble(coordinatesString[index+3]);
				double y1 = Double.parseDouble(coordinatesString[index]);
				double y2 = Double.parseDouble(coordinatesString[index+1]);

				x1 = (x1 * SVGTree.X_SCALE) + xMax + leftMargin;
				x2 = (x2 * SVGTree.X_SCALE) + xMax + leftMargin;
				y1 = (y1 * SVGTree.Y_SCALE) + topMargin;
				y2 = (y2 * SVGTree.Y_SCALE) + topMargin;

				lines.add(new SVGLine(x1, x2, y1, y2));

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

	private boolean parseFinalMoveAndIterations(String chosenMoveAndIterations, double xMax, double leftMargin, double topMargin) {

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

			double x1 = Double.parseDouble(chosenMoveAndIterationsString[4]);
			double x2 = Double.parseDouble(chosenMoveAndIterationsString[5]);
			double y1 = Double.parseDouble(chosenMoveAndIterationsString[2]);
			double y2 = Double.parseDouble(chosenMoveAndIterationsString[3]);

			x1 = (x1 * SVGTree.X_SCALE) + xMax + leftMargin;
			x2 = (x2 * SVGTree.X_SCALE) + xMax + leftMargin;
			y1 = (y1 * SVGTree.Y_SCALE) + topMargin;
			y2 = (y2 * SVGTree.Y_SCALE) + topMargin;

			this.finalMoveLine = new SVGLine(x1, x2, y1, y2);

		}catch(NumberFormatException nfe){
			System.out.println("Wrong format of coordinate values for final move of turn " + this.gameTurn + "!");
			return false;
		}

		return true;
	}

	@Override
	public String toString() {

		String svgString = "";

		if(!(this.lines == null)) {
			for(SVGLine line : this.lines) {
				svgString += "<line " + line + " stroke=" + SVGTree.DEFAULT_STROKE + " stroke-width=" + SVGTree.DEFAULT_STROKE_WIDTH + " />";
				svgString += "\n";
			}
		}
		svgString += "<line " + this.finalMoveLine + " stroke=" + SVGTree.DEFAULT_CHOSEN_MOVE_STROKE + " stroke-width=" + SVGTree.DEFAULT_CHOSEN_MOVE_STROKE_WIDTH + " />";
		svgString += "\n";

		return svgString;
	}

	public int getTotIterations() {
		return this.totalIterations;
	}

	public int getTurn() {
		return this.gameTurn;
	}


}
