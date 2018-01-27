package com.frinika.sequencer.project;

import java.io.Serializable;
import javax.sound.midi.MidiDevice;
import javax.swing.Icon;

public interface MidiDeviceDescriptorIntf {

    String getMidiDeviceName();

    /**
     * Set the name of the midi device as registered in MidiSystem
     *
     * @param midiDeviceName
     */
    void setMidiDeviceName(String midiDeviceName);

    /**
     * Get the name of the midi device as the user has set for it in the Frinika
     * project
     *
     * @return
     */
    String getProjectName();

    /**
     * Set the name of the midi device as the user wants to name it in the
     * Frinika project
     *
     * @param projectName
     */
    void setProjectName(String projectName);

    MidiDevice getMidiDevice();

    Serializable getSerializableMidiDevice();

    Icon getIcon();

    Icon getLargeIcon();

    /**
     * @param midiDevice the midiDevice to set
     */
    void setMidiDevice(MidiDevice midiDevice);
}
