/*
 * Copyright (c) 2011 Peter Johan Salomonsen (http://petersalomonsen.com) - Licensed under GNU LGPL
 */


package com.frinika.codesynth.note;

import com.frinika.codesynth.CodeSynthMidiChannel;

/**
 *
 * @author Peter Johan Salomonsen
 */
public abstract class Note {
    protected int noteNumber;
    protected int velocity;

    protected CodeSynthMidiChannel midiChannel;

    float sampleRate;

    public void setNoteNumber(int noteNumber) {
        this.noteNumber = noteNumber;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public void setMidiChannel(CodeSynthMidiChannel midiChannel) {
        this.midiChannel = midiChannel;
    }
    
    public void startPlaying() {
        midiChannel.addPlayingNote(this);
        sampleRate = midiChannel.getSynth().getFormat().getSampleRate();
    }
    
    public void release(int velocity) {
        midiChannel.removePlayingNote(this);
    }

    
    public void fillBuffer(float[] floatBuffer,int numberOfFrames,int channels)
    {
        beforeFill();
        for(int n=0;n<numberOfFrames*channels;n+=channels) {
            fillFrame(floatBuffer,n,channels);            
        }
    }

    /**
     * Update radian increment values etc.
     */
    public abstract void beforeFill();

    /**
     * Fill one frame with data
     * @param floatBuffer
     * @param bufferPos
     * @param channels
     */
    public abstract void fillFrame(float[] floatBuffer,int bufferPos,int channels);
}
