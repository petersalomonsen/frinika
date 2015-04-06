/*
 * Created on Feb 22, 2006
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.*;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;
import javax.sound.midi.ShortMessage;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import com.frinika.gui.OptionsEditor;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.FrinikaTrackWrapper;
import com.frinika.sequencer.gui.menu.RepeatAction;
import com.frinika.sequencer.gui.menu.SplitSelectedPartsAction;
import com.frinika.sequencer.gui.partview.PartView;
import com.frinika.sequencer.model.tempo.TempoList;

/**
 * Contains a List of MultiEvents.
 * Belongs to a lane.
 * 
 * @author Paul
 *
 */
public class MidiPart extends Part implements EditHistoryRecorder<MultiEvent> {
	
    private static final long serialVersionUID = 1L;

    String name;
	TreeSet<MultiEvent> multiEvents = new TreeSet<MultiEvent>();
	Collection<CommitListener> commitListeners = null; // Jens
	transient TreeSet<MultiEventEndTickComparable> multiEventEndTickComparables = new TreeSet<MultiEventEndTickComparable>();
	
	public MidiPart() {
		
	}
    
    /**
     *  Constructor for an MidiPart.
     * @param lane
     */
    		
    public MidiPart(MidiLane lane) {
    	super(lane);
	}
    
  
	
	/**
	 * Rebuild the bounds from the multievent startTicks
	 *
	 */
	public void setBoundsFromEvents() {
        if(multiEvents.size()>0)
        {   
        	setStartTick(multiEvents.first().getStartTick());
             if(multiEventEndTickComparables.size()>0)
                setEndTick(multiEventEndTickComparables.last().getMultiEvent().getEndTick());
             else
                 // This should really not happen - but happens for old projects with notes without end ticks (and imported midi files)
                setEndTick(multiEvents.last().getStartTick());
        } else {
        	System.err.println(" Warning attempt to set bounds for an empty part");
        	setStartTick(0);
        	setEndTick(1);
        }
	}

	/**
	 * Import Midi events into a Part from a section of track.
	 *
	 * Notes with start ticks in the range and inserted and the end events will be found and added.
	 * (So end events may be outside the bounds)
	 *
	 * @param startTickArg   start tick (inclusive)
	 * @param endTickArg     end tick (exclusive)
	 */
	public void importFromMidiTrack(long startTickArg,long endTickArg) {
        
    	
    	HashMap<Integer,NoteEvent> pendingNoteEvents = new HashMap<Integer,NoteEvent>();
        
        FrinikaTrackWrapper track=((MidiLane) lane).getTrack();
        
        for(int n=0;n<track.size();n++)
        {
        	
            MidiEvent event = track.get(n);

            
            // Check if note event
            try
            {
                if(event.getMessage() instanceof ShortMessage)
                {
                    ShortMessage shm = (ShortMessage)event.getMessage();
                    if(shm.getCommand() == ShortMessage.NOTE_ON || shm.getCommand() == ShortMessage.NOTE_OFF)
                    {
                        // Note off
                        if(shm.getCommand() == ShortMessage.NOTE_OFF || shm.getData2()==0)
                        {
                        
                        	// Generate a note event
                            NoteEvent noteEvent = pendingNoteEvents.get(shm.getChannel() << 8 | shm.getData1());
                            if (noteEvent == null) {
                            	System.err.println("NoteOff event without start event, PLEASE FIX ME  in MidiPart ");
                            	continue;
                            }
                            noteEvent.setEndEvent(event);
                            pendingNoteEvents.remove(shm.getChannel() << 8 | shm.getData1());
                            multiEvents.add(noteEvent);
    
                        } else {
                            // Note on
                           	if (event.getTick() >=startTickArg && event.getTick() < endTickArg){
                                                   		pendingNoteEvents.put(shm.getChannel() << 8 | shm.getData1(),
                                    new NoteEvent(this,event));
                        	}
                        }
                    }
                    else if(shm.getCommand() == ShortMessage.CONTROL_CHANGE)
                    {
                        if (event.getTick() >=startTickArg && event.getTick() < endTickArg)                            
                            multiEvents.add(new ControllerEvent(this,event.getTick(),shm.getData1(),shm.getData2()));
                    }
                    else if(shm.getCommand() == ShortMessage.PITCH_BEND)
                    {
                        if (event.getTick() >=startTickArg && event.getTick() < endTickArg)
                            multiEvents.add(new PitchBendEvent(this,event.getTick(),((shm.getData1()) | (shm.getData2() << 7)) & 0x7fff));
                    } 
                    else if(shm.getCommand() == ShortMessage.PROGRAM_CHANGE)
                    {
                        System.out.println(" Discarding program change event ");
                    } 
                    
                    
                    
                    // TODO Sysex messages here
                    if (event.getTick() >= endTickArg && pendingNoteEvents.size()==0) 
                        break;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        if (pendingNoteEvents.size() != 0 ) {
        	System.err.println(" Some notes did not have a noteoff event ");
        }
        
        for (MultiEvent e:multiEvents) {
        	e.zombie=false;
        	if (e instanceof NoteEvent) {
        		((NoteEvent)e).validate();
        		
        	}
        }
        rebuildMultiEventEndTickComparables();
        setBoundsFromEvents();
    }
	
	/**
	 * Import Midi events into a Part from a section of track.
	 *
	 * Notes with start ticks in the range and inserted and the end events will be found and added.
	 * (So end events may be outside the bounds)
	 *
	 * @param startTickArg   start tick (inclusive)
	 * @param endTickArg     end tick (exclusive)
	 */
	public void importFromMidiTrack(Track track,long startTickArg,long endTickArg) {
        
    	
    	HashMap<Integer,NoteEvent> pendingNoteEvents = new HashMap<Integer,NoteEvent>();
        
      //  FrinikaTrackWrapper track=((MidiLane) lane).getTrack();
        
        for(int n=0;n<track.size();n++)
        {
        	
            MidiEvent event = track.get(n);

            
            // Check if note event
            try
            {
                if(event.getMessage() instanceof ShortMessage)
                {
                    ShortMessage shm = (ShortMessage)event.getMessage();
                    if(shm.getCommand() == ShortMessage.NOTE_ON || shm.getCommand() == ShortMessage.NOTE_OFF)
                    {
                        // Note off
                        if(shm.getCommand() == ShortMessage.NOTE_OFF || shm.getData2()==0)
                        {
                        
                        	// Generate a note event
                            NoteEvent noteEvent = pendingNoteEvents.get(shm.getChannel() << 8 | shm.getData1());
                            if (noteEvent == null) {
                            	System.err.println("NoteOff event without start event, PLEASE FIX ME  in MidiPart ");
                            	continue;
                            }
                            noteEvent.setEndEvent(event);
                            pendingNoteEvents.remove(shm.getChannel() << 8 | shm.getData1());
                            multiEvents.add(noteEvent);
    
                        } else {
                            // Note on
                           	if (event.getTick() >=startTickArg && event.getTick() < endTickArg){
                                                   		pendingNoteEvents.put(shm.getChannel() << 8 | shm.getData1(),
                                    new NoteEvent(this,event));
                        	}
                        }
                    }
                    else if(shm.getCommand() == ShortMessage.CONTROL_CHANGE)
                    {
                        if (event.getTick() >=startTickArg && event.getTick() < endTickArg)                            
                            multiEvents.add(new ControllerEvent(this,event.getTick(),shm.getData1(),shm.getData2()));
                    }
                    else if(shm.getCommand() == ShortMessage.PITCH_BEND)
                    {
                        if (event.getTick() >=startTickArg && event.getTick() < endTickArg)
                            multiEvents.add(new PitchBendEvent(this,event.getTick(),((shm.getData1()) | (shm.getData2() << 7)) & 0x7fff));
                    } 
                    // TODO Sysex messages here
                    if (event.getTick() >= endTickArg && pendingNoteEvents.size()==0) 
                        break;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        if (pendingNoteEvents.size() != 0 ) {
        	System.err.println(" Some notes did not have a noteoff event ");
        }
        
        for (MultiEvent e:multiEvents) {
        	e.zombie=false;
        	if (e instanceof NoteEvent) {
        		((NoteEvent)e).validate();
        		
        	}
        }
        rebuildMultiEventEndTickComparables();
        setBoundsFromEvents();
    }
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    /**
	 * Add a MultiEvent to the track.
	 * The client is responsible for adjusting the Part bounds is need be.
	 * 
	 * @param ev
	 * @return
	 */
	
    public void add(MultiEvent ev)
    { 
    		ev.part = this;

    		ev.commitAdd();       

    	   // TODO this should not be here ?
           /* if (startTick > ev.getStartTick()) 
    			startTick = ev.getStartTick();

    		if (endTick < ev.getEndTick()) 
    			endTick = ev.getEndTick();
					*/
    
    		multiEvents.add(ev);
    		if(multiEventEndTickComparables != null)
            multiEventEndTickComparables.add(ev.getMultiEventEndTickComparable());

            setChanged();
    		if (lane != null) getEditHistoryContainer().push(this,EditHistoryRecordableAction.EDIT_HISTORY_TYPE_ADD, ev);
    	//	System. out.println("added "+ev.toString());
    }
    
    /**
	 * Remove a MultiEvent from the track
	 * 
	 * @param multiEvent
	 * @return
	 */
    public void remove(MultiEvent multiEvent)
    {
        multiEvent.commitRemove();
        multiEvents.remove(multiEvent);
        if(multiEventEndTickComparables != null)
        multiEventEndTickComparables.remove(multiEvent.getMultiEventEndTickComparable());
        lane.project.getMultiEventSelection().removeSelected(multiEvent);
        setChanged();
        getEditHistoryContainer().push(this,EditHistoryRecordableAction.EDIT_HISTORY_TYPE_REMOVE, multiEvent);
    //    System. out.println("removed "+multiEvent.toString());
    }

/*
    protected void attachMultiEvents() {
      	if (multiEvents == null ) return;
          	for (MultiEvent e:multiEvents) {
    		e.commitAdd();
    	}
    }*/

    public void commitEventsRemove() {
      	if (multiEvents == null ) return;
        
    	for (MultiEvent e:multiEvents) {
    		if (!e.isZombie()) e.commitRemove();
    	}
    }
    
//    /**
//	 * Register updates on a MultiEvent
//	 * 
//	 * @param multiEvent
//	 */
//    public void update(MultiEvent multiEvent)
//    {
//       remove(multiEvent);
//       add(multiEvent);
//    }
    
    /**
	 * Returns the multievent array.
	 * 
	 * @return
	 */
    public SortedSet<MultiEvent> getMultiEvents()
    {
        return multiEvents;
    }
    
    /**
	 * Returns a subset of the multievent array including startTick excluding
	 * endTick
	 * 
	 * @param startTick
	 * @param endTick
	 * @return
	 */
    public SortedSet<MultiEvent> getMultiEventSubset(long startTick, long endTick)
    {
        return multiEvents.subSet(new SubsetMultiEvent(startTick),new SubsetMultiEvent(endTick));
    }

	public FrinikaTrackWrapper getTrack() {
		return ((MidiLane) lane).getTrack();
	}


 /*
	 * public EditHistoryContainer getEditHistoryContainer() { return
	 * track.getEditHistoryContainer(); }
	 * 
	 * public Sequence getSequence() { return track.getSequence(); }
	 */
	public int getMidiChannel() {
		return getTrack().getMidiChannel();
	}



    public EditHistoryContainer getEditHistoryContainer() {
        return getLane().getProject().getEditHistoryContainer();
    }


    /**
     * Make sure part is detached before calling this then reattach after
     * This operation does not change the database rep so we do not call setChanged()
     *
     */
	@Override
	protected void moveItemsBy(long deltaTick) {
		Vector<MultiEvent> list=new Vector<MultiEvent>();
		
	
		
		for(MultiEvent ev:multiEvents) {	
			list.add(ev);
		}
		
		for (MultiEvent ev:list) {
			long newTick = deltaTick+ev.getStartTick();
			remove(ev);
			ev.setStartTick(newTick);
			add(ev);			
		}
		
	
	}

	public void moveContentsBy(double dTick,Lane dstLane) {
		
		setStartTick (getStartTick() + dTick);
		setEndTick(getEndTick() + dTick);
		
		commitEventsRemove();
	
		
		if (dstLane != lane) {			
			lane.getParts().remove(this);
			dstLane.getParts().add(this);
			lane=dstLane;
		}
		
		for (MultiEvent ev : multiEvents) {
			long newTick = (long)(dTick + ev.getStartTick());
			ev.setStartTick(newTick);
		}

		commitEventsAdd();
		
	}
	
    public void restoreFromClone(EditHistoryRecordable o) {
    	MidiPart clone=(MidiPart)o;
       	lane=clone.lane;
    	setStartTick(clone.getStartTick());
    	setEndTick(clone.getEndTick());
    	
    	// selection 
    	selected=false; // clone.selected;     
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
    	Part clone=new MidiPart();
       	clone.lane= lane;
    	clone.setStartTick(getStartTick());
    	clone.setEndTick(getEndTick());
    	clone.selected = false;
        
        // database history fields
        //clone.editParent=editParent;
        //clone.rootPart=rootPart;
        //clone.partResourceId=partResourceId;

    	return clone;
    }

    @Override
    /**
     * Generate native MIDI event out of generic Frinika MultiEvents
     */
    public void commitEventsAdd() {
    	if (multiEvents == null ) return;
    //	System.out.println("Committing " + multiEvents.size() + " events");
        for(MultiEvent multiEvent : multiEvents)  {
        	long tick=multiEvent.getStartTick();
        	if (tick >= getStartTick() && tick < getEndTick())
        		multiEvent.commitAdd();
        }
    }

    
	@Override
	public void copyBy(double deltaTick,Lane dst) {
		
		MidiPart part = new MidiPart((MidiLane)dst);
		
		
		Collection<MultiEvent> events=getMultiEvents();

		part.setStartTick(getStartTick()+deltaTick);
		part.setEndTick(getEndTick()+deltaTick);

		for (MultiEvent ev:events) {
			try {
				MultiEvent newEv=(MultiEvent)ev.clone();
				double newTick = deltaTick+ev.getStartTick();
				newEv.setStartTick((long)newTick);
				part.add(newEv);
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

        part.rootPart=rootPart;
        part.partResourceId=partResourceId;
        part.editParent=editParent;
		
	}

	
	public Selectable deepCopy(Selectable parent) {
	
		MidiPart clone;
		
		if (parent != null) {
			clone = (MidiPart) ((MidiLane)parent).createPart();
		}else {
			clone=new MidiPart();
		}
		clone.setStartTick(getStartTick());
		clone.setEndTick(getEndTick());
		if (parent == null) {
			clone.lane=lane;
		}
	    clone.name="Copy of "+name;
	    clone.color=color;
        clone.rootPart=this.rootPart;
        clone.partResourceId=this.partResourceId;
        clone.editParent=this.editParent;
        
		for (MultiEvent ev:multiEvents) {
			clone.multiEvents.add((MultiEvent)ev.deepCopy(clone));
		}

		
		return clone;
	}

	public void deepMove(long tick) {
		
		Collection<MultiEvent> events=getMultiEvents();
		for (MultiEvent ev:events) {
			ev.deepMove(tick);
		}
		
		setStartTick(getStartTick()+tick);
		setEndTick(getEndTick() +tick);	
	}
    
	public void rebuildMultiEventEndTickComparables()
    {
        multiEventEndTickComparables = new TreeSet<MultiEventEndTickComparable>();
        for(MultiEvent multiEvent : multiEvents)
        {
            multiEventEndTickComparables.add(multiEvent.getMultiEventEndTickComparable());
        }
        //setBoundsFromEvents();
        
    }
	
	/**
	 * deprecated
	 *//*
	public Rectangle getEventBounds() {
		Rectangle rect=new Rectangle();
		rect.x=(int) startTick;
		rect.width=(int) (endTick-startTick);
		
		int low=128;
		int high=0;
		for(MultiEvent ev:multiEvents) {
			if (!ev.isZombie() && (ev instanceof NoteEvent)){
				int pit=((NoteEvent)ev).getNote();
				if (pit > high) high=pit;
				if (pit < low) low=pit;
			}
			
		}
		rect.y=low;
		rect.height=high-low;
		// TODO Auto-generated method stub
		return rect;
	}
*/
	public int[] getPitchRange() {
		
		int low=128;
		int high=0;
		for(MultiEvent ev:multiEvents) {
			if (!ev.isZombie() && (ev instanceof NoteEvent)){
				int pit=((NoteEvent)ev).getNote();
				if (pit > high) high=pit;
				if (pit < low) low=pit;
			}	
		}
		int [] ret={low,high};
		return ret;
		
	}
	
	/**
	 * Commit the MultiEvents as MidiEvents to a Sequencers Track event list.
	 */
	public void onLoad() {
	//	System.out.println(" On load 1");
		commitEventsAdd();
	//	System.out.println(" On load 2");

		rebuildMultiEventEndTickComparables();
	//	System.out.println(" On load 3");

	}

//	@Override
//	public void attach() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void detach() {
//		// TODO Auto-generated method stub
//		
//	}
		
	public void drawThumbNail(Graphics2D g, Rectangle rect,PartView panel) {
		TempoList tl=lane.getProject().getTempoList();
		
		for (MultiEvent e : getMultiEvents()) {
			double x = tl.getTimeAtTick(e.getStartTick());
			if (e instanceof NoteEvent) {
				double w = tl.getTimeAtTick(e.getEndTick());
				int note = ((NoteEvent) e).getNote();
				int y = (int) ((rect.y + (rect.height * (128 - note)) / 128.0));
				if (e.isZombie())
					g.setColor(Color.WHITE);
				else
					g.setColor(Color.BLACK);

				g.drawLine((int)panel.userToScreen(x), y, (int)panel.userToScreen(w), y);
			}
		}

	}

	public void addCommitListener(CommitListener l) {
		if (commitListeners == null) { // auto-init
			commitListeners = new HashSet<CommitListener>();
		}
		commitListeners.add(l);
	}
	
	public void removeCommitListener(CommitListener l) {
		commitListeners.remove(l);
	}
	
	void fireCommitAdd(MultiEvent event) {
		if (commitListeners == null) return;
		for (CommitListener l : commitListeners) {
			l.commitAddPerformed(event);
		}
	}
	
	void fireCommitRemove(MultiEvent event) {
		if (commitListeners == null) return;
		for (CommitListener l : commitListeners) {
			l.commitRemovePerformed(event);
		}
	}
	
	
	// --- context menu ------------------------------------------------------
	
	/**
	 * Fills the part's context menu with menu-items.
	 *  
	 * @param popup
	 */
	@Override
	protected void initContextMenu(final ProjectFrame frame, JPopupMenu popup) {
		
		JMenuItem item = new JMenuItem(new RepeatAction(frame));
		//item.setText(item.getText()+"..."); // hack
		item.setMnemonic(KeyEvent.VK_R);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));		
		popup.add(item);
		
		item = new JMenuItem(new SplitSelectedPartsAction(frame));
		popup.add(item);
		
		super.initContextMenu(frame, popup);
	}

	
	// --- properties panel --------------------------------------------------
	
	/**
	 * Create PropertiesPanel.
	 * 
	 * @param frame
	 * @return
	 */
	@Override
	protected OptionsEditor createPropertiesPanel(ProjectFrame frame) {
		return new MidiPartPropertiesPanel(frame);
	}
	
	// --- inner class ---
	
	/**
	 * Instance returned via createProperitesPanel().
	 * 
	 * This is an example how type-specific Properties-Panels can be built.
	 * Currently, this just inherits all defaults. 
	 */
	protected class MidiPartPropertiesPanel extends PropertiesPanel {
		
		/**
		 * Constructor.
		 * 
		 * @param frame
		 */
		protected MidiPartPropertiesPanel(ProjectFrame frame) {
			super(frame);
		}
		
		/**
		 * Fills the panel with gui elements for editing the part's properties.
		 */
		@Override
		protected void initComponents() {
			super.initComponents();
			// do additional things here
			
			//GridBagConstraints gc = new GridBagConstraints();
			//gc.gridwidth = GridBagConstraints.REMAINDER;
			//this.add(new JLabel("MIDI-PART TEST"), gc);
		}

		/**
		 * Refreshes the GUI so that it reflects the model's current state.
		 */
		@Override
		public void refresh() {
			super.refresh();
			// do additional things here
		}
		
		/**
		 * Updates the model so that it contains the values set by the user.
		 */
		@Override
		public void update() {
			super.update();
			// do additional things here
		}
	}

	
		
}
