package csironi.ggp.course.statsSummarizer.treeDrawer.chart;

import java.awt.Color;
import java.awt.Paint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

public class JFCTree {

	/**
	 * Given a file with compressed tree plot logs without duplicate edges creates a .jpeg file containing the
	 * plot of the tree with the given color scale.
	 *
	 * Two arguments expected:
	 * [treePlotLogsFilePath] = path of the file containing the tree logs to be plotted
	 * [scaleType] = type of color scale to be used. The type can be one of the following:
	 * 		- COLOR: color scale with 255*4 distinct colors ranging from red->yellow-green-light blue
	 * 		- EXTENDED_COLOR: color scale with 255*6 distinct colors, cycling as follows:
	 * 		  green->lightblue->blue->purple->red->yellow->green
	 * 		- GRAY_SMALL: gray scale with 9 distinct shades of gray (no white)
	 * 		- GRAY_BIG: gray scale with 16 distinct shades of gray (no white)
	 * 		- REPEATED_COLOR: samples the EXTENDED_COLOR scale X times and the X obtained colors are
	 * 		  repeated cyclically (at the moment X=30)
	 * 		- REPEATED_DARK_COLOR: dark color scale with 15 distinct colors
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		if(args.length != 2) {
			System.out.println("Expecting 2 inputs. Specify the path of the .csv file to be plotted and the type of scale to be used (COLOR|EXTENDED_COLOR|GRAY_SMALL|GRAY_BIG|REPEATED_COLOR|REPEATED_DARK_COLOR)");
			return;
		}

		String filePath = args[0];
		String scaleType = args[1];

		File file = new File(filePath);

		//String outputFile = file.getParent() + "/Tree.svg";

		JFCTree tree = new JFCTree(file, scaleType);

		tree.parseTree();

		//StatsUtils.writeToFile(outputFile, toLog);
	}


	// Limit on the number of turns being plotted (i.e. limit on the plotted depth of the tree)
	public static int DEPTH_LIMIT = Integer.MAX_VALUE;

	/**
	 *
	 */
	private File logFile;

	private String outputFile;

	private String gameKey;

	private String scaleType;

	/**
	 * Lines corresponding to actions added to the tree in this game turn
	 */
	//private ArrayList<XYSeries> series;


	public JFCTree(File logFile, String scaleType) {
		this.logFile = logFile;
		String filename = logFile.getName();
		this.gameKey = filename.split("-")[1];

		this.outputFile = logFile.getParent() + "/" + logFile.getName().substring(0, logFile.getName().length()-4) + ".jpeg";

		this.scaleType = scaleType;
	}


	public boolean parseTree() {

		/**
		 * For each turn, memorizes the number of edges added to the tree, which corresponds to the number of series
		 * added to the plot;
		 */
		int[] edgesPerTurn;

		/**
		 * For each turn, memorizes the number of simulations performed in the game
		 */
		int[] iterationsPerTurn;

		XYSeriesCollection dataset = new XYSeriesCollection();

		//BufferedReader br;
		String theLine;
		String theNextLine;
		String[] splitLine;

		int playedTurns;
		int xMin;
		int xMax;
		double yMin;
		double yMax;

		int xMinForPlot;
		int xMaxForPlot;
		double yMinForPlot;
		double yMaxForPlot;

		try {
			BufferedReader br = new BufferedReader(new FileReader(logFile));
			// First line has total number of played turns, total number of simulations, min and
			// max tree depth (x coordinate) and min and max branches per depth (y coordinate)
			theLine = br.readLine();

			splitLine = theLine.split(" ");

			if(splitLine.length != 6) {
				System.out.println("Wrong format for first line. Deteced " + splitLine.length +
						" entries instead of 6!");
				br.close();
				return false;
			}

			try {
				playedTurns = Integer.parseInt(splitLine[0]);
				//int totalGameIterations = Integer.parseInt(splitLine[1]);
				xMin =  Integer.parseInt(splitLine[2]);
				xMax =  Integer.parseInt(splitLine[3]);
				yMin =  Double.parseDouble(splitLine[4]);
				yMax =  Double.parseDouble(splitLine[5]);
			}catch(NumberFormatException nfe) {
				System.out.println("Wrong format of coordinate values for the first line of the file!");
				br.close();
				return false;
			}

			edgesPerTurn = new int[playedTurns];
			iterationsPerTurn = new int[playedTurns];

			xMinForPlot = xMin + 1;
			xMaxForPlot = xMax + 1;
			yMinForPlot = yMin + yMax;
			yMaxForPlot = yMax + yMax;

			theLine = br.readLine();
			theNextLine = br.readLine();

			int currentTurn = 1;

			while((theLine != null && theNextLine !=null && !theNextLine.isEmpty()) && currentTurn <= DEPTH_LIMIT) {

				//System.out.println("Turn " + currentTurn);

				if(!this.parseGameTurn(theLine, theNextLine, yMax, currentTurn, dataset, edgesPerTurn, iterationsPerTurn)) {
					System.out.println("Error initializing turn " + currentTurn + "!");
					br.close();
					return false;
				}

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

		if(!this.createAndSaveTreePlot(dataset, edgesPerTurn, xMinForPlot, xMaxForPlot, yMinForPlot, yMaxForPlot)) {
			System.out.println("Error creating the plot!");
			return false;
		}

		return true;

	}

	private boolean createAndSaveTreePlot(XYSeriesCollection dataset, int[] edgesPerTurn, int xMinForPlot,
			int xMaxForPlot, double yMinForPlot, double yMaxForPlot) {

		//System.out.println("Plot");

		JFreeChart xylineChart = ChartFactory.createXYLineChart("TreePlot-" + this.gameKey, "Turn", "Branches", dataset,
				PlotOrientation.HORIZONTAL, false, false, false); //Sets x to be displayed vertically and y horizontally

		XYPlot plot = xylineChart.getXYPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setDomainGridlinesVisible(false); // Remove grid lines for x
		plot.setRangeGridlinesVisible(false); // Remove grid lines for y
		plot.getDomainAxis().setInverted(true); // Invert x axis
		plot.getDomainAxis().setRange(xMinForPlot, xMaxForPlot);
		plot.getRangeAxis().setRange(yMinForPlot, yMaxForPlot);
		NumberAxis axisX = (NumberAxis)plot.getDomainAxis();
		axisX.setTickUnit(new NumberTickUnit(1));
		//NumberAxis axisY = (NumberAxis)plot.getRangeAxis();
		//axisY.setTickUnit(new NumberTickUnit(100));

		plot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);

		TreePaintScale scale;

		switch(this.scaleType) {
			case "COLOR":
				scale = new ColorPaintScale(1, xMaxForPlot);
				break;
			case "EXTENDED_COLOR":
				scale = new ExtendedColorPaintScale(1, xMaxForPlot);
				break;
			case "GRAY_SMALL":
				scale = new SmallRepeatedGrayPaintScale(1, xMaxForPlot);
				break;
			case "GRAY_BIG":
				scale = new BigRepeatedGrayPaintScale(1, xMaxForPlot);
				break;
			case "REPEATED_COLOR":
				scale = new RepeatingColorPaintScale(1, xMaxForPlot, 30);
				break;
			case "REPEATED_DARK_COLOR":
				scale = new RepeatedDarkColorPaintScale(1, xMaxForPlot);
				break;
			default:
				System.out.println("Unrecognized print scale type " + this.scaleType + ".");
				return false;
		}

		PaintScaleLegend legend;
		try {
			legend = new PaintScaleLegend(scale, (ValueAxis) plot.getDomainAxis().clone());
		} catch (CloneNotSupportedException e) {
			System.out.println("Exception when creating the scale legend of the plot!");
        	e.printStackTrace();
        	return false;
		}
		xylineChart.addSubtitle(legend);
		legend.setPosition(RectangleEdge.LEFT);
		legend.setStripWidth(30);
		legend.setMargin(8.4, 0.0, 48.0, 0.0); // Top, left, bottom, right

		plot.getDomainAxis().setVisible(false);

		XYItemRenderer renderer = plot.getRenderer();

		Paint turnColor;
		Paint moveColor = scale.getSelectedActionColor();
		int currentSeries = 0;
		for(int turn = 1; turn <= edgesPerTurn.length; turn++) {

			turnColor = scale.getPaint(turn);

			for(int series = 0; series < edgesPerTurn[turn-1]; series++) {
				renderer.setSeriesPaint(currentSeries , turnColor); // Set color of edges in the turn
				currentSeries++;
			}

			renderer.setSeriesPaint(currentSeries , moveColor); // Set to black the color of the move selected at the end of the turn
			currentSeries++;

		}

		//System.out.println("Save");

		try {
			ChartUtilities.saveChartAsJPEG(new File(this.outputFile), xylineChart, 1000, 1000);
		} catch (IOException e) {
			System.out.println("Exception when creating the .jpeg image of the plot!");
        	e.printStackTrace();
        	return false;
		}

		return true;

	}

	private boolean parseGameTurn(String linesCoordinates, String chosenMoveAndIterations, double yMax, int gameTurn,
			XYSeriesCollection dataset, int[] edgesPerTurn, int[] iterationsPerTurn) {

		return (this.parseCoordinates(linesCoordinates, yMax, gameTurn, dataset, edgesPerTurn, iterationsPerTurn) &&
				this.parseFinalMoveAndIterations(chosenMoveAndIterations, yMax, gameTurn, dataset, edgesPerTurn, iterationsPerTurn));

	}

	/**
	 * Parses the coordinates of the edges added in the given game turn and creates the corresponding series
	 * of points to plot
	 *
	 * @param edgesCoordinates coordinates of all the edges added in the turn.
	 * @param yMax maximum value for the y coordinate, which corresponds to the number of branches for a step.
	 * This value is used to shift all the tree to the positive quadrant of the plot.
	 * @param gameTurn game turn for which edges are being parsed.
	 * @return true if everything went well, false if there has been any error.
	 */
	private boolean parseCoordinates(String linesCoordinates, double yMax, int gameTurn,
			XYSeriesCollection dataset, int[] edgesPerTurn, int[] iterationsPerTurn) {

		String[] coordinatesString = linesCoordinates.split(" ");

		if(coordinatesString.length % 4 != 0) {
			System.out.println("Wrong number of coordinate values for turn " + gameTurn +
					"! Found " + coordinatesString.length + " single coordinate values. Ignoring game turn in the printing of the tree!");
			return false;
		}

		int count = 0;
		try {
			int index = 0;
			while(index <= coordinatesString.length - 4) {

				double x1 = Double.parseDouble(coordinatesString[index]) + 1;
				double x2 = Double.parseDouble(coordinatesString[index+1]) + 1;
				double y1 = Double.parseDouble(coordinatesString[index+2]) + yMax;
				double y2 = Double.parseDouble(coordinatesString[index+3]) + yMax;

				dataset.addSeries(this.toJFCSeries(x1, x2, y1, y2, gameTurn, count));

				index+=4;
				count++;
			}
			edgesPerTurn[gameTurn-1] = count; // Turns start at 1 but the array starts at 0
		}catch(NumberFormatException nfe){
			System.out.println("Wrong format of coordinate values for turn " + gameTurn + "!");
					/*The error is in the following four coordinates: [ " +
					coordinatesString[index] + " " + coordinatesString[index+1] + " " +
					coordinatesString[index+2] + " " + coordinatesString[index+3] + " ]");*/
			return false;
		}

		return true;
	}

	/**
	 * Parses the move selected at the end of the given game turn and creates the corresponding series
	 * of points to plot.
	 *
	 * @param chosenMoveAndIterations coordinates of the move selected at the end of a turn, with the
	 * number of simulations performed in the turn and the number of the next turn.	 *
	 * @param yMax maximum value for the y coordinate, which corresponds to the number of branches for a step.
	 * This value is used to shift all the tree to the positive quadrant of the plot.
	 * @param gameTurn game turn for which edges are being parsed.
	 * @return true if everything went well, false if there has been any error.
	 */
	private boolean parseFinalMoveAndIterations(String chosenMoveAndIterations, double yMax, int gameTurn,
			XYSeriesCollection dataset, int[] edgesPerTurn, int[] iterationsPerTurn) {

		String[] chosenMoveAndIterationsString = chosenMoveAndIterations.split(" ");

		if(chosenMoveAndIterationsString.length != 6) {
			System.out.println("Wrong number of entries for chosen move and iterations for turn " + gameTurn +
					"! Found " + chosenMoveAndIterationsString.length + " enrties instead of 6!");
			return false;
		}

		try {

			iterationsPerTurn[gameTurn-1] = Integer.parseInt(chosenMoveAndIterationsString[0]);
			int nextTurn = Integer.parseInt(chosenMoveAndIterationsString[1]);

			if(nextTurn != gameTurn+1) {
				System.out.println("Wrong format of log file. Current turn is " + gameTurn +
						", but the line with the chosen move reports " + nextTurn + " as the next turn!");
				return false;
			}

			double x1 = Double.parseDouble(chosenMoveAndIterationsString[2]) +1;
			double x2 = Double.parseDouble(chosenMoveAndIterationsString[3]) +1; // Edges added in step t will have their starting point with coordinate x=t
			double y1 = Double.parseDouble(chosenMoveAndIterationsString[4]) + yMax;
			double y2 = Double.parseDouble(chosenMoveAndIterationsString[5]) + yMax;

			dataset.addSeries(this.toJFCSeries(x1, x2, y1, y2, gameTurn, edgesPerTurn[gameTurn-1]));

		}catch(NumberFormatException nfe){
			System.out.println("Wrong format of coordinate values for final move of turn " + gameTurn + "!");
			return false;
		}

		return true;
	}

	private XYSeries toJFCSeries(double x1, double x2, double y1, double y2, int gameTurn, int count) {
		XYSeries s = new XYSeries("" + gameTurn + "." + count);
		s.add(x1, y1);
		s.add(x2, y2);
		return s;
	}

}
