package csironi.ggp.course.statsSummarizer.treeDrawer.chart;

import java.awt.Color;
import java.awt.Paint;

public class SmallRepeatedGrayPaintScale implements TreePaintScale{

	private final int lowerBound;
    private final int upperBound;

    private final Color[] grayScaleColors;

    public SmallRepeatedGrayPaintScale(double lowerBound, double upperBound) {
        this.lowerBound = (int)Math.floor(lowerBound);
        this.upperBound = (int)Math.floor(upperBound);

        this.grayScaleColors = new Color[9];
        this.grayScaleColors[0] = new Color(0, 0, 0);
        this.grayScaleColors[1] = new Color(51, 51, 51);
        this.grayScaleColors[2] = new Color(77, 77, 77);
        this.grayScaleColors[3] = new Color(102, 102, 102);
        this.grayScaleColors[4] = new Color(128, 128, 128);
        this.grayScaleColors[5] = new Color(153, 153, 153);
        this.grayScaleColors[6] = new Color(179, 179, 179);
        this.grayScaleColors[7] = new Color(204, 204, 204);
        this.grayScaleColors[8] = new Color(230, 230, 230);
    }

    @Override
    public double getLowerBound() {
        return lowerBound;
    }

    @Override
    public double getUpperBound() {
        return upperBound;
    }

	@Override
	public Paint getPaint(double value) {

		int intValue = (int) Math.floor(value);

		return this.grayScaleColors[(intValue-this.lowerBound)%this.grayScaleColors.length];
	}

	@Override
	public Color getSelectedActionColor() {
		return new Color(255,0,0);
	}

}
