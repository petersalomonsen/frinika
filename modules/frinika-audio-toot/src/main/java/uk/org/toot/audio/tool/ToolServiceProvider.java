// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.tool;

import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.audio.spi.TootAudioServiceProvider;

import static uk.org.toot.misc.Localisation.*;

public class ToolServiceProvider extends TootAudioServiceProvider
{
    public ToolServiceProvider() {
        super(getString("Tools"), "0.1");
		String family = description;
        addControls(DenormalControls.class, ToolIds.DENORMAL_ID, "Denormaliser", family, "0.1");
        addControls(NaNTectorControls.class, ToolIds.NAN_TECTOR_ID, "NaN Tector", family, "0.1");
        addControls(FormatControls.class, ToolIds.FORMAT_ID, "Format", family, "0.1");
        addControls(NoiseControls.class, ToolIds.NOISE_ID, "Noise", family, "0.1");
        addControls(SineControls.class, ToolIds.SINE_ID, "Sine", family, "0.1");
        addControls(MonoControls.class, ToolIds.MONO_ID, "Mono", family, "0.1");

        add(DenormalProcess.class, "Denormaliser", family, "0.1");
        add(NaNTectorProcess.class, "NaN Tector", family, "0.1");
        add(FormatProcess.class, "Format", family, "0.1");
        add(NoiseProcess.class, "Noise", family, "0.1");
        add(SineProcess.class, "Sine", family, "0.1");
        add(MonoProcess.class, "Mono", family, "0.1");
    }

    public AudioProcess createProcessor(AudioControls c) {
        if ( c instanceof DenormalControls ) {
            return new DenormalProcess((DenormalControls)c);
        } else if ( c instanceof NaNTectorControls ) {
            return new NaNTectorProcess((NaNTectorControls)c);
        } else if ( c instanceof FormatControls ) {
            return new FormatProcess((FormatControls)c);
        } else if ( c instanceof NoiseControls ) {
            return new NoiseProcess((NoiseControls)c);
        } else if ( c instanceof SineControls ) {
            return new SineProcess((SineControls)c);
        } else if ( c instanceof MonoProcess.Variables ) {
            return new MonoProcess((MonoProcess.Variables)c);
        }
        return null; // caller then tries another provider
    }
}
