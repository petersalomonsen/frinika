/* Copyright (C) 2005 Steve Taylor (toot.org.uk) */

package uk.org.toot.midi.core.channel;

import uk.org.toot.midi.core.*;

/**
 * This implementation class is intended to be used by a Midi controller,
 * such as a virtual keyboard or neck controller. It allows writing Midi channel
 * messages with a rich channel-based write API.
 * @author Steve Taylor
 */
public class ChannelWriteMidiOutput extends DefaultMidiOutput
    implements MidiChannelWriterProvider
{
    /**
     * @supplierCardinality 16
     * @link aggregationByValue
     */
    private MidiChannelWriter[] encoders = new MidiChannelWriter[16];

    public ChannelWriteMidiOutput(String name) {
    	super(name);
    }

    public MidiChannelWriter getChannelWriter(int chan) {
        if ( encoders[chan] == null ) {
        	encoders[chan] = new DefaultMidiChannelWriter(this, chan);
        }
        return encoders[chan];
    }
}
