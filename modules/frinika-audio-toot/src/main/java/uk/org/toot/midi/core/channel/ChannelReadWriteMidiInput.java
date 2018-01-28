// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.midi.core.channel;

import java.beans.PropertyChangeSupport;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import uk.org.toot.midi.core.*;
import uk.org.toot.midi.message.ChannelMsg;
import static uk.org.toot.midi.message.ChannelMsg.*;

/**
 * This implementation class is intended to be used by midi synthesizers.
 * It provides a rich channel-based read/write API.
 * It is provided as a MidiInput adapter which simplifies decoding the written
 * state for reads.
 * As such it is suitable for use with external midi synths.
 * May not be suitable to implement a midi synth though, that probably needs
 * the state we decode!!!
 * @author Steve Taylor
 */
public class ChannelReadWriteMidiInput implements MidiInput, 
	MidiChannelReaderProvider, MidiChannelWriterProvider
{
	private MidiInput input;
	
    /**
     * @supplierCardinality 16
     * @link aggregationByValue
     */
    private MidiChannelReader[] decoders;
    /**
     * @supplierCardinality 16
     * @link aggregationByValue
     */
    private MidiChannelWriter[] encoders;
    private PropertyChangeSupport propertyChangeSupport;

    public ChannelReadWriteMidiInput(MidiInput input) throws MidiUnavailableException {
    	this.input = input;
        decoders = new MidiChannelReader[16];
        encoders = new MidiChannelWriter[16];
        for (int i = 0; i < 16; i++) {
            decoders[i] = createChannelReader(i);
        }
    }

    public MidiChannelReader getChannelReader(int chan) {
        return decoders[chan];
    }

    public MidiChannelWriter getChannelWriter(int chan) {
        if ( encoders[chan] == null ) {
        	encoders[chan] = createChannelWriter(chan);
        }
        return encoders[chan];
    }

    protected MidiChannelReader createChannelReader(int chan) {
        return new DefaultMidiChannelReader(chan);
    }

    protected MidiChannelWriter createChannelWriter(int chan) {
        return new DefaultMidiChannelWriter(this, chan);
    }

    public void transport(MidiMessage msg, long timestamp) {
        if ( isChannel(msg) ) {
            int chan = ChannelMsg.getChannel(msg);
   	        decoders[chan].decode(getCommand(msg), getData1(msg), getData2(msg));
        }
        input.transport(msg, timestamp);
    }
    
    public String getName() {
    	return input.getName();
    }
    
    public PropertyChangeSupport getPropertyChangeSupport() {
        if (propertyChangeSupport == null) { // lazy instantiation
            propertyChangeSupport = new PropertyChangeSupport(this);
//            createOutPort(); // !!! not really the best place
        }
        return propertyChangeSupport;
    }

    /**
     * A convenience method for the MidiChannels. Do not create a PropertyChangeSupport here because it won't have any
     * listeners to fire events to!
     */
    public void firePropertyChange(String property, int index, int oldVal, int newVal) {
        if (propertyChangeSupport != null) {
            //            Log.debug("CMIA : fire "+property+" change from "+oldVal+" to "+newVal);
            propertyChangeSupport.fireIndexedPropertyChange(property, index, oldVal, newVal);
        }
    }
}
