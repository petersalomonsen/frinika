package com.frinika.test;

import java.io.ByteArrayOutputStream;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;

public class CreateMIDIFileTest {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception{
        Sequence seq = new Sequence(Sequence.PPQ, 128,1);
        ByteArrayOutputStream sequenceOutputStream = new ByteArrayOutputStream();
        seq.createTrack();
        ShortMessage msg = new ShortMessage();
        msg.setMessage(ShortMessage.NOTE_ON,9,36,100);
        seq.getTracks()[1].add(new MidiEvent(msg,0));
        msg = new ShortMessage();
        msg.setMessage(ShortMessage.NOTE_ON,9,36,0);
        seq.getTracks()[1].add(new MidiEvent(msg,256));
        MidiSystem.write(seq,1,sequenceOutputStream);
        
        for(byte b : sequenceOutputStream.toByteArray())
        {
            System.out.println(b+",");
        }
        System.out.println(sequenceOutputStream.toByteArray()[36]);
        System.out.println(sequenceOutputStream.toByteArray()[40]);
    }

}
