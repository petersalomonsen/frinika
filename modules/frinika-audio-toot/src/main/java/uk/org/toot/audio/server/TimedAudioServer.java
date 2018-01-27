// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package uk.org.toot.audio.server;

/**
 * TimedAudioServer extends AbstractAudioServer to control the timing of an
 * AudioClient.
 * The buffer size, latency and timing strategy may be varied while running.
 * Note that changing latency may cause inputs to glitch. 
 * 
 * @author Steve Taylor
 * @author Peter Johan Salomonsen
 */
abstract public class TimedAudioServer extends AbstractAudioServer
    implements Runnable, ExtendedAudioServer
{
    protected boolean isRunning = false;
    protected boolean hasStopped = false;

    private static long ONE_MILLION = 1000000;

    private float bufferMilliseconds = 2f;
    private float requestedBufferMilliseconds = bufferMilliseconds; // for syncing

    private float latencyMilliseconds = 70;

    private float actualLatencyMilliseconds = 0;
    private float lowestLatencyMilliseconds = bufferMilliseconds;
    private int bufferUnderRuns = 0;
    private int bufferUnderRunThreshold = 0;

    private int outputLatencyFrames = 0;
//    private int inputLatencyFrames = 0;
    private int totalLatencyFrames = -1;
    
    private long totalTimeNanos;

    private boolean requestResetMetrics = false;

    protected float maximumLatencyMilliseconds = 140f; // default Linux constraint
    
    /**
     * @link aggregation
     * @supplierCardinality 1 
     */
    private AudioTimingStrategy timingStrategy;

	private Thread thread;

    /**
     * @link aggregation
     * @supplierCardinality 0..1 
     */
    protected AudioSyncLine syncLine;

    protected boolean started = false;
    protected int stableCount = 0;
 
    protected int stableThreshold = 1000;

    public TimedAudioServer() { //throws Exception {
        totalTimeNanos = (long)(bufferMilliseconds * ONE_MILLION);
        // estimate buffer underrun threshold for os
        String osName = System.getProperty("os.name");
        if ( osName.contains("Windows") ) {
            // only correct for DirectSound !!!
            bufferUnderRunThreshold = 33;
            // Windows hates SpinningTimingStrategy so revert to SleepTimingStrategy - st
            timingStrategy = new SleepTimingStrategy();
        } else {
            timingStrategy = new SleepTimingStrategy();
        }
    }

    // @Override
    @Override
    protected boolean canStart() {
        return super.canStart() && syncLine != null;
    }

    @Override
    protected void startImpl() {
        started = false;
        stableCount = 0;
       	thread = new Thread(this, THREAD_NAME);
       	thread.start();
    }

    @Override
    protected void stopImpl() {
        isRunning = false;
        // use Thread.join() here? TODO
        while (!hasStopped) {
            try {
	            Thread.sleep(10);
            } catch ( InterruptedException ie ) {
            }
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Return the total latency from analogue input to analogue output.
     */
    @Override
    public int getTotalLatencyFrames() {
    	return totalLatencyFrames;
    }
    
    @Override
    public void run() {
        try {
            hasStopped = false;
            isRunning = true;
            long expiryTimeNanos = System.nanoTime(); // init required for jitter
            long compensationNanos = 0;
            float lowLatencyMillis;

            while ( isRunning ) {
                sync(); // e.g. resize buffers if requested
                work();

                // calculate actual latency
				outputLatencyFrames = syncLine.getLatencyFrames();
		    	totalLatencyFrames = outputLatencyFrames + getInputLatencyFrames();
				actualLatencyMilliseconds = 1000 * outputLatencyFrames / getSampleRate();
                lowLatencyMillis = actualLatencyMilliseconds - bufferMilliseconds;
                if ( lowLatencyMillis < bufferUnderRunThreshold ) {
                    if ( started ) {
                    	bufferUnderRuns += 1;
                    	stableCount = 0;
                    }
                } else {
                    stableCount +=1;
                    if ( stableCount == stableThreshold ) { // !!! OK and every 49 days !!!
	                    started = true;
                        controlGained();
                    }
                }
                if ( lowLatencyMillis < lowestLatencyMilliseconds ) {
                    lowestLatencyMilliseconds = lowLatencyMillis;
                }
				if ( stableCount == 0 ) continue; // fast control stabilisation

                // calculate the latency control loop
                compensationNanos = (long)(ONE_MILLION * (actualLatencyMilliseconds - latencyMilliseconds));
                expiryTimeNanos = startTimeNanos + totalTimeNanos + compensationNanos;

                // block
                long sleepNanos = expiryTimeNanos - endTimeNanos;
                // never block for more than 20ms
                if ( sleepNanos > 20000000 ) {
                    sleepNanos = 20000000;
                    expiryTimeNanos = endTimeNanos + sleepNanos;
                }
                if ( sleepNanos > 500000 ) {
                    timingStrategy.block(endTimeNanos, sleepNanos);
                } else {
                    expiryTimeNanos = endTimeNanos;
                }
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
        hasStopped = true;
//        System.out.println("Thread stopped");
    }

    /**
     * Called synchronously with the server to simplify concurrency issues.
     */
    protected void sync() {
        if ( bufferMilliseconds != requestedBufferMilliseconds ) {
            bufferMilliseconds = requestedBufferMilliseconds;
            totalTimeNanos = (long)(bufferMilliseconds * ONE_MILLION);
            resizeBuffers(calculateBufferFrames());
        }
        if ( requestResetMetrics ) {
            reset();
            requestResetMetrics = false;
        }
    }

    /**
     * Called when the control loop gains control.
     */
    protected void controlGained() {
        resetMetrics(false);
    }

    @Override
    public void resetMetrics(boolean resetUnderruns) {
        requestResetMetrics = true;
        if ( resetUnderruns ) {
        	bufferUnderRuns = 0;
        }
    }

    protected void reset() {
        lowestLatencyMilliseconds = actualLatencyMilliseconds;
        // underruns can't be reset here because underruns cause reset
   }

    /**
     * Set the software output latency request in milliseconds.
     * This is the demand to the control loop.
     */
    @Override
    public void setLatencyMilliseconds(float ms) {
        latencyMilliseconds = ms;
        // reset other metrics synchronously
        resetMetrics(false);
    }

    /**
     * Return the requested software output latency in milliseconds.
     */
    @Override
    public float getLatencyMilliseconds() {
        return latencyMilliseconds;
    }

    /**
     * Return the actual instantaneous software output latency in milliseconds.
     * This is the controlled amount which will diverge from the requested
     * amount due to instantaneous control error caused by timing jitter.
     */
    @Override
    public float getActualLatencyMilliseconds() {
        return actualLatencyMilliseconds;
    }

    /**
     * Because latency is measured just after writing a buffer and represents
     * the maximum latency, the lowest latency has to be compensated by the
     * duration of the buffer.
     * This might not be the best place to do the compensation but it is the
     * cheapest. While bufferMilliseconds is effectively immutable it's ok.
     */
    @Override
    public float getLowestLatencyMilliseconds() {
        return lowestLatencyMilliseconds;
    }

    /**
     * Return the minimum software output latency which may be requested.
     */
    @Override
    public float getMinimumLatencyMilliseconds() {
        return bufferUnderRunThreshold + 2f;
    }

    /**
     * Return the maximum software output latency which may be requested.
     */
    @Override
    public float getMaximumLatencyMilliseconds() {
    	return maximumLatencyMilliseconds;
    }

    /**
     * Return the number of buffer underruns which may have resulted in an audio
     * glitch.
     */
    @Override
    public int getBufferUnderRuns() {
        return bufferUnderRuns;
    }

    protected int calculateBufferFrames() {
        return (int)(getSampleRate() * getBufferMilliseconds() / 1000);
    }

    /**
     * Return the duration of the buffers in milliseconds.
     */
    @Override
    public float getBufferMilliseconds() {
        return bufferMilliseconds;
    }

    /**
     * Set the duration of the buffers in milliseconds.
     */
    @Override
    public void setBufferMilliseconds(float ms) {
        requestedBufferMilliseconds = ms;
        if ( !isRunning ) sync();
    }

    public AudioTimingStrategy getTimingStrategy() {
        return timingStrategy;
    }

    public void setTimingStrategy(AudioTimingStrategy timingStrategy) {
        this.timingStrategy = timingStrategy;
    }
}
