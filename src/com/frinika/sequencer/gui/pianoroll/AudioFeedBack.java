package com.frinika.sequencer.gui.pianoroll;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.swing.Timer;

import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.NoteEvent;

public class AudioFeedBack implements ActionListener {
	
	
	NoteEvent ev=null;
	Receiver recv = null;
	private int chan;
	private int pitch;
	Timer timer;
	ProjectContainer project;
	
	public AudioFeedBack(ProjectContainer project) {
		this.project=project;
		timer=new Timer(0,this);
		timer.setRepeats(false);
	}
	
	
	public void select(NoteEvent ev) {
		off();
		this.ev=ev;
		on();
	}

	
	public void on() {
      
		MidiPart part = ev.getPart();
		MidiLane lane = ((MidiLane) (part.getLane()));
		chan = lane.getMidiChannel();
        if (chan < 0 ) return;

        recv = lane.getReceiver();
		if (recv == null)
			return;
        pitch = ev.getNote();
		ShortMessage shm = new ShortMessage();
		try {
			shm.setMessage(ShortMessage.NOTE_ON, chan, lane.mapNote(pitch), ev.getVelocity());
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
		recv.send(shm, -1);
		int millis = (int) ((60000.0*ev.getDuration()/project.getSequence().getResolution())/project.getSequencer().getTempoInBPM());
		timer.setInitialDelay(millis);
		timer.restart();
	}

	public void off() {
		timer.stop();
		if (recv == null)	return;
		ShortMessage shm = new ShortMessage();
		MidiPart part = ev.getPart();
		MidiLane lane = ((MidiLane) (part.getLane()));
		try {
			shm.setMessage(ShortMessage.NOTE_ON, chan, lane.mapNote(pitch), 0);
		} catch (InvalidMidiDataException e) {
		
			e.printStackTrace();
		}
		recv.send(shm, -1);
		recv = null;
	}


	public void actionPerformed(ActionEvent arg0) {
		off();
	}


}
