/*
 * Copyright (c) Frinika
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.audio.dummy;

import java.util.Properties;
import uk.org.toot.audio.server.AudioServer;
import uk.org.toot.audio.server.AudioServerConfiguration;
import uk.org.toot.audio.server.spi.AudioServerServiceProvider;

/**
 * Dummy audio server provider for simulation of audio servers.
 *
 * @author hajdam
 */
public class DummyAudioServerServiceProvider extends AudioServerServiceProvider {

    static final int DUMMY_PROVIDER_ID = 128;

    public DummyAudioServerServiceProvider() {
        super(DUMMY_PROVIDER_ID, "DummyAudioServer", "Dummy Audio Server", "0.1");
    }

    @Override
    public AudioServerConfiguration createServerConfiguration(AudioServer server) {
        if (server instanceof DummyAudioServer) {
            return new AudioServerConfiguration() {

                @Override
                public Properties getProperties() {
                    return null;
                }

                @Override
                public void applyProperties(Properties properties) {

                }
            };
        }
        return null; // try another provider
    }
}
