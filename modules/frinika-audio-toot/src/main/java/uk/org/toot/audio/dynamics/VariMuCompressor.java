// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.dynamics;

import static uk.org.toot.misc.Localisation.getString;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.LogLaw;

/**
 * A classic compressor with progressive ratio instead of fixed ratio.
 * @author st
 *
 */
public class VariMuCompressor extends ClassicDynamicsProcess
{
    public VariMuCompressor(DynamicsVariables vars) {
        super(vars);
    }

    @Override
    protected float function(float value) {
        if ( value < threshold ) return 1f;
        float over = value * inverseThreshold;
        int i = (int)over;
        if ( i > table.length-2 ) return table[table.length-1];
        float frac = over - i;
        return table[i] * (1 - frac) + table[i+1] * frac;
    }

    /*
     * A lookup table modelled on Manley Vari Mu
     * 19.5dB gain reduction over 40dB input range
     * accurate to within 0.5dB
     */
    private static float[] table = { 1.0f, 0.932854f, 0.8642038f, 0.8050942f,
            0.75459445f, 0.71100533f, 0.67292094f, 0.63927233f, 0.6092519f,
            0.5822428f, 0.55776614f, 0.53544414f, 0.51497537f, 0.4961141f,
            0.4786599f, 0.46244487f, 0.44732943f, 0.43319473f, 0.4199399f,
            0.40747792f, 0.3957336f, 0.38464162f, 0.37414485f, 0.3641927f,
            0.35474065f, 0.34574914f, 0.33718294f, 0.32901073f, 0.32120374f,
            0.31373683f, 0.3065864f, 0.29973197f, 0.29315424f, 0.28683585f,
            0.2807608f, 0.27491498f, 0.2692846f, 0.26385748f, 0.2586223f,
            0.25356877f, 0.24868676f, 0.24396776f, 0.23940325f, 0.23498534f,
            0.23070695f, 0.22656126f, 0.22254226f, 0.21864356f, 0.2148602f,
            0.21118645f, 0.20761783f, 0.2041498f, 0.20077798f, 0.19749813f,
            0.1943067f, 0.1912001f, 0.18817481f, 0.18522763f, 0.18235548f,
            0.17955561f, 0.17682526f, 0.17416173f, 0.17156264f, 0.16902573f,
            0.16654867f, 0.16412939f, 0.16176598f, 0.15945628f, 0.15719858f,
            0.1549912f, 0.15283251f, 0.15072076f, 0.1486545f, 0.14663227f,
            0.14465284f, 0.1427146f, 0.14081642f, 0.13895716f, 0.13713548f,
            0.13535036f, 0.1336007f, 0.13188554f, 0.13020368f, 0.12855436f,
            0.12693651f, 0.12534945f, 0.123792104f, 0.122263834f, 0.120763764f,
            0.11929111f, 0.117845185f, 0.11642523f, 0.11503068f, 0.11366076f,
            0.11231482f, 0.1109924f, 0.1096928f, 0.108415455f, 0.107159816f,
            0.105925374f };
    
    public static class Controls extends DynamicsControls
    {
        private final static ControlLaw RELEASE_LAW = new LogLaw(30f, 3000f, "ms");

        public Controls() {
            super(DynamicsIds.VARI_MU_COMPRESSOR, getString("Vari.Mu.Comp"));
        }

        public Controls(String name, int idOffset) {
            super(DynamicsIds.VARI_MU_COMPRESSOR, name, idOffset);
        }

        protected ControlLaw getReleaseLaw() { return RELEASE_LAW; }
        
        protected boolean hasGainReductionIndicator() { return true; }

        protected boolean hasDryGain() { return true; }

        protected boolean hasGain() { return true; }

        public boolean isRMS() { return false; }
        
//        protected boolean hasKey() { return true; }
    }

}
