package com.frinika.sequencer.gui.selection;

import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.project.AbstractProjectContainer;

public class LaneSelection extends SelectionContainer<Lane> {

    public LaneSelection(AbstractProjectContainer project) {
        super(project);
    }

    @Override
    protected void setMetaFocus() {

    }
}
