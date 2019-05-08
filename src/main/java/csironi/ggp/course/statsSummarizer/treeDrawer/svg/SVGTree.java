package csironi.ggp.course.statsSummarizer.treeDrawer.svg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SVGTree {

	public static String DEFAULT_STROKE = "\"red\"";
	public static String DEFAULT_STROKE_WIDTH = "\"1\"";
	public static String DEFAULT_CHOSEN_MOVE_STROKE = "\"black\"";
	public static String DEFAULT_CHOSEN_MOVE_STROKE_WIDTH = "\"2\"";
	/** Parameters that control the position and scale of the plot **/
	// Controls the left margin of the plot, where the y axis is situated representing the branch number
	//public static double X_MARGIN = 20;
	// Controls the bottom margin of the plot, where the x axis is situated representing the game steps.
	//public static double Y_MARGIN = 10;
	// Controls the distance between the plot and the axis labels
	public static double INTERNAL_MARGIN_UNIT = 10;
	// Controls the distance between game turns
	public static double X_SCALE = 1;
	// Controls the distance between tree nodes in the same game turn.
	public static double Y_SCALE = 1;
	// Limit on the number of turns being plotted (i.e. limit on the plotted depth of the tree)
	public static int DEPTH_LIMIT = 3; //Integer.MAX_VALUE;
	// Default font size
	// Do not make bigger than 1, because the space to print the text is at most equal to Y_SCALE,
	// which is already used to multiply this unit when computing the FONT_SIZE. Smaller values are
	// feasible if you want to reduce the size of the text with respect to the Y_SCALE.
	public static int FONT_SIZE_UNIT = 1;
	// Actual internal margin depending on the scale
	private static double INTERNAL_MARGIN_X = INTERNAL_MARGIN_UNIT * X_SCALE;
	private static double INTERNAL_MARGIN_Y = INTERNAL_MARGIN_UNIT * Y_SCALE;
	private static double FONT_SIZE = Y_SCALE * FONT_SIZE_UNIT;


	private File logFile;

	private ArrayList<SVGGameTurn> gameTurns;



	// Parameters to distribute elements on the plot area
	private double leftMargin;
	private double topMargin;
	private int maxTurnNumberDigits;
	private double turnMargin;


	public SVGTree(File logFile) {

		this.logFile = logFile;

		this.gameTurns = new ArrayList<SVGGameTurn>();

	}

	public boolean createTree() {

		//BufferedReader br;
		String theLine;
		String theNextLine;
		String[] splitLine;
		SVGGameTurn theTurn;

		try {
			BufferedReader br = new BufferedReader(new FileReader(logFile));
			// First line has total number of played turns, total number of simulations, min and
			// max tree depth (x coordinate) and min and max branches per depth (y coordinate)
			theLine = br.readLine();

			splitLine = theLine.split(" ");

			if(splitLine.length != 6) {
				System.out.println("Wrong format for first line. Deteced " + splitLine.length + " entries instead of 6!");
				br.close();
				return false;
			}

			double xMin;
			double xMax;
			int yMin;
			int yMax;

			try {
				int playedTurns = Integer.parseInt(splitLine[0]);
				int totalGameIterations = Integer.parseInt(splitLine[1]);
				xMin =  Double.parseDouble(splitLine[4]);
				xMax =  Double.parseDouble(splitLine[5]);
				yMin =  Integer.parseInt(splitLine[2]);
				yMax =  Integer.parseInt(splitLine[3]);
			}catch(NumberFormatException nfe) {
				System.out.println("Wrong format of coordinate values for the first line of the file!");
				br.close();
				return false;
			}

			this.maxTurnNumberDigits = Integer.toString(Math.min(yMax, DEPTH_LIMIT)).length(); // yMax corresponds to the depth of the tree that was logged

			this.turnMargin = (FONT_SIZE*this.maxTurnNumberDigits);

			this.leftMargin = 2 * (INTERNAL_MARGIN_X) + this.turnMargin; // Assuming that each digit in the number of turns to plot has width of 1 font-size unit

			this.topMargin = (2 * INTERNAL_MARGIN_Y) + FONT_SIZE;

			theLine = br.readLine();
			theNextLine = br.readLine();

			int currentTurn = 1;

			while((theLine != null && theNextLine !=null && !theNextLine.isEmpty()) && currentTurn <= DEPTH_LIMIT) {

				theTurn = new SVGGameTurn(currentTurn);
				if(!theTurn.initializeGameTurn(theLine, theNextLine, xMax, leftMargin, topMargin)) {
					System.out.println("Error initializing turn " + currentTurn + "!");
					br.close();
					return false;
				}

				this.gameTurns.add(theTurn);

				currentTurn++;

				theLine = br.readLine();
				theNextLine = br.readLine();

			}

			br.close();
		} catch (IOException e) {
			System.out.println("Exception when reading the log file to create the tree.");
        	e.printStackTrace();
        	return false;
		}

		return true;

	}


	@Override
	public String toString() {

		String svgString = "";

		if(!(this.gameTurns == null)) {

			double xCoordForTurn = INTERNAL_MARGIN_X + this.turnMargin;
			double yCoordForTurn = this.topMargin;

			int turnCheck = 1;
			for(SVGGameTurn turn : this.gameTurns) {

				if(turnCheck != turn.getTurn()) {
					System.out.println("Something is wrong with the order of the turns!");
				}

				yCoordForTurn += Y_SCALE;
				svgString += turn;
				svgString += "<text x=\"" + xCoordForTurn + "\" y=\"" + yCoordForTurn +
						"\" font-size=\"" + FONT_SIZE + "\" text-anchor=\"end\">" + turn.getTurn() + "</text>";

				turnCheck++;
			}

		}
		svgString += "\n";

		return svgString;
	}



}
