// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth;

import java.util.Iterator;
import java.util.List;

import uk.org.toot.service.*;
import uk.org.toot.synth.spi.SynthChannelServiceProvider;

/**
 * SynthChannelServices specialises Services with static methods to simplify the
 * provision of synth channel plugins.
 */
public class SynthChannelServices extends Services
{
    private static List<SynthChannelServiceProvider> providers =
        new java.util.ArrayList<SynthChannelServiceProvider>();

    static {
        scan();
    }

    protected SynthChannelServices() { // prevent direct instantiation
    }

    public static String lookupModuleName(int providerId, int moduleId) {
        String name;
		for ( SynthChannelServiceProvider provider : providers ) {
            if ( provider.getProviderId() == providerId ) {
	            name = provider.lookupName(moduleId);
    	        if ( name != null ) {
            	    return name;
            	}
            }
        }
        return null;
    }

    public static SynthChannelControls createControls(String name) {
        SynthChannelControls controls;
		for ( SynthChannelServiceProvider provider : providers ) {
            controls = provider.createControls(name);
            if ( controls != null ) {
                controls.setProviderId(provider.getProviderId());
                return controls;
            }
        }
        return null;
    }

    public static SynthChannel createSynthChannel(SynthChannelControls controls) {
        SynthChannel process;
		for ( SynthChannelServiceProvider provider : providers ) {
            process = provider.createSynthChannel(controls);
            if ( process != null ) {
            	return process;
            }
        }
        return null;
    }

    public static void scan() {
        Iterator<SynthChannelServiceProvider> it = lookup(SynthChannelServiceProvider.class);
        providers.clear();
        while ( it.hasNext() ) {
            providers.add((SynthChannelServiceProvider)it.next());
        }
    }

    public static void accept(ServiceVisitor v, Class<? extends SynthChannelControls> clazz) {
		for ( SynthChannelServiceProvider provider : providers ) {
            provider.accept(v, clazz);
        }
	}

	public static void printServiceDescriptors(Class<? extends SynthChannelControls> clazz) {
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

