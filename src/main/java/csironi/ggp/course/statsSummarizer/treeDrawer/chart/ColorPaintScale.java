package csironi.ggp.course.statsSummarizer.treeDrawer.chart;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.renderer.PaintScale;

public class ColorPaintScale implements PaintScale {

	private final double lowerBound;
    private final double upperBound;

    public ColorPaintScale(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public double getLowerBound() {
        return lowerBound;
    }

    @Override
    public double getUpperBound() {
        return upperBound;
    }

    /**
     * Give a value returns the corresponding color. Note that the color changes only for each game step, which is
     * represented by an integer number. Therefore, whenever a non-integer number is passed as value, it will first
     * be transformed to the closest bigger integer.
     */
	@Override
	public Paint getPaint(double value) {

		//System.out.println(value);

		double intValue = Math.floor(value);

		//System.out.println(intValue);

		int r = 0;
		int g = 0;
		int b = 0;

		int rescaledValue = (int) Math.round((intValue-this.lowerBound)/(this.upperBound-this.lowerBound) * (255.0*4.0));

		//System.out.println(rescaledValue);

		int colorInterval = rescaledValue/255;

		//System.out.println(colorInterval);

		int colorValue = rescaledValue - (colorInterval*255);

		//System.out.println(colorValue);

		switch(colorInterval) {
			case 0: // R=255, G=colorValue, B=0
				r = 255;
				g = colorValue;
				b = 0;
				break;
			case 1: // R=255-colorValue, G=255, B=0
				r = 255-colorValue;
				g = 255;
				b = 0;
				break;
			case 2: // R=0, G=255, B=colorValue
				r = 0;
				g = 255;
				b = colorValue;
				break;
			case 3: // R=0, G=255-colorValue, B=255
				r = 0;
				g = 255-colorValue;
				b = 255;
				break;
			case 4: // R=0, G=0, B=255
				r = 0;
				g = 0;
				b = 255;
				break;
			default:
				System.out.println("Something went wrong when computing the color!");
				break;
		}

		return new Color(r, g, b);
	}

}
