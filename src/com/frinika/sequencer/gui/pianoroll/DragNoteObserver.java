package com.frinika.sequencer.gui.pianoroll;

import com.frinika.sequencer.model.NoteEvent;

public interface DragNoteObserver {
	public void noteDraggedNotifiction(NoteEvent e);

}
