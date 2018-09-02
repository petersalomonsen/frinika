// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentLinkedQueue;

import uk.org.toot.control.BooleanControl;
import uk.org.toot.control.CompoundControl;
import uk.org.toot.control.Control;
import uk.org.toot.control.EnumControl;
import uk.org.toot.control.FloatControl;

/**
 * This class is the CompoundControl for all the Controls of a SynthChannel.
 * It also handles MIDI learn by mapping controllers to Controls and
 * supporting snapshot persistence of this map.
 * @author st
 */
public abstract class SynthChannelControls extends CompoundControl 
	implements Observer, Runnable
{
	private boolean learn;
	private Control learnControl;
	private Control[] map;
	private ConcurrentLinkedQueue<ControlChange> changeQueue;
	private Thread changeThread;
	private Observable observable;
	
	protected SynthChannelControls(int id, String name) {
		super(id, name);
	}

    // @override to ensure resources are released
    public void close() {
    	if ( observable != null ) {
    		observable.deleteObserver(this);
    	}
    	if ( changeThread != null ) {
    		changeThread = null;
    		synchronized ( this ) {
    			notify(); // ensure the thread stops
    		}
    	}
    }
    
	// @Override to obtain the Control to be learnt
    protected void notifyParent(Control obj) {
    	if ( learn ) {
    		learnControl = obj;
    	}
    	super.notifyParent(obj);
    }
    
    protected void ensureMapExists() {
		if ( map == null ) {
			map = new Control[128]; // lazy instantiation
		}    	
    }
    /**
     * React to an observed MIDI controller message and send it to the Controls which
     * the controller is mapped to.
     * public as an implementation side effect
     * May need smoothing to avoid zipper noise.
     */
    public void update(Observable obs, Object obj) {
    	if ( !(obj instanceof ControlChange) ) return;
    	if ( observable == null ) observable = obs;
    	ControlChange change = (ControlChange)obj;
    	int controller = change.getController();
    	if ( controller >= 0x20 && controller < 0x40 ) return; // lsb ignored
    	if ( learn && learnControl != null ) {
			ensureMapExists();
    		map[controller] = learnControl;
    		learn = false;
    		learnControl = null;
    	} 
    	if ( map != null && map[controller] != null ) {
    		if ( changeQueue == null ) { // lazy instantiation
    			changeQueue = new ConcurrentLinkedQueue<ControlChange>();
    			changeThread = new Thread(this, getName()+" CC");
    			changeThread.start();
    		}
    		changeQueue.add(change);
    		synchronized ( this ) {
    			notify();
    		}
    	}
    }

    // public as an implementation side effect
    public void run() {
    	Thread thisThread = Thread.currentThread();
    	while ( thisThread == changeThread ) {
    		while ( !changeQueue.isEmpty() ) {
    			relay(changeQueue.poll());
    		}
    		synchronized ( this) {
    			try {
    				wait();
    			} catch ( InterruptedException ie ) {
    			
    			}
    		}
    	}
    }
    
    protected void relay(ControlChange change) {
    	int controller = change.getController();
    	int value = change.getValue();
    	Control c = map[controller];
    	if ( c == null ) return ; // was non-null but might not still be
    	if ( c instanceof FloatControl ) {
    		FloatControl f = (FloatControl)c;
    		f.setIntValue((int)((float)value * f.getLaw().getResolution() / 128));
    	} else if ( c instanceof EnumControl ) {
    		EnumControl e = (EnumControl)c;
    		e.setIntValue((int)((float)value * e.getValues().size() / 128));
    	} else if ( c instanceof BooleanControl ) {
    		BooleanControl b = (BooleanControl)c;
    		b.setValue(value > 63);
    	}
    }
    
    /**
     * @param n - the midi controller mapped to a Control
     * @return the Control Id or -1 if no Control is mapped to the specified midi controller
     */
	public int getMappedControlId(int n) {
		assert n >= 0;
		assert n < 128;
		if ( map == null ) return -1;		// no learnt controllers
		if ( map[n] == null ) return -1;	// this controller not learnt
		return map[n].getId();
	}
	
	/**
     * @param n - the midi controller mapped to a Control
	 * @param cid - the Control Id to be mapped from the specified midi controller
	 */
	public void setMappedControlId(int n, int cid) {
		assert n >= 0;
		assert n < 128;
		ensureMapExists();
		map[n] = deepFind(cid);
	}
	
	// @Override to enable Learn menu item
    public boolean canLearn() { return true; }
    
	// @Override
    public boolean getLearn() { return learn; }
    
	// @Override
    public void setLearn(boolean learn) { this.learn = learn; }
}
