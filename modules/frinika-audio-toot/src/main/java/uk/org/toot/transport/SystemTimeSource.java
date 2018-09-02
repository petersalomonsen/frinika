// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.transport;

/**
 * A concrete implementation of a TimeSource using System.nanoTime()
 */
public class SystemTimeSource implements TimeSource
{
    /**
     * @supplierCardinality 1
     * @link aggregation 
     */
    private Transport transport;
    private long startTime = 0;
	private long stopTime = 0;

    public SystemTimeSource(Transport t) {
        transport = t;
		TransportListener transportListener = new TransportAdapter() {
			public void play() {
                startTime = microsecondTime() - stopTime;
            }
            public void stop() {
                stopTime = getMicrosecondLocation();
            }
            public void locate(long microseconds) {
                // !!! !!! only valid when stopped
                stopTime = microseconds;
            }
        };
        transport.addTransportListener(transportListener);
    }

    protected long microsecondTime() {
        return (long)(System.nanoTime() / 1000);
    }

    /**
     * called by Slaves
     */
    public long getMicrosecondLocation() {
        if ( transport.isPlaying() ) {
        	return microsecondTime() - startTime;
        }
        return stopTime;
    }
}
