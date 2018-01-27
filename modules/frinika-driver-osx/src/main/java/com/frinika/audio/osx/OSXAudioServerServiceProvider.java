/*
 * Created on May 13, 2016
 *
 * Copyright (c) 2005-2016 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.audio.osx;

import java.util.Properties;
import uk.org.toot.audio.id.ProviderId;
import uk.org.toot.audio.server.AudioServer;
import uk.org.toot.audio.server.AudioServerConfiguration;
import uk.org.toot.audio.server.spi.AudioServerServiceProvider;

public class OSXAudioServerServiceProvider extends AudioServerServiceProvider {

    public OSXAudioServerServiceProvider() {
        super(ProviderId.FRINIKA_PROVIDER_ID, "OSXAudioServer", "OSX Audio Server", "0.1");
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
