// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control;

import static uk.org.toot.misc.Localisation.getString;

import java.awt.Color;

public class DefaultInsertColorer implements InsertColorer
{
	public Color getColor(LawControl control) {
    	String units = control.getLaw().getUnits();
    	if ( units.equals("ms") ) {								
    		return Color.RED.darker();							// Time
    	} else if ( units.equals("Hz") ) {						
    		return Color.YELLOW;								// Frequency
    	} else if ( units.equals(getString("semitones")) ) {
			return Color.YELLOW;    							// Frequency
    	} else if ( units.equals("dB") ) {						
    		if ( control.getLaw().getMinimum() >= -60 ||
                    control.getLaw().getMaximum() < 0 ) {
    			return Color.WHITE;
    		} else {
    			return Color.BLACK;								// Amplitude
    		}
    	} else if ( control.getName().equals(getString("Resonance")) ) {
    		return Color.ORANGE;
    	} else if ( control.getName().equals(getString("Level")) ) {
    		return Color.BLACK;
    	}
    	return null;
	}

}
