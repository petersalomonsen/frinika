package com.frinika.main.action;

import com.frinika.main.FrinikaFrame;
import com.frinika.sequencer.gui.ProjectFrame;
import java.awt.event.KeyEvent;

public class SelectAllAction {

    private final ProjectFrame project;

    public SelectAllAction(ProjectFrame project) {
        this.project = project;
    }

    public boolean selectAll(KeyEvent e) {
        FrinikaFrame frinikaFrame = (FrinikaFrame) project;
        if (((FrinikaFrame) project).getTrackerPanel().getTable().hasFocus()) {
            ((FrinikaFrame) project).getTrackerPanel().getTable().selectAll();
        } else if (project.getPartViewEditor().getPartview().getMousePosition() != null) {
            project.getPartViewEditor().getPartview().selectAll();
        } else if (frinikaFrame.getPianoControllerPane().getPianoRoll().getMousePosition() != null) {
            frinikaFrame.getPianoControllerPane().getPianoRoll().selectAll();
        } else if (frinikaFrame.getPianoControllerPane().getControllerView().getMousePosition() != null) {
            frinikaFrame.getPianoControllerPane().getControllerView().selectAll();
        } else {
            return true;
        }
        return false;
    }
}
