
package com.frinika.audio.osx;


import java.util.Properties;
import uk.org.toot.audio.server.AudioServer;
import uk.org.toot.audio.server.AudioServerConfiguration;
import uk.org.toot.audio.server.spi.AudioServerServiceProvider;
import uk.org.toot.audio.id.ProviderId;


public class OSXAudioServerServiceProvider extends AudioServerServiceProvider
{
    public OSXAudioServerServiceProvider() {
        super(ProviderId.FRINIKA_PROVIDER_ID, "OSXAudioServer", "OSX Audio Server", "0.1");	
        
    }

    @Override
    public AudioServerConfiguration createServerConfiguration(AudioServer server) {	
    	if ( server instanceof OSXAudioServer ) {
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
