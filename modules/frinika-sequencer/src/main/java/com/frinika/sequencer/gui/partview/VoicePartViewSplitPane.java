package com.frinika.sequencer.gui.partview;

import com.frinika.sequencer.gui.ProjectFrame;
import com.frinika.sequencer.gui.selection.SelectionContainer;
import com.frinika.sequencer.gui.selection.SelectionListener;
import com.frinika.sequencer.model.Lane;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class VoicePartViewSplitPane extends JPanel implements
        SelectionListener<Lane> {

    private static final long serialVersionUID = 1L;

    LaneView laneView;
    PartViewEditor partViewEditor;
    ProjectFrame project;
    JSplitPane splitPane = new JSplitPane();
    LaneView nullView = new LaneView(null);

    boolean showVoiceView = true;

    public JComponent getLaneView() {
        return this;
    }

    public JComponent getPartViewEditor() {
        return partViewEditor;
    }

    boolean dockmode;

    public VoicePartViewSplitPane(ProjectFrame projectFrame, boolean dockmode) {
        this.project = projectFrame;
        this.dockmode = dockmode;
        setLayout(new BorderLayout());
        partViewEditor = new PartViewEditor(projectFrame);
        Lane lane = projectFrame.getProjectContainer().getProjectLane();
        laneView = new LaneView(lane);
        laneView.setEnabled(false);

        if (dockmode) {
            add(laneView);
        } else {
            add(splitPane, BorderLayout.CENTER);
            splitPane.add(partViewEditor, JSplitPane.RIGHT);
            splitPane.setResizeWeight(0.0);
            splitPane.add(laneView, JSplitPane.LEFT);
        }
        projectFrame.getProjectContainer().getLaneSelection().addSelectionListener(this);
        //	toggleVoiceView();
    }

    public void toggleVoiceView() {

        if (dockmode) {
            return;
        }
        showVoiceView = !showVoiceView;

        if (showVoiceView) {
            remove(partViewEditor);
            add(splitPane, BorderLayout.CENTER);
            splitPane.add(partViewEditor, JSplitPane.RIGHT);

        } else {
            splitPane.remove(partViewEditor);
            remove(splitPane);
            add(partViewEditor, BorderLayout.CENTER);
        }
        validate();
    }

    void dispose() {
        project.getProjectContainer().getLaneSelection().removeSelectionListener(this);
    }

    @Override
    public void selectionChanged(SelectionContainer<? extends Lane> src) {

        Lane lane = src.getFocus();

        if (lane == null) {
            return;
        }

        LaneHeaderItem header = null;
        for (Component c : partViewEditor.laneHeaderPanel.getComponents()) {
            if (c instanceof LaneHeaderItem) {
                LaneHeaderItem h = (LaneHeaderItem) c;
                Lane il = h.lane;
                if (il == lane) {
                    header = h;
                    break;
                }
            }
        }

        LaneView newVoiceView = nullView;

        if (header != null) {
            newVoiceView = header.voiceView;
        }

        if (laneView != newVoiceView) {

            if (dockmode) {
                // PJL defensive programming to get rounf null pointers TODO 
                if (laneView != null) {
                    remove(laneView);
                }
                laneView = newVoiceView;
                if (laneView != null) {
                    add(laneView);
                    laneView.setEnabled(true);
                }
                validate();
                repaint();
                partViewEditor.repaint();
            } else {
                remove(laneView);
                splitPane.setTopComponent(laneView = newVoiceView); //, BorderLayout.WEST);
                laneView.setEnabled(true);
                repaint();
            }
        }

    }

    public PartView getPartview() {
        return partViewEditor.getPartView();
    }
}
