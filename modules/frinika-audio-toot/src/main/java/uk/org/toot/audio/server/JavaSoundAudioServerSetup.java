// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.server;

import java.util.Properties;

/**
 * This class allows the sample rate of the JavSoundAudioServer be set before
 * inputs and outputs are set.
 * It should be useful for sound cards that work at 48KHz, SB Live, etc.
 * The sample rate property is not prexifed with the server key because it is
 * expected that projects or hardware will need to work at a certain sample rate
 * whatever server they are using.
 * The sample rate property units are Hz.
 * @author st
 *
 */
public class JavaSoundAudioServerSetup extends AudioServerConfiguration
{
	private final static String SAMPLE_RATE = "sample.rate";
	
	private JavaSoundAudioServer server;
	
	public JavaSoundAudioServerSetup(JavaSoundAudioServer server) {
		this.server = server;
	}
	
	public Properties getProperties() {
		Properties p = new Properties();
		p.setProperty(SAMPLE_RATE, String.valueOf(server.getSampleRate()));
		return p;
	}

	public void applyProperties(Properties p) {
    	if ( p == null ) {
     	   System.err.println("null properties passed to JavaSoundAudioServerSetup.applyProperties()");
     	   return;
     	}
		String value;
		value = p.getProperty(SAMPLE_RATE);
		if ( value != null ) {
			server.setSampleRate(Float.valueOf(value));
		}
	}
}
