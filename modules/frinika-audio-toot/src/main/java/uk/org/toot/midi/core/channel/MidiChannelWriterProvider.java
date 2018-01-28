/* Copyright (C) 2005 Steve Taylor (toot.org.uk) */

package uk.org.toot.midi.core.channel;

public interface MidiChannelWriterProvider {
    /**
     * Return a MidiChannelWriter for the specified channel index.
     * @supplierCardinality 16
     * @link aggregation
     */
    /*#MidiChannelWriter lnkMidiChannelWriter;*/
    MidiChannelWriter getChannelWriter(int chan);
}
