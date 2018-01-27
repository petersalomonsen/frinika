package com.frinika.project;

import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.sequencer.project.SoundBankNameHolder;
import com.frinika.sequencer.project.SynthesizerDescriptorIntf;
import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

/**
 * A serializable representation of a Synthesizer Midi Device used in a Frinika
 * project. Contains resources neccesary for re-opening the correct midi device.
 *
 * @author Peter Johan Salomonsen
 */
public class SynthesizerDescriptor extends MidiDeviceDescriptor implements SynthesizerDescriptorIntf, SoundBankNameHolder {

    private static final long serialVersionUID = 1L;

    String soundBankFileName;

    public SynthesizerDescriptor(Synthesizer midiDevice) {
        super(midiDevice);
    }

    /**
     * Get the filename for the loaded soundbank
     *
     * @return
     */
    @Override
    public String getSoundBankFileName() {
        return soundBankFileName;
    }

    /**
     * Set the filename for the loaded soundbank
     *
     * @param soundBankFileName
     */
    @Override
    public void setSoundBankFileName(String soundBankFileName) {
        this.soundBankFileName = soundBankFileName;
    }

    @Override
    protected void installImp(FrinikaProjectContainer project) {
        super.installImp(project);
        if (soundBankFileName != null) {
            try {
                Soundbank soundbank;
                if (midiDevice instanceof SynthWrapper) {
                    soundbank = ((SynthWrapper) midiDevice).getSoundbank(new File(soundBankFileName));
                } else {
                    soundbank = MidiSystem.getSoundbank(new File(soundBankFileName));
                }
                ((Synthesizer) midiDevice).loadAllInstruments(soundbank);
                System.out.println("Soundbank loaded");
            } catch (IOException | InvalidMidiDataException e) {
                e.printStackTrace();
            }
        }
    }
}
