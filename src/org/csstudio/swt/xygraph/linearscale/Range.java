/*******************************************************************************
 * Copyright (c) 2008-2009 SWTChart project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.csstudio.swt.xygraph.linearscale;

/** A value range of 'start' ... 'end' or 'lower' .. 'upper'.
 * 
 * Part of code of this class is from SWTChart which is available at http://www.swtchart.org/
 * 
 *  @author Xihui Chen
 *  @author Kay Kasemir Removed a broken and unused copy-constructor, 'final'
 */
public class Range {
    /** the lower value of range */
    final private double lower;

    /** the upper value of range */
    final private double upper;

    /** Initialize with start...end values, sorting them to get lower...upper.
     * 
     * @param start
     *            the start value of range
     * @param end
     *            the end value of range
     */
    public Range(final double start, final double end) {
    	//if(end == start)
    	//	end = start + 1;
        if (start <= end)
        {
            lower = start;
            upper = end;
        }
        else
        {
            lower = end;
            upper = start;
        }
    }

    /**If a value in the range or not.
     * @param value
     * @param includeBoundary true if the boundary should be considered.
     * @return true if the value is in the range. Otherwise false.
     */
    public boolean inRange(final double value, final boolean includeBoundary){
    	if(includeBoundary)
    		return (value >= lower && value <= upper);
    	else
    		return (value > lower && value < upper);
    }
    
    /**If a value in the range or not. The boundary is included.
     * @param value
     * @return true if the value is in the range. Otherwise false.
     */
    public boolean inRange(final double value){
    	return value >= lower && value <= upper;
    }

	/**
	 * @return the lower
	 */
	public double getLower() {
		return lower;
	}

	/**
	 * @return the upper
	 */
	public double getUpper() {
		return upper;
	}

	/** {@inheritDoc} */
	@Override
    public boolean equals(final Object obj)
    {   // See "Effective Java" Item 7
	    if (this == obj)
	        return true;
	    if (! (obj instanceof Range))
	        return false;
	    final Range other = (Range) obj;
	    return other.lower == lower  &&  other.upper == upper;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        // "Effective Java" Item 8: When overriding equals(), also implement hashCode
        int result = (int) Double.doubleToLongBits(lower);
        result = 37*result + (int) Double.doubleToLongBits(upper);
        return result;
    }

    /*
     * @see Object#toString()
     */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return "lower=" + lower + ", upper=" + upper;
    }
}
