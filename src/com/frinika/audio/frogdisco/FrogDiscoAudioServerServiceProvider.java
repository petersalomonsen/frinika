
package com.frinika.audio.frogdisco;

import java.util.Properties;
import uk.org.toot.audio.server.AudioServer;
import uk.org.toot.audio.server.AudioServerConfiguration;
import uk.org.toot.audio.server.ExtendedAudioServer;
import uk.org.toot.audio.server.ExtendedAudioServerConfiguration;
import uk.org.toot.audio.server.spi.AudioServerServiceProvider;
import uk.org.toot.audio.id.ProviderId;


public class FrogDiscoAudioServerServiceProvider extends AudioServerServiceProvider
{
    public FrogDiscoAudioServerServiceProvider() {
        super(ProviderId.FRINIKA_PROVIDER_ID, "FrogDisco", "FrogDisco Audio Server", "0.1");	
        
    }

    @Override
    public AudioServerConfiguration createServerConfiguration(AudioServer server) {	
    	if ( server instanceof FrogDiscoAudioServer ) {
    		return new AudioServerConfiguration() {

                        @Override
                        public Properties getProperties() {
                            return null;
                        }

                        @Override
                        public void applyProperties(Properties prprts) {

                        }
                    };
    	}
    	return null; // try another provider
    }
}
