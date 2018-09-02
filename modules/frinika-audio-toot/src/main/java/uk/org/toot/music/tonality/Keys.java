// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.music.tonality;

import java.util.List;

/**
 * The Keys class represents all Keys and provides static methods to find
 * specific Keys which match certain criteria.
 * @author st
 *
 */
public class Keys 
{
    /**
     * Return a List of Keys with the specified notes
     */
    static public List<Key> withNotes(int[] keynotes)
    {
        List<Key> match = new java.util.ArrayList<Key>() ;
        
        // iterate Scales
        for ( Scale scale : Scales.getScales() ) {
        	// iterate PitchClasses
        	for ( int pc = 0; pc < 12; pc++ ) {
        		Key key = new Key(pc, scale);
    			if ( key.contains(keynotes) )
                    match.add(key) ;
        	}

        }

        return match ;
    }
    
}
