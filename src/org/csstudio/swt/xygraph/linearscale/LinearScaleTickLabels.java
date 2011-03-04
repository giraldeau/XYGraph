/*******************************************************************************
 * Copyright (c) 2008-2009 SWTChart project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.csstudio.swt.xygraph.linearscale;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
/**
 * Linear Scale tick labels. Part of code of this class is from SWTChart which is available at http://www.swtchart.org/
 * @author Xihui Chen
 */
public class LinearScaleTickLabels extends Figure {

    /** the array of tick label vales */
    private ArrayList<Double> tickLabelValues;

    /** the array of tick label */
    private ArrayList<String> tickLabels;

    /** the array of tick label position in pixels */
    private ArrayList<Integer> tickLabelPositions;

    /** the array of visibility state of tick label */
    private ArrayList<Boolean> tickVisibilities;

   

	/** the maximum length of tick labels */
    private int tickLabelMaxLength;
    
    /** the maximum height of tick labels */
    private int tickLabelMaxHeight;
    
    private int gridStepInPixel;
    
    private LinearScale scale;

    /**
     * Constructor.
     * 
     * @param linearScale
     *            the scale
     */
    protected LinearScaleTickLabels(LinearScale linearScale) {
    	
    	this.scale = linearScale;
        tickLabelValues = new ArrayList<Double>();
        tickLabels = new ArrayList<String>();
        tickLabelPositions = new ArrayList<Integer>();
        tickVisibilities = new ArrayList<Boolean>();

        setFont(this.scale.getFont());
        setForegroundColor(this.scale.getForegroundColor());
    }

    /**
     * Updates the tick labels.
     * 
     * @param length
     *            scale tick length (without margin)
     */
    protected void update(int length) {
        tickLabelValues.clear();
        tickLabels.clear();
        tickLabelPositions.clear();


        if (scale.isLogScaleEnabled()) {
            updateTickLabelForLogScale(length);
        }else {
            updateTickLabelForLinearScale(length);
        }

        updateTickVisibility();
        updateTickLabelMaxLengthAndHeight();
    }


    /**
     * Updates tick label for log scale.
     * 
     * @param length
     *            the length of scale
     */
    private void updateTickLabelForLogScale(int length) {
        double min = scale.getRange().getLower();
        double max = scale.getRange().getUpper();
        if(min <= 0 || max <= 0)
        	throw new IllegalArgumentException(
        			"the range for log scale must be in positive range");
        if (min >= max) {        	
        	throw new IllegalArgumentException("min must be less than max.");
        }
        
        int digitMin = (int) Math.ceil(Math.log10(min));
        int digitMax = (int) Math.ceil(Math.log10(max));

        final BigDecimal MIN = new BigDecimal(new Double(min).toString());
        BigDecimal tickStep = pow(10, digitMin - 1);
        BigDecimal firstPosition;

        if (MIN.remainder(tickStep).doubleValue() <= 0) {
            firstPosition = MIN.subtract(MIN.remainder(tickStep));
        } else {
            firstPosition = MIN.subtract(MIN.remainder(tickStep)).add(tickStep);
        }
        
      //add min
        if(MIN.compareTo(firstPosition) == -1 ) {
        	tickLabelValues.add(min);
        	if (scale.isDateEnabled()) {
                Date date = new Date((long) MIN.doubleValue());
                tickLabels.add(scale.format(date));
            } else {
                tickLabels.add(scale.format(MIN.doubleValue()));
            }
        	tickLabelPositions.add(scale.getMargin());        	
        }
       
        for (int i = digitMin; i <= digitMax; i++) {
            for (BigDecimal j = firstPosition; j.doubleValue() <= pow(10, i)
                    .doubleValue(); j = j.add(tickStep)) {
                if (j.doubleValue() > max) {
                    break;
                }

                if (scale.isDateEnabled()) {
                    Date date = new Date((long) j.doubleValue());
                    tickLabels.add(scale.format(date));
                } else {
                    tickLabels.add(scale.format(j.doubleValue()));
                }
                tickLabelValues.add(j.doubleValue());

                int tickLabelPosition = (int) ((Math.log10(j.doubleValue()) - Math
                        .log10(min))
                        / (Math.log10(max) - Math.log10(min)) * length)
                        + scale.getMargin();
                tickLabelPositions.add(tickLabelPosition);               
            }
            tickStep = tickStep.multiply(pow(10, 1));
            firstPosition = tickStep.add(pow(10, i));
            
        }
        
        //add max
        if(max > tickLabelValues.get(tickLabelValues.size()-1)) {
        	tickLabelValues.add(max);
        	if (scale.isDateEnabled()) {
                Date date = new Date((long) max);
                tickLabels.add(scale.format(date));
            } else {
                tickLabels.add(scale.format(max));
            }
        	tickLabelPositions.add(scale.getMargin() + length);
        }
    }

    /**
     * Updates tick label for normal scale.
     * 
     * @param length
     *            scale tick length (without margin)
     */
    private void updateTickLabelForLinearScale(int length) {
        double min = scale.getRange().getLower();
        double max = scale.getRange().getUpper();
        BigDecimal gridStepBigDecimal = getGridStep(length, min, max);
        gridStepInPixel = (int) (length * gridStepBigDecimal.doubleValue()/(max - min));
        updateTickLabelForLinearScale(length, gridStepBigDecimal);
    }

    /**
     * Updates tick label for normal scale.
     * 
     * @param length
     *            scale tick length (without margin)
     * @param tickStep
     *            the tick step
     */
    private void updateTickLabelForLinearScale(int length, BigDecimal tickStep) {
        double min = scale.getRange().getLower();
        double max = scale.getRange().getUpper();

        final BigDecimal MIN = new BigDecimal(new Double(min).toString());
        BigDecimal firstPosition;

        //make firstPosition as the right most of min based on tickStep 
        /* if (min % tickStep <= 0) */
        if (MIN.remainder(tickStep).doubleValue() <= 0) {
            /* firstPosition = min - min % tickStep */
            firstPosition = MIN.subtract(MIN.remainder(tickStep));
        } else {
            /* firstPosition = min - min % tickStep + tickStep */
            firstPosition = MIN.subtract(MIN.remainder(tickStep)).add(tickStep);
        }

        // the unit time starts from 1:00
        if (scale.isDateEnabled()) {
            BigDecimal zeroOclock = firstPosition.subtract(new BigDecimal(
                    new Double(3600000).toString()));
            if (MIN.compareTo(zeroOclock) == -1) {
                firstPosition = zeroOclock;
            }
        }
        
        //add min
        if(MIN.compareTo(firstPosition) == -1 ) {
        	tickLabelValues.add(min);
        	if (scale.isDateEnabled()) {
                Date date = new Date((long) MIN.doubleValue());
                tickLabels.add(scale.format(date));
            } else {
                tickLabels.add(scale.format(MIN.doubleValue()));
            }
        	tickLabelPositions.add(scale.getMargin());        	
        }
        	
        for (BigDecimal b = firstPosition; b.doubleValue() <= max; b = b
                .add(tickStep)) {
            if (scale.isDateEnabled()) {
                Date date = new Date((long) b.doubleValue());
                tickLabels.add(scale.format(date));
            } else {
                tickLabels.add(scale.format(b.doubleValue()));
            }
            tickLabelValues.add(b.doubleValue());

            int tickLabelPosition = (int) ((b.doubleValue() - min)
                    / (max - min) * length) + scale.getMargin();
                    //- LINE_WIDTH;
            tickLabelPositions.add(tickLabelPosition);
        }
        
        //add max
        if(max > tickLabelValues.get(tickLabelValues.size()-1)) {
        	tickLabelValues.add(max);
        	if (scale.isDateEnabled()) {
                Date date = new Date((long) max);
                tickLabels.add(scale.format(date));
            } else {
                tickLabels.add(scale.format(max));
            }
        	tickLabelPositions.add(scale.getMargin() + length);
        }
        	
    }

    /**
     * Updates the visibility of tick labels.
     */
    private void updateTickVisibility() {

        // initialize the array of tick label visibility state
        tickVisibilities.clear();
        for (int i = 0; i < tickLabelPositions.size(); i++) {
            tickVisibilities.add(Boolean.TRUE);
        }

        if (tickLabelPositions.size() == 0) {
            return;
        }

        // set the tick label visibility
        int previousPosition = 0;
        String previousLabel = null;
        for (int i = 0; i < tickLabelPositions.size(); i++) {

            // check if there is enough space to draw tick label
            boolean hasSpaceToDraw = true;
            if (i != 0) {
                hasSpaceToDraw = hasSpaceToDraw(previousPosition,
                        tickLabelPositions.get(i), previousLabel, tickLabels.get(i));
            }

            // check if the same tick label is repeated
            String currentLabel = tickLabels.get(i);
            boolean isRepeatSameTickAndNotEnd = currentLabel.equals(previousLabel) &&
            	(i!=0 && i!=tickLabelPositions.size()-1);
            
            // check if the tick label value is major
            boolean isMajorTickOrEnd = true;
            if (scale.isLogScaleEnabled()) {
                isMajorTickOrEnd = isMajorTick(tickLabelValues.get(i)) 
                	|| i==0 || i==tickLabelPositions.size()-1;
            }

            if (!hasSpaceToDraw || isRepeatSameTickAndNotEnd || !isMajorTickOrEnd) {
                tickVisibilities.set(i, Boolean.FALSE);
            } else {
                previousPosition = tickLabelPositions.get(i);
                previousLabel = currentLabel;
            }
        }
    }


    /**
     * Checks if the tick label is major (...,0.01,0.1,1,10,100,...).
     * 
     * @param tickValue
     *            the tick label value
     * @return true if the tick label is major
     */
    private boolean isMajorTick(double tickValue) {
        if (!scale.isLogScaleEnabled()) {
            return true;
        }

        if (Math.log10(tickValue) % 1 == 0) {
            return true;
        }

        return false;
    }

    /**
     * Returns the state indicating if there is a space to draw tick label.
     * 
     * @param previousPosition
     *            the previously drawn tick label position.
     * @param tickLabelPosition
     *            the tick label position.
     *  @param previousTickLabel
     *            the prevoius tick label.          
     * @param tickLabel
     *            the tick label text
     * @return true if there is a space to draw tick label
     */
    private boolean hasSpaceToDraw(int previousPosition, int tickLabelPosition,
            String previousTickLabel, String tickLabel) {
        Dimension tickLabelSize = FigureUtilities.getTextExtents(tickLabel, scale.getFont());
        Dimension previousTickLabelSize = FigureUtilities.getTextExtents(previousTickLabel, scale.getFont());
        int interval = tickLabelPosition - previousPosition;
        int textLength = (int) (scale.isHorizontal() ? (tickLabelSize.width/2.0 + previousTickLabelSize.width/2.0)  
        		: tickLabelSize.height);
        boolean noLapOnPrevoius = interval > textLength;
       
        boolean noLapOnEnd = true;
        if(tickLabelPosition != tickLabelPositions.get(tickLabelPositions.size() - 1)){
        	Dimension endTickLabelSize = FigureUtilities.getTextExtents(
        		tickLabels.get(tickLabels.size()-1), scale.getFont());
        	interval = tickLabelPositions.get(tickLabelPositions.size() - 1) - tickLabelPosition;
        	textLength = (int) (scale.isHorizontal() ? (tickLabelSize.width/2.0 + endTickLabelSize.width/2.0)
        			: tickLabelSize.height);
        	noLapOnEnd = interval > textLength;
        }       
        return noLapOnPrevoius && noLapOnEnd;        
    }

    /**
     * Gets max length of tick label.
     */
    private void updateTickLabelMaxLengthAndHeight() {
        int maxLength = 0;
        int maxHeight = 0; 
        for (int i = 0; i < tickLabels.size(); i++) {
            if (tickVisibilities.size() > i && tickVisibilities.get(i) == true) {
            	Dimension p = FigureUtilities.getTextExtents(tickLabels.get(i), scale.getFont());
            	if (tickLabels.get(0).startsWith("-") && !tickLabels.get(i).startsWith("-")) {
                    p.width += FigureUtilities.getTextExtents("-", getFont()).width;
                }
                if (p.width > maxLength) {
                    maxLength = p.width;
                }
                if(p.height > maxHeight){
                	maxHeight = p.height;
                }
            }
        }
        tickLabelMaxLength = maxLength;
        tickLabelMaxHeight = maxHeight;
    }

    /**
     * Calculates the value of the first argument raised to the power of the
     * second argument.
     * 
     * @param base
     *            the base
     * @param expornent
     *            the exponent
     * @return the value <tt>a<sup>b</sup></tt> in <tt>BigDecimal</tt>
     */
    private BigDecimal pow(double base, int expornent) {
        BigDecimal value;
        if (expornent > 0) {
            value = new BigDecimal(new Double(base).toString()).pow(expornent);
        } else {
            value = BigDecimal.ONE.divide(new BigDecimal(new Double(base)
                    .toString()).pow(-expornent));
        }
        return value;
    }

    
    
    /**
     * Gets the grid step.
     * 
     * @param lengthInPixels
     *            scale length in pixels
     * @param min
     *            minimum value
     * @param max
     *            maximum value
     * @return rounded value.
     */
    private BigDecimal getGridStep(int lengthInPixels, double min, double max) {
    	if((int) scale.getMajorGridStep() != 0) {
    		return new BigDecimal(scale.getMajorGridStep());
    	}
    	
        if (lengthInPixels <= 0) {
            lengthInPixels = 1;
        }
        if (min >= max) {        	
        	if(max == min)
        		max ++;
        	else 
        		throw new IllegalArgumentException("min must be less than max.");
        }

        double length = Math.abs(max - min);
        double gridStepHint = length / lengthInPixels
                * scale.getMajorTickMarkStepHint();
        
        
        if(scale.isDateEnabled()) {
        	//by default, make the least step to be minutes
        	
        	long timeStep;
        	if(max - min < 60000) // < 1 min, step = 1 sec
        		timeStep= 1000l;
        	else if (max -min < 43200000) // < 12 hour, step = 1 min
        		timeStep = 60000l;
        	else if (max - min < 604800000) // < 7 days, step = 1 hour
        		timeStep = 3600000l;
        	else 
        		timeStep = 86400000l;
        		
        	if (scale.getTimeUnit() == Calendar.SECOND) {
        		timeStep = 1000l;
        	} else if (scale.getTimeUnit() == Calendar.MINUTE) {
        		timeStep = 60000l;
        	}else if (scale.getTimeUnit() == Calendar.HOUR_OF_DAY) {
        		timeStep = 3600000l;
        	}else if (scale.getTimeUnit() == Calendar.DATE) {
        		timeStep = 86400000l;
        	}else if (scale.getTimeUnit() == Calendar.MONTH) {
        		timeStep = 30l*86400000l;
        	}else if (scale.getTimeUnit() == Calendar.YEAR) {
        		timeStep = 365l*86400000l;  
        	}
        	double temp = gridStepHint + (timeStep - gridStepHint%timeStep);       	
        	return new BigDecimal(temp);
        }
        	
        
        // gridStepHint --> mantissa * 10 ** exponent
        // e.g. 724.1 --> 7.241 * 10 ** 2
        double mantissa = gridStepHint;
        int exponent = 0;
        if (mantissa < 1) {
            while (mantissa < 1) {
                mantissa *= 10.0;
                exponent--;
            }
        } else {
            while (mantissa >= 10) {
                mantissa /= 10.0;
                exponent++;
            }
        }

        // calculate the grid step with hint.
        BigDecimal gridStep;
        if (mantissa > 7.5) {
            // gridStep = 10.0 * 10 ** exponent
            gridStep = BigDecimal.TEN.multiply(pow(10, exponent));
        } else if (mantissa > 3.5) {
            // gridStep = 5.0 * 10 ** exponent
            gridStep = new BigDecimal(new Double(5).toString()).multiply(pow(
                    10, exponent));
        } else if (mantissa > 1.5) {
            // gridStep = 2.0 * 10 ** exponent
            gridStep = new BigDecimal(new Double(2).toString()).multiply(pow(
                    10, exponent));
        } else {
            // gridStep = 1.0 * 10 ** exponent
            gridStep = pow(10, exponent);
        }
        return gridStep;
    }

    /**
     * Gets the tick label positions.
     * 
     * @return the tick label positions
     */
    public ArrayList<Integer> getTickLabelPositions() {
        return tickLabelPositions;
    }

    @Override
    protected void paintClientArea(Graphics graphics) {
    	graphics.translate(bounds.x, bounds.y);
    	if (scale.isHorizontal()) {
            drawXTick(graphics);
        } else {
            drawYTick(graphics);
        }

    	super.paintClientArea(graphics);
    };

    /**
     * Draw the X tick.
     * 
     * @param grahics
     *            the graphics context
     */
    private void drawXTick(Graphics grahics) {
        // draw tick labels
        grahics.setFont(scale.getFont());
        for (int i = 0; i < tickLabelPositions.size(); i++) {
            if (tickVisibilities.get(i) == true) {
                String text = tickLabels.get(i);
                int fontWidth = FigureUtilities.getTextExtents(text, getFont()).width;
                int x = (int) Math.ceil(tickLabelPositions.get(i) - fontWidth / 2.0);// + offset);
                grahics.drawText(text, x, 0);
            }
        }
    }

    /**
     * Draw the Y tick.
     * 
     * @param grahpics
     *            the graphics context
     */
    private void drawYTick(Graphics grahpics) {
        // draw tick labels
        grahpics.setFont(scale.getFont());
        int fontHeight = tickLabelMaxHeight;
        for (int i = 0; i < tickLabelPositions.size(); i++) {
            if (tickVisibilities.size() == 0 || tickLabels.size() == 0) {
                break;
            }

            if (tickVisibilities.get(i) == true) {
                String text = tickLabels.get(i);
                int x = 0;
                if (tickLabels.get(0).startsWith("-") && !text.startsWith("-")) {
                    x += FigureUtilities.getTextExtents("-", getFont()).width;
                }
                int y = (int) Math.ceil(scale.getLength() - tickLabelPositions.get(i)
                        - fontHeight / 2.0);
                grahpics.drawText(text, x, y);
            }
        }
    }

	/**
	 * @return the tickLabelMaxLength
	 */
	public int getTickLabelMaxLength() {
		return tickLabelMaxLength;
	}

	/**
	 * @return the tickLabelMaxHeight
	 */
	public int getTickLabelMaxHeight() {
		return tickLabelMaxHeight;
	}

	 /**
	 * @return the tickVisibilities
	 */
	public ArrayList<Boolean> getTickVisibilities() {
		return tickVisibilities;
	}
	
	/**
	 * @return the gridStepInPixel
	 */
	public int getGridStepInPixel() {
		return gridStepInPixel;
	}

}
