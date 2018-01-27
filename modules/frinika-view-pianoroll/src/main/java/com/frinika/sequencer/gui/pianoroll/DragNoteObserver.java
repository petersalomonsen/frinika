package com.frinika.sequencer.gui.pianoroll;

import com.frinika.sequencer.model.NoteEvent;

public interface DragNoteObserver {

    void noteDraggedNotifiction(NoteEvent e);
}
