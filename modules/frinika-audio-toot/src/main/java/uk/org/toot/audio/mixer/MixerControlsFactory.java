// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.mixer;

import static uk.org.toot.audio.mixer.MixerControlsIds.*;
import uk.org.toot.audio.core.*;
import java.util.List;

import static uk.org.toot.misc.Localisation.*;

/**
 * MixerControlsFactory adds components to MixerControls
 * in order to separate the creational concerns from MixerControls.
 * Note: create Busses first, then Strips.
 */
public class MixerControlsFactory
{
    /**
     * Create stereo auxiliary busses with default names.
     */
    public static void createBusses(MixerControls mixerControls, int nsends, int naux) {
        String base = getString("FX");
        for ( int i = 0; i < nsends; i++) {
            mixerControls.createFxBusControls(base+'#'+(1+i), ChannelFormat.STEREO);
        }
        base = getString("Aux");
        for ( int i = 0; i < naux; i++ ) {
            mixerControls.createAuxBusControls(base+'#'+(1+i), ChannelFormat.STEREO);
        }
    }

    public static void createBusStrips(MixerControls mixerControls) {
        createBusStrips(mixerControls, "L-R", ChannelFormat.STEREO, mixerControls.getFxBusControls().size());
    }

    public static void createBusStrips(MixerControls mixerControls,
        	String mainStripName, ChannelFormat mainFormat, int nreturns) {
        mixerControls.createStripControls(MAIN_STRIP, 0, mainStripName, mainFormat);
        List<BusControls> busControlsList = mixerControls.getAuxBusControls();
        int naux = busControlsList.size();
        for ( int i = 0; i < naux; i++) {
        	mixerControls.createStripControls(AUX_STRIP, i,
                busControlsList.get(i).getName(), false,
                busControlsList.get(i).getChannelFormat());
        }
        busControlsList = mixerControls.getFxBusControls();
        int nsends = busControlsList.size();
        for ( int i = 0; i < nsends; i++) {
        	mixerControls.createStripControls(FX_STRIP, i,
                busControlsList.get(i).getName(), i < nreturns,
                busControlsList.get(i).getChannelFormat());
        }
    }

    public static void createGroupStrips(MixerControls mixerControls, int ngroups) {
        ChannelFormat mainFormat = mixerControls.getMainBusControls().getChannelFormat();
        for ( int i = 0; i < ngroups; i++) {
        	mixerControls.createStripControls(
                GROUP_STRIP, i, String.valueOf((char)('A'+i)), mainFormat);
        }
    }

    public static void createChannelStrips(MixerControls mixerControls, int nchannels) {
        ChannelFormat mainFormat = mixerControls.getMainBusControls().getChannelFormat();
        for (int i = 0; i < nchannels; i++) {
            mixerControls.createStripControls(
                CHANNEL_STRIP, i, String.valueOf(1 + i), mainFormat);
        }
    }

    static void addMixControls(MixerControls mixerControls,
        	AudioControlsChain controls, final boolean hasMixControls) {
        int stripId = controls.getId();
        if ( stripId == FX_STRIP ) {
            // add FX strip masters first
            // they SHOULD REMAIN FIRST IFF return !!!
            BusControls busControls = mixerControls.getBusControls(controls.getName());
            if ( busControls != null ) {
                MixControls masterControls =
                    new MixControls(mixerControls, stripId, busControls, true) {
                    	public boolean canBeInsertedBefore() { return !hasMixControls; }
                        public boolean canBeMovedBefore() { return !hasMixControls; }
					    public boolean canBeMoved() { return !hasMixControls; }
                	};
       	   		controls.add(masterControls);
            }
        } else if ( stripId == AUX_STRIP ) {
            BusControls busControls = mixerControls.getBusControls(controls.getName());
            if ( busControls != null ) {
                MixControls masterControls =
                    new MixControls(mixerControls, stripId, busControls, true);
       	   		controls.add(masterControls);
            }
        }
        MainMixControls mainMixControls = new MainMixControls(mixerControls,
            stripId, mixerControls.getMainBusControls(), stripId == MAIN_STRIP);
        // main and aux strips don't have mon or fx sends
        if ( stripId != MAIN_STRIP && stripId != AUX_STRIP ) {
	        for ( BusControls busControls : mixerControls.getAuxBusControls() ) {
                if ( hasMixControls ) {
       	    		controls.add(new MixControls(mixerControls,
   	            		stripId, busControls, false));
               	}
            }
	        if ( stripId != FX_STRIP ) {
		        // fx strips CANNOT send to fx bus to avoid feedback
                // aux strips donn't have fx sends
	        	for ( BusControls busControls : mixerControls.getFxBusControls() ) {
            		controls.add(new PostFadeMixControls(mixerControls,
                		stripId, busControls, mainMixControls));
        		}
        	}
        }
        if ( hasMixControls ) {
        	// add the main bus last (so it's at the bottom of the mixer)
        	controls.add(mainMixControls);
        }
	}
}
