package csironi.ggp.course.statsSummarizer.treeDrawer.chart;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class JFCLine {

	private double x1;
	private double x2;
	private double y1;
	private double y2;

	public JFCLine(double x1, double x2, double y1, double y2) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
	}

	public XYSeries toJFCSeries(int turn, int sim) {
		XYSeries s = new XYSeries("" + turn + "." + sim);
		s.add(x1, y1);
		s.add(x2, y2);
		return s;
	}

	public static void main(String args[]) {


		XYSeries s1 = new XYSeries("1");
		s1.add(0, 0);
		s1.add(1, 1);
		XYSeries s2 = new XYSeries("2");
		s2.add(0, 0);
		s2.add(1, -1);
		XYSeries s3 = new XYSeries("3");
		s3.add(1, 1);
		s3.add(2, 0);
		XYSeries s4 = new XYSeries("4");
		s4.add(1, 1);
		s4.add(2, 2);
		XYSeries s5 = new XYSeries("5");
		s5.add(0, 0);
		s5.add(1, -1);


		XYSeriesCollection dataset = new XYSeriesCollection();

		dataset.addSeries(s1);
		dataset.addSeries(s2);
		dataset.addSeries(s3);
		dataset.addSeries(s4);
		dataset.addSeries(s5);

		//dataset.


		JFreeChart xylineChart = ChartFactory.createXYLineChart("TreePlot", "Turn", "NumBranches", dataset,
				PlotOrientation.HORIZONTAL, false, false, false); //Sets x to be displayed vertically and y horizontally

		XYPlot plot = xylineChart.getXYPlot();
		plot.setDomainGridlinesVisible(false); // Remove grid lines for x
		plot.setRangeGridlinesVisible(false); // Remove grid lines for y
		plot.getDomainAxis().setInverted(true); // Invert x axis



		XYItemRenderer renderer = plot.getRenderer();
	      renderer.setSeriesPaint( 0 , new Color(255, 0, 0, 0));
	      renderer.setSeriesPaint( 1 , new Color(255, 0, 0, 255));
	      renderer.setSeriesPaint( 2 , new Color(0, 255, 0));
	      renderer.setSeriesPaint( 3 , new Color(0, 255, 255));
	      //renderer.setSeriesPaint( 4 , new Color(0, 0, 0));
	      //plot.setRenderer( renderer );
	      //setContentPane( chartPanel );

	      //PaintScaleLegend legend = new PaintScaleLegend(new LookupPaintScale(),plot.getDomainAxis());
	        //legend.setSubdivisionCount(128);
	        //legend.setAxisLocation(AxisLocation.TOP_OR_RIGHT);
	        //legend.setPadding(new RectangleInsets(10, 10, 10, 10));
	        //legend.setStripWidth(20);
	        //legend.setPosition(RectangleEdge.RIGHT);
	        //legend.setBackgroundPaint(Color.WHITE);

	      //xylineChart.addSubtitle(legend);


		try {
			ChartUtilities.saveChartAsJPEG(new File("C:/Users/c.sironi/Documents/!!!PROVA/provatree6.jpg"), xylineChart, 500, 500);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
