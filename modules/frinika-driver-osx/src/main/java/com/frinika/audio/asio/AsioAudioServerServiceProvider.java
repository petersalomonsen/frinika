package com.frinika.audio.asio;

import com.frinika.audio.osx.OSXAudioServer;
import java.util.Properties;
import uk.org.toot.audio.id.ProviderId;
import uk.org.toot.audio.server.AudioServer;
import uk.org.toot.audio.server.AudioServerConfiguration;
import uk.org.toot.audio.server.spi.AudioServerServiceProvider;

public class AsioAudioServerServiceProvider extends AudioServerServiceProvider {

    public AsioAudioServerServiceProvider() {
        super(ProviderId.FRINIKA_PROVIDER_ID, "AsioAudioServer", "Asio Audio Server", "0.1");
    }

    @Override
    public AudioServerConfiguration createServerConfiguration(AudioServer server) {
        if (server instanceof OSXAudioServer) {
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
