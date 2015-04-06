/*
 * Created on 14-Feb-2006
 *
 * Copyright (c) 2006 P.J.Leonard
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
package com.frinika.sequencer.gui.partview;

import static com.frinika.localization.CurrentLocale.getMessage;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.event.MouseEvent;
import java.util.Vector;

import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.FrinikaSequence;
import com.frinika.sequencer.SwingSongPositionListenerWrapper;
import com.frinika.sequencer.gui.ColorScheme;
import com.frinika.sequencer.gui.DragViewTool;
import com.frinika.sequencer.gui.EraseTool;
import com.frinika.sequencer.gui.Item;
import com.frinika.sequencer.gui.ItemPanel;
import com.frinika.sequencer.gui.ItemRollToolBar;
import com.frinika.sequencer.gui.ItemScrollPane;
import com.frinika.sequencer.gui.PartGlueTool;
import com.frinika.sequencer.gui.PartSplitTool;
import com.frinika.sequencer.gui.ToolAdapter;

import com.frinika.sequencer.gui.Layout;
import com.frinika.sequencer.gui.MyCursors;
import com.frinika.sequencer.gui.RectZoomTool;
import com.frinika.sequencer.gui.SelectTool;
import com.frinika.sequencer.gui.WriteTool;
import com.frinika.sequencer.gui.selection.SelectionContainer;
import com.frinika.sequencer.gui.selection.SelectionListener;
import com.frinika.sequencer.model.AudioLane;
import com.frinika.sequencer.model.AudioPart;
import com.frinika.sequencer.model.EditHistoryAction;
import com.frinika.sequencer.model.EditHistoryContainer;
import com.frinika.sequencer.model.EditHistoryListener;
import com.frinika.sequencer.model.Ghost;
import com.frinika.sequencer.model.GluePartEditAction;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MidiPlayOptions;
import com.frinika.sequencer.model.SynthLane;
import com.frinika.sequencer.model.TextLane;
import com.frinika.sequencer.model.MovePartEditAction;
import com.frinika.sequencer.model.Part;
import com.frinika.sequencer.model.ResizePartAction;
import com.frinika.sequencer.model.SplitPartAction;
import com.frinika.sequencer.model.tempo.TempoList;
import com.frinika.sequencer.model.tempo.TempoListListener;
import com.frinika.sequencer.model.timesignature.TimeSignatureList;
import com.frinika.sequencer.model.timesignature.TimeSignatureList.QStepIterator;
import com.frinika.sequencer.model.timesignature.TimeSignatureList.TimeSignatureEvent;
import com.frinika.sequencer.model.util.TimeUtils;

/**
 * 
 * Panel displaying the parts.
 * 
 * 
 * @author pjl
 * 
 */
public class PartView extends ItemPanel implements SelectionListener<Part>,
        EditHistoryListener {

    private static final long serialVersionUID = 1L;    // TODO use the piano roll event pattern and put this into the project
    Vector<PartImage> dragList;    // used for dragging
    int dxTotal = 0;
    int dLaneTotal = 0;    // used when creating and stretching
    private MidiPart newPart;
    EditHistoryContainer editHistory;
    private LaneHeaderPanel laneHeader;
    private PartSplitTool splitTool;
    private PartGlueTool glueTool;
    private int splitX;
    private boolean splitting;
    private long splitTick;
    private Stroke dashedLineStroke; // Jens
    private TempoList tempoList;
    private ProjectFrame frame;

    public PartView(ProjectFrame frame, ItemScrollPane scroller) {
        super(frame.getProjectContainer(),scroller, true, true, 20.0, true);
       project.getPartSelection().addSelectionListener(
                this);
        this.frame = frame;
        this.tempoList = frame.getProjectContainer().getTempoList();

        tempoList.addTempoListListener(new TempoListListener() {

            public void notifyTempoListChange() {

                // System.out.println(" PART VIEW TEMPO CHANGE");
                setDirty();// true;
                repaint();
            }
        });

        this.editHistory = frame.getProjectContainer().getEditHistoryContainer();
        this.editHistory.addEditHistoryListener(this);
        this.sequencer = frame.getProjectContainer().getSequencer();
        this.sequencer.addSongPositionListener(new SwingSongPositionListenerWrapper(
                this));
        FrinikaSequence seq = (FrinikaSequence) this.sequencer.getSequence();
        this.ticksPerBeat = seq.getResolution();

        if (frame.getProjectContainer().getPartViewSnapQuantization() == 0) {
            frame.getProjectContainer().setPartViewSnapQuantization(-1);
        // this.ticksPerBeat*4);
        // HACK PJL * frame.getProjectContainer().beatsPerBar);
        }
        // this.ticksToScreen = .02;

        dashedLineStroke = new BasicStroke(1, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL, 0, new float[]{5f, 5f}, 0); // Jens

        setLayout(null);
        setBackground(ColorScheme.partViewBackground);

        addComponentListener(this);
        makeTools();

        setFocusable(true);

        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }

    protected void processMouseEvent(MouseEvent e) {
        if (e.getID() == MouseEvent.MOUSE_PRESSED) {
            grabFocus();
        }
        super.processMouseEvent(e);
    }

    void setLaneHeader(LaneHeaderPanel head) {
        this.laneHeader = head;
        repaintItems();
    }

    void dispose() {
        this.project.getPartSelection().removeSelectionListener(this);
        this.editHistory.removeEditHistoryListener(this);
        removeComponentListener(this);
    }

    @Override
    public void dragTo(Point p) {

        int dxDragged = p.x - this.xAnchor;
        int dyDragged = p.y - this.yAnchor;

        // System.out.println(dxDragged + " " + dyDragged);

        double dt = dxDragged / userToScreen;
        double t = this.xAnchor / userToScreen;

        double tick = tempoList.getTickAtTime(t);
        double tick2 = tempoList.getTickAtTime(t + dt);

        // double bpm=tempoList.getTempoAt((long) tick);
        double dtick = tick2 - tick; // dt*project.getTicksPerBeat()*bpm/60.0;
        // System.out.println(dt + " " + t + " "+ dtick);

        if (!altIsDown && isSnapQuantized()) {
            double quant = this.getSnapQuantization();

            if (quant > 0.0) {
                dtick = Math.round(dtick / this.getSnapQuantization()) * this.getSnapQuantization();
            } else {
                double beat = tick / project.getTicksPerBeat();
                TimeSignatureEvent ev = project.getTimeSignatureList().getEventAtBeat((int) beat);
                quant = ev.beatsPerBar * project.getTicksPerBeat();
                dtick = Math.round(dtick / quant) * quant;
            }
        }

        // TODO will break when lanes are different hieghts
        int dLane = dyDragged / Layout.getLaneHeightScale();

        // Lane lane=laneAt(p.y);

        // If no significant movement then return
        if (Math.abs(dtick) < 1e-10 && dLane == 0 && !altIsDown && this.dragMode != OVER_ENVELOPE_GAIN && this.dragMode != OVER_ENVELOPE_RIGHT && this.dragMode != OVER_ENVELOPE_LEFT) {
            return;
        }
        int lMax = Integer.MIN_VALUE;
        int lMin = Integer.MAX_VALUE;
        int nLane = this.laneHeader.visibleLanes.getVisibleLanes().size();

        if (this.dragArmed) {
            startDrag();
        }
        PartImage dddPart = null;
        if (altIsDown && (this.dragMode == OVER_ITEM_MIDDLE || this.dragMode == OVER_ITEM_LEFT || this.dragMode == OVER_ITEM_RIGHT) && project.isPartViewSnapQuantized()) {
            dddPart = (PartImage) (dragList.get(0));
            if (this.dragMode == OVER_ITEM_RIGHT) {
//                double refTick = tempoList.getTickAtTime(dddPart.endTimeSecs) + dtick;
//                double sTick = project.partQuantize((long) refTick);
//                dtick = sTick - tempoList.getTickAtTime(dddPart.endTimeSecs);
                double refTick = tempoList.getTickAtTime(dddPart.endTimeSecs) + dtick;
                double sTick = project.partQuantize((long) refTick);
                dtick = sTick - tempoList.getTickAtTime(dddPart.endTimeSecs);
            } else {
                double refTick = tempoList.getTickAtTime(dddPart.startTimeSecs) + dtick;
                double sTick = project.partQuantize((long) refTick);
                dtick = sTick - tempoList.getTickAtTime(dddPart.startTimeSecs);
            }
            // System.out.println(dddNote +" " + dtick);
            if (dtick == 0) {
                return;
            }
        }

        if (this.dragMode == OVER_ENVELOPE_GAIN) {
            if (dyDragged == 0) {
                return;
            }
            for (PartImage pi : this.dragList) {
                if (pi.part instanceof AudioPart) {
                    AudioPart ap = (AudioPart) pi.part;
                    ap.getEvelope().setGain(
                            (pi.y + pi.height - p.y) / (double) pi.height);
                    ap.refreshEnvelope();
                }
            }
        } else if (this.dragMode == OVER_ENVELOPE_LEFT || this.dragMode == OVER_ENVELOPE_RIGHT) {
            if (dxDragged == 0) {
                return;
            }
            for (PartImage pi : this.dragList) {
                if (pi.part instanceof AudioPart) {
                    double fact = (p.x - pi.x) / (double) pi.width;
                    AudioPart ap = (AudioPart) pi.part;
                    if (this.dragMode == OVER_ENVELOPE_RIGHT) {
                        ap.getEvelope().setTOffRel(fact);
                    } else {
                        ap.getEvelope().setTOnRel(fact);
                    }
                    ap.refreshEnvelope();
                }
            }
        } else {

            for (PartImage pi : this.dragList) {
                lMax = Math.max(lMax, pi.laneId);
                lMin = Math.min(lMin, pi.laneId);
            }
            if (lMax + dLane >= nLane) {
                dLane = nLane - 1 - lMax;
            } else if (lMin + dLane < 0) {
                dLane = -lMin;
            }
            this.dragArmed = false;

            if (this.dragList == null) {
                return;
            }
            for (PartImage pi : this.dragList) {
                // MidiPart part = pi.part;

                switch (this.dragMode) {
                    case OVER_ITEM_MIDDLE:
                        pi.setMoveByDeltaTicks(dtick);

                        pi.laneId += dLane;
                        break;
                    case OVER_ITEM_RIGHT:
                        pi.setEndMoveByDeltaTicks(dtick);

                        dLane = 0;
                        break;
                    case OVER_ITEM_LEFT:
                        pi.setStartMoveByDeltaTicks(dtick);

                        // pi.startTimeSecs += dtick;
                        dLane = 0;
                        break;

                    default:
                        System.err.println(" unknown dragmode PartView" + this.dragMode);
                }

                pi.positionPartImage();
            }
        }
        // TODO will break when lanes are different heights
        // this.xAnchor = this.xAnchor + (int) (dt * this.userToScreen);
        this.yAnchor = this.yAnchor + dLane * Layout.getLaneHeightScale();

        repaintItems();
    }

    @Override
    public void clientClearSelection() {
        this.project.getPartSelection().clearSelection();
    }

    /**
     * Call this to start dragging with the reference point. See dragTo
     * 
     * @param e
     */
    public void startDrag() {

        this.dragList = new Vector<PartImage>();
        for (Part it : this.project.getPartSelection().getSelected()) {
            PartImage pi = new PartImage(it, it.getLane().getDisplayID());
            this.dragList.add(pi);
            pi.positionPartImage();
        }
    }

    /*
     * int laneIdOf(Lane lane) { return laneList.indexOf(lane); }
     */
    /**
     * Set all selectables in the rect to yes.
     * 
     * @param yes
     * @param rect
     */
    @Override
    public synchronized void selectInRect(Rectangle rect, boolean shift) {
        Vector<Part> addTmp = new Vector<Part>();
        Vector<Part> delTmp = new Vector<Part>();

        for (Lane lane : this.project.getLanes()) {

            for (Part it : lane.getParts()) {
                if (rect.intersects(getBounds(it))) {
                    if (shift) {
                        if (it.isSelected()) {
                            delTmp.add(it);
                        } else {
                            addTmp.add(it);
                        }
                    } else {
                        addTmp.add(it);
                    }
                }
            }
        }
        this.project.getPartSelection().removeSelected(delTmp);
        this.project.getPartSelection().addSelected(addTmp);
        this.project.getPartSelection().notifyListeners();

    }
    /**
     * return rectange of part in virtaul screen (unaffected by scrolling)
     */
    final Rectangle rectTmp = new Rectangle();

    final Rectangle getBounds(Part part) {

        double x1 = part.getStart(timeBased);
        double w = part.getDuration(timeBased);

        x1 = (int) userToScreen(x1);
        w = (int) userToScreen(w);

        Lane lane = part.getLane();
        int y1 = lane.getDisplayY();
        rectTmp.setBounds((int) x1, y1, (int) w, lane.getDisplayH());
        return rectTmp;
    // return new Rectangle((int) x1, y1, (int) w, lane.getDisplayH());

    }

    // Jens: (same as above, but returning a new instance of Rectangle)
    public Rectangle getPartBounds(Part part) {
        double x1 = part.getStart(timeBased);
        double w = part.getDuration(timeBased);
        x1 = (int) userToScreen(x1);
        w = (int) userToScreen(w);
        Lane lane = part.getLane();
        int y1 = lane.getDisplayY();
        return new Rectangle((int) x1, y1, (int) w, lane.getDisplayH());
    }

    /**
     * 
     */
    @Override
    public void rightButtonPressedOnItem(int x, int y) {
        frame.showRightButtonPartPopup(this, x, y);
    }

    @Override
    public void rectZoomFinished() {
        ((ItemRollToolBar) this.toolBar).rectZoomFinished();
    }

    @Override
    public synchronized void writeDraggedAt(Point p) {
        if (this.newPart == null) {
            return;
        }
        long tick = screenToTickAbs(p.x, true);

        double tick1 = this.newPart.getStart(timeBased);
        double tick2 = this.newPart.getEnd(timeBased);

        if (tick > tick2) {
            this.newPart.setEndTick(tick);
        } else if (tick < tick2 && (tick - tick1) > this.project.getPartViewSnapQuantization()) {
            this.newPart.setEndTick(tick);
        } else {
            return;
        }
        repaintItems();
    }

    @Override
    protected void writeReleasedAt(Point p) {

        if (this.newPart == null) {
            return;
        }
        this.project.getPartSelection().setSelected(this.newPart);
        this.project.getEditHistoryContainer().notifyEditHistoryListeners();
        this.project.getPartSelection().notifyListeners();
    }

    @Override
    protected synchronized void writePressedAt(Point p) {
        Lane lane;
        this.newPart = null;
        if (itemAt(p) != null) {
            System.out.println(" Can only create a Part in an empty space ");
            return;
        } else {
            lane = laneAt(p.y);
            if (lane == null) {
                System.out.println(" Please create a lane first");
                return;
            }
        }

        if (lane instanceof MidiLane) {
            this.project.getEditHistoryContainer().mark(
                    getMessage("sequencer.lane.add_part"));
            this.newPart = (MidiPart) lane.createPart(); // new
            // MidiPart((MidiLane)
            // lane);

            long tick = screenToTickAbs(p.x, true);
            this.newPart.setStartTick(tick);
            long quant = (long) this.project.getPartViewSnapQuantization();

            if (quant < 0) {
                TimeSignatureEvent ev = project.getTimeSignatureList().getEventAtBeat(
                        (int) (tick / this.project.getTicksPerBeat()));
                int beatPerBar = ev.beatsPerBar;
                quant = beatPerBar * project.getTicksPerBeat();
            }

            this.newPart.setEndTick(tick + quant);
            repaintItems();

        } else if (lane instanceof TextLane) { // Jens
            this.project.getEditHistoryContainer().mark(
                    getMessage("sequencer.lane.add_part"));
            long tick = screenToTickAbs(p.x, true);
            ((TextLane) lane).createNewTextPart(tick); // replace with gerenic
            // lane.createPart() ?
            repaintItems();

        }
    }

    int mapY(int y) {
        return y; // TODO stop being so confused about all this ampping
    }

    int unmapY(int y) {
        return y; // TODO stop being so confused about all this ampping
    }

    /**
     * Find component the contains point x,y and set. first component found is
     * set (is this what we want ?) TODO multitrack thinking.
     * 
     * @param x
     * @param y
     * @return
     */
    @Override
    public Item itemAt(Point p) {
        for (Lane lane : this.project.getLanes()) {
            if (lane.getParts() != null) {
                for (Part part : lane.getParts()) {
                    if (getBounds(part).contains(p)) {
                        return part;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 
     * @param virtualScreenRect
     *            area of screen that needs to be painted
     * 
     * 
     */
    @Override
    public synchronized void paintImageImpl(final Rectangle clipRect,
            Graphics2D g) {

        if (g == null) {
            return;
        }
        int w = clipRect.width;
        int x = clipRect.x;
        int y = clipRect.y;
        int h = clipRect.height;

        // Draw vertical lines

        g.setColor(ColorScheme.partViewBackground);
        g.fill(clipRect);

        double step = this.project.getPartViewSnapQuantization() / ticksPerBeat;

        TimeSignatureList ts = project.getTimeSignatureList();

        double beat1 = screenToTickAbs(x, true) / ticksPerBeat;
        double beat2 = screenToTickAbs(x + w, true) / ticksPerBeat;

        QStepIterator iter = project.getTimeSignatureList().createQStepIterator(beat1, beat2, step);
        // System.out.println(beat1 + " " + beat2 + " " + step);
        boolean drawSub = true; // (int)userToScreen((long) (step *
        // ticksPerBeat)) > 5;
        boolean drawBeat = true; // (int)userToScreen((long) (ticksPerBeat))
        // > 5;

        while (iter.hasNext()) {
            iter.next();
            double beat = iter.getBeat();
            boolean isBar = iter.isBar();
            // System.out.println(beat + " " + isBar);

            if (isBar) {
                g.setColor(ColorScheme.partViewLinesBar);
            } else {
                if (!drawBeat) {
                    continue;
                }
                if (Math.abs((beat + 1e-7) % 1) < 2e-7) {
                    g.setColor(ColorScheme.partViewLinesBeat);
                } else {
                    if (!drawSub) {
                        continue;
                    }
                    g.setColor(ColorScheme.partViewLinesSubBeat);
                }
            }

            long tick = (long) (beat * ticksPerBeat);
            double tt = project.getTempoList().getTimeAtTick(tick);

            // System.out.println(beat + " " + isBar + " "+ tick + " "+ tt);

            int x1 = (int) userToScreen(tt);
            g.drawLine(x1, y, x1, y + h);

        }

        // // Horizontal lines
        // //*/
        // g.setColor(ColorScheme.partViewLinesHoriz); int p1 = screenToLane(y);
        //		  
        // int y1 = laneToScreen(p1); if (y1 < y) { p1 -= 1; y1 =
        // laneToScreen(p1); } // assert (y1 >= y);
        //		  
        // while (y1 < y + h) { g.drawLine(x, y1, x + w, y1); p1 += 1; y1 =
        // laneToScreen(p1); }
        // //* /

        paintParts(g, clipRect);

    }

    /**
     * 
     * Private method to paint the part images.
     * 
     * 
     * @param g
     *            graphic
     * @param pb
     */
    private synchronized void paintParts(Graphics2D g, Rectangle pb) {

        final int gap = 2;

        // System.out.println("Paint parts ");

        Part focus = this.project.getPartSelection().getFocus();

        for (Lane lane : this.laneHeader.visibleLanes) {

            int lastX = -1; // Jens

            if (lane.getParts() == null) {
                continue;
            }
            synchronized (lane) {
                for (Part part : lane.getParts()) {

                    Rectangle rect = getBounds(part);
                    if (rect.isEmpty()) {
                        continue;
                    }
                    rect.y += gap;
                    rect.height -= 2 * gap;

                    if (pb != null && !pb.intersects(rect)) {
                        if (rect.x > pb.x + pb.width) {
                            lastX = -2; // last part is out of visible rect, mark as
                        // don't look further, Jens
                        }
                        continue;
                    }

                    // Jens:
                    if (lastX != -2) {
                        int xx = rect.x + rect.width;
                        if (xx > lastX) {
                            lastX = xx;
                        }
                    }

                    Color color = part.getTransparentColor(); // Jens
                    if (part instanceof Ghost) {
                        int a = color.getAlpha() / 4;
                        color = new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
                    }
                    g.setColor(color);
                    g.fill(rect);

                    if (part == focus) {
                        g.setColor(Color.BLUE);
                    } else {
                        g.setColor(Color.BLACK);
                    }
                    g.draw(rect);

                    part.drawThumbNail(g, rect, this);

                    if (part.isSelected() && !(part.getLane() instanceof SynthLane)) {

                        if (this.splitting) {
                            if (part.getStartTick() < this.splitTick && part.getEndTick() > this.splitTick) {
                                double time = tempoList.getTimeAtTick(this.splitTick);
                                int xSplit = (int) userToScreen(time);
                                g.setColor(Color.WHITE);
                                g.drawLine(xSplit, rect.y - 2, xSplit, rect.y + rect.height + 2);
                            }
                        }
                    }

                }
            }
            // Jens:
            MidiPlayOptions opt;
            if ((lastX >= 0) && (lane instanceof MidiLane) && ((opt = ((MidiLane) lane).getPlayOptions()).looped)) {
                g.setColor(Color.GRAY); // mark rest of lane gray if looped-mode
                // (indicating that track still goes on
                // playing)
                java.awt.Composite compositeBackup = g.getComposite();
                g.setComposite(java.awt.AlphaComposite.getInstance(
                        java.awt.AlphaComposite.SRC_OVER, (float) 0.666));
                g.fillRect(lastX + 1, lane.getDisplayY() + 1, pb.width - lastX - 2, lane.getDisplayH() - 2);
                long lastTick = ((MidiLane) lane).getTrack().lastTickUsed();
                Rectangle r = new Rectangle();
                r.width = (int) userToScreen(opt.loopedTicks);
                r.height = lane.getDisplayH() - 2;
                r.x = (int) userToScreen(lastTick) + 1;
                r.y = lane.getDisplayY() + 1;
                g.setColor(Color.BLACK);
                java.awt.Stroke strokeBackup = g.getStroke();
                g.setStroke(dashedLineStroke);
                while (pb.intersects(r)) {
                    g.drawRect(r.x, r.y, r.width, r.height);
                    lastTick += opt.loopedTicks;
                    r.x = (int) userToScreen(lastTick);
                }
                g.setStroke(strokeBackup);
                g.setComposite(compositeBackup);
            }
        }

        if (this.dragList == null) {
            for (Part part : this.project.getPartSelection().getSelected()) {

                Rectangle rect = getBounds(part);

                if (part instanceof AudioPart) {
                    rect.y += gap;
                    rect.height -= 2 * gap;
                    ((AudioPart) part).drawEnvelope(g, rect, this);
                } else {

                    rect.y += gap - 1;
                    rect.height -= 2 * (gap - 1);
                    // rect.x += 1;
                    // rect.width -= 2;
                    if (part == focus) {
                        g.setColor(Color.BLUE);
                    } else {
                        g.setColor(Color.BLACK);
                    }
                    g.draw(rect);
                    part.drawThumbNail(g, rect, this);
                }
            }

            return;
        }

        for (PartImage partImage : this.dragList) {
            if (pb != null && !pb.intersects(partImage)) {
                continue;
            }
            Rectangle rect = new Rectangle(partImage);
            Part part = partImage.part;
            if ((this.dragMode == OVER_ENVELOPE_GAIN || this.dragMode == OVER_ENVELOPE_RIGHT || this.dragMode == OVER_ENVELOPE_LEFT) && part instanceof AudioPart) {
                rect.y += gap;
                rect.height -= 2 * gap;
                ((AudioPart) part).drawEnvelope(g, rect, this);
                continue;
            }

            g.setColor(Color.RED);
            g.draw(rect);

            if (this.dragMode != OVER_ITEM_MIDDLE) {
                if (!(partImage.part instanceof AudioPart)) {
                    partImage.part.drawThumbNail(g, rect, this);
                }
            }
        }
    }

    @Override
    public void endDrag() {

        if (this.dragList == null) {
            return;
        }
        switch (this.dragMode) {
            case OVER_ITEM_MIDDLE:
                if (!this.controlIsDown) {
                    this.editHistory.mark(getMessage("sequencer.partview.drag_move_part"));
                } else {
                    this.editHistory.mark(getMessage("sequencer.partview.drag_copy_part"));
                }
                break;
            case OVER_ITEM_RIGHT:
            case OVER_ITEM_LEFT:
                this.editHistory.mark(getMessage("sequencer.partview.resize"));
                break;
            case OVER_ENVELOPE_LEFT:
            case OVER_ENVELOPE_RIGHT:
            case OVER_ENVELOPE_GAIN:
                System.out.println(" TODO  history for envelope dragging " + this.dragMode);
                break;
            default:
                System.err.println(" unknown dragmode " + this.dragMode);
        }

        // Collection<Part> selected = project.getPartSelection().getSelected();

        for (PartImage pi : this.dragList) {
            pi.drop();
        }

        this.dragList = null;

        this.project.getEditHistoryContainer().notifyEditHistoryListeners();
    // repaintItems();
    }

    @Override
    public void erase(Item it) {
        Part part = (Part) it;
        this.editHistory.mark("sequencer.project.erase_part");
        part.getLane().remove(part);
        this.project.getPartSelection().removeSelected(part);
        this.editHistory.notifyEditHistoryListeners();
        this.project.getPartSelection().notifyListeners();
    }

    /*
     * public void addTrackSelectionListener(PartSelectionListener l) { //
     * trackSelectionListeners.add(l); }
     */
    @Override
    public boolean requiresNotificationOnEachTick() {
        return false;
    }

    public class PartImage extends Rectangle {

        private double endTimeSecs;
        private double endTimeDelta;
        private double startTimeSecs;
        private double startTimeDelta;
        public Color color;
        /**
         * 
         */
        private static final long serialVersionUID = 1L;        // long length;
        int laneId;
        Part part;

        PartImage(Part part, int laneId) {

            this.color = part.getColor();
            this.startTimeSecs = part.getStartInSecs();
            this.endTimeSecs = part.getEndInSecs();
            // length = part.getDuration();
            this.laneId = laneId;
            this.part = part;
        // print("Create");
        }

        private void print(String txt) {
            if (txt != null) {
                System.out.println(txt);
            }
            TimeUtils tu = project.getTimeUtils();

            System.out.println(" start=" + tu.tickToBarBeatTick((long) tempoList.getTickAtTime(startTimeSecs + startTimeDelta)));
            System.out.println("   end=" + tu.tickToBarBeatTick((long) tempoList.getTickAtTime(endTimeSecs + endTimeDelta)));

        }

        public void setEndMoveByDeltaTicks(double dtick) {
            double endTick = tempoList.getTickAtTime(endTimeSecs);
            endTimeDelta = tempoList.getTimeAtTick(endTick + dtick) - endTimeSecs;
        }

        public void setMoveByDeltaTicks(double dtick) {
            if (part instanceof MidiPart) {
                setStartMoveByDeltaTicks(dtick);
                setEndMoveByDeltaTicks(dtick);
            } else {
                setStartMoveByDeltaTicks(dtick);
                endTimeDelta = startTimeDelta;
            }
        }

        public void setStartMoveByDeltaTicks(double dtick) {
            double startTick = tempoList.getTickAtTime(startTimeSecs);
            startTimeDelta = tempoList.getTimeAtTick(startTick + dtick) - startTimeSecs;
        }

        void drop() {

            switch (PartView.this.dragMode) {
                case OVER_ITEM_MIDDLE:
                    Lane lane = this.part.getLane();
                    Lane destLane = PartView.this.laneHeader.visibleLanes.getVisibleLanes().get(this.laneId);

                    if (lane.getClass() != destLane.getClass()) {
                        if (!(destLane instanceof AudioLane && lane instanceof SynthLane && PartView.this.controlIsDown)) {
                            System.out.println(" Lanes must be the same type to copy a part");
                            return;
                        }
                    }

                    if (lane.getDisplayID() == this.laneId) {
                        assert (destLane == lane);
                    }

                    double dTick = tempoList.getTickAtTime(this.startTimeSecs + startTimeDelta) - tempoList.getTickAtTime(startTimeSecs);

                    if (!PartView.this.controlIsDown) {
                        // System.out.println("moveby " + dTick);
                        MovePartEditAction act = new MovePartEditAction(this.part,
                                dTick, destLane);

                        act.redo();

                        PartView.this.editHistory.push(act);

                    } else {
                        this.part.copyBy(dTick, destLane);
                        try {
                            if (MidiPart.class.isInstance(part)) {
                                ((MidiPart) this.part).rebuildMultiEventEndTickComparables(); // PJS:
                            // Previously
                            // was
                            // .onLoad()
                            // -
                            // but
                            // caused
                            // that
                            // the
                            // original
                            // events
                            // where
                            // inserted
                            // into
                            // the
                            // ftw
                            // twice
                            } else {
                                this.part.onLoad();
                            }
                        } catch (Exception e) {
                            System.err.println(" SHOULD NEVER HAPPEN ");
                            e.printStackTrace();
                        }
                    }
                    break;
                case OVER_ITEM_RIGHT:
                case OVER_ITEM_LEFT:

                    ResizePartAction act = new ResizePartAction(this.part,
                            this.startTimeSecs + startTimeDelta, this.endTimeSecs + endTimeDelta);
                    act.redo();
                    PartView.this.editHistory.push(act);
                    break;
                default:
                    System.err.println(" unknown dragmode In PARTVIEW PartImage drop" + PartView.this.dragMode);
            }

        }

        /**
         * Set the rectanlge for this PartImage
         * 
         * @param pi
         */
        private void positionPartImage() {

            double x1 = startTimeSecs + startTimeDelta;
            double w = endTimeSecs + endTimeDelta - startTimeSecs - startTimeDelta;

            x1 = (int) userToScreen(x1);
            w = (int) userToScreen(w);

            Lane lane = laneHeader.visibleLanes.getVisibleLanes().get(laneId);

            int y1 = lane.getDisplayY();

            x = (int) x1;
            y = y1;
            width = (int) w;
            height = lane.getDisplayH();
        // System.out.println(pi);

        }
    }

    /*
     * @Override public void deleteSelected() { // TODO Auto-generated method
     * stub }
     */
    @Override
    public void clientAddToSelection(Item item) {
        this.project.getPartSelection().addSelected((Part) item);
        this.project.getPartSelection().notifyListeners();
    }

    @Override
    public void clientRemoveFromSelection(Item item) {
        this.project.getPartSelection().removeSelected((Part) item);
        this.project.getPartSelection().notifyListeners();
    }

    /*
     * public void selectionCleared(SelectionContainer<Part> src) {
     * repaintItems(); }
     * 
     * public void addedToSelection(SelectionContainer<? extends Part> src,
     * Collection<? extends Part> items) { repaintItems(); }
     * 
     * public void removedFromSelection(SelectionContainer<? extends Part> src,
     * Collection<? extends Part> items) { repaintItems(); }
     */
    @Override
    public int getHoverStateAt(Point p) {

        final int endTol = 20;
        // final int extraX = 20 ;
        // if (true) return OVER_ITEM_MIDDLE;

        int tol = endTol;

        for (Lane lane : this.laneHeader.visibleLanes) {
            if (lane.getParts() == null) {
                continue;
            }
            for (Part part : lane.getParts()) {
                Rectangle rect = getBounds(part);

                if (rect.contains(p)) {

                    if (part instanceof AudioPart) {
                        int ret = ((AudioPart) part).getHoverState(p, rect);
                        if (ret != -1) {
                            return ret;
                        }
                    }

                    if (rect.width < endTol * 5) {
                        tol = rect.width / 3;
                    }
                    if ((p.x - rect.x) <= tol) {
                        return OVER_ITEM_LEFT;
                    }
                    if ((rect.x + rect.width - p.x) <= tol) {
                        return OVER_ITEM_RIGHT;
                    }
                    return OVER_ITEM_MIDDLE;
                }
            }
        }
        return OVER_NOTHING;
    }

    void makeTools() {
        Cursor c = new Cursor(Cursor.DEFAULT_CURSOR);
        this.selectTool = new SelectTool(c);
        this.rectZoomTool = new RectZoomTool(c);

        this.writeTool = new WriteTool(MyCursors.getCursor("pencil"));
        this.eraseTool = new EraseTool(MyCursors.getCursor("eraser"));
        this.dragViewTool = new DragViewTool(MyCursors.getCursor("move"));
        this.splitTool = new PartSplitTool(Cursor.getDefaultCursor());
        this.glueTool = new PartGlueTool(MyCursors.getCursor("glue"));

    }

    Lane laneAt(int yy) {
        int y = unmapY(yy);
        for (Lane lane : this.laneHeader.visibleLanes) {
            if (lane.getDisplayY() <= y && (lane.getDisplayY() + lane.getDisplayH()) > y) {
                return lane;
            }
        }
        return null;
    }

    public void fireSequenceDataChanged(EditHistoryAction[] edithistoryActions) {
        repaintItems();
    }

    public void selectionChanged(SelectionContainer<? extends Part> src) {
        repaintItems();
    }

    @Override
    protected void paintImageImplLabel(Graphics2D graphics) {
        // no labels to draw do nothing.
    }

    @Override
    public double getSnapQuantization() {
        return this.project.getPartViewSnapQuantization();
    }

    @Override
    public void setSnapQuantization(double quant) {
        this.project.setPartViewSnapQuantization(quant);
        repaintItems();

    }

    @Override
    public boolean isSnapQuantized() {
        return this.project.isPartViewSnapQuantized();
    }

    @Override
    public void setSnapQuantized(boolean b) {
        this.project.setPartViewSnapQuantized(b);
    }

    @Override
    public void setFocus(Item item) {
        this.project.getPartSelection().setFocus((Part) item);

    }

    @Override
    public ToolAdapter getTool(String name) {
        this.splitting = false;
        if (name.equals("split")) {
            this.splitting = true;
            return this.splitTool;
        } else if (name.equals("glue")) {
            return this.glueTool;
        }
        return super.getTool(name);
    }

    public void splitIsOver(Point p) {
        // System.out.println(" split is over " + p );
        boolean quant=project.isPartViewSnapQuantized();
        long tick = screenToTickAbs(p.x, quant);
        if (this.splitTick == tick) {
            return;
        }
        this.splitTick = tick;

//	System.out.println(" split is over " + p + "  "
//				+ project.getTimeUtils().tickToBarBeatTick(tick));
        repaintItems();

    // TODO Auto-generated method stub

    }

    public void splitAt(Point p) {

        SplitPartAction act = new SplitPartAction(this.project,
                screenToTickAbs(p.x, project.isPartViewSnapQuantized()));
        this.project.getEditHistoryContainer().mark(
                getMessage("sequencer.partview.split.part"));
        act.redo();
        this.project.getEditHistoryContainer().push(act);
        this.project.getEditHistoryContainer().notifyEditHistoryListeners();
        this.project.getPartSelection().notifyListeners();
    }

    public void gluePart(Item item) {
        EditHistoryAction act = new GluePartEditAction((MidiPart) item);
        this.project.getEditHistoryContainer().mark(
                getMessage("sequencer.partview.glue.part"));
        act.redo();
        this.project.getEditHistoryContainer().push(act);
        this.project.getEditHistoryContainer().notifyEditHistoryListeners();
        this.project.getPartSelection().notifyListeners();
    }

    public void selectAll() {
        Vector<Part> list = new Vector<Part>();
        for (Lane lane : this.laneHeader.visibleLanes) {
            for (Part part : lane.getParts()) {
                list.add(part);
            }
        }
        this.project.getPartSelection().setSelected(list);
        this.project.getPartSelection().notifyListeners();
    }

    @Override
    public void clientNotifySelectionChange() {
        this.project.getPartSelection().notifyListeners();

    }

    @Override
    /**
     * Invoked by ItemPanel.setTimeLineEvent to set cursor based on a click on
     * the timeline
     */
    public void setTimeAtX(int x) {
        boolean iQuant=project.isPartViewSnapQuantized();
        long tick = screenToTickAbs(x, iQuant);
        this.sequencer.setTickPosition(tick);
    }

    public ProjectFrame getProjectFrame() {
        return frame;
    }
}
