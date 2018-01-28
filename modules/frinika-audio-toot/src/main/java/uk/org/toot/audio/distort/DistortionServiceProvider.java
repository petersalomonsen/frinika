// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.distort;

import uk.org.toot.audio.spi.TootAudioServiceProvider;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.AudioControls;

import static uk.org.toot.misc.Localisation.*;
import static uk.org.toot.audio.distort.DistortionIds.*;

/**
 * Exposes distortion as a plugin service
 * @author st
 */
public class DistortionServiceProvider extends TootAudioServiceProvider
{
    public DistortionServiceProvider() {
        super(getString("Distortion"), "0.1");
        String family = description;
        
        addControls(Distort1Controls.class, DISTORT1, getString("Drive"), family, "0.2");
        addControls(GuitarAmpControls.class, GUITAR_AMP, getString("Guitar.Amp"), family, "0.1");
        addControls(BitCrusherControls.class, BIT_CRUSH, getString("BitCrush"),	family, "0.1");
        
        add(Distort1Process.class, getString("Drive"), family, "0.1");
        add(GuitarAmpProcess.class, getString("Guitar.Amp"), family, "0.1");
        add(BitCrusherProcess.class, getString("BitCrush"), family, "0.1");
    }

    public AudioProcess createProcessor(AudioControls c) {
    	if ( c instanceof Distort1Process.Variables ) {
            return new Distort1Process((Distort1Process.Variables)c);
        } else if ( c instanceof BitCrusherProcess.Variables ) {
        	return new BitCrusherProcess((BitCrusherProcess.Variables)c);
        } else if ( c instanceof GuitarAmpProcess.Variables ) {
            return new GuitarAmpProcess((GuitarAmpProcess.Variables)c);
        }
        return null; // caller then tries another provider
    }
}
