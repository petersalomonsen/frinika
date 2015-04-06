// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package com.frinika.project;

import com.frinika.tools.BufferedPlayback;
import java.util.List;


import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.server.AudioClient;
import uk.org.toot.audio.server.AudioServer;
import uk.org.toot.audio.server.IOAudioProcess;

/**
 * An AudioServer that adapts any other AudioServer to add
 * a non-real-time capability.
 */
public class FrinikaAudioServer
    implements AudioServer
{
    private boolean realTime = true;

    /**
     * @link aggregation
     * @supplierCardinality 1 
     */
    private AudioServer server;

    /**
     * @link aggregation
     * @supplierCardinality 1 
     */

    private List<AudioBuffer> buffers = new java.util.ArrayList<AudioBuffer>();

  
    public FrinikaAudioServer(AudioServer server) {
        this.server = server;
    }

  
	/**
	 *
	 * @return esitmated output latency
	 */

	public double getOutputLatencyMillis() {
		long latInFrames = getTotalLatencyFrames()
				- getInputLatencyFrames();
		return latInFrames * 1000.0 / getSampleRate();
	}

    public void returnAudioServer(Object thief) {
        FrinikaAudioSystem.returnAudioServer(thief);
    }

    public void setRealTime(boolean rt) {
        // a server is running so we stop it, change mode and start the new one

    	if (server.isRunning()) {
    		try {
				throw new Exception(" Maybe not a good idea to set the real time flag whilst server is running ?");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}
    	
    	if ( realTime != rt ) {
            realTime = rt;
            // sync the buffer modes
	        for ( AudioBuffer buffer : buffers ) {
            	buffer.setRealTime(realTime);
        	}
        }
    }

    public boolean isRealTime() {
        return realTime;
    }

    public void start() {
        	server.start();
    }

    public void stop() {
	        server.stop();
	}


    public boolean isRunning() {
        return  server.isRunning();
    }

    public float getLoad() {
        return server.getLoad();
    }



    public AudioBuffer createAudioBuffer(String name) {
        AudioBuffer buffer = server.createAudioBuffer(name);
        // we maintain a list of created buffers so that we can switch
        // them between real-time and non-real-time modes.
        buffers.add(buffer);
		return buffer;
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

    public void setSampleRate(float sampleRate) {
        throw new UnsupportedOperationException();
//        server.setSampleRate(sampleRate);
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

	public void setClient(AudioClient client) {
		server.setClient(client);
	}
	public static AudioServer stealAudioServer(Object thief, AudioClient client) {
        	return FrinikaAudioSystem.stealAudioServer(thief, client);
    }

    public void removeAudioBuffer(AudioBuffer ab) {
        buffers.remove(ab);
    }
    
//	public static double getOutputlatency() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
	
}
