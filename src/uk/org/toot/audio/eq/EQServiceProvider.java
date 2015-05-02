// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.eq;

import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.audio.spi.TootAudioServiceProvider;

import static uk.org.toot.misc.Localisation.*;

/**
 * The ServiceProvider for Toot EQ.
 */
public class EQServiceProvider extends TootAudioServiceProvider
{
    public EQServiceProvider() {
        super(getString("EQ"), "0.4");
		String family = description;
        addControls(ClassicEQ.Controls.class, EQIds.CLASSIC_EQ_ID, getString("Classic.EQ"), family, "0.1");
        addControls(ParametricEQ.Controls.class, EQIds.PARAMETRIC_EQ_ID, getString("Parametric.EQ"), family, "0.2");
        addControls(GraphicEQ.Controls.class, EQIds.GRAPHIC_EQ_ID, getString("Graphic.EQ"), family, "0.2");
        addControls(CutEQ.Controls.class, EQIds.CUT_EQ_ID, getString("Cut.EQ"), family, "0.1");
        addControls(FormantEQ.Controls.class, EQIds.FORMANT_EQ_ID, getString("Formant.EQ"), family, "0.1");
        addControls(CabEQ.Controls.class, EQIds.CAB_EQ_ID, getString("Cab.EQ"), family, "0.1");

        add(ClassicEQ.class, getString("Classic.EQ"), family, "0.1");
        add(ParametricEQ.class, getString("Parametric.EQ"), family, "0.2");
        add(GraphicEQ.class, getString("Graphic.EQ"), family, "0.2");
        add(CutEQ.class, getString("Cut.EQ"), family, "0.1");
        add(FormantEQ.class, getString("Formant.EQ"), family, "0.1");
        add(CabEQ.class, getString("Cab.EQ"), family, "0.1");
    }

    public AudioProcess createProcessor(AudioControls c) {
        if ( c instanceof ClassicEQ.Controls ) {
            return new ClassicEQ((ClassicEQ.Controls)c);
        } else if ( c instanceof ParametricEQ.Controls ) {
            return new ParametricEQ((ParametricEQ.Controls)c);
        } else if ( c instanceof GraphicEQ.Controls ) {
            return new GraphicEQ((GraphicEQ.Controls)c);
        } else if ( c instanceof CutEQ.Controls ) {
            return new CutEQ((CutEQ.Controls)c);
        } else if ( c instanceof FormantEQ.Controls ) {
            return new FormantEQ((FormantEQ.Controls)c);
        } else if ( c instanceof CabEQ.Controls ) {
            return new CabEQ((CabEQ.Controls)c);
        }
        return null; // caller then tries another provider
    }
}
