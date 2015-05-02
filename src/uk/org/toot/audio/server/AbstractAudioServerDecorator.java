// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.server;

import java.util.List;

import uk.org.toot.audio.core.AudioBuffer;

/**
 * This class is provided so that classes that want to decorate some AudioServer
 * methods don't have to delegate all methods to the decorated AudioServer.
 * @author st
 *
 */
public abstract class AbstractAudioServerDecorator implements AudioServer
{
	private AudioServer server;
	
	public AbstractAudioServerDecorator(AudioServer server) {
		this.server = server;
	}

	public IOAudioProcess openAudioInput(String name, String label) throws Exception {
		return server.openAudioInput(name, label);
	}

	public IOAudioProcess openAudioOutput(String name, String label) throws Exception {
		return server.openAudioOutput(name, label);
	}

	public void closeAudioInput(IOAudioProcess input) {
		server.closeAudioInput(input);
	}

	public void closeAudioOutput(IOAudioProcess output) {
		server.closeAudioOutput(output);
	}

	public List<String> getAvailableInputNames() {
		return server.getAvailableInputNames();
	}

	public List<String> getAvailableOutputNames() {
		return server.getAvailableOutputNames();
	}

	public AudioBuffer createAudioBuffer(String name) {
		return server.createAudioBuffer(name);
	}

	public void removeAudioBuffer(AudioBuffer buffer) {
		server.removeAudioBuffer(buffer);
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

	public boolean isRunning() {
		return server.isRunning();
	}

	public void setClient(AudioClient client) {
		server.setClient(client);
	}

	public void start() {
		server.start();
	}

	public void stop() {
		server.stop();
	}

	public float getSampleRate() {
		return server.getSampleRate();
	}

	public float getLoad() {
		return server.getLoad();
	}
}
