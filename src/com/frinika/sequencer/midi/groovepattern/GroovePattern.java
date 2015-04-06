/*
 * Created on Mar 6, 2007
 *
 * Copyright (c) 2007 Jens Gulden
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.frinika.sequencer.midi.groovepattern;

/**
 * Groove pattern. For quantzing with a "humanized" rhythm "feeling".
 * 
 * @author Jens Gulden
 */
public interface GroovePattern {

	/**
	 * Returns the name of the groove pattern.
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * Suggests a tick position corresponding to the "feeling" of the represented groove.
	 * 
	 * @param tick
	 * @param quantizeResolution resolution of a hard-quantized grid which is the target for quantization
	 * @param smudge factor for implementation-specific intensity if the possible de-quantization, -1.0 .. 1.0
	 * @param velocityByRef returns the velocity value suggested by the groove pattern for that tick
	 * @return
	 */
	public long quantize(long tick, int quantizeResolution, float smudge, int[] velocityByRef);
	
}
