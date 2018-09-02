// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.server;

import uk.org.toot.audio.core.*;
import java.util.List;

/**
 * An AudioServer represents the single thread of time and is responsible
 * for separating other code from the concerns of timing and hardware i/o.
 *
 * The timing concern is seperated with setClient(), the server should call
 * work() on the currently set client when it isRunning(), synchronously
 * with hardware i/o.
 * The server can be started with start() and stopped with stop().
 * Also, createAudioBuffer() returns an AudioBuffer suitable for use with
 * the particular timing, it is assumed that work() is called at
 * regular intervals for which the buffer size is appropriate.
 * The server should synchronously resize buffers if the implicit timing is
 * changed.
 *
 * The hardware i/o is abstracted with getAvailableOutputNames() and
 * getAvailableInputNames() to discover the names of hardware output and input
 * lines respectively. The first line returned should be the most obvious stereo
 * line. Stereo lines should be returned before mono lines.
 * Also, createAudioOutput() and createAudioInput() create AudioProcess's
 * backed by named hardware lines with user specified labels.
 * 
 * An AudioServer implementation should use a particular audio format,
 * defined in its own terms, to return appropriate AudioBuffers and
 * IOAudioProcesses.
 * 
 * Latency occurs in hardware and software for both input and output.
 * Total latency from analogue input to analogue output is
 *  Hardware Input + Software Input + Software Output + Hardware Output
 */
public interface AudioServer
{
    final String THREAD_NAME = "AudioServer";

    /**
     * Requests that the server starts if possible, otherwise actual start
     * will be deferred until it is possible.
     * Typically start may become possible after a setClient call and
     * potentially one or more createAudioOutput or createAudioInput calls.
     */
    void start();

    /**
     * Stops the server.
     */
    void stop();

    /**
     * Returns whether running.
     * i.e. started but not stopped.
     * Contract is that AudioClient.work(int nFrames) must be called when true.
     */
    boolean isRunning();

    /**
     * Sets the single AudioClient.
     * Use CompoundAudioClient for multiple client support.
     * Typically start may be deferred until called at least once.
     */
    void setClient(AudioClient client);

    /*#List getAvailableOutputNames();*/
    List<String> getAvailableOutputNames();

    /*#List getAvailableInputNames();*/
    List<String> getAvailableInputNames();

    /**
     * Returns an AudioProcess backed by a hardware audio output line
     * represented by 'name' and labelled 'label'.
     * start may be deferred until called at least once.
     */
    IOAudioProcess openAudioOutput(String name, String label) throws Exception; // !!!

    /**
     * Returns an AudioProcess backed by a hardware audio input line
     * represented by 'name' and labelled 'label'.
     */
    IOAudioProcess openAudioInput(String name, String label) throws Exception; // !!!

    void closeAudioOutput(IOAudioProcess output);

    void closeAudioInput(IOAudioProcess input);

    AudioBuffer createAudioBuffer(String name);
    
    void removeAudioBuffer(AudioBuffer buffer);

    float getSampleRate();

    /**
     * @return the normalised server load 0..1f
     */
    float getLoad();
    
    /**
     * @return the number of frames input latency due to software and hardware.
     */
    int getInputLatencyFrames();
    
    /**
     * @return the number of frames output latency due to software and hardware.
     */
    int getOutputLatencyFrames();
    
    /**
     * @return the number of frames latency for inputs and outputs due to
     * both hardware and software.
     */
    int getTotalLatencyFrames();
}
