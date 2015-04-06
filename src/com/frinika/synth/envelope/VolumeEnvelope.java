/*
 * Created on Feb 26, 2005
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.synth.envelope;

import com.frinika.audio.Decibel;

/**
 * 
 * @author Peter Johan Salomonsen
 *
 */
public final class VolumeEnvelope {
    static final int ENVELOPESTATE_DELAY = 0;
    static final int ENVELOPESTATE_ATTACK = 1;
    static final int ENVELOPESTATE_HOLD = 2;
    static final int ENVELOPESTATE_DECAY = 3;
    static final int ENVELOPESTATE_SUSTAIN = 4;
    static final int ENVELOPESTATE_RELEASE = 5;

    float min;
    float max; 
    float maxdB;
    
    int sampleRate;
    int envelopeState = ENVELOPESTATE_DELAY;  
    
    int delaySampleCount;
    int holdSampleCount;
    
    float attackDeltaLevelPerSample;
    float decayAttenuationPerSample;
    float releaseAttenuationPerSample;
    
    float attenuation; 
    float sustainAttenuation;
    
    boolean isReleased = false;
    
    public VolumeEnvelope(int sampleRate, float mindB, float maxdB)
    {
        this.min = Decibel.getAmplitudeRatio(mindB);
        this.max = Decibel.getAmplitudeRatio(maxdB);
        this.maxdB = maxdB;
        this.attenuation = min;
        
        this.sampleRate = sampleRate;
        this.setDelay(-32768);
        this.setAttack(0);
        this.setHold(-32768);
        this.setDecay(0);
        this.setSustain(400);
        this.setRelease(0);
    }
   
    public final void setDelay(int delayTimeCents)
    {
        delaySampleCount = timeCentsToSampleCount(delayTimeCents);
    }

    public final void setAttack(int attackTimeCents)
    {
        attackDeltaLevelPerSample = 1f / timeCentsToSampleCount(attackTimeCents);
    }
    
    public final void setHold(int holdTimeCents)
    {
        holdSampleCount = timeCentsToSampleCount(holdTimeCents);
    }

    public final void setDecay(int decayTimeCents)
    {
        decayAttenuationPerSample = getAttenuationPerSample(-100f,decayTimeCents);
    }
    
    public final void setSustain(int centiBelsDecrease)
    {
        float dB = (maxdB - (centiBelsDecrease / 10f));
        
        if(dB<-100)
            dB=-100;
        
        sustainAttenuation = Decibel.getAmplitudeRatio(dB); // centiBels
    }

    public final void setRelease(int releaseTimeCents)
    {
        releaseAttenuationPerSample = getAttenuationPerSample(-100f,releaseTimeCents);
    }

    public final void release()
    {
        envelopeState = ENVELOPESTATE_RELEASE;
    }
    
    public final boolean isReleased()
    {
        return isReleased;
    }
    
    public final float getAttenuation()
    {
        switch(envelopeState)
        {
            case ENVELOPESTATE_DELAY:
                if(--delaySampleCount<0)
                    envelopeState = ENVELOPESTATE_ATTACK;
                else
                    break;
            case ENVELOPESTATE_ATTACK:
                if(attenuation>=max)
                    envelopeState = ENVELOPESTATE_HOLD;
                else
                {
                    attenuation+=attackDeltaLevelPerSample;
                    if(attenuation>=max)
                        attenuation=max;
                    break;
                }
            case ENVELOPESTATE_HOLD:
                if(--holdSampleCount<0)
                    envelopeState = ENVELOPESTATE_DECAY;
                else
                    break;
            case ENVELOPESTATE_DECAY:
                if(attenuation<=sustainAttenuation)
                    envelopeState = ENVELOPESTATE_SUSTAIN;
                else
                    attenuation*=decayAttenuationPerSample;
                break;                
            case ENVELOPESTATE_RELEASE:
                if(attenuation<=min)
                    isReleased = true;
                else
                    attenuation*=releaseAttenuationPerSample;
        }
        return attenuation;
    }
    
    final int timeCentsToSampleCount(int timeCents)
    {
        return(int)( (sampleRate*Math.pow(2.0,timeCents/1200.0)) +1);
    }
    
    public final float getAttenuationPerSample(float deciBels, int timeCents)
    {
        return((float)Math.pow(10.0,(deciBels / ( 20 * timeCentsToSampleCount(timeCents)))));
    }
    
    public static void main(String[] args)
    {
        VolumeEnvelope env = new VolumeEnvelope(10,-100f,0);
        env.setAttack(0);
        env.setSustain(500);
        System.out.println(env.timeCentsToSampleCount(7973));
        System.out.println(Decibel.getAmplitudeRatio(-40));
        for(int n=0;n<50;n++)
            System.out.println(n+" "+env.getAttenuation()+" "+env.envelopeState);
        env.release();
        for(int n=0;n<50;n++)
            System.out.println(n+" "+env.getAttenuation()+" "+env.envelopeState);

    }
}
