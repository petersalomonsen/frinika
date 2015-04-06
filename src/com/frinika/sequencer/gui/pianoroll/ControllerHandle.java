package com.frinika.sequencer.gui.pianoroll;

import javax.sound.midi.ShortMessage;

import com.frinika.sequencer.model.ControllerEvent;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.model.PitchBendEvent;

public class ControllerHandle {
		
		String name;
		private int contrl;
		int minVal=0;
		int maxVal=127;
		int cmd;
		//private int zeroValue=0;
		
		public ControllerHandle(String name,int min,int max,int cntrl,int cmd) {
			this.name=name;
			minVal=min;
			maxVal=max;
			contrl=cntrl;
			this.cmd=cmd;
		}
		
	/*	public ControllerHandle(String string, int i, int j, int k, int l, int zeroVal) {
			this(string,i,j,k,l);
			zeroValue=zeroVal;
			// TODO Auto-generated constructor stub
		}*/

		public String toString() {
			return name;		
		}

		public String getName() {
	
			return name;
		}
	
		public boolean isVelocity() {
			return cmd == ShortMessage.NOTE_ON;
			
		}

		public MultiEvent createEvent(MidiPart part, long tick, int val) {
			assert(!isVelocity());
			if (cmd == ShortMessage.CONTROL_CHANGE)
				return new ControllerEvent(part, tick, contrl, val);
			else if (cmd == ShortMessage.PITCH_BEND)
				return new PitchBendEvent(part, tick, val-minVal);
			
					try {
						throw new Exception(" Should never happen ");
					} catch (Exception e) {
					
						e.printStackTrace();
					}
				return null;
				
		}

		public boolean isValid(MultiEvent ev) {
			
			switch(cmd) {
			case ShortMessage.NOTE_ON:
				return (ev instanceof NoteEvent) ;
				case  ShortMessage.CONTROL_CHANGE:
				if (!(ev instanceof ControllerEvent)) return false;
			
				ControllerEvent ce=(ControllerEvent)ev;
			
				return contrl == ce.getControlNumber();
			case ShortMessage.PITCH_BEND:
				return (ev instanceof PitchBendEvent) ;
				default:
					try {
						throw new Exception(" unknown event type " + ev);
					} catch (Exception e) {
						
						e.printStackTrace();
					}
				return false;
			}
		}
		
		public int getController() { // Jens
			return contrl;
		}
}
