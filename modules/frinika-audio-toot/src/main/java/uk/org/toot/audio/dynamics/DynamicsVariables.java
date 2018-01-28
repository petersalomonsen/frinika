package uk.org.toot.audio.dynamics;

import uk.org.toot.audio.core.AudioBuffer;

public interface DynamicsVariables
{

    public void update(float sampleRate);

    public boolean isBypassed();

    public boolean isRMS();

    public float getThreshold(); //  NOT dB, the actual level

    public float getInverseThreshold();
    
    public float getThresholddB();

    public float getInverseRatio();

    public float getKneedB();

    public float getAttack(); //	NOT ms, the exponential coefficient

    public int getHold(); //	NOT ms, samples

    public float getRelease(); //	NOT ms, the exponential coefficient

    public float getDepth(); //	NOT dB, the actual level

    public float getDryGain(); //  NOT dB, the actual gain factor

    public float getGain(); // 	NOT dB, the actual static makeup gain

    public float getHysteresis(); //  NOT dB, the actual factor

    public void setDynamicGain(float gain); // NOT dB, the actual (sub sampled) dynamic gain

    public AudioBuffer getKeyBuffer();

}