// Copyright (C) 2007 Steve Taylor.
// Distributed under under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of the License,
// or (at your option) any later version.

package com.frinika.toot.gui;

import com.frinika.toot.javasoundmultiplexed.MultiplexedJavaSoundAudioServer;
import uk.org.toot.audio.server.*;
import uk.org.toot.swingui.audioui.serverui.AudioServerPanel;
import uk.org.toot.swingui.audioui.serverui.spi.AudioServerUIServiceProvider;
import uk.org.toot.audio.id.ProviderId;
import javax.swing.JComponent;

public class FrinikaAudioServerUIServiceProvider
    extends AudioServerUIServiceProvider
{
    public FrinikaAudioServerUIServiceProvider() {
        super(ProviderId.FRINIKA_PROVIDER_ID, "Frinika", "Frinika Audio Server UIs", "0.1");
        // we lazily omit to register individual UI services
        // you can't really choose one
        // you need one that matches the server you've chosen
        // which is what createServerUI does
        // effectively the Abstract Factory design pattern where this
        // is a concrete factory implemented as a plugin service provider
        // or more simply, it works
    }

    public JComponent createServerUI(AudioServer server, AudioServerConfiguration p) {
        if ( server instanceof MultiplexedJavaSoundAudioServer ) {
            return new AudioServerPanel((ExtendedAudioServer)server, p);
        }
        return null; // we can't provide the UI, try another provider
    }

}
