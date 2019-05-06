package csironi.ggp.course.statsSummarizer.treeDrawer;

import java.io.File;

import csironi.ggp.course.statsSummarizer.StatsUtils;

public class SvgTreeDrawer {


	public static void main(String[] args) {

		String filePath = args[0];

		File file = new File(filePath);

		String outputFile = file.getParent() + "/Tree.svg";

		SVGTree tree = new SVGTree(file);

		tree.createTree();

		String toLog = "<?xml version=\"1.0\" standalone=\"no\"?>\n" +
				"<svg width=\"10000\" height=\"300\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\">" +
				tree.toString() + "</svg>";

		StatsUtils.writeToFile(outputFile, toLog);
	}

}
