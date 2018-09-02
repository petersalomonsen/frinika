// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.server;

import java.util.Properties;
import java.util.Observable;
import java.util.Enumeration;

/**
 * An AudioServerConfiguration seperates the configuration concerns from
 * an AudioServer.
 * A suitable implementation for a particular AudioServer implementation is 
 * instantiated as a provided service by:
 *     AudioServerServices.createServerConfiguration(AudioServer server);
 * @author Steve Taylor
 */
public abstract class AudioServerConfiguration extends Observable
{
	/**
	 * Return the Properties representing our server's configuration.
	 * @return Properties
	 */
    public abstract Properties getProperties();
    
    /**
     * Apply passed Properties for those keys which are recognized.
     * @param properties
     */
    public abstract void applyProperties(Properties properties);
    
    /**
     * Merge our properties into the specifed properties
     * @param properties the properties to be merged into
     */
    public void mergeInto(Properties properties) {
    	if ( properties == null ) {
    	   System.err.println("null properties passed to AudioServerConfiguration.mergeInto(...)");
    	   return;
    	}
		Properties scp = getProperties();
		for ( Enumeration e = scp.propertyNames(); e.hasMoreElements(); ) {
			String key = (String)e.nextElement();
			if ( key == null ) {
				System.err.println("null key from AudioServerConfiguration.getProperties() ignored");
				continue;
			}
			String value = scp.getProperty(key);
			if ( value == null ) {
				System.err.println("null value for key '"+key+"' from AudioServerConfiguration.getProperties() ignored");
				continue;
			}
			properties.setProperty(key, value);
		}
    	
    }
    
    /*
     * When an Observer is notified it should call mergeInto() or getProperties() 
     */
    public void update() {
    	setChanged();
    	notifyObservers();
    }
}
