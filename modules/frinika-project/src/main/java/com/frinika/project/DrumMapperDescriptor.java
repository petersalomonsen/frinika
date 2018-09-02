package com.frinika.project;

import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.sequencer.midi.DrumMapper;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class DrumMapperDescriptor extends MidiDeviceDescriptor {

    private static final long serialVersionUID = 1L;
    transient FrinikaProjectContainer project;
    MidiDeviceDescriptor target;
    int[] noteMap = new int[128];

    public DrumMapperDescriptor(SynthWrapper midiDevice, FrinikaProjectContainer proj) {
        super(midiDevice);
        assert (midiDevice.getRealDevice() instanceof DrumMapper);
        project = proj;
    }

    @Override
    protected void installImp(FrinikaProjectContainer project) {

        // super will sort out setting my real device 
        super.installImp(project);
        this.project = project;
        System.out.println("Installing DRUMMAPER ");

        if (target != null) {

            // target might not be installed at this point.
            target.install(project);
            ((DrumMapper) ((SynthWrapper) midiDevice).getRealDevice()).setDefaultMidiDevice(target.getMidiDevice());
        }

        ((DrumMapper) ((SynthWrapper) midiDevice).getRealDevice()).setNoteMap(noteMap);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {

        System.out.println("Saving DRUMMAPPER");

        DrumMapper mapper = (DrumMapper) ((SynthWrapper) midiDevice).getRealDevice();
        for (int i = 0; i < 128; i++) {
            DrumMapper.NoteMap map = mapper.getNoteMap(i);
            noteMap[i] = map.note;
        }
        target = project.getMidiDeviceDescriptor(mapper.getDefaultMidiDevice());
        out.defaultWriteObject();
    }
}
