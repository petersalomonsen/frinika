// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.transport;

import java.util.List;

/**
 * A default concrete implementation of a Transport.
 */
public class DefaultTransport implements Transport
{
    private boolean playing = false;
    private boolean recording = false;
    private List<TransportListener> listeners;

    /**
     * @supplierCardinality 1
     * @link aggregation 
     */
    private TimeSource timeSource;

    public DefaultTransport() {
        // probably no point having a transport if nothing listening
        // so we create a listener list non-lazily to simplify implementation
		listeners = new java.util.ArrayList<TransportListener>();
        timeSource = new SystemTimeSource(this); // !!! should be aggregated
    }

    public void stop() {
        if ( !playing ) {
            if ( getMicrosecondLocation() != 0 ) {
            	locate(0);	// !!! !!! probably shouldn't be here
            }
            return;
        }
        for ( TransportListener l : listeners ) {
            l.stop();
        }
        playing = false;
    }

    public void play() {
        if ( playing ) return;
        playing = true;
        for ( TransportListener l : listeners ) {
            l.play();
        }
    }

    public void record(boolean rec) {
        recording = rec;
        for ( TransportListener l : listeners ) {
            l.record(recording);
        }
    }

    public void locate(long microseconds) {
        for ( TransportListener l : listeners ) {
            l.locate(microseconds);
        }
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isRecording() {
        return recording;
    }

    public void addTransportListener(TransportListener listener) {
        if ( !listeners.contains(listener) && listener != this ) {
        	listeners.add(listener);
        }
    }

    public void removeTransportListener(TransportListener listener) {
        if ( listeners.contains(listener) ) {
			listeners.remove(listener);
        }
    }

    /**
     * called by Slaves
     */
    public long getMicrosecondLocation() {
        return timeSource.getMicrosecondLocation();
    }
}
