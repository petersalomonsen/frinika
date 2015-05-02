// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.core;

import java.util.Iterator;
import java.util.List;
import uk.org.toot.service.*;
import uk.org.toot.audio.spi.AudioServiceProvider;

/**
 * AudioServices specialises Services with static methods to simplify the
 * provision of plugin audio services extending AudioProcess and AudioControls.
 */
public class AudioServices extends Services
{
    private static List<AudioServiceProvider> providers =
        new java.util.ArrayList<AudioServiceProvider>();

    static {
        scan();
    }

    protected AudioServices() { // prevent direct instantiation
    }

    public static String lookupModuleName(int providerId, int moduleId) {
        String name;
		for ( AudioServiceProvider provider : providers ) {
            if ( provider.getProviderId() == providerId ) {
	            name = provider.lookupName(moduleId);
    	        if ( name != null ) {
            	    return name;
            	}
            }
        }
        return null;
    }

    public static AudioControls createControls(int providerId, int moduleId, int instanceIndex) {
    	AudioControls controls;
		for ( AudioServiceProvider provider : providers ) {
            if ( provider.getProviderId() == providerId ) {
	            controls = provider.createControls(moduleId);
    	        if ( controls != null ) {
    	        	controls.setProviderId(providerId);
    	        	if ( instanceIndex > 0 ) {
    	        		// properly disambiguate the name
    	        		controls.setName(controls.getName()+" #"+(instanceIndex+1));
    	        		controls.setInstanceIndex(instanceIndex);
    	        	}
            	    return controls;
            	}
            }
        }
        return null;
    }

    public static AudioControls createControls(String name) {
        AudioControls controls;
		for ( AudioServiceProvider provider : providers ) {
            controls = provider.createControls(name);
            if ( controls != null ) {
                controls.setProviderId(provider.getProviderId());
                return controls;
            }
        }
        return null;
    }

    public static AudioProcess createProcess(AudioControls controls) {
        AudioProcess process;
		for ( AudioServiceProvider provider : providers ) {
            process = provider.createProcessor(controls);
            if ( process != null ) return process;
        }
        return null;
    }

    public static void scan() {
        Iterator<AudioServiceProvider> it = lookup(AudioServiceProvider.class);
        providers.clear();
        while ( it.hasNext() ) {
            providers.add((AudioServiceProvider)it.next());
        }
    }

    public static void accept(ServiceVisitor v, Class<?> clazz) {
		for ( AudioServiceProvider provider : providers ) {
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
    }
}

