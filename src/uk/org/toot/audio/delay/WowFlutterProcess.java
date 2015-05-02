// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.delay;

import uk.org.toot.dsp.Phasor;

/**
 * An emulation of tape wow and flutter.
 * @author st
 */
public class WowFlutterProcess extends FrequencyModulationProcess
{
    private Variables vars;
    private float level;
    private float[] amplitudes;
    private Phasor[] phasors;
    private int fuid = -1;
    
    
    public WowFlutterProcess(Variables v) {
        super(v);
        vars = v;
    }

    @Override
    protected void sampleRateChanged() {
        super.sampleRateChanged();
        fuid -= 1; // cause createPhasors to be called
    }
    
    @Override
    protected void cacheProcessVariables() {
        super.cacheProcessVariables();
        level = vars.getLevel();
        int uid = vars.getFrequencyUid();
        amplitudes = vars.getAmplitudes(); // must be after uid!
        if ( fuid != uid ) {
            createPhasors(vars.getFrequencies());
            fuid = uid;
        }
    }
    
    @Override
    protected float modulation() {
        float sample = 0f;
        for ( int i = 0; i < phasors.length; i++ ) {
            sample += phasors[i].out() * amplitudes[i];
        }
        return sample * level;
    }
    
    private void createPhasors(float[] frequencies) {
        final int n = frequencies.length;
        phasors = new Phasor[n];
        for ( int i = 0; i < n; i++ ) {
            phasors[i] = new Phasor(2*Math.PI*frequencies[i]/sampleRate, 0);
        }
    }
    
    
    public interface Variables extends DelayVariables
    {
        float getLevel();           // 0..1
        float[] getAmplitudes();    // 0..1
        int getFrequencyUid();      // a unique id for the current frequencies
        float[] getFrequencies();   // Hz
    }
}
