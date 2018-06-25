/*
 * Copyright (c) 2011 Peter Johan Salomonsen (http://petersalomonsen.com) - Licensed under GNU LGPL
 */

package com.frinika.codesynth.control;

import com.frinika.codesynth.CodeSynthMidiChannel;

/**
 * The channel control master is used to apply master effects/controls for a channel such as
 * volume, panning, modulation, reverb etc.
 * @author Peter Johan Salomonsen
 */
public abstract class ChannelControlMaster {
    protected CodeSynthMidiChannel midiChannel;

    public void setMidiChannel(CodeSynthMidiChannel midiChannel) {
        this.midiChannel = midiChannel;
    }

    public void fillBufferBeforeNotes(float[] floatBuffer,int numberOfFrames,int channels)
    {
        for(int n=0;n<numberOfFrames*channels;n+=channels) {
            fillFrameBeforeNotes(floatBuffer,n,channels);
        }
    }

    /**
     * Fill one frame with data
     * @param floatBuffer
     * @param bufferPos
     * @param channels
     */
    public abstract void fillFrameBeforeNotes(float[] floatBuffer,int bufferPos,int channels);

    public void fillBufferAfterNotes(float[] floatBuffer,int numberOfFrames,int channels)
    {
        for(int n=0;n<numberOfFrames*channels;n+=channels) {
            fillFrameAfterNotes(floatBuffer,n,channels);
        }
    }

    /**
     * Fill one frame with data
     * @param floatBuffer
     * @param bufferPos
     * @param channels
     */
    public abstract void fillFrameAfterNotes(float[] floatBuffer,int bufferPos,int channels);
}
