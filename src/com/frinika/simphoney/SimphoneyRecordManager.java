/*
 *
 * Copyright (c) 2004-2008 Paul John Leonard
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.simphoney;

import com.frinika.project.RecordingManager;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.FrinikaTrackWrapper;
import com.frinika.sequencer.model.ControllerEvent;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.model.PitchBendEvent;
import javax.sound.midi.ShortMessage;
import uk.org.toot.control.BooleanControl;

/**
 *
 * @author pjl
 */
public class SimphoneyRecordManager extends RecordingManager {

//    MultiEvent puncEvent;
    private ProjectFrame frame;
    //  boolean armed=false;

    BooleanControl loopMarker;
    private boolean createTakeRequest=false;
       
    public SimphoneyRecordManager(ProjectFrame frame) {
        super(frame.getProjectContainer(), 1000);
        this.frame = frame;
    //    puncEvent = null;
        loopMarker = new BooleanControl(0, "loopMarker", false, true) {
          

            @Override
            public void setValue(boolean flag) {
                System.out.println(" LOOP MARKER  " + flag );
                
                if (true) {
                    createTakeRequest=true;
                }
                notifyParent(this);
                
                
            }
        };
        
 //       frame.setStatusBarMessage(" Hit special key to define ");
    }

    
    public BooleanControl getLoopMarkerControl() {
        return loopMarker;       
    }


    /**
     * 
     * Do the processing on a low priority Tick notification thread
     *  
     * @param tick
     */
    @Override
    public void notifyTickPosition(long tick) {
        
        if (createTakeRequest) {
            createTake();
            createTakeRequest=false;
        }
           
        processEvents();
    }

    void processEvents() {


//        if (currentRecordingTake.size() == 0 && (!stack.isEmpty())) {
//            if (lastPart != null) {    
//                project.getEditHistoryContainer().mark(" Recording take ");
//                if (!isDrumTake) detachLastTake();
//                project.getEditHistoryContainer().notifyEditHistoryListeners();
//            }
//        }

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
//                        if (puncEvent == null) {
//                            puncEvent = noteEvent;
//                            frame.setStatusBarMessage(" Special key defined. Hit it when take is ready ");
////                            armed=false;
//                        }

                        addEventToRecordingTracks(noteEvent);
                    }
                } else {
                    //Note on
//                    if (puncEvent != null) {
//                        if (shm.getData1() == ((NoteEvent) puncEvent).getNote()) {
//                            frame.setStatusBarMessage(" creating a take ");
//                            createTake();
//                        }
//                    }
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

    void createTake() {

        System.out.println(" CREATE A TAKE ");
        MidiLane ml = null;
        if (currentRecordingTake.size() == 0) {
            return;
        }

        for (Lane lane : project.getLanes()) {

            if (!(lane instanceof MidiLane)) {
                continue;
            }

            if (!((MidiLane)lane).isRecording()) continue;
       
            ml = (MidiLane)lane;
            break;
        }

        if (ml == null) {
            return;
        }

        project.getEditHistoryContainer().mark(" Recording take ");
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
        project.getEditHistoryContainer().notifyEditHistoryListeners();
        currentRecordingTake.clear();
    }
    
    
}
