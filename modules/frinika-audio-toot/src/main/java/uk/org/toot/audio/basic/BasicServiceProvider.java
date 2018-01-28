// Copyright (C) 2006,2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.basic;

import uk.org.toot.audio.spi.TootAudioServiceProvider;
import uk.org.toot.audio.basic.stereoImage.StereoImageControls;
import uk.org.toot.audio.basic.stereoImage.StereoImageProcess;
import uk.org.toot.audio.basic.stereoImage.StereoImageProcessVariables;
import uk.org.toot.audio.basic.tap.TapControls;
import uk.org.toot.audio.basic.tap.TapProcess;
import uk.org.toot.audio.basic.trim.TrimControls;
import uk.org.toot.audio.basic.trim.TrimProcess;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.audio.core.ChannelFormat;

import static uk.org.toot.misc.Localisation.*;
import static uk.org.toot.audio.basic.BasicIds.*;

/**
 * Exposes basic plugins as services
 * @author st
 */
public class BasicServiceProvider extends TootAudioServiceProvider
{
    public BasicServiceProvider() {
        super(getString("Basic"), "0.1");
        String family = getString("Basic");
        
        addControls(TapControls.class, TAP, getString("Tap"), family, "0.1");
        addControls(TrimControls.class, TRIM, getString("Trim"), family, "0.1");
        addControls(StereoImageControls.class, STEREO_IMAGE, 
        		getString("Stereo.Image"), family, "0.1", ChannelFormat.STEREO, null);
        
        add(TapProcess.class, getString("Tap"), family, "0.1");
        add(TrimProcess.class, getString("Trim"), family, "0.1");
        add(StereoImageProcess.class, getString("Stereo.Image"), family, "0.1");
    }

    public AudioProcess createProcessor(AudioControls c) {
    	if ( c instanceof TapControls ) {
    		return new TapProcess((TapControls)c);
    	} else if ( c instanceof TrimControls ) {
        	return new TrimProcess((TrimControls)c);
    	} else if ( c instanceof StereoImageProcessVariables ) {
            return new StereoImageProcess((StereoImageProcessVariables)c);
        } 
        return null; // caller then tries another provider
    }
}
