// Copyright (C) 2006,2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.core;

import java.util.List;
import java.util.ArrayList;
import java.util.Observer;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;
import uk.org.toot.control.*;

/**
 * A composite AudioProcess that processes its child processes sequentially and
 * modifies its structure to track its associated AudioControlsChain in a
 * thread-safe manner.
 * The buffer is of little concern, we just pass it to the right things
 * in the right order.
 * Structural changes should use the Command pattern in order to
 * decouple real-time UI changes from process-time processing which occurs
 * 'before' nominal real-time.
 * Commands are:
 *  Move 'name' before 'name'
 *  Insert new before 'name'
 *  Delete 'name'
 */
public class AudioProcessChain implements AudioProcess {
    /**
     * @supplierCardinality 1
     * @link aggregationByValue 
     */
    protected AudioControlsChain controlChain;

    /**
     * @link aggregation
     * @supplierCardinality 0..* 
     */
    /*#protected AudioProcess linkProcesses;*/
    protected List<AudioProcess> processes;
    private ConcurrentLinkedQueue<AudioControlsChain.ChainMutation> mutationQueue;
    private Observer controlChainObserver;
    private boolean debugTimes = false; // !!! !!! !!! DEBUG
    private long[] t; // !!! !!! !!! DEBUG

    public AudioProcessChain(AudioControlsChain controlChain) {
         this.controlChain = controlChain;
         controlChainObserver = new ControlChainObserver();
         mutationQueue = new ConcurrentLinkedQueue<AudioControlsChain.ChainMutation>();
         processes = new ArrayList<AudioProcess>();
    }

    public void open() throws Exception {
         for ( Control control : controlChain.getControls() ) {
            if ( control instanceof AudioControls ) {
       	        AudioProcess p = createProcess((AudioControls)control);
   	        	processes.add(p);
                if ( p != null ) {
                	p.open();
/*                } else {
                    System.out.println(controlChain.getName()+
                        " adding null process at "+(processes.size()-1)); */
                }
            }
         }
		controlChain.addObserver(controlChainObserver);
//        debugTimes = processes.size() > 5; // !!! !!! !!! DEBUG
        if ( debugTimes ) {
            t = new long[20];
        }
    }

    private int debugIndex = 0;
    private long prevNanos = 0;
    private long tstart;
	private long elapsed;

    public int processAudio(AudioBuffer buffer) {
        processMutations();
        if ( debugTimes ) {
	        tstart = System.nanoTime();
    	    elapsed = tstart - prevNanos;
        	prevNanos = tstart;
            debugIndex += 1;
            debugIndex %= 100; // 500ms of 100 5ms timeslices
            if ( debugIndex == 0 ) {
            	return debugProcessAudio(buffer);
            }
        }

        // processAudio may throw an Exception
        // we then need to disable that process
        // and draw the problem to the users attention
        for ( int i = 0; i < processes.size(); i++ ) {
            AudioProcess p = processes.get(i);
            try {
	            if ( p != null ) p.processAudio(buffer);
            } catch ( Exception e ) {
            	try {
            		p.close();
            	} catch ( Exception e2 ) {
            		// nothing we can do here
            	}
                processes.set(i, null);
                System.out.println("DISABLED "+p+" in "+getName()+" due to:");
                e.printStackTrace();
            }
        }
        return AUDIO_OK;
    }

    public int debugProcessAudio(AudioBuffer buffer) {
        int len = processes.size();
        for ( int i = 0; i < len; i++ ) {
            processes.get(i).processAudio(buffer);
            t[i] = System.nanoTime();
        }
        int load = (int)(100 * (t[len-1] - tstart) / elapsed);
        System.out.print(load+"%: ");
        long prevt = tstart;
        for ( int i = 0; i < len; i++ ) {
	        System.out.print((t[i]-prevt)+", ");
            prevt = t[i];
        }
        System.out.println();
        return AUDIO_OK;
    }

    public void close() {
		controlChain.deleteObserver(controlChainObserver);
        for ( AudioProcess p : processes ) {
            if ( p == null ) continue;
        	try {
        		p.close();
        	} catch ( Exception e ) {
        		// can't do anything useful
        	}
        }
        processes.clear();
        t = null;
    }

    public String getName() {
        return controlChain.getName();
    }

    protected AudioProcess createProcess(AudioControls controls) {
        return AudioServices.createProcess(controls);
    }

    // process a single mutation each iteration
    protected void processMutations() {
        AudioControlsChain.ChainMutation m = mutationQueue.poll();
        if ( m == null ) return;
        AudioProcess p;
        try {
	        switch ( m.getType() ) {
    	    case AudioControlsChain.ChainMutation.DELETE:
        	    p = processes.get(m.getIndex0());
        		p.close();
	            processes.remove(p);
    	        break;
        	case AudioControlsChain.ChainMutation.INSERT:
            	Control controls = controlChain.getControls().get(m.getIndex0());
	        	if ( controls instanceof AudioControls ) {
		            p = createProcess((AudioControls)controls);
            		processes.add(m.getIndex0(), p);
        	        if ( p != null ) {
            	        p.open();
        	        }
                }
	            break;
    	    case AudioControlsChain.ChainMutation.MOVE:
        	    AudioProcess process = processes.get(m.getIndex0());
	            processes.remove(m.getIndex0());
    	        processes.add(m.getIndex1(), process);
	            break;
    	    }
        } catch ( Exception e ) {
            System.err.println("Exception for "+controlChain.getName()+
                ", Mutation: "+m);
            System.err.println(e);
        }
    }

    /**
     * A ControlChainObserver observes an AudioControlsChain for
     * asynchronous ChainMutation commands, adding these mutations to a
     * thread-safe queue for use at 'process-time'.
     */
    protected class ControlChainObserver implements Observer
    {
        public void update(Observable obs, Object obj) {
            if ( obj instanceof AudioControlsChain.ChainMutation )
	        	mutationQueue.add((AudioControlsChain.ChainMutation)obj);
        }
    }

}
