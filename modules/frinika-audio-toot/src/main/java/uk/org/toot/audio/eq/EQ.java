// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.eq;

import java.util.List;
//import java.util.ArrayList;
import uk.org.toot.control.*;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.audio.filter.FilterSpecification;

/**
 * EQ is the abstract base AudioProcess class for all forms of EQ.
 */
abstract public class EQ implements AudioProcess
{
    abstract public EQ.Specification getSpecification();

    abstract public int getSize();

    /**
    * Since EQ is a List of Filters, an EQ.Specification is a List
    * of FilterSpecifications.
    */
    public interface Specification {
        List<FilterSpecification> getFilterSpecifications();

        boolean isBypassed();
    }

    /**
     * An abstract base class for the AudioControls for all forms of EQ.
     */
    static abstract public class Controls extends AudioControls implements EQ.Specification {
        private List<FilterSpecification> fdSpecs = null;

        public Controls(int id, String name) {
            super(id, name);
        }

        public boolean hasOrderedFrequencies() { return true; } // EQ sections in frequency order

        // implement EQ.Specification --------------------------------------------
        public List<FilterSpecification> getFilterSpecifications() {
            if (fdSpecs == null) {
                fdSpecs = new java.util.ArrayList<FilterSpecification>();
                for (Control control : getControls()) {
                    if (control instanceof FilterSpecification) {
                        fdSpecs.add((FilterSpecification) control);
                    } else if ( control instanceof CompoundControl ) {
                        for ( Control c : ((CompoundControl)control).getControls() ) {
		                    if ( c instanceof FilterSpecification ) {
        	                	fdSpecs.add((FilterSpecification)c);
                            }
                        }
                    }
                }
            }
            return fdSpecs;
        }
    }
}
