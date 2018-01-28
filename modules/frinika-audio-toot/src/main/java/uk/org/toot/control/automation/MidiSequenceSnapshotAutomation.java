package uk.org.toot.control.automation;

import javax.sound.midi.Sequence;

public interface MidiSequenceSnapshotAutomation 
{
    void configureSequence(Sequence snapshot);
    void recallSequence(Sequence snapshot);
    Sequence storeSequence(String name);
}
