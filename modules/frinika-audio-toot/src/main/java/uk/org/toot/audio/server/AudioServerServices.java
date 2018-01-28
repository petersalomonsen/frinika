// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package uk.org.toot.audio.server;

import java.util.Iterator;
import java.util.List;
import uk.org.toot.service.*;
import uk.org.toot.audio.server.spi.*;

/**
 * AudioServices specialises Services with static methods to simplify the
 * provision of plugin audio services extending AudioProcess and AudioControls.
 */
public class AudioServerServices extends Services
{
    private static List<AudioServerServiceProvider> providers =
        new java.util.ArrayList<AudioServerServiceProvider>();

    static {
        scan();
    }

    protected AudioServerServices() { // prevent direct instantiation
    }

    public static AudioServer createServer(String name) {
        AudioServer server;
		for ( AudioServerServiceProvider provider : providers ) {
            server = provider.createServer(name);
            if ( server != null ) {
                return server;
            }
        }
        return null;
    }

    public static AudioServerConfiguration createServerConfiguration(AudioServer server) {
        AudioServerConfiguration serverProperties;
		for ( AudioServerServiceProvider provider : providers ) {
            serverProperties = provider.createServerConfiguration(server);
            if ( serverProperties != null ) {
                return serverProperties;
            }
        }
        return null;
    }

    public static AudioServerConfiguration createServerSetup(AudioServer server) {
        AudioServerConfiguration serverProperties;
		for ( AudioServerServiceProvider provider : providers ) {
            serverProperties = provider.createServerSetup(server);
            if ( serverProperties != null ) {
                return serverProperties;
            }
        }
        return null;
    }

    public static void scan() {
        Iterator<AudioServerServiceProvider> it = lookup(AudioServerServiceProvider.class);
        providers.clear();
        while ( it.hasNext() ) {
            providers.add((AudioServerServiceProvider)it.next());
        }
    }

    public static void accept(ServiceVisitor v, Class<?> clazz) {
		for ( AudioServerServiceProvider provider : providers ) {
            provider.accept(v, clazz);
        }
	}

	public static void printServiceDescriptors(Class<?> clazz) {
        accept(new ServicePrinter(), clazz);
    }

    public static void main(String[] args) {
        try {
	        printServiceDescriptors(null);
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        try {
            System.in.read();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}

