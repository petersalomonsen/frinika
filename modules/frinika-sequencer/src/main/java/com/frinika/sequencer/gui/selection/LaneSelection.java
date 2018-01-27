package com.frinika.sequencer.gui.selection;

import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.project.SequencerProjectContainer;

public class LaneSelection extends SelectionContainer<Lane> {

    public LaneSelection(SequencerProjectContainer project) {
        super(project);
    }

    @Override
    protected void setMetaFocus() {

    }
}
