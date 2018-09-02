/*
 * Created on Feb 10, 2007
 *
 * Copyright (c) 2007 Jens Gulden
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
package com.frinika.sequencer.gui.menu.midi;

import com.frinika.sequencer.gui.menu.midi.AbstractMidiAction;
import com.frinika.sequencer.model.ControllerEvent;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.project.AbstractProjectContainer;
import java.util.*;
import javax.swing.JComponent;

/**
 * Menu-action for reversing selected MIDI notes.
 *
 * @author Jens Gulden
 */
public class MidiReverseAction extends AbstractMidiAction {

    boolean hasNotes = true;
    boolean starts = true;
    boolean hasDurations = true;
    boolean hasVelocities = false;
    boolean hasControllers = false;
    boolean hasMirror = false;
    int note = 36; // C3
    boolean mirrorQuarter = false;

    public MidiReverseAction(AbstractProjectContainer project) {
        super(project, "sequencer.midi.reverse");
    }

    @Override
    public void modifyEvents(Collection<MultiEvent> events) {
        List<NoteEvent> notes = new ArrayList<>();
        List<ControllerEvent> controllers = new ArrayList<>();
        for (MultiEvent event : events) {
            if (event instanceof NoteEvent) {
                notes.add((NoteEvent) event);
            } else if (event instanceof ControllerEvent) {
                controllers.add((ControllerEvent) event);
            }
        }

        if (hasMirror) {
            for (NoteEvent note : notes) {
                note.setNote(mirrorNote(note.getNote(), this.note, this.mirrorQuarter));
            }
        }

        if (this.hasNotes) {
            new Reverser<NoteEvent, Integer>(notes) {
                @Override
                Integer get(NoteEvent e) {
                    return e.getNote();
                }

                @Override
                void set(NoteEvent e, Integer note) {
                    e.setNote(note);
                }
            };
        }

        final long first = notes.get(0).getStartTick();
        final long last = notes.get(notes.size() - 1).getStartTick();

        if (this.starts) {
            new Reverser<NoteEvent, Long>(notes) {
                @Override
                Long get(NoteEvent e) {
                    return (last - e.getStartTick()) + first;
                }

                @Override
                void set(NoteEvent e, Long start) {
                    e.setStartTick(start);
                }
            };
        }

        if (this.hasDurations) {
            new Reverser<NoteEvent, Long>(notes) {
                @Override
                Long get(NoteEvent e) {
                    return e.getDuration();
                }

                @Override
                void set(NoteEvent e, Long d) {
                    e.setDuration(d);
                }
            };
        }

        if (this.hasVelocities) {
            new Reverser<NoteEvent, Integer>(notes) {
                @Override
                Integer get(NoteEvent e) {
                    return e.getVelocity();
                }

                @Override
                void set(NoteEvent e, Integer v) {
                    e.setVelocity(v);
                }
            };
        }

        if (this.hasControllers) {
            new Reverser<ControllerEvent, Integer>(controllers) {
                @Override
                Integer get(ControllerEvent e) {
                    return e.getValue();
                }

                @Override
                void set(ControllerEvent e, Integer value) {
                    e.setValue(value);
                }
            };
        }

    }

    private static int mirrorNote(int note, int mirror, boolean q) {
        int diff = mirror - note;
        note = mirror + diff + (q ? 1 : 0); //( q ? ((diff > 0) ? 1 : -1) : 0 );
        return note;
    }

    @Override
    public void modifyNoteEvent(NoteEvent note) {
        // nop
    }

    @Override
    protected JComponent createGUI() {
        return new MidiReverseActionEditor(this);
    }

    // --- inner class ---
    private abstract class Reverser<A, T> {

        Reverser(List<A> l) {
            super();
            int len = l.size();
            for (int i = 0; i < ((len / 2) + 1); i++) {
                A a = l.get(i);
                A b = l.get(len - i - 1);
                T tmp = get(a);
                set(a, get(b));
                set(b, tmp);
            }
        }

        abstract T get(A event);

        abstract void set(A event, T value);
    }
}
