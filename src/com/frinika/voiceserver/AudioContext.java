/*
 * Created on Feb 12, 2006
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika.voiceserver;

import com.frinika.audio.*;

/**
 * An audio context is the glue between a Frinika Voice server and the audio interfaces of the
 * operating system. By creating an instance of audio context, you'll automatically get a voice
 * server connected to your operating systems sound resources. 
 * 
 * @author Peter Johan Salomonsen
 */
public class AudioContext {
    private static AudioContext defaultAudioContext = null;
    
    VoiceServer voiceServer = null;
    
    /**
     *  Create an audio context with the given VoiceServer
     *  
     * @param server
     */
    public AudioContext(VoiceServer server) {
        defaultAudioContext = this;
        voiceServer=server;
    }
    
    /**
     * Create a audio context with a Jack voice server if it exists ot JavaSound if not
     * 
     * @throws Exception
     */
    public AudioContext() throws Exception
    {
        voiceServer = new JavaSoundVoiceServer();
        defaultAudioContext = this;
    }

    /**
     * @return Returns the voiceServer.
     */
    public VoiceServer getVoiceServer() {
        return voiceServer;
    }
    
    /**
     * Returns the default audio context
     * @return
     */
    public static final AudioContext getDefaultAudioContext()
    {
        return defaultAudioContext;
    }
}
