// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.dynamics;

import static uk.org.toot.misc.Localisation.getString;

import org.tritonus.share.sampled.TVolumeUtils;

import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.LogLaw;

import static uk.org.toot.dsp.FastMath.pow;

/**
 * A Bus Compressor which has a log VCA so it is controlled in dB. This means attack and
 * release curves remain curves whereas a linear vca makes them straight lines (in dB).
 * A soft knee is implemented by a cubic hermite spline interpolater.
 * Intended for use on busses because it isn't cheap because of per sample log and pow.
 * @author st
 */
public class BusCompressor extends ClassicDynamicsProcess
{
    protected float inverseRatio, oneMinusInverseRatio;
    protected float kneedB, halfInverseKneedB;
    protected float thresholddB, thresholdMinusKneedB, thresholdPlusKneedB;
    private float y1, y2; // cubic hermite spline initial and final values
    private float m1, m2; // cubic hermite spline initial and final tangents

    public BusCompressor(DynamicsVariables vars) {
        super(vars);
    }

    /**
     * Called once per AudioBuffer
     */
    @Override
    protected void cacheProcessVariables() {
        super.cacheProcessVariables();
        inverseRatio = vars.getInverseRatio();
        thresholddB = vars.getThresholddB();
        kneedB = vars.getKneedB();
        deriveLocalVariables();
    }
    
    protected void deriveLocalVariables() {
        float twoKneedB = kneedB * 2;
        halfInverseKneedB = 1f / twoKneedB;
        y1 = thresholddB - kneedB;
        y2 = thresholddB + inverseRatio * kneedB;
        // we have to scale the gradients because we normalised kneedB * 2 input to be 1
        m1 = twoKneedB;                 // initial tangent = 1 scaled
        m2 = twoKneedB * inverseRatio;  // final tangent = inverseRatio scaled
        thresholdMinusKneedB = thresholddB - kneedB;
        thresholdPlusKneedB = thresholddB + kneedB;
        oneMinusInverseRatio = 1f - inverseRatio;
    }
    
    /**
     * @return the gain reduction in dB
     */
    @Override
    protected float function(float value) {
        if ( value < threshold * 0.1f ) return 0f; // optimised quietness to avoid log
        float dB = (float)TVolumeUtils.lin2log(value * inverseThreshold);
        if ( dB <= thresholdMinusKneedB ) {
            return 0f;
        } else if ( dB >= thresholdPlusKneedB ) {
            return (dB - thresholddB) * oneMinusInverseRatio;
        }
        // cubic hermite spline for knee
        // gives us output dB so we subtract from input dB for gain reduction
        float t = (dB - thresholdMinusKneedB) * halfInverseKneedB;
        float t2 = t*t;
        float t3 = t2*t;
        return dB - ((t3 - 2*t2 + t)*m1 + 
                     (2*t3 - 3*t2 + 1)*y1 + 
                     (-2*t3 + 3*t2)*y2 + 
                     (t3 - t2)*m2);   
    }
    
//    @Override
    protected float dynamics(float target) {
        target += 0.001f; // prevent denormals
        float factor = target > envelope ?  attack : release;
        envelope = factor * (envelope - target) + target;
        return (float)pow(10, -envelope * 0.05f); // for log VCA
     }

    public static class Controls extends DynamicsControls
    {
        private final static ControlLaw ATTACK_LAW = new LogLaw(0.1f, 30f, "ms");
        private final static ControlLaw RELEASE_LAW = new LogLaw(30f, 3000f, "ms");

        public Controls() {
            super(DynamicsIds.BUS_COMPRESSOR, getString("Bus.Comp"));
        }

        public Controls(String name, int idOffset) {
            super(DynamicsIds.BUS_COMPRESSOR, name, idOffset);
        }

        protected ControlLaw getAttackLaw() { return ATTACK_LAW; }
        
        protected ControlLaw getReleaseLaw() { return RELEASE_LAW; }
        
        protected boolean hasGainReductionIndicator() { return true; }

        protected boolean hasDryGain() { return true; }

        protected boolean hasGain() { return true; }
        
        protected boolean hasKnee() { return true; }
        
        protected boolean hasInverseRatio() { return true; }
        
        protected boolean hasRMS() { return true; }
    }
}
