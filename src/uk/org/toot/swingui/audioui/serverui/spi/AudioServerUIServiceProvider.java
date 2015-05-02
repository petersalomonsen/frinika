// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.serverui.spi;

import uk.org.toot.service.ServiceProvider;
import uk.org.toot.audio.server.AudioServer;
import uk.org.toot.audio.server.AudioServerConfiguration;
import javax.swing.JComponent;

public abstract class AudioServerUIServiceProvider extends ServiceProvider
{
    /**
     * Constructs an <code>AudioServerUIServiceProvider</code> with a given
     * provider name, description and version identifier.
     *
     * @param providerName the provider name.
     * @param description the description of the provided services
     * @param version a version identifier.
     */
    public AudioServerUIServiceProvider(int providerId, String providerName, String description, String version) {
        super(providerId, providerName, description, version);
    }

    /**
     * Create the UI that is used when the server is running.
     * @param server the AudioServer to provide the UI for.
     * @param p the AudioServerConfiguration to use.
     * @return JComponent the UI.
     */
    public abstract JComponent createServerUI(AudioServer server, AudioServerConfiguration p);
    
    /**
     * Create the UI that is used prior to use of the server.
     * Typically it might allow sample rate to be changed.
     * @param server the AudioServer to provide the UI for.
     * @return JComponent the UI, null representing no UI.
     */
    public JComponent createSetupUI(AudioServer server, AudioServerConfiguration p) {
    	return null;
    }
}
