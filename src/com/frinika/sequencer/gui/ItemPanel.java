/*
 * Created on Feb 8, 2006
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
package com.frinika.sequencer.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.ConcurrentModificationException;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.SongPositionListener;
import com.frinika.sequencer.model.tempo.TempoList;
import com.frinika.sequencer.model.timesignature.TimeSignatureList;
import com.frinika.sequencer.model.timesignature.TimeSignatureList.QStepIterator;
import com.frinika.sequencer.model.timesignature.TimeSignatureList.TimeSignatureEvent;
import com.frinika.sequencer.model.util.TimeUtils;
import static com.frinika.localization.CurrentLocale.getMessage;

/**
 * Basis for PianoRoll and PartView
 * 
 * We imagine the pianoRoll/trackView to be drawn on a large "virtualScreen"
 * such that the origins (pitch=0,beat=0) coincide with (0,0)
 * 
 * ItemPanel provides a view of this through itemViewRect.
 * 
 * 
 * @author pjl
 * 
 */
abstract public class ItemPanel extends JPanel implements SongPositionListener,
        ComponentListener, Snapable {

    public static final int OVER_NOTHING = 0;
    public static final int OVER_ITEM_MIDDLE = 1;
    public static final int OVER_ITEM_RIGHT = 2;
    public static final int OVER_ITEM_LEFT = 3;
    protected static final int OVER_ITEM_TOP = 4;
    public static final int OVER_ENVELOPE_LEFT = 5;
    public static final int OVER_ENVELOPE_RIGHT = 6;
    public static final int OVER_ENVELOPE_GAIN = 7;
    static Cursor cursors[] = new Cursor[OVER_ENVELOPE_GAIN + 1];


    static {
        cursors[OVER_NOTHING] = new Cursor(Cursor.DEFAULT_CURSOR);
        cursors[OVER_ITEM_MIDDLE] = new Cursor(Cursor.MOVE_CURSOR);
        cursors[OVER_ITEM_RIGHT] = new Cursor(Cursor.E_RESIZE_CURSOR);
        cursors[OVER_ITEM_LEFT] = new Cursor(Cursor.W_RESIZE_CURSOR);
        cursors[OVER_ITEM_TOP] = new Cursor(Cursor.N_RESIZE_CURSOR);
        cursors[OVER_ENVELOPE_LEFT] = new Cursor(Cursor.NW_RESIZE_CURSOR);
        cursors[OVER_ENVELOPE_RIGHT] = new Cursor(Cursor.NE_RESIZE_CURSOR);
        cursors[OVER_ENVELOPE_GAIN] = new Cursor(Cursor.N_RESIZE_CURSOR);
    }
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // int cursorInc = Layout.cursorInc; // minimum jump of the tmie cursor in

    // pixels (increase

    // to reduce CPU burn)
    private boolean rightButtonPressed;
    private Graphics itemGraphics; // main offscreen image graphics
    private Graphics timeGraphics; // time line offscreen image graphics
    boolean ignoreRepaint = false;
    private Image itemImage;
    Rectangle itemViewRect = new Rectangle();
    protected Rectangle lastItemViewRect = new Rectangle();
    StrechyRectangle selectRect = null;
    protected int ticksPerBeat;

    // TODO yuckkkk
    public double userToScreen = .5; // scaling for ticks
    protected boolean timeBased = false;
    private Image timeImage;

    // protected int leftMargin = 0; // TODO doesn't work (well I can not work
    // out what to do).

    // I only need this to inform a rectangleZoom finish TODO a better way ?
    protected JToolBar toolBar;
    protected int xAnchor = 0;
    int xCursor = -1; // current time cursor position
    private double xFractLeftStaticWindow = .1;
    private double xFractRightStaticWindow = .9;
    protected int yAnchor = 0;
    StrechyRectangle zoomRect = null;
    protected ItemScrollPane scroller; // thing that controls the scrolling
    private boolean followSong = true; // automatic scrolling to keep time

    // cursor

    // in window

    // protected boolean quantize; //
    protected FrinikaSequencer sequencer;
    protected boolean dragArmed; // about to be dragged? but not started yet.
    private boolean dirty;
    protected int dragMode;
    protected ProjectContainer project;
    boolean hasTimeLine;
    private int timePanelHeight;
    private boolean canScrollY;
    protected ToolAdapter selectTool;
    protected ToolAdapter eraseTool;
    protected ToolAdapter writeTool;
    protected ToolAdapter dragViewTool;
    protected ToolAdapter rectZoomTool;

    /*
     * current tool
     */
    protected ToolAdapter tool;

    /*
     * remember old tool doing zoom rects;
     */
    private ToolAdapter prevTool;
    private ToolAdapter origTool;
    Polygon leftMarker;
    int xLMarker[] = {0, (int) (Layout.timePanelHeight * .6), 0};
    int yMarker[] = {0, 0, (int) (Layout.timePanelHeight * .6)};
    Polygon rightMarker;
    int xRMarker[] = {0, -xLMarker[1], 0};
    long startLoopTime;
    long endLoopTime;
    protected Item dragItem = null;
    protected boolean controlIsDown = true;
    protected boolean altIsDown = true;
    ExtendingRangeModel xRangeModel = new ExtendingRangeModel();
    DefaultBoundedRangeModel yRangeModel = new DefaultBoundedRangeModel();
    private double endTimeOrTick = -1;
    TimeUtils timeUtil;
    protected boolean isChanging = false;

    //protected ProjectFrame frame;
    private int previousCursor;

    protected ItemPanel(ProjectContainer project, ItemScrollPane scroller,
            boolean hasTimeLine, boolean canScrollY, double ticksToScreen,
            boolean sampleBased) {
        super(false);
        this.userToScreen = ticksToScreen;
        this.timeBased = sampleBased;
        timeUtil = new TimeUtils(project);

        this.canScrollY = canScrollY;
        this.hasTimeLine = hasTimeLine;
        if (hasTimeLine) {
            timePanelHeight = Layout.timePanelHeight;
        }
        this.project = project;
        //	this.frame = project;
        this.scroller = scroller;
        this.selectRect = new StrechyRectangle(this);
        this.zoomRect = new StrechyRectangle(this);
        addComponentListener(this);
        if (hasTimeLine) {
            leftMarker = new Polygon(xLMarker, yMarker, 3);
            rightMarker = new Polygon(xRMarker, yMarker, 3);
        }
        startLoopTime = project.getSequencer().getLoopStartPoint();
        endLoopTime = project.getSequencer().getLoopEndPoint();
        validateEndTick();

        setToolTipText(""); // Indicate that this component has tooltips
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (isTimeLineEvent(event)) {
            return getMessage("sequencer.itempanel.timeline.loopmarkers.tooltip");
        } else {
            return null;
        }
    }

    public ItemScrollPane getScroller() {
        return scroller;
    }

    public void componentHidden(ComponentEvent arg0) {
    }

    public void componentMoved(ComponentEvent arg0) {
    }

    public synchronized void componentResized(ComponentEvent arg0) {

        Dimension newSize = getSize();

        newSize.height = Math.max(0, newSize.height - timePanelHeight);
        newSize.width = Math.max(0, newSize.width);

        if (newSize.height == 0 || newSize.width == 0) {
            this.itemImage = null;
            this.itemGraphics = null;
        // System.err.println(" Warning zero height in ItemPanel image ");
        // return;
        } else {
            this.itemImage = createImage(newSize.width, newSize.height);
            this.itemGraphics = this.itemImage.getGraphics();
        // this.itemViewRect.setSize(newSize);

        }

        if (newSize.width == 0) {

            // System.err.println(" Warning zero width in ItemPanel image ");
        } else {

            //
            if (hasTimeLine) {
                this.timeImage = createImage(newSize.width, timePanelHeight);
                this.timeGraphics = this.timeImage.getGraphics();
            }
        }

        // newSize.width = newSize.width;
        this.itemViewRect.setSize(newSize);

        paintImages();
        repaint(); // TODO move up ?
    }

    public void componentShown(ComponentEvent arg0) {
    }

    protected abstract void writePressedAt(Point p);

    protected abstract void writeReleasedAt(Point p);

    public abstract void dragTo(Point p);

    /**
     * This will be called when the mouse is released from a dragging operation
     *
     */
    abstract public void endDrag();

    public Rectangle getVirtualScreenRect() {
        return this.itemViewRect;
    }

    public abstract Item itemAt(Point p);

    public int mapX(int x) {
        return x + this.itemViewRect.x;
    }

    /**
     *
     * @param point
     */
    public void map(Point point) {
        point.x += this.itemViewRect.x;
        point.y += this.itemViewRect.y;
        if (hasTimeLine) {
            point.y -= Layout.timePanelHeight;
        }
    }

    Rectangle mapRect(Rectangle screenRect) {
        Rectangle rect = new Rectangle(screenRect);
        rect.x += this.itemViewRect.x;
        rect.y += this.itemViewRect.y;
        if (hasTimeLine) {
            rect.y -= Layout.timePanelHeight;
        }
        return rect;
    }

    // abstract public void notifySelectionChange();
    static Rectangle dirtyRect = new Rectangle();

    public synchronized void notifyTickPosition(long tick) {

        //if (this instanceof PartView ) System.out.println("A" + tick);
        double userTime;

        if (timeBased) {
            userTime = project.getTempoList().getTimeAtTick(tick);
        } else {
            userTime = tick;
        }


        // make sure scroll is OK

        int pixPerRedraw = 1;

        if (sequencer.isRunning()) {
            if (!isShowing()) {
                return;
            }

            pixPerRedraw = project.getPixelsPerRedraw();

            if (pixPerRedraw <= 0) {
                return;
            }
        } else {
            //if (this instanceof PartView ) System.out.println("B" );
            validateEndTick();
        }





        double scrX = userToScreen(userTime);
        int x = (int) (scrX / pixPerRedraw) * pixPerRedraw;

        //if (this instanceof PartView ) System.out.println("B " + x + "  " + this.xCursor + " " + pixPerRedraw  + " " + userTime + " " + scrX);


        long st = project.getSequencer().getLoopStartPoint();
        long et = project.getSequencer().getLoopEndPoint();

        if ((x == this.xCursor)) {
            if (!hasTimeLine) {
                return;
            }
            if ((st == startLoopTime) && (et == endLoopTime)) {
                return;
            }
        }
        //if (this instanceof PartView ) System.out.println("C" );

        this.previousCursor = this.xCursor;
        this.xCursor = x;

        if ((st == startLoopTime) && (et == endLoopTime)) {

            if (followSong) {
                try {
                    if (!scrollToContain(x)) {


                        Rectangle tR = this.getBounds();
                        dirtyRect.y = tR.y;
                        dirtyRect.height = tR.height;
                        int x1 = previousCursor - this.itemViewRect.x;
                        if (x1 >= 0 && x1 < tR.x + tR.width) {
                            dirtyRect.x = x1;
                            dirtyRect.width = 1;
                            repaint(dirtyRect);
                        }

                        int x2 = x - this.itemViewRect.x;

                        if (x2 >= 0 && x2 < tR.x + tR.width) {
                            dirtyRect.x = x2;
                            dirtyRect.width = 1;
                            repaint(dirtyRect);
                        }

                    }
                } catch (ConcurrentModificationException e) {
                    // TODO: This exception occurs every now and then - ignore
                    // until a better solution is found
                    e.printStackTrace();
                }
            } else {
                repaint();
            }

        } else {
            endLoopTime = et;
            startLoopTime = st;
            repaintItems();
        }
    }

    public synchronized boolean scrollToContain(int x) {

        double posFract = (x - this.itemViewRect.x) / (double) this.itemViewRect.width;

        boolean jumped = false;
        int xLeft = 0;

        if (posFract < 0 || posFract > this.xFractRightStaticWindow) {
            xLeft = (int) (x - this.itemViewRect.width * this.xFractLeftStaticWindow);

            if (xLeft < 0) {
                xLeft = 0;
            }
            jumped = true;
        }

        if (jumped) {
            scroller.setX(xLeft);
            paintImages(); // TODO copyArea ?
            repaint();
        }
        return jumped;
    }

    private synchronized void drawCursor(Graphics g, int xt) {

        int x = xt - this.itemViewRect.x;
        int y1 = 0;
        int h = getHeight() - 1;
        int y2 = y1 + h;
        g.setColor(Color.PINK);
        g.drawLine(x, y1, x, y2);
    // g.setPaintMode();
    }

    public synchronized void copyImageToScreen(Graphics g) {
        if (this.ignoreRepaint) {
            return;
        }
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        if (this.itemImage != null) {
            g.drawImage(this.itemImage, 0, timePanelHeight, null);
        }
        if (hasTimeLine && this.timeImage != null) {
            g.drawImage(this.timeImage, 0, 0, null);
        }

        if (this.zoomRect.isActive()) {
            g.setColor(Color.GREEN);
            g2.draw(this.zoomRect);
        }

        if (this.selectRect.isActive()) {
            g.setColor(Color.BLUE);
            g2.draw(this.selectRect);
        }

    }

    abstract protected void paintImageImpl(Rectangle clipRect, Graphics2D g);

    /**
     * paint the itemImage and timeLineImage.
     *
     * Notes:is it worth doing copyArea if we have a wide static window ? Maybe
     * for the user scroll ?
     *
     */
    protected synchronized void paintImages() {
        if (this.ignoreRepaint) {
            return;
        }

        if (this.itemViewRect.isEmpty()) {
            return;
        }

        // HACK HACK HACK HACK
        if (true) {
            paintItemImage_(this.itemViewRect);
            if (hasTimeLine) {
                paintTimeImage_(this.itemViewRect.x, this.itemViewRect.width);
            }
            return;
        }

        // ************** THIS GOT COMPLICATED
        // *******************************************
        // TODO this stuff might be worth getting working for small scrolls
        // (lots of)
        if (!this.lastItemViewRect.getSize().equals(this.itemViewRect.getSize())) {

            // first time or resize repaint the lot.

            paintItemImage_(this.itemViewRect);
            if (hasTimeLine) {
                paintTimeImage_(this.itemViewRect.x, this.itemViewRect.width);
            }
            this.lastItemViewRect.setBounds(this.itemViewRect);

        } else if (this.lastItemViewRect.equals(this.itemViewRect)) {
            System.err.println(" paintItemImage with same virtual rect !!!!!!! ");

            // Same as last rect probably mistake in my logic / resize causes
            // this to happen.
            // paint it anyway otherwise it doesn't work

            paintItemImage_(this.itemViewRect);
            if (hasTimeLine) {
                paintTimeImage_(this.itemViewRect.x, this.itemViewRect.width);
            }

        } else if (this.lastItemViewRect.x != this.itemViewRect.x) {

            if (this.lastItemViewRect.y != this.itemViewRect.y) {
                paintItemImage_(this.itemViewRect);
                if (hasTimeLine) {
                    paintTimeImage_(this.itemViewRect.x,
                            this.itemViewRect.width);
                }
                this.lastItemViewRect.setBounds(this.itemViewRect);
            } else {
                if (hasTimeLine) {
                    paintTimeImage_(this.itemViewRect.x,
                            this.itemViewRect.width);
                }
                // TODO scrolly stuff ?
                scrollItemImageX();
                this.lastItemViewRect.setBounds(this.itemViewRect);
            }

        } else if (this.lastItemViewRect.y != this.itemViewRect.y) {
            scrollItemImageY();
            this.lastItemViewRect.setBounds(this.itemViewRect);
        }
    }

    private synchronized void paintItemImage_(Rectangle visibleRect) {
        this.itemGraphics.translate(-this.itemViewRect.x, -this.itemViewRect.y);
        paintImageImpl(visibleRect, (Graphics2D) this.itemGraphics);
        this.itemGraphics.translate(this.itemViewRect.x, this.itemViewRect.y);
        paintImageImplLabel((Graphics2D) this.itemGraphics);
    }

    /**
     * override this to draw on top of screen
     *
     * @param graphics
     */
    protected abstract void paintImageImplLabel(Graphics2D graphics);

    private void paintTimeImage_(int x, int width) {
        assert (hasTimeLine);
        this.timeGraphics.translate(-this.itemViewRect.x, 0);
        paintTimeImpl(x, width, (Graphics2D) this.timeGraphics);
        this.timeGraphics.translate(this.itemViewRect.x, 0);

    }

    public void paintTimeImpl(int xClip, int widthClip, Graphics g1) {
        if (timeBased) {
            paintTimeImplUser(xClip, widthClip, g1);
        } else {
            paintTimeImplTick(xClip, widthClip, g1);
        }

    }

    private void paintTimeImplTick(int xClip, int widthClip, Graphics g1) {
        assert (hasTimeLine);

        Graphics2D g = (Graphics2D) g1;
        // TODO clipping rect
        g.setColor(Color.BLACK);

        /*
         * TODO This next bit needs thinking about. because the text has a width
         * we must sometimes draw text even if the start point is outside the
         * graphics cip region this is a brute force implementation.
         */
        Rectangle b = new Rectangle();
        b.x = this.itemViewRect.x;
        b.y = 0;
        b.height = timePanelHeight;
        b.width = this.itemViewRect.width;

        g.fill(b);
        // if (true) return;

        // Add a bit to the clipping because of extent of the characters (TODO
        // do this better)
        int w = b.width + 400;
        int x = Math.max(0, b.x - 200);
        int y = b.y;
        int h = b.height;

        int charHeight = timePanelHeight - 5;

        int y1 = Math.max(y, charHeight);

        double step = this.getSnapQuantization() / ticksPerBeat;

        TimeSignatureList ts = project.getTimeSignatureList();

        assert (x >= 0);
        double beat1 = screenToTickAbs(x, true) / ticksPerBeat;
        double beat2 = screenToTickAbs(x + w, true) / ticksPerBeat;

        QStepIterator iter = project.getTimeSignatureList().createQStepIterator(beat1, beat2, step);

        g.setColor(Color.WHITE);
        boolean drawlines = (y1 < y + h);
        boolean drawNumber = (y < charHeight);

        boolean drawSub = drawlines && userToScreen((long) (step * ticksPerBeat)) > 5;
        boolean drawBeat = drawlines && userToScreen((long) (ticksPerBeat)) > 5;

        int minSpace = 50;
        int xlast = -2 * minSpace;

        while (iter.hasNext()) {
            iter.next();
            double beat = iter.getBeat();
            boolean isBar = iter.isBar();
            int x1 = (int) (userToScreen((long) (beat * ticksPerBeat)));

            if (isBar) {
                if (x1 - xlast > minSpace) {
                    g.setColor(Color.WHITE);
                    g.drawString(String.valueOf(iter.getBar()), x1,
                            charHeight - 2);
                    xlast = x1;
                    g.drawLine(x1, y1, x1, y1 + 3);
                }
            }
        }


        long xStart = (int) userToScreen(startLoopTime);
        long xEnd = (int) userToScreen(endLoopTime);

        if (xStart > x && xStart < x + w) {
            g.setColor(Color.YELLOW);
            g.translate(xStart, 0);
            g.fill(leftMarker);
            g.translate(-xStart, 0);

        }

        if (xEnd > x && xEnd < x + w) {
            g.setColor(Color.YELLOW);
            g.translate(xEnd, 0);
            g.fill(rightMarker);
            g.translate(-xEnd, 0);

        }

    }

    public void paintTimeImplUser(int xClip, int widthClip, Graphics g1) {
        assert (hasTimeLine);

        Graphics2D g = (Graphics2D) g1;
        // TODO clipping rect
        g.setColor(Color.BLACK);

        /*
         * TODO This next bit needs thinking about. because the text has a width
         * we must sometimes draw text even if the start point is outside the
         * graphics cip region this is a brute force implementation.
         */
        Rectangle b = new Rectangle();
        b.x = this.itemViewRect.x;
        b.y = 0;
        b.height = timePanelHeight;
        b.width = this.itemViewRect.width;

        g.fill(b);
        // if (true) return;

        // Add a bit to the clipping because of extent of the characters (TODO
        // do this better)
        int w = b.width + 400;
        int x = Math.max(0, b.x - 200);
        int y = b.y;
        int h = b.height;

        int charHeight = timePanelHeight - 5;

        int y1 = Math.max(y, charHeight);

        double step = this.getSnapQuantization() / ticksPerBeat;

        TimeSignatureList ts = project.getTimeSignatureList();
        TempoList tl = project.getTempoList();
        assert (x >= 0);

        double beat1 = screenToTickAbs(x, true) / ticksPerBeat;
        double beat2 = screenToTickAbs(x + w, true) / ticksPerBeat;

        QStepIterator iter = project.getTimeSignatureList().createQStepIterator(beat1, beat2, step);

        g.setColor(Color.WHITE);
        boolean drawlines = (y1 < y + h);
        boolean drawNumber = (y < charHeight);

        boolean drawSub = true;
        //drawlines
        //		&& userToScreen((long) (step * ticksPerBeat)) > 5;
        boolean drawBeat = true;
        // drawlines && userToScreen((long) (ticksPerBeat)) > 5;

        int minSpace = 50;
        int xlast = -2 * minSpace;

        while (iter.hasNext()) {
            iter.next();
            double beat = iter.getBeat();
            boolean isBar = iter.isBar();
            int x1 = (int) (userToScreen(tl.getTimeAtTick((beat * ticksPerBeat))));

            if (isBar) {
                if (x1 - xlast > minSpace) {
                    g.setColor(Color.WHITE);
                    g.drawString(String.valueOf(iter.getBar()), x1,
                            charHeight - 2);
                    xlast = x1;
                    g.drawLine(x1, y1, x1, y1 + 3);
                }
            }

        }



        long xStart = (int) userToScreen(tl.getTimeAtTick(startLoopTime));
        long xEnd = (int) userToScreen(tl.getTimeAtTick(endLoopTime));

        if (xStart > x && xStart < x + w) {
            g.setColor(Color.YELLOW);
            g.translate(xStart, 0);
            g.fill(leftMarker);
            g.translate(-xStart, 0);

        }

        if (xEnd > x && xEnd < x + w) {
            g.setColor(Color.YELLOW);
            g.translate(xEnd, 0);
            g.fill(rightMarker);
            g.translate(-xEnd, 0);

        }

    }

    /**
     * This is called when the scale of painoRoll to screen mapping changes the
     * sub class must workpout the new postion of all items.
     */
    public synchronized void scaleX(double fact) {
        // We want left to stay still TODO do we really ?
        // Current visible rect
        Rectangle vr = this.itemViewRect;
        this.userToScreen = this.userToScreen * fact;
        vr.x = (int) (vr.x * fact);
        this.xCursor = (int) (sequencer.getTickPosition() * userToScreen);
        rebuildXScrollBars();
        repaintItems();

    }


      /**
     * Convert virtual screen x to a tick. with optional quantization
     *
     * Do not use this if x is a delta quantity
     *
     * @param x
     * @return
     */
    public long screenToTickAbs(int x, boolean quantizeMe) {
        return screenToTickAbs(x,quantizeMe,false);
    }
    /**
     * Convert virtual screen x to a tick. with optional quantization
     *
     * if (drumMode) quantize using round not truncate
     * Do not use this if x is a delta quantity
     *
     * @param x
     * @return
     */
    public long screenToTickAbs(int x, boolean quantizeMe,boolean drumMode) {
        double tt = (x / this.userToScreen);

        // if sample based then we need
        if (timeBased) {
            tt = project.getTempoList().getTickAtTime(tt);
        }

        if (quantizeMe) {
            double quant = this.getSnapQuantization();
            if (quant > 0.0) {
//				tt = (long) (Math.round(tt / this.getSnapQuantization()))
//				* this.getSnapQuantization();
                if (!drumMode) {
                    tt = (long) (tt / quant) * quant;
                } else {
                    tt = (long) ((tt+0.5*quant) / quant) * quant;
                }
            } else {
                double beat = tt / project.getTicksPerBeat();
                TimeSignatureEvent ev = project.getTimeSignatureList().getEventAtBeat((int) beat);
                int nBar = (int) ((beat - ev.beat + ev.beatsPerBar / 2.0) / ev.beatsPerBar);
                tt = (ev.beat + nBar * ev.beatsPerBar) * project.getTicksPerBeat();
            // System.out.println(" STT -ve quant " + tt);
            }
        }

        return (long) tt;
    }

    /**
     * Convert delta on the virtual screen to a tick with optional quntization
     *
     * @param x
     *            reference point to deduce bar boundaries
     * @param dx
     *            screen delta to convert
     * @return
     */
    public long screenToTickRel(int x, int dx, boolean quantizeMe) {

        assert (!timeBased);

        double tt = (dx / this.userToScreen);


        // XXX
        if (quantizeMe) {
            double quant = this.getSnapQuantization();
            if (quant > 0.0) {
                tt = (long) (Math.round(tt / this.getSnapQuantization())) * this.getSnapQuantization();
            } else {
                double beat = x / this.userToScreen / project.getTicksPerBeat();
                TimeSignatureEvent ev = project.getTimeSignatureList().getEventAtBeat((int) beat);
                quant = ev.beatsPerBar * project.getTicksPerBeat();
                tt = (long) (Math.round(tt / quant)) * quant;
            }
        }

        return (long) tt;
    }

    abstract public double getSnapQuantization();

    abstract public boolean isSnapQuantized();

    // final protected long snaptoQuantize(double val) {
    // return (long) ((Math.round(val / this.getSnapQuantization())) * this
    // .getSnapQuantization());
    // }
    protected synchronized void scrollItemImageX() {
        int dx = this.itemViewRect.x - this.lastItemViewRect.x;

        // if scroll has jumped to far might just piant the lot.
        if (Math.abs(dx) > this.lastItemViewRect.width) {
            dirty = true;
            repaintItems();
            // paintItemImage_(this.itemViewRect);
            return;
        }

        Rectangle exposedRect = new Rectangle(this.itemViewRect);

        if (dx > 0) {
            this.itemGraphics.copyArea(dx, 0, this.itemViewRect.width - dx - 1,
                    this.itemViewRect.height, -dx, 0);

        } else {
            this.itemGraphics.copyArea(0, 0, this.itemViewRect.width + dx - 1,
                    this.itemViewRect.height, -dx, 0);

        }

        if (dx > 0) {
            int xOld = exposedRect.x;
            exposedRect.x = this.lastItemViewRect.x + this.lastItemViewRect.width - 1;
            exposedRect.width -= exposedRect.x - xOld;
        } else {
            exposedRect.width = -dx;
        }

        paintItemImage_(exposedRect);
        if (hasTimeLine) {
            paintTimeImage_(exposedRect.x, exposedRect.width);
        }

    }

    protected synchronized void scrollItemImageY() {
        int dy = this.itemViewRect.y - this.lastItemViewRect.y;

        if (Math.abs(dy) > this.lastItemViewRect.height) {
            paintItemImage_(this.itemViewRect);
            return;
        }

        Rectangle exposedRect = new Rectangle(this.itemViewRect);

        if (dy > 0) {
            this.itemGraphics.copyArea(0, dy, this.itemViewRect.width,
                    this.itemViewRect.height - dy - 1, 0, -dy);

        } else {
            this.itemGraphics.copyArea(0, 0, this.itemViewRect.width,
                    this.itemViewRect.height + dy - 1, 0, -dy);

        }

        if (dy > 0) {
            int yOld = exposedRect.y;
            exposedRect.y = this.lastItemViewRect.y + this.lastItemViewRect.height - 1;
            exposedRect.height -= exposedRect.y - yOld;
        } else {
            exposedRect.height = -dy;
        }

        paintItemImage_(exposedRect);

    }

    @Override
    public void scrollRectToVisible(Rectangle r) {
        System.err.println("scrollToVisibleRect not allow for ItemPanel");
    }

    public Point scrollToContian(Point p) {

        int dx = p.x - (itemViewRect.x + itemViewRect.width);

        if (dx > 0) {
            if (xRangeModel.getMaximum() < p.x) {
                xRangeModel.setMaximum(p.x);
            }
            xRangeModel.setValue(itemViewRect.x + dx - 1);
        } else if (p.x > 0) {
            dx = p.x - itemViewRect.x;
            if (dx < 0) {
                xRangeModel.setValue(itemViewRect.x + dx + 1);
            } else {
                dx = 0;
            }
        } else {
            dx = 0;
        }

        int dy = p.y - (itemViewRect.y + itemViewRect.height);

        if (dy > 0) {
            if (p.y < yRangeModel.getMaximum()) {
                yRangeModel.setValue(itemViewRect.y + dy - 1);
            } else {
                dy = 0;
            }
        } else if (p.y > 0) {
            dy = p.y - itemViewRect.y;
            if (dy < 0) {
                yRangeModel.setValue(itemViewRect.y + dy + 1);
            } else {
                dy = 0;
            }
        } else {
            dy = 0;
        }

        long endTimeOrTick2 = (long) ((itemViewRect.x + itemViewRect.width) / userToScreen);
        if (endTimeOrTick2 > endTimeOrTick) {
            endTimeOrTick = endTimeOrTick2;
        }
        return new Point(dx, dy);

    }

    /**
     * Clears all items from the ItemPanels selection
     *
     */
    public abstract void clientClearSelection();

    /**
     * Add items in the rectangle to the ItemPanels selection.
     */
    public abstract void selectInRect(Rectangle rect, boolean shift);

    public void setIgnoreRepaints(boolean yes) {
        this.ignoreRepaint = yes;
    }

    /**
     * Sets the x cordinate of the viewport on the items space.
     *
     * @param xNew
     */
    public synchronized void setX(int xNew) {
        if (isChanging) {
            return;
        }
        if (xNew == this.itemViewRect.x) {
            return;
        }
        this.itemViewRect.x = xNew;
        paintImages();
        repaint(); // TODO move up ?

    }

    public synchronized void setY(int yNew) {
        if (isChanging) {
            return;
        }
        if (!canScrollY) {
            return;
        }
        if (yNew == this.itemViewRect.y) {
            return;
        }
        this.itemViewRect.y = yNew;

        paintImages();
        repaint();
    }

    /**
     * Convert time or tick to virtual screen x
     *
     * @param userTIme
     * @return
     */
    public double userToScreen(double userTIme) {
        return (userTIme * this.userToScreen);
    }

    public void zoomIn() {
        scaleX(1.0 / 0.8);
    }

    public void zoomOut() {
        scaleX(0.8);
    }

    public void zoomToRect(Rectangle rect) {

        if (rect.width <= 0) {
            return;
        }
        Rectangle vr = this.itemViewRect;

        double scale = vr.width / (double) rect.width;
        double tt = this.userToScreen * scale;

        if (tt > 5) {
            tt = 5;
        }
        this.userToScreen = tt;
        vr.x = (int) (rect.x * scale);
        rebuildXScrollBars();
        dirty = true;
        repaintItems();

    }

    protected void rebuildXScrollBars() {

        double endTime2 = project.getEndTick();

        if (timeBased) {
            endTime2 = project.getTempoList().getTimeAtTick(endTime2);
        }

        endTime2 = endTime2 * userToScreen;
        if (scroller.horizScroll.getMaximum() - scroller.horizScroll.getVisibleAmount() < endTime2) {
            scroller.horizScroll.setMaximum((int) endTime2 + itemViewRect.width);
        }

        scroller.horizScroll.setVisibleAmount(itemViewRect.width);
        scroller.horizScroll.setValue(itemViewRect.x);
    }

    public void setRightButton(boolean buttonState) {
        this.rightButtonPressed = buttonState;
    }

    /**
     * @return Returns the rightButtonPressed.
     */
    public boolean isRightButtonPressed() {
        return rightButtonPressed;
    }

    /**
     *
     * @param b
     *            if true panel will auto scroll to follow sequencer
     *            songPosition.
     */
    public void followSong(boolean b) {

        followSong = b;
        if (b) {
            scrollToContain(xCursor);
        }
    }

    public boolean isFollowSong() {
        return followSong;
    }

    public abstract void setTimeAtX(int x);

    public boolean requiresNotificationOnEachTick() {
        return false;
    }

    public boolean pointInTimeLine(int y) {
        if (!hasTimeLine) {
            return false;
        }
        return y <= timePanelHeight;

    }

    public void setToolBar(ItemRollToolBar bar) {
        this.toolBar = bar;
    }

    public void armDrag(Point p, Item item) {

        dragArmed = true;
        dragItem = item;
        xAnchor = p.x;
        yAnchor = p.y;
    }

    public abstract void rightButtonPressedOnItem(int x, int y);

    // TODO common interface for the selection containers ?
    public abstract void clientAddToSelection(Item item);

    public abstract void clientRemoveFromSelection(Item item);

    public abstract void erase(Item item);

    /**
     * flag reconstruction the image of the notes and request a repaint
     *
     */
    public void repaintItems() {
        // TODO area of dirty ?
        dirty = true;
        repaint();
    }

    public void paintComponent(Graphics g) {

        // System.out.println(g.getClip());
        if (dirty) {
            // System.out.println("DIRTY");
            paintImages();
            dirty = false;
        }

        copyImageToScreen(g);
        drawCursor(g, this.xCursor);

    }

    public void rectZoomFinished() {
        ((ItemRollToolBar) toolBar).rectZoomFinished();
    }

    public abstract int getHoverStateAt(Point p);

    public void setDragMode(int mode) {
        this.dragMode = mode;
        setCursor(cursors[mode]);
    }

    public ProjectContainer getProjectContainer() {
        return project;
    }

    public void setTool(String name) {
        tool = getTool(name);

        if (tool == prevTool) {
            return;
        }

        removeMouseListener(prevTool);
        removeMouseMotionListener(prevTool);
        if (tool == null) {
            return;
        }

        addMouseListener(tool);
        addMouseMotionListener(tool);
        setCursor(tool.getCursor());
        if (prevTool instanceof EditTool) {
            origTool = prevTool;
        }
        prevTool = tool;
    }

    /**
     * Get the tool associated with name.
     *
     * @param name
     * @return
     */
    public ToolAdapter getTool(String name) {
        if (name.equals("origtool")) {
            return origTool;
        } else if (name.equals("select")) {
            return selectTool;
        } else if (name.equals("erase")) {
            return eraseTool;
        } else if (name.equals("write")) {
            return writeTool;
        } else if (name.equals("magrect")) {
            return rectZoomTool;
        } else if (name.equals("dragview")) {
            return dragViewTool;
        } else if (name.equals("dragview")) {
            return dragViewTool;

        } else {
            try {
                throw new Exception(" Unknown tool " + name);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Notififiction that mouse has been drag during after a writeTool press.
     * Default to ignoring it
     *
     * @param p
     *
     */
    public void writeDraggedAt(Point p) {
    }

    abstract public void setSnapQuantization(double quant);

    /*
     * Used by slection tool to provide audio feed back default to do nothing.
     *
     */
    public void feedBack(Item item) {
    }

    boolean isTimeLineEvent(MouseEvent e) {
        if (pointInTimeLine(e.getY())) {
            int x = mapX(e.getX());

            if (e.getButton() == MouseEvent.BUTTON1) {
                if (e.isAltDown() || (e.isShiftDown() && e.isControlDown())) // Jens,
                // Alt-click
                // might
                // not
                // be
                // available
                // on
                // Linux
                // (seems
                // to
                // be
                // taken
                // by
                // the
                // window-manager),
                // so
                // also
                // allow
                // Shift-Ctrl
                {
                    setRightMarkAt(x);
                } else if (e.isControlDown()) {
                    setLeftMarkAt(x);
                } else {
                    setTimeAtX(x);
                }
            }
            return true;
        }
        return false;
    }

    private void setRightMarkAt(int x) {
        long tick = screenToTickAbs(x, isSnapQuantized());
        sequencer.setLoopEndPoint(tick);
    }

    private void setLeftMarkAt(int x) {

        long tick = screenToTickAbs(x, isSnapQuantized());

        sequencer.setLoopStartPoint(tick);
    }

    public abstract void setSnapQuantized(boolean b);

    /**
     * method to let the ItemPanel know the state of the control key Look at
     * controlIsDown field.
     */
    public void setControlState(boolean b) {
        controlIsDown = b;
    }

    /**
     * Force a complete redraw on next repaint();
     */
    public void setDirty() {
        dirty = true;

    }

    void validateEndTick() {


        double endTimeOrTick2 = project.getEndTick();

        //	System.out.println(" project end time is "+ project.getTimeUtils().tickToBarBeatTick((long) endTimeOrTick2));

        if (timeBased) {
            endTimeOrTick2 = project.getTempoList().getTimeAtTick(endTimeOrTick2);
        }

        xRangeModel.setExtent(itemViewRect.width);
        xRangeModel.setMaximum((int) (endTimeOrTick2 * userToScreen));
        endTimeOrTick = endTimeOrTick2;

    }

    public abstract void setFocus(Item item);

    public void ignorePartWarp(boolean b) {
        // TODO Auto-generated method stub
    }

    public abstract void clientNotifySelectionChange();

    public ExtendingRangeModel getXRangeModel() {
        return xRangeModel;
    }

    public DefaultBoundedRangeModel getYRangeModel() {
        return yRangeModel;
    }

    public void setAltState(boolean b) {
        altIsDown = b;

    }

    // public void rightButtonPressedInSpace() {
    // // TODO Auto-generated method stub
    //
    // }
}
