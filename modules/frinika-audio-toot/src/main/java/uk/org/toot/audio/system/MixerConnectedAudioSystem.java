// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.system;

import static uk.org.toot.audio.mixer.MixerControlsIds.CHANNEL_STRIP;

import java.util.List;
import java.util.Collections;

import uk.org.toot.audio.core.AudioControlsChain;
import uk.org.toot.audio.mixer.AudioMixer;
import uk.org.toot.audio.mixer.AudioMixerStrip;
import uk.org.toot.audio.mixer.MixerControls;

/**
 * This class adds a composition of AudioConnections to the composition
 * of AudioDevices.
 * Connection are created and closed automatically without user interaction.
 * When an output port is added a connection is made to a mixer strip.
 * When an output port is removed the connection to the mixer strip is closed.
 * But auto connect should be disabled during snapshot configuration,
 * because connections will be restored somewhere else.
 * However auto disconnect should still be allowed during snapshot configuration.
 * Each port may only have a single connection.
 */
public class MixerConnectedAudioSystem extends DefaultAudioSystem
	implements ConnectedAudioSystem
{
    private List<AudioConnection> connections;
    private AudioMixer mixer;
    private MixerControls mixerControls;

    public MixerConnectedAudioSystem(AudioMixer mixer) {
        connections = new java.util.ArrayList<AudioConnection>();
        this.mixer = mixer;
        mixerControls = mixer.getMixerControls();        
    }

    /** 
     * every added or removed device or port will arrive here
     */
    @Override
    public void notifyObservers(Object obj) {
    	if ( obj instanceof AudioDevice ) {
    		AudioDevice device = (AudioDevice)obj;
    		boolean added = getAudioDevices().contains(device);
//    		String change = added ? "added" : "removed";
//    		System.out.println("AudioDevice "+device.getName()+" "+change);
   			List<AudioOutput> outputs = device.getAudioOutputs();
   			for ( AudioOutput output : outputs ) {
   				if ( added ) {
//   	   				System.out.println("AudioOutput "+output.getName()+" already present");
   					if ( autoConnect ) {
   						createConnectionFrom(output);
   					}
   				} else {
   					closeConnectionFrom(output);
   				}
    		}
    	} else if ( obj instanceof AudioOutput ) {
    		AudioOutput output = (AudioOutput)obj;
    		boolean added = getAudioOutputs().contains(output);
//    		String change = added ? "added" : "removed";
//    		System.out.println("AudioOutput "+output.getName()+" "+change);
    		if ( added ) {
    			if ( autoConnect ) {
    				createConnectionFrom(output);
    			}
    		} else {
    			closeConnectionFrom(output);
    		}
    	}
    	super.notifyObservers(obj);
    }
    
    // to enable storing of connections
    public List<AudioConnection> getConnections() {
        return Collections.unmodifiableList(connections);
    }

    // to enable restoring of connections
	public void createConnection(String fromPortName, String fromPortLocation, String toPortName, int flags) {
		AudioOutput from = getOutputPort(fromPortName, fromPortLocation);
		if ( from == null ) {
			throw new IllegalArgumentException(fromPortName+" @ "+fromPortLocation+" does not exist");
		}
		AudioMixerStrip to = mixer.getStrip(toPortName);
		if ( to == null ) {
			throw new IllegalArgumentException(toPortName+" does not exist");
		}
		createConnection(from, to, flags);
	}

	// to enable auto connection
    protected void createConnection(AudioOutput from, AudioMixerStrip to, int flags) {
    	if ( getConnectionFrom(from.getName(), from.getLocation()) != null ) {
    		return; //throw new IllegalStateException(from.getName()+" is already connected");
    	}
    	AudioConnection connection = new MixerInputConnection(from, to, flags);
    	connections.add(connection);
    	setChanged();
    	notifyObservers();    	
    }
    
/*	public void closeConnection(String fromPortName, String fromPortLocation, String toPortName) {
		AudioConnection connection = getConnection(fromPortName, fromPortLocation, toPortName);
		closeConnection(connection); 
	}

 	public void closeConnection(AudioOutput from, AudioMixerStrip to) {
    	closeAConnection(from.getName(), from.getLocation(), to.getName());
    }
    
    protected AudioConnection getConnection(String from, String fromLocation, String to) {
    	for ( AudioConnection c : connections ) {
    		if ( c.getAudioOutputName().equals(from) &&
    			 c.getAudioOutputLocation().equals(fromLocation) &&
    			 c.getAudioInputName().equals(to) ) {
    			return c;
    		}
    	}
    	throw new IllegalArgumentException(
    		"No AudioConnection from "+from+" to "+to);
    } */
    
    protected AudioConnection getConnectionFrom(String from, String fromLocation) {
    	for ( AudioConnection c : connections ) {
    		if ( c.getOutputName().equals(from) &&
    			 c.getOutputLocation().equals(fromLocation) ) {
    			return c;
    		}
    	}
    	return null;
    }
    
    /**
     * Auto Connect an AudioOutput to an AudioMixerStrip.
     * Try to find an unsed miser strip, otherwise create a new mixer strip.
     * @param output the AudioOutput to connect from
     */
    protected void createConnectionFrom(AudioOutput output ) {
		AudioControlsChain stripControls;
		AudioControlsChain namedControls;
		try {
			AudioMixerStrip strip = mixer.getUnusedChannelStrip();
			if ( strip == null ) {
				int i = -1;
				int max = 1 + mixerControls.getControls().size();
				String name;
				do {
					stripControls = mixerControls.getStripControls(CHANNEL_STRIP, ++i);
					name = String.valueOf(i+1);
					namedControls = mixerControls.getStripControls(name);
				} while ( stripControls != null && namedControls != null && i < max );
				// no strip with same number or name
				mixerControls.createStripControls(CHANNEL_STRIP, i, name);
				strip = mixer.getStrip(name);
				if ( strip == null ) {
					System.err.println("Failed to create mixer strip for "+nameAndLocation(output));
					return;
				}
			}
			createConnection(output, strip, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Auto Disconnect an AudioOutput from an AudioMixerStrip
     * @param output the AudioOutput to disconnect
     */
    protected void closeConnectionFrom(AudioOutput output ) {
    	String name = output.getName();
    	String location = output.getLocation();
    	AudioConnection connection = getConnectionFrom(name, location);
    	if ( connection == null ) {
    		System.err.println("Failed to close connection from "+name+" @ "+location);
    		return; 
    	}
    	closeConnection(connection);
    }
    
    protected void closeConnection(AudioConnection connection) {
    	connections.remove(connection);
    	connection.close();
    	setChanged();
    	notifyObservers();    	
    }
    
    protected AudioOutput getOutputPort(String name, String location) {
    	for ( AudioDevice device : getAudioDevices() ) {
//    		String deviceName = device.getName();
    		List<AudioOutput> outputs = device.getAudioOutputs();
    		for ( AudioOutput output : outputs ) {
    			if ( name.equals(output.getName()) && location.equals(output.getLocation()) ) {
    				return output;
    			}
    		}
    	}
		throw new IllegalArgumentException("AudioOutput "+name+" @ "+location+" not found");
    }

    protected String nameAndLocation(AudioOutput output) {
    	return output.getName()+" @ "+output.getLocation();
    }
    
    /**
     * This class represents an audio connection to a mixer strip.
     * The connection may be created with various flags.
     * 
     * @author st
     */
    protected class MixerInputConnection extends AudioConnection
    {
        /**
         * @link aggregation
         * @supplierCardinality 1 
         */
        private AudioOutput from;

        /**
         * @link aggregation
         * @supplierCardinality 1 
         */
        private AudioMixerStrip to;
        
        public MixerInputConnection(AudioOutput from, AudioMixerStrip to) {
        	this(from, to, 0);
        }
        
        /**
         * Create a connection from a MidiOutput to a MidiInput with
         * the specified flags.
         * @param from the MidiOutput to connect from
         * @param to the MidiInput to connect to.
         * @param flags for the connection.
         */
        public MixerInputConnection(AudioOutput from, AudioMixerStrip to, int flags) {
        	super(flags);
        	if ( from == null || to == null ) {
        		throw new IllegalArgumentException("MixerInputConnection constructor null argument");
        	}
//        	System.out.println("create AudioConnection from "+nameAndLocation(from)+" to "+to.getName());
            this.from = from;
            this.to = to;
            try {
            	to.setInputProcess(from);
        	} catch ( Exception e ) {
        		e.printStackTrace();
        	}
        }

        public void close() {
//        	System.out.println("close AudioConnection from "+nameAndLocation(from)+" to "+to.getName());
        	try {
        		to.setInputProcess(null);
        	} catch ( Exception e ) {
        		e.printStackTrace();
        	}
        	from = null;
        	to = null;
        }
        
        /**
         * @return String - the connection source name.
         */
        public String getOutputName() {
        	return from.getName();
        }

        /**
         * @return String - the connection source location
         */
        public String getOutputLocation() {
        	return from.getLocation();
        }
        
        /**
         * @return String - the connection destination name.
         */
        public String getInputName() {
        	return to.getName();
        }
    }

}
