// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)


package uk.org.toot.audio.server;

import uk.org.toot.audio.server.spi.AudioServerServiceProvider;
import uk.org.toot.audio.id.ProviderId;

public class TootAudioServerServiceProvider extends AudioServerServiceProvider
{
    public TootAudioServerServiceProvider() {
        super(ProviderId.TOOT_PROVIDER_ID, "Toot Software", "Toot Audio Servers", "0.1");
//        add(JavaSoundAudioServer.class, "JavaSound (stereo)", "default stereo", "0.4");
        add(MultiIOJavaSoundAudioServer.class, "JavaSound (stereo)", "JavaSound", "0.1");
    }
    
    public AudioServerConfiguration createServerConfiguration(AudioServer server) {
    	if ( server instanceof ExtendedAudioServer ) {
    		return new ExtendedAudioServerConfiguration((ExtendedAudioServer)server);
    	}
    	return null;
    }
    
    public AudioServerConfiguration createServerSetup(AudioServer server) {
    	if ( server instanceof JavaSoundAudioServer ) {
    		return new JavaSoundAudioServerSetup((JavaSoundAudioServer)server);
    	}
    	return null;
    }
}
