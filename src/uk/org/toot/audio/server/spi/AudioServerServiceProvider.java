// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.server.spi;

import uk.org.toot.service.*;
import uk.org.toot.audio.server.*;
import java.util.List;

public abstract class AudioServerServiceProvider extends ServiceProvider
{
    protected List<ServiceDescriptor> servers;

    /**
     * Constructs an <code>AudioServiceProvider</code> with a given
     * provider name and version identifier.
     *
     * @param providerName the provider name.
     * @param version a version identifier.
     *
     * @exception IllegalArgumentException if <code>providerName</code>
     * is <code>null</code>.
     * @exception IllegalArgumentException if <code>version</code>
     * is <code>null</code>.
     */
    public AudioServerServiceProvider(int providerId, String providerName, String description, String version) {
        super(providerId, providerName, description, version);
        servers = service(AudioServer.class);
    }

    public AudioServer createServer(String name) {
        for ( ServiceDescriptor d : servers ) {
            try {
	            if ( d.getName().equals(name) ) {
    	            return (AudioServer)d.getServiceClass().newInstance();
        	    }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public abstract AudioServerConfiguration createServerConfiguration(AudioServer server);
    
    public AudioServerConfiguration createServerSetup(AudioServer server) {
    	return null;
    }
}
