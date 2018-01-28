// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.mixer;

import uk.org.toot.audio.mixer.MixControls;
import uk.org.toot.audio.core.*;
import uk.org.toot.control.*;
import java.util.List;

import static uk.org.toot.audio.mixer.MixerControlsIds.*;
import static uk.org.toot.audio.mixer.MixControlIds.*;
import static uk.org.toot.misc.Localisation.*;

/**
 * MainMixControls add a route control to MixControls.
 */
public class MainMixControls extends MixControls implements MainMixVariables
{
    /**
     * @supplierCardinality 0..1
     * @link aggregationByValue 
     */
    private EnumControl routeControl;

    public MainMixControls(MixerControls mixerControls,	int stripId,
        					BusControls busControls, boolean isMaster) {
		super(mixerControls, stripId, busControls, isMaster);
    }

    protected EnumControl createRouteControl(int stripId) {
        // the main bus has an internal route unless it's the main strip or an aux strip
        if ( stripId != MAIN_STRIP && stripId != AUX_STRIP ) {
			routeControl = new RouteControl(
                mixerControls.getControls().get(0).getName(), // !!!
                stripId == CHANNEL_STRIP);
        	return routeControl;
        }
        return null;
    }

    public EnumControl getRouteControl() { return routeControl; }

    protected class RouteControl extends EnumControl
    {
        private boolean canRouteToGroups = true;

        public RouteControl(String defaultRoute, boolean canRouteToGroups) {
            super(ROUTE, getString("Route"), defaultRoute);
            this.canRouteToGroups = canRouteToGroups;
            indicator = !canRouteToGroups;
        }

        public List getValues() {
            List<String> values = new java.util.ArrayList<String>();
            for ( Control control : mixerControls.getControls() ) {
                if ( control instanceof AudioControlsChain ) {
    	            // can only route to main and group strips
        	        if ( control.getId() == MAIN_STRIP ||
                        (control.getId() == GROUP_STRIP && canRouteToGroups) ) {
            	    	values.add(control.getName());
        			}
                }
            }
            return values; // !!!
        }
    }
}
