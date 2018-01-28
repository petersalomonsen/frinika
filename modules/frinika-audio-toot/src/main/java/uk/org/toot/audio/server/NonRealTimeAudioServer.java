// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.server;

import java.util.List;

import uk.org.toot.audio.core.*;

/**
 * An AudioServer that decorates any other AudioServer to add
 * a non-real-time capability.
 */
public class NonRealTimeAudioServer
    implements AudioServer, AudioClient, Runnable
{
    private boolean realTime = true;
    private boolean isRunning = false;

    /**
     * @link aggregation
     * @supplierCardinality 1 
     */
    private AudioServer server;

    /**
     * @link aggregation
     * @supplierCardinality 1 
     */
    private AudioClient client;

    private List<AudioBuffer> buffers = new java.util.ArrayList<AudioBuffer>();

    private Thread thread;

    private boolean startASAP = false;

    public NonRealTimeAudioServer(AudioServer server) {
        this.server = server;
    }

    public void setRealTime(boolean rt) {
        if ( !isRunning() ) {
            realTime = rt;
            return;
        }
        // a server is running so we stop it, change mode and start the new one
        if ( realTime != rt ) {
            try {
	            stop();
            } catch ( Exception e ) {
            }
            realTime = rt;
            // sync the buffer modes
	        for ( AudioBuffer buffer : buffers ) {
            	buffer.setRealTime(realTime);
        	}
//            if ( realTime ) {
                try {
                    start();
	            } catch ( Exception e ) {
    	        }
//            }
            // probably no real point starting the non-real-time server here
            // maybe it needs starting when transport starts
            // and stopping when transport stops
        }
    }

    public boolean isRealTime() {
        return realTime;
    }

    public void start() {
        if ( isRunning() ) return; // already started
        if ( realTime ) {
        	server.start();
        } else {
            startNRT();
        }
    }

    protected void startNRT() {
        if ( client == null ) {
            startASAP = true;
            return;				// deferred start until client is set
        }
   	   	System.out.println("NonRealTimeAudioServer starting");
       	thread = new Thread(this, "NonRealTime"+THREAD_NAME);
        thread.start();
    }

    public void stop() {
        if ( !isRunning() ) return; // already stopped
        if ( realTime ) {
	        server.stop();
        } else if ( isRunning ) {
            stopNRT();
	    }
	}

    protected void stopNRT() {
        // non-real-time stop
   	   	System.out.println("NonRealTimeAudioServer stopping");
       	isRunning = false;
    }

    public void setClient(AudioClient client) {
        server.setClient(this);
        this.client = client;
        if ( startASAP ) {
            startASAP = false;
            start();
        }
    }

    public boolean isRunning() {
        return realTime ? server.isRunning() : isRunning;
    }

    public float getLoad() {
        return realTime ? server.getLoad() : isRunning ? 1.0f : 0.0f;
    }

    public void setEnabled(boolean enable) {
    }

    /**
     * Override this method to synchronise with non-real-time timing
     */
    public void work(int nFrames) {
       	client.work(nFrames);
    }

    /**
     * Public as an implementation side-effect.
     * Override this method to completely take over non-real-time timing
     * on the existing non-real-time thread.
     */
    public void run() {
		isRunning = true;
        while ( isRunning ) {
            work(buffers.get(0).getSampleCount());
            Thread.yield();
        }
    }

    public AudioBuffer createAudioBuffer(String name) {
        AudioBuffer buffer = server.createAudioBuffer(name);
        // we maintain a list of created buffers so that we can switch
        // them between real-time and non-real-time modes.
        buffers.add(buffer);
		return buffer;
    }

    public void removeAudioBuffer(AudioBuffer buffer) {
    	server.removeAudioBuffer(buffer);
    	buffers.remove(buffer);
    }
    
    public List<String> getAvailableOutputNames() {
        return server.getAvailableOutputNames();
    }

    public List<String> getAvailableInputNames() {
        return server.getAvailableInputNames();
    }

    public IOAudioProcess openAudioOutput(String name, String label) throws Exception {
        return server.openAudioOutput(name, label);
    }

    public IOAudioProcess openAudioInput(String name, String label) throws Exception {
        return server.openAudioInput(name, label);
    }

    public void closeAudioOutput(IOAudioProcess output) {
        server.closeAudioOutput(output);
    }

    public void closeAudioInput(IOAudioProcess input) {
        server.closeAudioInput(input);
    }

    public float getSampleRate() {
        return server.getSampleRate();
    }

    public int getInputLatencyFrames() {
    	return server.getInputLatencyFrames();
    }
    
    public int getOutputLatencyFrames() {
    	return server.getOutputLatencyFrames();
    }

    public int getTotalLatencyFrames() {
    	return server.getTotalLatencyFrames();
    }
}
