/* Copyright (C) 2005 Steve Taylor (toot.org.uk) */

package uk.org.toot.midi.core.channel;

public interface MidiChannelReaderProvider
{
    /**
     * Return a MidiChannelReader for the specified channel index.
     * @supplierCardinality 16
     * @link aggregation
     */
    /*#MidiChannelReader lnkMidiChannelReader;*/
    MidiChannelReader getChannelReader(int chan);
}
