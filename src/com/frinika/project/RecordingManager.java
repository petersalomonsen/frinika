/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frinika.project;

import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.FrinikaTrackWrapper;
import com.frinika.sequencer.SequencerListener;
import com.frinika.sequencer.SongPositionListener;
import com.frinika.sequencer.midi.MidiMessageListener;
import com.frinika.sequencer.model.ChannelEvent;
import com.frinika.sequencer.model.ControllerEvent;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.model.PitchBendEvent;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

/**
 *
 * This is added as a message listener to the FrinikaSequencer
 *
 * @author pjl
 */
public class RecordingManager implements SongPositionListener, MidiMessageListener, SequencerListener {

    MultiPart multiPart;
    MidiPart lastPart;
    // The current recording take - will be added to recordingTake when recording is stopped, or in case of a loop - and provided that there are multievents in the take
    protected Vector<MultiEvent> currentRecordingTake = new Vector<MultiEvent>();
    protected HashMap<Integer, NoteEvent> pendingNoteEvents = new HashMap<Integer, NoteEvent>();
    protected FrinikaSequencer sequencer;
    long lastTick = -1;
    protected Stack stack;
    protected ProjectContainer project;
    boolean looped;
    boolean isDrumTake;

    public RecordingManager(ProjectContainer proj, int buffSize) {
        this.project = proj;
        this.sequencer = proj.getSequencer();
        sequencer.addSongPositionListener(this);
        sequencer.addSequencerListener(this);
        sequencer.addMidiMessageListener(this);
        stack = new Stack(buffSize);
        looped = false;
    }

    public boolean requiresNotificationOnEachTick() {
        return false;
    }

    public void midiMessage(MidiMessage message) {

        if (!sequencer.isRecording()) {
            return;
        }

        long tick = sequencer.getRealTimeTickPosition();

        if (message instanceof ShortMessage) {
            try {
                ShortMessage shm = (ShortMessage) message;
                Event event = stack.poke();
                event.mess = (ShortMessage) message;
                event.stamp = tick;
            } catch (Exception ex) {
                Logger.getLogger(RecordingManager.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    void detachLastTake() {


        // disable last part 
        if (lastPart != null) {
            if (lastPart.getLane() != null) {
                lastPart.removeFromModel();
            }
        }


    }

    /**
     * Add a new take for recording
     *
     */
    void commitRecordingTake() {

        if (currentRecordingTake.size() > 0) {


            System.out.println(" THAT WAS A TAKE ");

            project.getEditHistoryContainer().mark(" Recording take ");


            if (!isDrumTake) {
                detachLastTake();
            }

            boolean flag = false;

            for (Lane lane : project.getLanes()) {

                if (!(lane instanceof MidiLane)) {
                    continue;
                }
                MidiLane ml = (MidiLane) lane;

                if (!ml.isRecording()) {
                    continue;
                }


                assert (!flag); // fixme for multiple lanes.
                flag = true;

                MidiPart part = new MidiPart(ml);

                for (MultiEvent event : currentRecordingTake) {

                    try {
                        part.add((MultiEvent) event.clone());
                    } catch (CloneNotSupportedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                part.setBoundsFromEvents();



                if (lastPart != null) {
                    if (multiPart == null) {
                        multiPart = new MultiPart();
                        multiPart.add(lastPart);
                    }
                    multiPart.add(part);

                }
                lastPart = part;
                project.getEditHistoryContainer().notifyEditHistoryListeners();

            }


//            recordingTakes.add(currentRecordingTake);
//            if (recordingTakeDialog != null) {
//                recordingTakeDialog.notifyNewTake(recordingTakes.size() - 1);
//
//                // If this is a loop then show the dialog
//                if (recordingTakes.size() > 1) {
//                    recordingTakeDialog.setVisible(true);
//                }
//            }


        }
        currentRecordingTake.clear();
    }

    public void notifyTickPosition(long tick) {


        processEvents();


        if (tick < lastTick) {
            looped = true;
        }

        if (looped && pendingNoteEvents.isEmpty()) {

            commitRecordingTake();
            looped = false;

        }
        lastTick = tick;
    }

    void processEvents() {

        if (currentRecordingTake.size() == 0 && (!stack.isEmpty())) {
            if (lastPart != null) {
                project.getEditHistoryContainer().mark(" Recording take ");
                if (!isDrumTake) {
                    detachLastTake();
                }
                project.getEditHistoryContainer().notifyEditHistoryListeners();
            }
        }

        Event e = null;

        while ((e = stack.pop()) != null) {
            ShortMessage shm = e.mess;
            long tick = e.stamp;
            if (shm.getCommand() == ShortMessage.NOTE_ON || shm.getCommand() == ShortMessage.NOTE_OFF) {
                //Note off
                if (shm.getCommand() == ShortMessage.NOTE_OFF || shm.getData2() == 0) {
                    // Generate a note event 
                    NoteEvent noteEvent = pendingNoteEvents.get(shm.getChannel() << 8 | shm.getData1());
                    if (noteEvent != null) {
                        long duration = tick - noteEvent.getStartTick();

                        if (duration < 0) { // PJL if we hold a note after the loop end then correct the tick
                            duration = duration + sequencer.getLoopEndPoint() - sequencer.getLoopStartPoint();
                        }

                        noteEvent.setDuration(duration);
                        pendingNoteEvents.remove(shm.getChannel() << 8 | shm.getData1());
                        addEventToRecordingTracks(noteEvent);
                    }
                } else {
                    //Note on       
                    pendingNoteEvents.put(shm.getChannel() << 8 | shm.getData1(),
                            new NoteEvent((FrinikaTrackWrapper) null, tick, shm.getData1(), shm.getData2(), shm.getChannel(), 0));
                }
            } else if (shm.getCommand() == ShortMessage.CONTROL_CHANGE) {
                addEventToRecordingTracks(new ControllerEvent((FrinikaTrackWrapper) null, tick, shm.getData1(), shm.getData2()));
            } else if (shm.getCommand() == ShortMessage.PITCH_BEND) {
                addEventToRecordingTracks(new PitchBendEvent((FrinikaTrackWrapper) null, tick, ((shm.getData1()) | (shm.getData2() << 7)) & 0x7fff));
            }
        }

    }

    protected void addEventToRecordingTracks(ChannelEvent event) {
        currentRecordingTake.add(event);
    }

    protected void reset() {
        notifyTickPosition(-1); // processEvents();    
        assert (currentRecordingTake.size() == 0);
        //      currentRecordingTake.clear();
        lastPart = null;
        multiPart = null;
        looped = false;

    }

    public void beforeStart() {

        //     recording = sequencer.isRecording();
        reset();

        for (Lane lane : project.getLanes()) {

            if (!(lane instanceof MidiLane)) {
                continue;
            }
            MidiLane ml = (MidiLane) lane;

            if (!ml.isRecording()) {
                continue;
            }
            isDrumTake = ml.isDrumLane();
        }
        debug("Before START");
    }

    public void start() {
        if (!sequencer.isRecording()) {
            reset();
        }
        debug("START");

    }

    public void stop() {
        //    recording = false;
        reset();
        debug("STOP");
    }

    void debug(Object str) {
        System.out.println(" RECORDING:    " + str);
    }

    protected class Stack {

        int in;
        int out;
        Event stack[];
        private int size;

        Stack(int size) {
            stack = new Event[size];
            for (int i = 0; i < size; i++) {
                stack[i] = new Event();
            }
            out = size - 1;
            in = size - 1;
            this.size = size;
        }
        
        void clear() {
            out = size - 1;
            in = size - 1;        
        }

        Event poke() throws Exception {
            in = (++in) % size;

            if (in == out) {
                throw new Exception("Recorded buffer overflow ");
            }
            Event e = stack[in];
            return e;
        }

        public Event pop() {
            if (out == in) {
                return null;
            }
            out = (++out) % size;
            return stack[out];
        }

        public   boolean isEmpty() {
            return in == out;
        }
    }

   protected class Event {

        public ShortMessage mess; // = new ShortMessage();
        public long stamp;
        MidiLane lane;
    }
}
