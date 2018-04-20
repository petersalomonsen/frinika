package com.frinika.sequencer.gui.selection;

import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.Part;
import com.frinika.sequencer.project.AbstractProjectContainer;

public class MultiEventSelection extends SelectionContainer<MultiEvent> {

    public MultiEventSelection(AbstractProjectContainer project) {
        super(project);
    }

    @Override
    protected void setMetaFocus() {
        MultiEvent ev = (MultiEvent) focus;
        Part part = ev.getPart();
        project.getPartSelection().setFocus(part);
    }
}
