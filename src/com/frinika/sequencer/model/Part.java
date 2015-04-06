/*
 * Created on Mar 2, 2006
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
package com.frinika.sequencer.model;

import static com.frinika.localization.CurrentLocale.getMessage;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.frinika.gui.OptionsDialog;
import com.frinika.gui.OptionsEditor;
import com.frinika.project.MultiPart;
import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.gui.Item;
import com.frinika.sequencer.gui.TimeFormat;
import com.frinika.sequencer.gui.TimeSelector;
import com.frinika.sequencer.gui.partview.PartView;
import java.util.Vector;

/**
 * A Part encapsulates what can be displayed in the partview.
 * The startTick and endTick define the range in the display.
 * (These do not need to correspond to the range of any contained items)
 * It's Lane defines the row of display.
 * 
 * @author Paul
 *
 */
public abstract class Part implements Item, Selectable, EditHistoryRecordable, Serializable, MenuPlugable {

    private static final long serialVersionUID = -7282369887900349287L;
    protected Lane lane;
    // Default tick based limits. Subclasses can override this scheme with it's own fields and methods
    private long startTick = 0;
    private long endTick = 0;
//	protected int colorID=0;
    Color color;
    transient Color transColor;
    transient protected boolean selected = false;
    private MultiPart multiPart;
    //   transient boolean attached;
    Long partResourceId; // The database id for my resource (null then not saved yet)
    Part rootPart;      // the part that I was copied from.
    transient Part editParent;    // the part I was edited from.


    public Part getEditParent() {
        return editParent;
    }

    public void setEditParent(Part editParent) {
        this.editParent = editParent;
    }
    public Part getRootPart() {
        return rootPart;
    }

    public void setRootPart(Part rootPart) {
        this.rootPart = rootPart;
    }

    public Long getPartResourceId() {
        return partResourceId;
    }

    public void setPartResourceId(Long partResourceId) {
        this.partResourceId = partResourceId;
    }

    protected Part() {
        color = Color.lightGray;
        rootPart = this;
        editParent= null;
    }

    /**
     * Construct a new Part and add it to it's lane.
     * 
     * @param lane
     */
    public Part(Lane lane) {
        rootPart = this;  // default to not being a copy !!!
        this.lane = lane;
        if (lane != null) {
            this.lane.add(this);
            color = lane.color;
        }

    }

    /**
     * 
     * @return the Lane that contains the Part
     */
    public Lane getLane() {
        return lane;
    }

    /**
     * 
     * @return length of the part in ticks
     */
    public long getDurationInTicks() {
        if (startTick > endTick) {
            return 0;
        }
        return endTick - startTick;
    }

    /**
     * 
     * @return start of the part in ticks
     */
    public long getStartTick() {
        //if (startTick > endTick ) return 0;
        return startTick;
    }

    /**
     * 
     * @return end tick of the part
     */
    public long getEndTick() {
        //if (startTick > endTick ) return 0;
        return endTick;
    }

    /**
     * 
     * @return length of the part in samples
     */
    public double getDurationInSecs() {
        if (startTick > endTick) {
            return 0;
        }
        return (lane.project.getTempoList().getTimeAtTick(endTick) - lane.project.getTempoList().getTimeAtTick(startTick));
    }

    /**
     * 
     * @return start of the part in samples
     */
    public double getStartInSecs() {
        //if (startTick > endTick ) return 0;
        return lane.project.getTempoList().getTimeAtTick(startTick);
    }

    /**
     * 
     * @return end samples of the part
     */
    public double getEndInSecs() {
        //if (startTick > endTick ) return 0;
        return lane.project.getTempoList().getTimeAtTick(endTick);
    }

    /**
     * 
     * @return length of the part
     */
    public double getDuration(boolean sampleBased) {
        if (sampleBased) {
            return getDurationInSecs();
        } else {
            return getDurationInTicks();
        }
    }

    /**
     * 
     * @return start of the part 
     */
    public double getStart(boolean sampleBased) {
        if (sampleBased) {
            return getStartInSecs();
        } else {
            return getStartTick();
        }

    }

    /**
     * 
     * @return end  of the part
     */
    public double getEnd(boolean sampleBased) {
        if (sampleBased) {
            return getEndInSecs();
        } else {
            return getEndTick();
        }
    }

    /**
     * used by the GUI
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * used by the GUI
     */
    public void setSelected(boolean b) {
        selected = b;
    }

    /**
     * 
     * NOTE AudioPert overrides these methods
     * 
     *  Set the start tick.
     *  This does not effect any items contained in the part.
     *  Purely for display.
     *  
     * @param tick new start tick
     */
    public void setStartTick(double tick) {
        startTick = (long) tick; // XXX  
    }

    /**
     * 
     * @param tick new end tick for display purpose only
     */
    public void setEndTick(double tick) {
        endTick = (long) tick; // XXX
    }

    public void setStartInSecs(double start) {
        startTick = (long) lane.getProject().getTempoList().getTickAtTime(start);
    }

    public void setEndInSecs(double end) {
        endTick = (long) lane.getProject().getTempoList().getTickAtTime(end);
    }

    /**
     *  Moves the part and all it contains by deltaTick.
     *  Concrete subclasses implement moveItemsBy method to move the contents of the Part.
     * 
     * @param tick
     * @deprecated
     */
    public void moveBy(long deltaTick) {

        startTick += deltaTick;
        endTick += deltaTick;

        moveItemsBy(deltaTick);


    }

    /**
     * @deprecated
     */
    abstract protected void moveItemsBy(long deltaTick);

    public abstract Object clone() throws CloneNotSupportedException;

    /*
     * 
     * Called when part is inserted into the model
     * 
     */
    public abstract void commitEventsAdd();

    /**
     * 
     * Called when part is removed from the model
     * 
     */
    public abstract void commitEventsRemove();

    abstract public void copyBy(double tick, Lane dst);

    /**
     * move the contents by tick into dstLane 
     *  @param tick
     */
    public abstract void moveContentsBy(double tick, Lane dstLane);

    public void removeFromModel() {

        lane.remove(this);
    }

    /**
     * 
     * @return if part is part of the model.
     * 
     */
    public boolean isAttached() {
        if (lane == null) {
            return false;
        }
        synchronized (lane) {
            return lane.getParts().contains(this);
        }
    }

    public void addToModel() {

        lane.add(this);
    }

    public long leftTickForMove() {
        return getStartTick();
    }

    public long rightTickForMove() {
        return getEndTick();
    }

    public Rectangle getEventBounds() {

        // TODO Auto-generated method stub
        return null;
    }

//	public int getColorID() {
//		return colorID;		
//	}
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in)
            throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        if (rootPart == null) {
            rootPart = this;
        }
    }

    public abstract void onLoad() throws Exception;

    public abstract void drawThumbNail(Graphics2D g, Rectangle rect, PartView partView);

    public void displayStructure(String prefix, PrintStream out) {
        out.println(prefix + toString());
    }

    /**
     *  Override to customize the right button popup
     *  
     * @param invoker
     * @param x
     * @param y
     * @return
     */
    public boolean showRightButtonMenu(Component invoker, int x, int y) {
        return false;
    }
    static Vector<MenuPlugin> menuPlugins = new Vector<MenuPlugin>();

    /**
     * Allow custom menus to be added.
     * 
     * This will appear at the top of the right button popup menu.
     * 
     * @param menuPlugin
     */
    public static void addPluginRightButtonMenu(MenuPlugin menuPlugin) {
        Part.menuPlugins.add(menuPlugin);
    }

    /**
     * Shows the right-click context menu of the current component.
     * 
     * @param frame
     * @param invoker
     * @param x
     * @param y
     */
    public void showContextMenu(final ProjectFrame frame, Component invoker, int x, int y) {
        // build popup-menu from menuPrefix and own items
        JPopupMenu popup = new JPopupMenu();
        for (MenuPlugin plugin : menuPlugins) {
            plugin.initContextMenu(popup, this);
        }
        initContextMenu(frame, popup);
        popup.show(invoker, x, y);
    }

    /**
     * Fills the context menu with part-type specific (or possibly even instance-specific) items.
     * TO BE EXTENDED BY SUBCLASSES.
     * 
     * @param popup
     */
    protected void initContextMenu(final ProjectFrame frame, JPopupMenu popup) {
        // ... subclass overwrite and do something, then call super....() ...
        if (popup.getComponentCount() > 0) {
            popup.addSeparator();
        }
        // "Properties..."
        JMenuItem item = new JMenuItem(getMessage("project.menu.properties") + "...");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                showPropertiesDialog(frame);
            }
        });
        popup.add(item);
    }

    public Color getTransparentColor() {
        Color c = getColor();
        if (transColor == null) {
            transColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (0.8f * 255));
        }
        return transColor;
    }

    public Color getColor() {
        if (color == null) {
            color = Color.RED;
        }
        return color;
    }

    public void setColor(Color col) {
        color = col;
        transColor = null;
    }

    public void showPropertiesDialog(ProjectFrame frame) {
        createPropertiesDialog(frame).show();
        frame.repaintPartView();
    }

    protected JDialog createPropertiesDialog(ProjectFrame frame) {
        final OptionsEditor contentEditor = createPropertiesPanel(frame);
        final OptionsEditor backup = createPropertiesPanel(frame); // a second one which keeps initial values (and will not be displayed)
        backup.refresh();
        OptionsDialog dialog = new OptionsDialog(frame, (JComponent) contentEditor, "Part Properties") {

            /**
             * Called when Ok is chosen.
             */
            public void ok() {
                super.ok();
                // commit as undoable action
                ProjectContainer project = frame.getProjectContainer();
                project.getEditHistoryContainer().mark(getMessage("project.menu.edit_properties"));
                EditHistoryAction action = new EditHistoryAction() {

                    public void redo() {
                        contentEditor.update();
                        frame.repaintPartView();
                    }

                    public void undo() {
                        backup.update();
                        frame.repaintPartView();
                    }
                };
                project.getEditHistoryContainer().push(action);
                project.getEditHistoryContainer().notifyEditHistoryListeners();
            }

            /**
             * Special handling of cancel, because values changed in the dialog will directly be applied
             * and thus must explicitly be restored if cancel is chosen.
             */
            public void cancel() {
                backup.update();
                super.cancel();
                frame.repaintPartView();
            }
        };
        return dialog;
    }

    /**
     * Create PropertiesPanel.
     * 
     * Optionally overwritten by subclass, returning a subclass-instance of PropertiesPanel.
     * 
     * @param frame
     * @return
     */
    protected OptionsEditor createPropertiesPanel(ProjectFrame frame) {
        return new PropertiesPanel(frame); // default, may be overwritten
    }

    public MultiPart getMultiPart() {
        return multiPart;
    }

    public void setMultiPart(MultiPart multiPart) {
        this.multiPart = multiPart;
    }

    // --- inner class ---
    /**
     * Optionally to be extended by subclass and returned via createProperitesPanel().
     */
    protected class PropertiesPanel extends JPanel implements OptionsEditor {

        protected ProjectFrame frame;
        protected TimeSelector startTimeSelector;
        protected TimeSelector endTimeSelector;
        protected TimeSelector lengthTimeSelector;

        /**
         * Constructor.
         * 
         * @param frame
         */
        protected PropertiesPanel(ProjectFrame frame) {
            super();
            this.frame = frame;
            setLayout(new GridBagLayout());
            initComponents();
        }

        /**
         * Fills the panel with gui elements for editing the part's properties.
         * 
         * TO BE EXTENDED BY SUBCLASS.
         */
        protected void initComponents() {
            JLabel startLabel = new JLabel("Start:");
            startTimeSelector = new TimeSelector(startTick, frame.getProjectContainer(), TimeFormat.BAR_BEAT_TICK);
            JLabel endLabel = new JLabel("End:");
            JLabel lengthLabel = new JLabel("Duration:");
            endTimeSelector = new TimeSelector(Part.this.endTick, frame.getProjectContainer(), TimeFormat.BAR_BEAT_TICK);
            lengthTimeSelector = new TimeSelector(Part.this.getDurationInTicks(), frame.getProjectContainer(), TimeFormat.BEAT_TICK);

            // special: directly apply change and make visible
            startTimeSelector.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    long d = getDurationInTicks();
                    startTick = startTimeSelector.getTicks();
                    endTick = startTick + d;
                    endTimeSelector.setTicks(endTick);
                    frame.repaintPartView();
                }
            });
            // special: directly apply change and make visible
            endTimeSelector.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    endTick = endTimeSelector.getTicks();
                    lengthTimeSelector.setTicks(getDurationInTicks());
                    frame.repaintPartView();
                }
            });
            // special: directly apply change and make visible
            lengthTimeSelector.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    long t = lengthTimeSelector.getTicks();
                    endTick = startTick + t;
                    endTimeSelector.setTicks(endTick);
                    frame.repaintPartView();
                }
            });

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(5, 5, 5, 5);
            gc.anchor = GridBagConstraints.WEST;
            this.add(startLabel, gc);
            this.add(startTimeSelector, gc);
            this.add(endLabel, gc);
            gc.gridwidth = GridBagConstraints.REMAINDER;
            gc.anchor = GridBagConstraints.EAST;
            this.add(endTimeSelector, gc);
            gc.anchor = GridBagConstraints.WEST;
            gc.gridwidth = 2;
            this.add(new JPanel(), gc); // spacer
            gc.gridwidth = 1;
            this.add(lengthLabel, gc);
            gc.gridwidth = GridBagConstraints.REMAINDER;
            gc.anchor = GridBagConstraints.EAST;
            this.add(lengthTimeSelector, gc);
            gc.anchor = GridBagConstraints.WEST;
            gc.gridwidth = 1;
        }

        /**
         * Refreshes the GUI so that it reflects the model's current state.
         * 
         * TO BE EXTENDED BY SUBCLASS.
         */
        public void refresh() {
            startTimeSelector.setTicks(startTick);
            endTimeSelector.setTicks(endTick);
            lengthTimeSelector.setTicks(getDurationInTicks());
        }

        /**
         * Updates the model so that it contains the values set by the user.
         * 
         * TO BE EXTENDED BY SUBCLASS.
         */
        public void update() {
            startTick = startTimeSelector.getTicks();
            endTick = endTimeSelector.getTicks();
        }
    }

    /**
     *
     * Must be called if structure is changed.
     *  - the database partResourceID is set to null (flag for new)
     *  - rootPart is set to this.
     *  - if we have edited a copied part then
     */
    public void setChanged() {


        if (rootPart != this) {
            // this part is currently a copy of a different part
            System.out.println(" Detaching a copied part ");
            editParent = rootPart;    // make the original the parent.
            partResourceId = null;      // not in the data base yet
            rootPart = this;            // this means it is/will be an original
        } else {
            // we are an original.
            // so disassociate from any saved part resource.
            if (partResourceId != null) {
                System.out.println(" Detaching a root part from "+partResourceId);
                partResourceId=null;
            }

        }


        if (partResourceId != null) {
            // should not happen
            try {
                throw new Throwable(" Part with resourceID!=null changed ");
            } catch (Throwable ex) {
                Logger.getLogger(Part.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
  

    }
}
