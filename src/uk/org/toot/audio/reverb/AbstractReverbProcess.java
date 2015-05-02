// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.reverb;

import static uk.org.toot.audio.core.FloatDenormals.zeroDenorm;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.SimpleAudioProcess;

/**
 * @author st
 *
 */
public abstract class AbstractReverbProcess extends SimpleAudioProcess
{
    public int processAudio(AudioBuffer buffer) {
        cacheVariables();
        buffer.monoToStereo();
        float[] samplesL = buffer.getChannel(0);
        float[] samplesR = buffer.getChannel(1);
        int ns = buffer.getSampleCount();
        for ( int i = 0; i < ns; i++ ) {
            reverb(samplesL[i], samplesR[i]);
            samplesL[i] = left();
            samplesR[i] = right();
        }
        return AUDIO_OK;
    }

    abstract protected void cacheVariables();
    
    abstract protected void reverb(float right, float left);
    
    abstract protected float left();
    
    abstract protected float right();
   
    protected class Filter
    {
        private float zm1 = 0;
        
        public float filter(float sample, float k) {
            return zm1 = zeroDenorm(k * (zm1 - sample) + sample);
        }
    }
    
    /**
     * A fixed length delay that can be tapped
     * @author st
     */
    protected class Delay
    {
        private float[] line;
        private int head = 0;
        private int length;
        private final int maxLength;
        
        public Delay(int length) {
            line = new float[length];
            maxLength = length;
            this.length = length;
        }
        
        public float read() {
            return line[head];
        }
        
        public void append(float sample) {
            line[head++] = sample;
            if ( head > length-1 ) head = 0;            
        }
        
        public float delay(float sample) {
            float s = read();
            append(sample);
            return s;
        }
        
        public float tap(int zm) {
            assert zm > 0;
            assert zm < length;
            int p = head - zm;
            if ( p < 0 ) p += length;
            return line[p];
        }
        
        public void resize(float factor) {
            length = (int)(factor * maxLength);
            if ( head > length-1 ) head = 0; // may glitch!            
        }
    }
    
    protected class Diffuser extends Delay
    {
        private float b = 0;
        
        public Diffuser(int length) {
            super(length-1);
        }
        
        public float diffuse(float sample, float k) {
            float a = sample - k * b;
            float out = k * a + b;
            b = zeroDenorm(delay(a));
            return out;
        }

        // TODO remove -1 in ctor, remove b
        // is denormal needed?
        // signed are different! dattoro is odd one out
        public float diffuse2(float sample, float k) {
            float out = read();
            float in = sample + k * out;
            append(in);
            return out - k * in;
        }

    }
}
