package com.frinika.sequencer.gui.pianoroll;

import com.frinika.sequencer.model.ControllerEvent;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.model.PitchBendEvent;
import javax.sound.midi.ShortMessage;

public class ControllerHandle {

    final String name;
    final int controller;
    final int minVal;
    final int maxVal; // default 127
    final int command;
    //private int zeroValue=0;

    public ControllerHandle(String name, int min, int max, int cntrl, int command) {
        this.name = name;
        minVal = min;
        maxVal = max;
        controller = cntrl;
        this.command = command;
    }

    /*	public ControllerHandle(String string, int i, int j, int k, int l, int zeroVal) {
			this(string,i,j,k,l);
			zeroValue=zeroVal;
			// TODO Auto-generated constructor stub
		}*/
    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public boolean isVelocity() {
        return command == ShortMessage.NOTE_ON;
    }

    public MultiEvent createEvent(MidiPart part, long tick, int val) {
        assert (!isVelocity());
        if (command == ShortMessage.CONTROL_CHANGE) {
            return new ControllerEvent(part, tick, controller, val);
        } else if (command == ShortMessage.PITCH_BEND) {
            return new PitchBendEvent(part, tick, val - minVal);
        }

        try {
            throw new Exception(" Should never happen ");
        } catch (Exception e) {

            e.printStackTrace();
        }
        return null;
    }

    public boolean isValid(MultiEvent event) {
        switch (command) {
            case ShortMessage.NOTE_ON:
                return (event instanceof NoteEvent);
            case ShortMessage.CONTROL_CHANGE:
                if (!(event instanceof ControllerEvent)) {
                    return false;
                }

                ControllerEvent ce = (ControllerEvent) event;

                return controller == ce.getControlNumber();
            case ShortMessage.PITCH_BEND:
                return (event instanceof PitchBendEvent);
            default:
                try {
                    throw new Exception(" unknown event type " + event);
                } catch (Exception e) {

                    e.printStackTrace();
                }
                return false;
        }
    }

    public int getController() {
        return controller;
    }
}
