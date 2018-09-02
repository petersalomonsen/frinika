package com.frinika.sequencer.gui.partview;

import com.frinika.model.EditHistoryAction;
import com.frinika.model.EditHistoryListener;
import com.frinika.model.EditHistoryRecordable;
import com.frinika.model.EditHistoryRecordableAction;
import com.frinika.sequencer.gui.Layout;
import com.frinika.sequencer.gui.ProjectFrame;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.LaneTreeListener;
import com.frinika.sequencer.model.ViewableLaneList;
import com.frinika.sequencer.project.AbstractProjectContainer;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JPanel;
import javax.swing.Timer;

public class LaneHeaderPanel extends JPanel implements ComponentListener,
        AdjustmentListener, EditHistoryListener, LaneTreeListener {

    private static final long serialVersionUID = 1L;

    // Box.Filler filler;
    PartView partView;

    int preferredWidth = 200;

    ProjectFrame project;

    ViewableLaneList visibleLanes;

    private Timer timer;

    public LaneHeaderPanel(PartView partView, AbstractProjectContainer project) {

        this.partView = partView;

        setLayout(null);
        setPreferredSize(new Dimension(preferredWidth, 1000));
        // this.setBackground(Color.PINK);
        // validate();
        visibleLanes = new ViewableLaneList(project);
        rebuild();
        rePositionItems();
        addComponentListener(this);
        project.getEditHistoryContainer()
                .addEditHistoryListener(this);
        timer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                updateMeters();
            }

        });
        timer.start();
    }

    private void updateMeters() {
        if (!isVisible()) {
            return;
        }
        for (Component comp : getComponents()) {
            assert (comp instanceof LaneHeaderItem);
            LaneHeaderItem item = (LaneHeaderItem) comp;
            item.updateMeter();
        }
    }

    void dispose() {
        timer.stop();
        timer = null;
        removeComponentListener(this);
        project.getProjectContainer().getEditHistoryContainer()
                .removeEditHistoryListener(this);
    }

    void rebuild() {
        removeAll();
        visibleLanes.rebuild();
        int i = 0;
        for (Lane lane : visibleLanes) {
            Component c = new LaneHeaderItem(partView.getProjectFrame().getProjectContainer(), this, lane, i);
            c.setSize(new Dimension(getWidth(), lane.getHeight()
                    * Layout.getLaneHeightScale()));
            add(c);
            i++;
        }
        validate();
        repaint();
    }

    void rePositionItems() {
        int n = getComponentCount();
        if (n == 0) {
            return;
        }
        int widthOld = getComponent(0).getWidth();
        int widthNew = getWidth();
        int yRef = Layout.timePanelHeight - partView.getVirtualScreenRect().y;
        for (Component comp : getComponents()) {
            assert (comp instanceof LaneHeaderItem);

            // if (widthOld == widthNew) comp.setLocation(0,yRef);
            // else
            int h = ((LaneHeaderItem) comp).lane.getDisplayH();
            comp.setBounds(0, yRef, widthNew, h);
            yRef += h;
        }
        validate();
        repaint();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        rePositionItems();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {
        System.out.println("SHWON");
        timer.start();

    }

    @Override
    public void componentHidden(ComponentEvent e) {
        timer.stop();
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        rePositionItems();

    }

    @Override
    public void fireSequenceDataChanged(EditHistoryAction[] edithistoryActions) {
        boolean doit = false;

        for (EditHistoryAction e : edithistoryActions) {
            if (e instanceof EditHistoryRecordableAction) {
                EditHistoryRecordable r = ((EditHistoryRecordableAction) e)
                        .getRecordable();
                if (r instanceof Lane) {
                    doit = true;
                    break;

                }
            }
        }
        if (doit) {
            rebuild();
            rePositionItems();
        }
    }

    @Override
    public void fireLaneTreeChanged() {
        rebuild();
        rePositionItems();
    }
}
